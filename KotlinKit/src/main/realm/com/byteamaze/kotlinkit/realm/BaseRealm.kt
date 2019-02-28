package com.byteamaze.kotlinkit.realm

import com.byteamaze.kotlinkit.extension.app
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.schedulers.Schedulers
import io.realm.*
import java.io.File

abstract class BaseRealm {

    // 数据库配置
    private var mRealmConfiguration: RealmConfiguration? = null

    // 数据库升级操作
    private var mRealmMigration = BaseRealmMigration()

    /**
     * BaseRealm子类实例为单例模式，保证同一个Realm使用相同的Config
     */
    private fun getConfiguration(): RealmConfiguration {
        if (mRealmConfiguration == null)
            mRealmConfiguration = buildConfiguration().build()

        return mRealmConfiguration!!
    }

    // 构造新的Configuration
    private fun buildConfiguration(): RealmConfiguration.Builder {
        val builder = RealmConfiguration.Builder()
                .directory(dbFile().parentFile)
                .name(dbFile().name)
                .schemaVersion(dbVersion())
                .migration(BaseRealmMigration()) /* 数据库升级操作 */
        // 自定义配置Configuration
        configRealm(builder)
        return builder
    }

    // 默认数据库文件路径
    val defaultDBFile = File(app.filesDir, Realm.DEFAULT_REALM_NAME)

    /**
     * 数据库文件路径
     */
    abstract fun dbFile(): File

    /**
     * 数据库版本号，最小值为1
     */
    abstract fun dbVersion(): Long

    /**
     * 数据库升级操作
     * @param version 需要进行升级的版本号，例：版本1升级到版本2，version值为1
     * 示例：从版本1升级到版本2时候，为Person表添加一个name字段
     *     when (version) {
     *        1L -> {
     *            realm?.schema?.get("Person")?.let {
     *              it.addField("name", String::class.java)
     *            }
     *        }
     *     }
     */
    abstract fun migrationAction(realm: DynamicRealm?, version: Long)

    /**
     * 配置Realm数据库
     * @param 数据库配置构造器
     */
    abstract fun configRealm(builder: RealmConfiguration.Builder)

    /**
     * 打开Realm数据库
     */
    fun openRealm(): Realm {
        if (dbVersion() < 1) throw IllegalArgumentException("Realm数据库版本号必须大于0")

        return Realm.getInstance(getConfiguration())
    }

    /**
     * 插入Models到数据库
     * @param models 需要插入的Model对象
     */
    fun insertOrUpdate(vararg models: RealmModel) {
        execute({ realm, committer ->
            realm.executeTransaction {
                it.insertOrUpdate(models.toList())
            }
            committer.onComplete()
        })
    }

    /**
     * 插入Models到数据库
     * @param models 需要插入的Model对象
     */
    fun insertOrUpdate(models: Collection<RealmModel>) {
        execute({ realm, committer ->
            realm.executeTransaction {
                it.insertOrUpdate(models)
            }
            committer.onComplete()
        })
    }

    /**
     * 删除数据库对象
     * @param clazz 对象类型
     * @param where 查找需要删除的对象
     */
    fun <E : RealmModel> remove(clazz: Class<E>, where: (RealmQuery<E>) -> Unit) {
        execute { realm, committer ->
            realm.where(clazz)?.apply(where)?.findAll()?.let { results ->
                realm.executeTransaction { results.deleteAllFromRealm() }
            }
            committer.onComplete()
        }
    }

    /**
     * 返回数据库中对象的个数
     */
    fun <E : RealmModel> count(clazz: Class<E>): Long {
        var count = 0L
        execute { realm, observableEmitter ->
            count = realm.where(clazz).count()
            observableEmitter.onComplete()
        }
        return count
    }

    /**
     * 同步查询Models
     * @param clazz 需要查询数据的类名
     * @param action 过滤查询数据的事件(排序，筛选等)
     * @return 返回不受管理的Model对象的List
     */
    fun <E : RealmModel> loadData(clazz: Class<E>, action: ((RealmQuery<E>) -> Unit)? = null): List<E> {
        var models = ArrayList<E>()
        execute { realm, observableEmitter ->
            var query = realm.where(clazz)
            action?.let { it(query) }
            models.addAll(realm.copyFromRealm(query.findAll()))
            observableEmitter.onComplete()
        }
        return models
    }

    /**
     * 异步查询Models，并返回不受管理的Model对象的List
     * @param clazz 需要查询数据的类名
     * @param action 过滤查询数据的事件(排序，筛选等)
     * @param complete 查询完成后的通知回调
     */
    fun <E : RealmModel> loadDataAsync(
            clazz: Class<E>, action: ((RealmQuery<E>) -> Unit)? = null,
            complete: (List<E>) -> Unit) {
        Observable
                .create<List<E>> {
                    it.onNext(loadData(clazz, action))
                    it.onComplete()
                }
                .subscribeOn(Schedulers.io())
                .subscribe { complete(it) }
    }

    /**
     * 执行数据库操作
     * @param action 执行Realm操作的Block
     *               执行完成后使用ObservableEmitter.onComplete关闭数据库
     */
    open fun execute(action: (Realm, ObservableEmitter<Unit>) -> Unit) {
        val realm = openRealm()
        Observable.create<Unit> {
            action(realm, it)
        }.subscribe({}, { it.printStackTrace() }, { closeRealm(realm) })
    }

    /**
     * 关闭Realm
     */
    private fun closeRealm(realm: Realm) {
        if (!realm.isClosed) realm.close()
    }

    // 数据库升级操作
    inner class BaseRealmMigration : RealmMigration {
        override fun migrate(realm: DynamicRealm?, oldVersion: Long, newVersion: Long) {
            // 循环执行每个版本的升级操作
            for (ver in oldVersion until newVersion)
                migrationAction(realm, ver)
        }

        override fun hashCode(): Int {
            return 36
        }

        override fun equals(other: Any?): Boolean {
            return other is BaseRealmMigration
        }
    }
}
package com.domobile.arch.proxy

/**
 * 数据传输代理
 * 用于在Activity之间传送数据
 */
open class TransportProxy {

    companion object {
        private var instance: TransportProxy? = null

        // 返回实例(单例模式)
        fun get(): TransportProxy {
            if (instance == null)
                instance = TransportProxy()
            return instance!!
        }
    }

    // 数据集合
    protected var data = HashMap<String, Any>()

    // 添加数据
    fun put(key: String, value: Any): TransportProxy {
        data[key] = value
        return this
    }

    // 移除数据
    fun remove(key: String): TransportProxy {
        data.remove(key)
        return this
    }

    // 获取数据
    private inline fun <reified T> get(key: String): T? {
        val anyObject = data[key]
        if (anyObject is T) return anyObject

        return null
    }

    // 获取数据，并从传输层移出对象
    inline fun <reified T> pop(key: String): T? {
        val anyObject = data.remove(key)
        if (anyObject is T) return anyObject

        return null
    }
}
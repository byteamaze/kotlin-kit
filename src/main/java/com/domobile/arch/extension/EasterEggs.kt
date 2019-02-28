package com.domobile.arch.extension

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.Point
import android.util.TypedValue
import android.view.*
import android.widget.Toast
import com.domobile.arch.proxy.ApplicationProxy
import java.util.*

// SETTINGS，系统设置包名
const val PKG_SETTINGS = "com.android.settings"

// ==========================Any==========================
// 获取应用Application实例，全局唯一Context
val Any.app: Application
    get() = ApplicationProxy.getApp()

// 获取设备屏幕尺寸
val Any.screenSize: Point
    get() = ApplicationProxy.getScreenSize()

/**
 * Toast提示
 * @param text resId或者字符串
 * @param lengthLong 默认false，显示时间短，true则显示时间长
 */
fun Any.toast(text: Any, lengthLong: Boolean = false) {
    val textStr = if (text is Int) app.getString(text) else text.toString()
    val duration = if (lengthLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
    Toast.makeText(app, textStr, duration).show()
}
// ==========================Any==========================


// ==========================Context==========================

/**
 * 返回所有已安装的Launcher列表
 *
 * @param context
 * @return
 */
fun Context.getLaunchers(): HashMap<String, String> {
    val launchers = HashMap<String, String>()
    val intent = Intent(Intent.ACTION_MAIN)
    intent.addCategory(Intent.CATEGORY_HOME)
    // 查询所有桌面启动器，并加入Map中
    packageManager.queryIntentActivities(intent, 0)
            .forEach {
                val pkg = it.activityInfo.packageName
                launchers[pkg] = pkg
            }
    // Settings不能当Launcher处理
    launchers.remove(PKG_SETTINGS)
    return launchers
}

/**
 * 加载全部已安装应用(不包含没有启动页的App)
 */
fun Context.loadAllInstalledApps(): Collection<ResolveInfo> {
    // 查询所有已安装应用的Launcher
    val intent = Intent(Intent.ACTION_MAIN)
    intent.addCategory(Intent.CATEGORY_LAUNCHER)
    return packageManager.queryIntentActivities(intent, 0)
}

/**
 * 安全打开Activity，防止找不到Activity引起崩溃
 */
fun Context.startActivitySafety(intent: Intent): Boolean {
    if (intent.activityAvailable()) {
        startActivity(intent)
        return true
    }
    return false
}

/**
 * 从layout资源加载布局
 */
fun Context.inflate(layoutRes: Int, parent: ViewGroup? = null, attachToParent: Boolean = false): View {
    return LayoutInflater.from(this).inflate(layoutRes, parent, attachToParent)
}

// ==========================Context==========================


// ==========================Intent==========================
/**
 * 判断是否可以打开intent指向的activity
 * startActivity前均需要执行此检查
 */
fun Intent.activityAvailable(): Boolean {
    return app.packageManager.resolveActivity(this, 0) != null
}
// ==========================Intent==========================


// ==========================Int==========================
/**
 * 将数字dp转换对应的像素值
 * Example:
 * 在2倍屏下，2.dp() = 2dp = 4px，返回值为4
 */
fun Int.dp(): Int {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(), app.resources.displayMetrics).toInt()
}
// ==========================Int==========================

// ==========================Bitmap==========================
/**
 * 安全回收bitmap
 */
fun Bitmap.recycleSafety() {
    if (!this.isRecycled) this.recycle()
}
// ==========================Bitmap==========================


// ==========================View==========================
/**
 * 等待View绘制后进行操作
 */
fun View.waitForLayout(action: () -> Unit) = with(viewTreeObserver) {
    addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            viewTreeObserver.removeOnGlobalLayoutListener(this)
            action()
        }
    })
}

/**
 * 隐藏View
 */
fun View.gone() {
    this.visibility = View.GONE
}

/**
 * 显示View
 */
fun View.visible() {
    this.visibility = View.VISIBLE
}

/**
 * 半隐藏状态
 */
fun View.invisible() {
    this.visibility = View.INVISIBLE
}

// ==========================View==========================


// ==========================Menu==========================
/**
 * 加载MenuItem到菜单中
 * @param menuRes 需要加载的menu布局资源
 */
fun Menu.inflate(menuRes: Int) {
    MenuInflater(app).inflate(menuRes, this)
}
// ==========================Menu==========================
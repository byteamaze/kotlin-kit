package com.byteamaze.kotlinkit.proxy

import android.app.Application
import android.content.Context.WINDOW_SERVICE
import android.graphics.Point
import android.view.WindowManager

class ApplicationProxy {
    private val isException = IllegalStateException("ApplicationProxy.init() must be called in Application.onCreate()")
    // 屏幕尺寸
    private val screenSize = Point()
    private var app: Application? = null

    companion object {
        private val proxy = ApplicationProxy()

        // 返回Application
        fun getApp(): Application {
            if (proxy.app == null) throw proxy.isException
            return proxy.app!!
        }

        // 返回屏幕尺寸
        fun getScreenSize(): Point {
            if (proxy.app == null) throw proxy.isException
            return proxy.screenSize
        }

        // 初始化ApplicationProxy
        fun init(app: Application) {
            // 全局唯一Context
            proxy.app = app
            // 屏幕尺寸
            (app.getSystemService(WINDOW_SERVICE) as WindowManager)
                    .defaultDisplay.getSize(proxy.screenSize)
        }
    }
}
package com.byteamaze.kotlinkit.store

import com.byteamaze.kotlinkit.dispatcher.Dispatcher

/**
 * 数据操作类
 * 所有的数据操作均在Store中进行，通过Dispatcher进行分发事件
 */
abstract class Store protected constructor(private val dispatcher: Dispatcher) {

    // 发送事件
    fun emitStoreChange(tag: String, event: StoreChangeEvent) {
        dispatcher.emitChange(tag, event)
    }

    // 事件实体接口
    interface StoreChangeEvent
}

package com.domobile.arch.dispatcher

import android.text.TextUtils
import com.domobile.arch.actions.Action
import com.domobile.arch.store.Store
import com.hwangjr.rxbus.Bus
import com.hwangjr.rxbus.RxBus

/**
 * 事件调度中心
 * @param bus 事件总线
 */
class Dispatcher private constructor(private val bus: Bus) {

    companion object {
        private var instance: Dispatcher? = null

        // 单列Dispatcher
        private fun get(bus: Bus): Dispatcher {
            if (instance == null) {
                instance = Dispatcher(bus)
            }
            return instance!!
        }

        // 单列Dispatcher
        fun get(): Dispatcher {
            return get(RxBus.get())
        }
    }

    // 注册
    fun register(cls: Any) {
        bus.register(cls)
    }

    // 反注册
    fun unregister(cls: Any) {
        bus.unregister(cls)
    }

    // 发送事件到UI
    fun emitChange(type: String, event: Store.StoreChangeEvent) {
        bus.post(type, event)
    }

    // 发送事件到Store
    fun dispatch(type: String, vararg data: Pair<String, Any>) {
        if (TextUtils.isEmpty(type)) {
            throw IllegalArgumentException("Type must not be empty")
        }

        // 组装事件参数
        val actionBuilder = Action.type(type)
        for (pair in data)
            actionBuilder.bundle(pair.first, pair.second)
        post(actionBuilder.build())
    }

    // 发送事件到Store
    private fun post(event: Action) {
        bus.post(event.type, event)
    }

    /**
     * 发送消息
     */
    fun post(tag: String, data: Any) {
        bus.post(tag, data)
    }
}
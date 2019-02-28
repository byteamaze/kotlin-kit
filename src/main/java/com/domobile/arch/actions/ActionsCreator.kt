package com.domobile.arch.actions

import com.domobile.arch.dispatcher.Dispatcher

/**
 * 事件创建者
 * App端扩展ActionsCreator发送事件
 * fun ActionCreate.loadData() {
 *      dispatcher.dispatch(Actions.ADD_MEDIA)
 * }
 */
class ActionsCreator internal constructor(val dispatcher: Dispatcher) {
    companion object {
        private var instance: ActionsCreator? = null

        // 单列创建
        fun get(): ActionsCreator {
            if (instance == null)
                instance = ActionsCreator(Dispatcher.get())
            return instance!!
        }
    }
}
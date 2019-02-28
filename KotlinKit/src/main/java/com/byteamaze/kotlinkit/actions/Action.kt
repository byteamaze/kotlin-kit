package com.byteamaze.kotlinkit.actions

/**
 * 向内传送的事件实体
 * type: 事件类型
 * data: 该事件传输的数据
 */
class Action private constructor(val type: String, val data: HashMap<String, Any>) {

    companion object {
        /**
         *  事件构造器
         *
         */
        fun type(type: String): Builder {
            return Builder(type)
        }
    }

    // 事件构造器
    class Builder(private val type: String) {
        private var data = HashMap<String, Any>()

        /**
         * 设置传递的数据
         *
         * @param key
         * @param value
         * @return
         */
        fun bundle(key: String?, value: Any?): Builder {
            if (key == null) {
                throw IllegalArgumentException("Key may not be null.")
            }

            if (value == null) {
                throw IllegalArgumentException("Value may not be null.")
            }
            data[key] = value
            return this
        }

        fun build(): Action {
            if (type.isEmpty()) {
                throw IllegalArgumentException("At least one key is required.")
            }
            return Action(type, data)
        }
    }
}
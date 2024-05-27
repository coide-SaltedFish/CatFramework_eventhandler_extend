package org.sereinfish.catcat.framework.eventhandler.extend.filter

import org.catcat.sereinfish.qqbot.universal.abstraction.layer.events.message.GroupMessageEvent
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.events.message.MessageEvent

class LimitCallIntervalFilter<E: MessageEvent>(
    val interval: Long, // 调用间隔时间

    val keyHandler: E.() -> String = LimitRateFilter.USER_KEY
) {
    companion object {
        private val data = HashMap<String, Long>()

        val USER_KEY: MessageEvent.() -> String = {
            "KEY_${this::class.java.name}_${target.id}_${sender.id}"
        }

        val GROUP_KEY: GroupMessageEvent.() -> String = {
            "KEY_${this::class.java.name}_${group.id}"
        }
    }

    fun onCall(event: E) {
        data[key(event)] = System.currentTimeMillis()
    }

    fun isLimit(event: E): Boolean {
        return data[key(event)]?.let {
            System.currentTimeMillis() - it <= interval
        } ?: false
    }

    private fun key(event: E): String {
        return keyHandler(event)
    }
}
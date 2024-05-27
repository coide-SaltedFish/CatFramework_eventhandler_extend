package org.sereinfish.catcat.framework.eventhandler.extend.filter

import org.catcat.sereinfish.qqbot.universal.abstraction.layer.events.message.GroupMessageEvent
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.events.message.MessageEvent
import org.sereinfish.cat.frame.event.Event
import java.util.Stack
import java.util.concurrent.LinkedBlockingQueue

/**
 * 频率调用限制
 */
class LimitRateFilter<E: MessageEvent>(
    val maxCalls: Int,
    val time: Long,

    val keyHandler: E.() -> String = USER_KEY
) {
    /**
     * 伴生对象保存全局数据
     */
    companion object {
        private val data = HashMap<String, LimitRateData>()

        val USER_KEY: MessageEvent.() -> String = {
            "KEY_${this::class.java.simpleName}_${target.id}_${sender.id}"
        }

        val GROUP_KEY: GroupMessageEvent.() -> String = {
            "KEY_${this::class.java.simpleName}_${group.id}"
        }
    }

    /**
     * 记录一次调用
     */
    fun onCall(event: E) {
        val entity = data[key(event)] ?: run {
            LimitRateData().also {
                data[key(event)] = it
            }
        }
        entity.onCall()
    }

    /**
     * 是否限制调用
     * 按照指定键取出对应记录，判断是否限制
     *
     * @return true:限制 false:不限制
     */
    fun isLimit(event: E): Boolean {
        return data[key(event)]?.isLimit(maxCalls, time) ?: false
    }

    private fun key(event: E): String {
        return keyHandler(event)
    }

    private data class LimitRateData (
        val cache: LinkedBlockingQueue<Long> = LinkedBlockingQueue()
    ){
        /**
         * 首先移除已失效的记录，当记录的时间加上限时间小于当前时间，则移除
         * 然后统计数量查看是否到达上限
         */
        fun isLimit(maxCalls: Int, time: Long): Boolean {
            cache.removeIf {
                it + time < System.currentTimeMillis()
            }
            return cache.size >= maxCalls
        }

        fun onCall() {
            cache.add(System.currentTimeMillis())
        }
    }
}


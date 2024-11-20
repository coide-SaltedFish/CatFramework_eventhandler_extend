package org.sereinfish.catcat.framework.eventhandler.extend.build

import org.catcat.sereinfish.qqbot.universal.abstraction.layer.events.message.MessageEvent
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.ExternalResource
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.MessageChain
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.MessageFactory
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.MessageRouter
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.MessageRouterBuilder
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.MessageRouterContext
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.SimpleMessageRouterBuilder
import org.sereinfish.catcat.framework.eventhandler.extend.filter.LimitCallIntervalFilter
import org.sereinfish.catcat.framework.eventhandler.extend.filter.LimitRateFilter
import org.sereinfish.catcat.framework.eventhandler.extend.handler.CatEventHandlerExtendContext
import org.sereinfish.catcat.framework.eventhandler.extend.handler.CatEventHandlerPropertyContext
import java.io.InputStream

val CatEventHandlerPropertyContext<out MessageEvent>.routerInfo: MessageRouter?
    get() = this["routerInfo"] as? MessageRouter

/**
 * 路由构建扩展
 */
fun <E: MessageEvent> EventHandlerBuilder<E>.router(block: MessageRouterBuilder.(E) -> Unit) {
    filter {
        val router = SimpleMessageRouterBuilder().apply { block(event) }.build()
        val context = MessageRouterContext(event.bot, event.message)
        router.match(context).also {
            context.mergeTo(this) // 将路由上下文合并到事件处理器上下文

            eventHandlerContext["routerInfo"] = router // 将路由对象保存到上下文
        }
    }
}

fun <E: MessageEvent> EventHandlerBuilder<E>.router(router: MessageRouter) {
    filter {
        val context = MessageRouterContext(event.bot, event.message)
        router.match(context).also {
            context.mergeTo(this)

            eventHandlerContext["routerInfo"] = router // 将路由对象保存到上下文
        }
    }
    builder {
        eventHandlerContext["routerInfo"] = router
    }
}

fun <E: MessageEvent> EventHandlerBuilder<E>.staticRouter(block: MessageRouterBuilder.() -> Unit) {
    val router = SimpleMessageRouterBuilder().apply { block() }.build()
    filter {
        val context = MessageRouterContext(event.bot, event.message)
        router.match(context).also {
            context.mergeTo(this)

            eventHandlerContext["routerInfo"] = router // 将路由对象保存到上下文
        }
    }
    builder {
        eventHandlerContext["routerInfo"] = router
    }
}

suspend fun <E: MessageEvent> E.buildMessage(block: suspend MessageFactory.() -> Unit): MessageChain {
    val factory = bot.messageFactory()
    block.invoke(factory)
    return factory.build()
}

fun <E: MessageEvent> E.externalResource(inputStream: InputStream): ExternalResource {
    return bot.externalResource(inputStream)
}

suspend fun <E: MessageEvent> E.reply(message: String) = reply(buildMessage {
    + message
})

/**
 * 调用频率限制
 */
fun <E: MessageEvent> EventHandlerBuilder<E>.limitRate(
    maxCalls: Int,
    time: Long,
    keyHandler: E.() -> String = LimitRateFilter.USER_KEY,
    handleRateOverflow: suspend E.(CatEventHandlerExtendContext<E>) -> Unit = {}
) {
    val limitRateFilter = LimitRateFilter(maxCalls, time, keyHandler)

    filter {
        val ret = limitRateFilter.isLimit(event)
        // 不限制则记录本次调用
        if (ret)
            event.handleRateOverflow(this)
        else
            limitRateFilter.onCall(event)

        ret.not()
    }
}

fun <E: MessageEvent> EventHandlerBuilder<E>.limitCallInterval(
    interval: Long,
    keyHandler: E.() -> String = LimitRateFilter.USER_KEY,
    handleIntervalOverflow: suspend E.(CatEventHandlerExtendContext<E>) -> Unit = {}
) {
    val limitCallIntervalFilter = LimitCallIntervalFilter(interval, keyHandler)

    filter {
        val ret = limitCallIntervalFilter.isLimit(event)

        if (ret)
            event.handleIntervalOverflow(this)
        else
            limitCallIntervalFilter.onCall(event)

        ret.not()
    }
}
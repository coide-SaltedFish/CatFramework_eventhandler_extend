package org.sereinfish.catcat.framework.eventhandler.extend.router

import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.MessageRouter
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.MessageRouterBuilder
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.RouterContext
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.SimpleMessageRouterBuilder
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.parser.MessageRouterEncode

class NotRouter(
    val router: MessageRouter
): MessageRouter {

    companion object: MessageRouterEncode<NotRouter> {
        override val target: String = "not"

        override fun decode(vararg params: Any?): NotRouter {
            val router = params.singleOrNull()?.let {
                it as? MessageRouter
            } ?: error("Not路由只能接受单个MessageRouter参数：[size: ${params.size}," +
                    " params: ${params.joinToString{ it?.let { it::class.java.name } ?: "null" }}]")
            return NotRouter(router)
        }
    }

    override fun encode(): String {
        return "[$target: ${router.encode()}]"
    }

    override fun parser(context: RouterContext): Boolean {
        // 新建一个上下文
        val childContext = context.clone()
        childContext.handledMessages.clear()

        return router.match(childContext).not()
    }
}

fun MessageRouterBuilder.not(router: MessageRouter) = NotRouter(router)

fun MessageRouterBuilder.not(block: MessageRouterBuilder.() -> Unit) = NotRouter(
    SimpleMessageRouterBuilder().apply(block).build()
)
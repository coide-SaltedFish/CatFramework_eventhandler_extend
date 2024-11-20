package org.sereinfish.catcat.framework.eventhandler.extend.router

import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.MessageRouter
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.MessageRouterBuilder
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.RouterContext
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.SimpleMessageRouterBuilder
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.parser.MessageRouterEncode

class NotAndRouter(
    val notRouters: List<MessageRouter>,
    val andRouter: MessageRouter
) : MessageRouter {

    companion object : MessageRouterEncode<NotAndRouter> {
        override val target: String = "notAnd"

        override fun decode(vararg params: Any?): NotAndRouter {
            val routers = params.map { it as? MessageRouter }.filterNotNull()
            if (routers.size < 2)
                error("NotAnd路由至少需要两个MessageRouter参数")

            return NotAndRouter(routers.subList(0, routers.size - 1), routers.last())
        }
    }

    override fun encode(): String {
        TODO("Not yet implemented")
    }

    override fun parser(context: RouterContext): Boolean {
        // 所有not路由都必须不能被匹配
        for (router in notRouters) {
            val childContext = context.clone()
            childContext.handledMessages.clear()
            if (router.match(childContext))
                return false
        }
        return andRouter.match(context)
    }
}

fun MessageRouterBuilder.notAnd(notRouters: List<MessageRouter>, router: MessageRouter) =
    NotAndRouter(notRouters, router)

fun MessageRouterBuilder.notAnd(vararg notRouters: MessageRouter, router: MessageRouter) =
    NotAndRouter(notRouters.toList(), router)

fun MessageRouterBuilder.notAnd(vararg notRouters: MessageRouterBuilder.() -> Unit, router: MessageRouter) =
    NotAndRouter(notRouters.toList().map { SimpleMessageRouterBuilder().apply(it).build() }, router)

fun MessageRouterBuilder.notAnd(notRouters: List<MessageRouter>, block: MessageRouterBuilder.() -> Unit) =
    NotAndRouter(notRouters, SimpleMessageRouterBuilder().apply(block).build())

fun MessageRouterBuilder.notAnd(vararg notRouters: MessageRouter, block: MessageRouterBuilder.() -> Unit) =
    NotAndRouter(notRouters.toList(), SimpleMessageRouterBuilder().apply(block).build())

fun MessageRouterBuilder.notAnd(
    vararg notRouters: MessageRouterBuilder.() -> Unit,
    block: MessageRouterBuilder.() -> Unit
) =
    NotAndRouter(
        notRouters.toList().map { SimpleMessageRouterBuilder().apply(it).build() },
        SimpleMessageRouterBuilder().apply(block).build()
    )
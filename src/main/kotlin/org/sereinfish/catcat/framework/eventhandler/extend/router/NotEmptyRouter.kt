package org.sereinfish.catcat.framework.eventhandler.extend.router

import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.MessageRouter
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.MessageRouterBuilder
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.RouterContext
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.SimpleMessageRouterBuilder
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.parser.MessageRouterEncode

class NotEmptyRouter(
    val router: MessageRouter
): MessageRouter {

    companion object: MessageRouterEncode<NotEmptyRouter> {
        override val target: String = "notEmpty"

        override fun decode(vararg params: Any?): NotEmptyRouter {
            val router = params.singleOrNull()?.let {
                it as? MessageRouter
            } ?: error("NotEmpty路由只能接受单个MessageRouter参数：[size: ${params.size}," +
                    " params: ${params.joinToString{ it?.let { it::class.java.name } ?: "null" }}]")
            return NotEmptyRouter(router)
        }
    }

    override fun encode(): String {
        return "[$target: ${router.encode()}]"
    }

    override fun parser(context: RouterContext): Boolean {
        // 新建一个上下文
        val childContext = context.clone()
        childContext.handledMessages.clear()

        val ret = router.match(childContext)
        if (ret.not()) return false

        // 提取参数
        val params = childContext.getParamMessageChain()
        if (params.isEmpty()) return false

        // 环境同步
        context.merge(childContext)

        context.waitHandleMessages = childContext.waitHandleMessages
        context.handledMessages.addAll(childContext.handledMessages)

        return ret
    }
}

fun MessageRouterBuilder.notEmpty(router: MessageRouter) = NotEmptyRouter(router)

fun MessageRouterBuilder.notEmpty(block: MessageRouterBuilder.() -> Unit) = NotEmptyRouter(
    SimpleMessageRouterBuilder().apply(block).build()
)
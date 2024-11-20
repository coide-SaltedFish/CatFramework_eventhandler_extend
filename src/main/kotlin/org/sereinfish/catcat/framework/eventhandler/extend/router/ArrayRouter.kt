package org.sereinfish.catcat.framework.eventhandler.extend.router

import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.MessageRouter
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.MessageRouterBuilder
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.RouterContext
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.SimpleMessageRouterBuilder
import org.sereinfish.catcat.framework.eventhandler.extend.router.entity.ArrayMatchInfo

class ArrayRouter(
    val matchBlock: ArrayMatchInfo.() -> MessageRouter
): MessageRouter {

    override fun encode(): String {
        TODO("Not yet implemented")
    }

    override fun parser(context: RouterContext): Boolean {
        var index = 0

        while (true) {
            val childContext = context.clone()
            childContext.handledMessages.clear()
            val matchResult = ArrayMatchInfo(index).matchBlock().match(childContext)

            if (matchResult.not())
                break
            else {
                context.merge(childContext)
                context.waitHandleMessages = childContext.waitHandleMessages
                context.handledMessages.addAll(childContext.handledMessages)
            }

            index ++
        }

        val ret = index > 0

        return ret
    }

}

fun MessageRouterBuilder.array(block: MessageRouterBuilder.(ArrayMatchInfo) -> Unit) =
    ArrayRouter {
        SimpleMessageRouterBuilder().apply {
            block(this, this@ArrayRouter)
        }.build()
    }
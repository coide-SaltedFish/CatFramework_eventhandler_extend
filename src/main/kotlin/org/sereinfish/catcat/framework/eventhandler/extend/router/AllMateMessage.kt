package org.sereinfish.catcat.framework.eventhandler.extend.router

import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.MessageMateData
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.MessageRouter
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.MessageRouterBuilder
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.RouterContext
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.parser.MessageRouterEncode

class AllMateMessage: MessageRouter {

    companion object: MessageRouterEncode<AllMateMessage> {
        override val target: String = "allMateMessage"

        override fun decode(vararg params: Any?): AllMateMessage {
            return AllMateMessage()
        }
    }

    override fun encode(): String {
        return "[AllMateMessage]"
    }

    override fun parser(context: RouterContext): Boolean {
        // 找出所有的 MateMessage 元素，然后匹配
        context.handledMessages.addAll(context.waitHandleMessages.filterIsInstance<MessageMateData>())
        // 移除待匹配元素
        context.waitHandleMessages.removeIf { it is MessageMateData }

        return true
    }
}

fun MessageRouterBuilder.allMateMessage() = AllMateMessage()
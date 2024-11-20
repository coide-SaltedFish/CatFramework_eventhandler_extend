package org.sereinfish.catcat.framework.eventhandler.extend.router

import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.element.Reply
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.MessageRouter
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.MessageRouterBuilder
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.RouterContext
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.parser.MessageRouterEncode

class AllReplyRouter: MessageRouter {

    companion object: MessageRouterEncode<AllReplyRouter> {
        override val target: String = "allReply"

        override fun decode(vararg params: Any?): AllReplyRouter {
            return AllReplyRouter()
        }
    }

    override fun encode(): String {
        return "[AllReply]"
    }

    override fun parser(context: RouterContext): Boolean {
        // 找出所有的Reply元素，然后匹配
        context.handledMessages.addAll(context.waitHandleMessages.filterIsInstance<Reply>())
        context.waitHandleMessages.removeIf { it is Reply }

        return true
    }
}

fun MessageRouterBuilder.allReply() = AllReplyRouter()
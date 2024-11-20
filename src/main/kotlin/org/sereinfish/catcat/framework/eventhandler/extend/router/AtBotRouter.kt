package org.sereinfish.catcat.framework.eventhandler.extend.router

import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.element.At
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.MessageRouter
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.MessageRouterBuilder
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.RouterContext
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.parser.MessageRouterEncode
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.utils.tryMatch

class AtBotRouter: MessageRouter {

    companion object : MessageRouterEncode<AtBotRouter> {
        override val target: String = "atBot"

        override fun decode(vararg params: Any?): AtBotRouter {
            return AtBotRouter()
        }
    }

    override fun encode(): String {
        return "[$target]"
    }

    override fun parser(context: RouterContext): Boolean {
        val childContext = context.clone()
        childContext.handledMessages.clear()

        val ret = childContext.waitHandleMessages.firstOrNull()?.let { message ->
            if (message is At && message.target == context.bot.id){
                childContext.waitHandleMessages.removeFirst()
                childContext.handledMessages.add(message)
                true
            }else false
        } ?: false

        if (ret) {
            context.merge(childContext)

            // 更新处理进度
            context.waitHandleMessages = childContext.waitHandleMessages
            context.handledMessages.addAll(childContext.handledMessages)
        }
        return ret
    }
}


fun MessageRouterBuilder.atBot() = AtBotRouter()
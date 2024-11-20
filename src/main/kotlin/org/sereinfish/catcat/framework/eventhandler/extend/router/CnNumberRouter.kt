package org.sereinfish.catcat.framework.eventhandler.extend.router

import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.Message
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.element.PlantText
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.MessageRouter
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.MessageRouterBuilder
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.RouterContext
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.parser.MessageRouterEncode
import org.sereinfish.catcat.framework.eventhandler.extend.utils.CnNumberFactory
import java.util.Stack
import java.util.concurrent.CopyOnWriteArrayList

class CnNumberRouter: MessageRouter {

    companion object: MessageRouterEncode<CnNumberRouter> {
        override val target: String = "cnNumber"

        override fun decode(vararg params: Any?): CnNumberRouter {
            if (params.isNotEmpty()) error("CnNumberRouter 不支持传入参数构建")
            return CnNumberRouter()
        }

    }

    override fun encode(): String {
        return "[$target]"
    }

    override fun parser(context: RouterContext): Boolean {
        // 读取字符串
        // 尝试整合多个文本元素
        val waitMessages = Stack<Message>().apply {
            context.waitHandleMessages.reversed().forEach {
                push(it)
            }
        }

        val textMessage = buildString {
            while (waitMessages.isNotEmpty()) {
                if (waitMessages.peek() is PlantText) {
                    val text = waitMessages.pop() as PlantText
                    append(text.text)
                }else break
            }
        }
        // 尝试正则匹配
        var matchStr = ""
        for (c in textMessage) {
            if (CnNumberFactory.isCnNumber(matchStr + c)) {
                matchStr += c
            }else break
        }
        if (matchStr.isEmpty()) return false

        // 处理上下文
        context.waitHandleMessages.clear()
        context.waitHandleMessages.add(context.bot.messageFactory().text(textMessage.removePrefix(matchStr)))
        context.waitHandleMessages.addAll(waitMessages)

        context.handledMessages.add(context.bot.messageFactory().text(matchStr))

        return true
    }
}

fun MessageRouterBuilder.cnNumber() = CnNumberRouter()
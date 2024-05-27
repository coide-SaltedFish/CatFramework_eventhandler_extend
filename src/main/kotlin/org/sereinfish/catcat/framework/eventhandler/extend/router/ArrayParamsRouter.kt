package org.sereinfish.catcat.framework.eventhandler.extend.router

import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.MessageChain
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.MessageRouter
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.MessageRouterBuilder
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.RouterContext
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.SimpleMessageRouterBuilder
import org.sereinfish.cat.frame.utils.isTrue
import org.sereinfish.catcat.framework.eventhandler.extend.router.entity.ArrayMatchInfo

class ArrayParamsRouter(
    val key: String,
    val matchBlock: ArrayMatchInfo.() -> MessageRouter
): MessageRouter {
    override fun parser(context: RouterContext): Boolean {
        var index = 0

        val params = ArrayList<MessageChain>()

        while (true) {
            val childContext = context.clone()
            childContext.tempHandleMessage.clear()
            val matchResult = ArrayMatchInfo(index).matchBlock().match(childContext)

            if (matchResult.not())
                break
            else {
                // 提取参数
                val param = childContext.getParamMessageChain()
                params.add(param)

                context.merge(childContext)
                context.tempMessage = childContext.tempMessage
                context.tempHandleMessage.addAll(childContext.tempHandleMessage)
            }
            index ++
        }

        val ret = index > 0
        ret isTrue {
            // 注入
            context[key] = params.toTypedArray()
        }

        return ret
    }
}

fun MessageRouterBuilder.arrayParams(key: String, block: MessageRouterBuilder.(ArrayMatchInfo) -> Unit) =
    ArrayParamsRouter(key) {
        SimpleMessageRouterBuilder().apply {
            block(this, this@ArrayParamsRouter)
        }.build()
    }
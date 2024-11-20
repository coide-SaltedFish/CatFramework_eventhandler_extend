package org.sereinfish.catcat.framework.eventhandler.extend.router

import org.catcat.sereinfish.qqbot.universal.abstraction.layer.Bot
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.Message
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.MessageChain
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.element.PlantText
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.*
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.parser.MessageRouterEncode
import org.sereinfish.cat.frame.utils.toJson
import java.util.*

class BreakMatcherRoute(
    val router: MessageRouter,
    val allowEmpty: Boolean = false, // 是否允许为空
): MessageRouter {
    companion object: MessageRouterEncode<BreakMatcherRoute> {
        override val target: String = "breakMatcher"

        override fun decode(vararg params: Any?): BreakMatcherRoute {
            if (params.size > 2) {
                error("参数数量异常：[size: ${params.size}]," +
                        " params: ${params.joinToString{ it?.let { it::class.java.name } ?: "null" }}]")
            }


            val router = params.getOrNull(0)?.let {
                it as? MessageRouter
            } ?: error("BreakMatcherRoute 第一个参数必须为 MessageRouter，" +
                    " params: ${params.joinToString{ it?.let { it::class.java.name } ?: "null" }}]")
            val allowEmpty = params.getOrNull(1)?.let {
                it as? Boolean
            } ?: false
            return BreakMatcherRoute(router, allowEmpty)
        }
    }

    override fun encode(): String {
        return "[${NotRouter.target}: ${router.encode()}]"
    }

    /**
     * 依次遍历消息，直到匹配成功
     * 如果直到最后都没有匹配成功，返回 false
     * 匹配成功后，返回 true，将未匹配内容填入参数
     * 根据参数来判断是否允许为空
     */
    override fun parser(context: RouterContext): Boolean {
        // 依次构建匹配消息
        val subMessage = BreakMatcherRouteMessageChain(context.bot, context.waitHandleMessages)

//        println("待匹配消息：${subMessage.toLogString()}")

        for (start in subMessage.indices) {
            for (end in (start + 1)..(subMessage.size + 1)) {
                val subMessageChain = if ((start + 1) == subMessage.size && end > subMessage.size)
                    listOf()
                else if (end > subMessage.size)
                    continue
                else subMessage.subList(start, end)

                // 构建上下文，开始判断是否匹配
                val subContext = context.clone()
                subContext.waitHandleMessages.clear() // 清空待匹配消息列表
                subContext.waitHandleMessages.addAll(subMessageChain) // 添加待匹配消息

                subContext.handledMessages.clear() // 清空参数列表

                // 子路由匹配测试
                if (router.match(subContext)) {
                    // 匹配成功，判断参数是否为空
                    if (start == 0 && allowEmpty.not()) {
                        // 参数为空，返回 false
                        return false
                    }
                    // 上下文合并，返回 true
                    context.merge(subContext) // 合并最后匹配成功的上下文
                    context.waitHandleMessages = Vector(subMessage.getWaitHandleMessages(if (end > subMessage.size) end else start)) // 设置为包含匹配成功部分的后面部分
                    context.handledMessages.addAll(subMessage.getHandledMessages(if (end > subMessage.size) end else start)) // 添加已处理部分到上下文

                    return true
                }
            }
        }
        return false
    }

    /**
     * 处理为IfNot路由专用消息链
     */
    private class BreakMatcherRouteMessageChain(
        val bot: Bot,
        sourceMessageChain: List<Message>
    ): MessageChain, ArrayList<Message>() {

        init {
            sourceMessageChain.forEach {
                if (it is PlantText) {
                    // 拆字放入
                    it.text.forEach {
                        add(bot.messageFactory().text("$it"))
                    }
                }else {
                    add(it)
                }
            }
        }

        /**
         * 获取待匹配部分消息
         * 遍历消息链，遇到连续的文本元素就合并
         */
        fun getWaitHandleMessages(start: Int): List<Message> {
            val result = ArrayList<Message>()
            var i = start
            while (i < size) {
                val message = get(i)

                if (message is PlantText) {
                    // 遇到连续文本元素，合并
                    var text = message.text
                    i++
                    while (i < size) {
                        val nextMessage = get(i)
                        if (nextMessage is PlantText) {
                            text += nextMessage.text
                            i++
                        } else break
                    }
                    result.add(bot.messageFactory().text(text))
                }else {
                    result.add(message)
                    i ++
                }
            }

            return result
        }

        fun getHandledMessages(end: Int): List<Message> {
            val result = ArrayList<Message>()
            var i = 0
            while (i < minOf(end, size)) {
                val message = get(i)

                if (message is PlantText) {
                    // 遇到连续文本元素，合并
                    var text = message.text
                    i++
                    while (i < minOf(end, size)) {
                        val nextMessage = get(i)
                        if (nextMessage is PlantText) {
                            text += nextMessage.text
                            i++
                        } else break
                    }
                    result.add(bot.messageFactory().text(text))
                }else {
                    result.add(message)
                }

                i++
            }
            return result
        }

        override fun encode(): Any {
            error("BreakMatcherRouteMessageChain 不支持序列化")
        }

        override fun serializeToJsonString(): String {
            return this.toJson()
        }
    }
}

@Router
fun MessageRouterBuilder.breakMatcher(allowEmpty: Boolean = false, block: MessageRouterBuilder.() -> Unit): BreakMatcherRoute {
    val router = SimpleMessageRouterBuilder().apply(block).build()
    return BreakMatcherRoute(router, allowEmpty)
}

@Router
fun MessageRouterBuilder.breakMatcher(router: MessageRouter, allowEmpty: Boolean = false): BreakMatcherRoute {
    return BreakMatcherRoute(router, allowEmpty)
}
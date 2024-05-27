package org.sereinfish.catcat.framework.eventhandler.extend.router

import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.Message
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.element.PlantText
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.MessageRouter
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.MessageRouterBuilder
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.RouterContext
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.SimpleMessageRouterBuilder
import java.util.*


/**
 * 中间件路由
 *
 * 该路由负责协调子路由完成匹配，确保所有子路由都能匹配成功
 */
class MiddlewareRouter(
    val greedy: Boolean = false, // 是否贪心匹配
    val startRouter: MessageRouter,
    val lastRouter: MessageRouter
): MessageRouter {

    /**
     * 分割消息内容
     *
     * 依次匹配，直到所有子路由匹配成功或者所有匹配均失败
     */
    override fun parser(context: RouterContext): Boolean {

        var result: ResultData? = null
        // 开始循环分割消息内容
        var i = 0;
        var textIndex = -1

        while (i < context.tempMessage.size && contextTextMatch(context, i)) {
            val data = messageSplit(context.tempMessage, i, ++ textIndex)

            // 构建上下文
            val startRouterContext = context.clone()
            startRouterContext.tempMessage = data.startMessage
            startRouterContext.tempHandleMessage.clear()

            val lastRouterContext = context.clone()
            lastRouterContext.tempMessage = data.lastMessage
            lastRouterContext.tempHandleMessage.clear()

            // 进行匹配
            if (startRouter.match(startRouterContext) && lastRouter.match(lastRouterContext)) {
                // 进行上下文整理
                result = ResultData(startRouterContext, lastRouterContext)
                if (greedy.not()){
                    result.merge(context)
                    return true
                }
            }

            i = data.i
            textIndex = data.textIndex
        }

        return result?.let {
            it.merge(context)
            true
        } ?: false
    }

    private fun contextTextMatch(context: RouterContext, i: Int): Boolean {
        val element = context.tempMessage[i]
        return if (element is PlantText){
            return i < element.text.length
        }else false
    }

    /**
     * 对传入的消息进行分割
     *
     * @param message 消息
     * @param i 消息列表分割位置
     * @param textIndex 当前文本元素索引
     */
    private fun messageSplit(
        message: Vector<Message>,
        i: Int,
        textIndex: Int
    ): SplitMessageData {
        return if ((message[i] is PlantText).not()){
            // 表示没有分割字符消息
            SplitMessageData(
                startMessage = Vector(message.subList(0, i + 1)),
                lastMessage = Vector(message.subList(i + 1, message.size)),
                i = i + 1,
                textIndex = textIndex
            )
        }else {
            // 表示有分割字符消息
            /**
             * 先进行分割，排除 i 位置的元素
             * 取出i位置的元素，判断是否是文本元素，
             * 如果不是，把元素加入前一个消息元素，返回的i为 i + 1，textIndex为 -1
             * 如果是，按照字符分割索引分割，分别放入前后索引，
             * 判断字符索引是否已到尾部
             * 如果是，返回 i + 1，textIndex为 -1
             * 如果不是，返回 i，textIndex为 textIndex为 + 1
             */

            var nextI = i
            var nextTextIndex = textIndex

            val startMessage = Vector(message.subList(0, i))
            val lastMessage = Vector(message.subList(i + 1, message.size))

            val element = message[i]
            if (element is PlantText){
                val text = element.text

                if (textIndex >= text.length){
                    startMessage.add(element)
                    return SplitMessageData(startMessage, lastMessage, nextI + 1, -1)
                }


                val startText = text.substring(0, textIndex)
                val lastText = text.substring(textIndex)

                val startTextMessage = object : PlantText {
                    override val text: String = startText
                    override fun encode(): Any {
                        error("该元素为临时消息元素，无法完成序列化")
                    }
                }
                val lastTextMessage = object : PlantText {
                    override val text: String = lastText
                    override fun encode(): Any {
                        error("该元素为临时消息元素，无法完成序列化")
                    }
                }

                startMessage.add(startTextMessage)
                lastMessage.add(0, lastTextMessage)
            }else {
                startMessage.add(element)
                nextI += 1
                nextTextIndex = -1
            }

            SplitMessageData(
                startMessage = startMessage,
                lastMessage = lastMessage,
                i = nextI,
                textIndex = nextTextIndex
            )
        }
    }

    /**
     * 结果数据
     */
    private data class ResultData (
        val startRouterContext: RouterContext,
        val lastRouterContext: RouterContext,
    ){
        fun merge(context: RouterContext) {
            context.merge(startRouterContext)
            context.merge(lastRouterContext)
            context.tempHandleMessage.addAll(startRouterContext.tempHandleMessage)
            context.tempHandleMessage.addAll(lastRouterContext.tempHandleMessage)
            // 清除
            context.tempMessage.clear()
            context.tempMessage.addAll(startRouterContext.tempMessage)
            context.tempMessage.addAll(lastRouterContext.tempMessage)
        }
    }

    private data class SplitMessageData(
        val startMessage: Vector<Message>,
        val lastMessage: Vector<Message>,
        val i: Int, // 下一步的消息分割位置
        val textIndex: Int // 下一步的文本元素分割位置
    ){
        override fun toString(): String {
            return "SplitMessageData(startMessage=$startMessage, lastMessage=$lastMessage, i=$i, textIndex=$textIndex)"
        }
    }
}

fun MessageRouterBuilder.middleware(
    startRouter: MessageRouter,
    lastRouter: MessageRouter,
    greedy: Boolean = false, // 是否贪心匹配
) = MiddlewareRouter(greedy, startRouter, lastRouter)

fun MessageRouterBuilder.middleware(
    startRouter: MessageRouterBuilder.() -> Unit,
    greedy: Boolean = false, // 是否贪心匹配
    lastRouter: MessageRouterBuilder.() -> Unit
) = MiddlewareRouter(
    greedy,
    SimpleMessageRouterBuilder().apply(startRouter).build(),
    SimpleMessageRouterBuilder().apply(lastRouter).build()
)
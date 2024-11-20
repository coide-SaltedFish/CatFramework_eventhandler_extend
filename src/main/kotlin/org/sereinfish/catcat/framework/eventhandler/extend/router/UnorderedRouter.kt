package org.sereinfish.catcat.framework.eventhandler.extend.router

import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.*
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.parser.MessageRouterEncode
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 无序路由
 *
 * 可以对传入的路由进行无顺序的匹配
 */
class UnorderedRouter(
    val routers: List<MessageRouter>,
    val all: Boolean = true // 是否全部匹配
): MessageRouter {

    companion object: MessageRouterEncode<UnorderedRouter> {
        override val target: String = "unordered"

        override fun decode(vararg params: Any?): UnorderedRouter {
            return if (params.lastOrNull() is Boolean) {
                val all = (params.lastOrNull() as? Boolean) ?: true
                val routers = params.dropLast(1).map { it as MessageRouter }
                UnorderedRouter(routers, all)
            }else {
                val routers = params.map { it as MessageRouter }
                UnorderedRouter(routers)
            }



        }
    }

    override fun encode(): String {
        return "[$target: ${routers.joinToString(","){ it.encode() }}]"
    }

    /**
     * 对子路由进行顺序遍历
     *
     * 一个一个的不按顺序匹配下去，直到所有匹配成功
     */
    override fun parser(context: RouterContext): Boolean {
        val waitRouters = CopyOnWriteArrayList(routers)
        var isAnyMatched = false
        while (waitRouters.isNotEmpty()) {
            var isMatched = false
            for (router in waitRouters) {
                // 新建一个上下文
                val childContext = context.clone()
                childContext.handledMessages.clear()

                // 尝试匹配
                if (router.match(childContext)) {
                    // 匹配成功，合并结果
                    context.merge(childContext)

                    // 更新处理进度
                    context.waitHandleMessages = childContext.waitHandleMessages
                    context.handledMessages.addAll(childContext.handledMessages)
                    // 移除该路由，并且再次循环
                    waitRouters.remove(router)
                    isMatched = true
                    break
                }
            }
            // 没有一个路由可以匹配成功
            if (!isMatched) {
                // 已有匹配
                return if (isAnyMatched) all.not() else false
            }
            isAnyMatched = true
        }
        return true
    }
}

class UnorderedRouterBuilder {
    private val routers = mutableListOf<MessageRouter>()

    fun unorderedRouter(block: MessageRouterBuilder.() -> Unit) {
        routers.add(SimpleMessageRouterBuilder().apply(block).build())
    }

    fun build(all: Boolean) = UnorderedRouter(routers, all)
}

@Router
fun MessageRouterBuilder.unordered(all: Boolean = true, vararg routers: MessageRouter) =
    UnorderedRouter(routers.toList(), all)
fun MessageRouterBuilder.unordered(routers: List<MessageRouter>, all: Boolean = true) =
    UnorderedRouter(routers, all)

fun MessageRouterBuilder.unordered(all: Boolean = true, vararg routers: MessageRouterBuilder.() -> Unit) =
    UnorderedRouter(routers.map { SimpleMessageRouterBuilder().apply(it).build() }, all)

fun MessageRouterBuilder.unordered(all: Boolean = true, block: UnorderedRouterBuilder.() -> Unit) =
    UnorderedRouterBuilder().apply(block).build(all)
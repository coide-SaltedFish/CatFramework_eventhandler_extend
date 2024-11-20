package org.sereinfish.catcat.framework.eventhandler.extend.build

import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.events.message.GroupMessageEvent
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.events.message.MessageEvent
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.element.Reply
import org.sereinfish.cat.frame.PluginInfo
import org.sereinfish.cat.frame.context.TypeParser
import org.sereinfish.cat.frame.event.Event
import org.sereinfish.cat.frame.event.EventManager
import org.sereinfish.cat.frame.event.handler.EventHandler
import org.sereinfish.cat.frame.plugin.PluginManager
import org.sereinfish.catcat.framework.eventhandler.extend.handler.CatEvent
import org.sereinfish.catcat.framework.eventhandler.extend.handler.CatEventHandlerExtendContext
import org.sereinfish.catcat.framework.eventhandler.extend.handler.SimpleEventHandler
import org.sereinfish.catcat.framework.eventhandler.extend.handler.type.parser.MessageChainToMessageContent
import org.sereinfish.catcat.framework.eventhandler.extend.handler.type.parser.MessageChainToString
import org.sereinfish.catcat.framework.eventhandler.extend.handler.type.parser.MessageContentToString
import org.slf4j.LoggerFactory
import java.lang.reflect.Method
import kotlin.coroutines.resume

/**
 * 事件处理器构建
 */
class EventHandlerBuilder<E : Event>(
    val level: Int = 0,
    val eventType: Class<out Event>,
    val handler: suspend E.(CatEventHandlerExtendContext<E>) -> Unit
) {
    private val eventHandler: SimpleEventHandler<E>
    private val buildBlocks: ArrayList<EventHandlerBuilder<E>.() -> Unit> = ArrayList()
    var isProcessingComplete = true
    val eventHandlerContext get() = eventHandler.context

    init {
        val callerInfo = getCallerInfo()

        eventHandler = SimpleEventHandler(level, callerInfo?.first, callerInfo?.second, handler)

        // 添加事件类型筛选
        eventHandler.filter.add(buildEventFilterHandler {
            eventType.isAssignableFrom(event::class.java)
        })
    }

    private fun getCallerInfo(): Pair<PluginInfo, Method>? {
        // 获取调用者
        return Thread.currentThread().stackTrace.let {
            for (element in it) {
                val pluginInfo: Pair<PluginInfo, Class<*>>? = PluginManager.plugins
                    .values.firstNotNullOfOrNull { info ->
                        runCatching {
                            info.classLoader.loadPluginClass(element.className)
                        }.getOrNull()?.let {
                            if (CatEvent::class.java.isAssignableFrom(it)) info to it else null
                        }
                    }
                pluginInfo?.let {
                    val (plugin, clazz) = it
                    clazz.methods.forEach { method ->
                        if (method.name == element.methodName && (
                                    method.isAnnotationPresent(CatEvent.Handler::class.java)
                                            || method.isAnnotationPresent(CatEvent.Before::class.java)
                                            || method.isAnnotationPresent(CatEvent.After::class.java)
                                            || method.isAnnotationPresent(CatEvent.Catch::class.java)
                                    )
                        ) {
                            return plugin to method
                        }
                    }
                }
            }
            null
        }
    }

    fun <T> typeHandler(
        match: (Any, output: Class<*>) -> Boolean,
        builder: TypeParserBuilder<T>.(E) -> Unit = {},
        cast: (Any) -> T
    ) {
        before(level = -100) {
            it.addTypeParser(TypeParserBuilder<T>(match, cast).apply {
                builder(this@before)
            }.build())
        }
    }

    fun <T> typeHandler(parser: TypeParser<T>) {
        before(level = -100) {
            it.addTypeParser(parser)
        }
    }

    /**
     * 添加筛选器
     */
    fun filter(
        builder: EventFilterHandlerBuilder<E>.() -> Unit = {},
        level: Int = 0,
        filter: suspend CatEventHandlerExtendContext<E>.() -> Boolean
    ) {
        eventHandler.filter.add(EventFilterHandlerBuilder(level, filter).apply(builder).build())
    }

    /**
     * 添加前置处理器
     */
    fun before(
        builder: EventHandlerBuilder<E>.() -> Unit = {},
        level: Int = 0,
        block: suspend E.(CatEventHandlerExtendContext<E>) -> Unit = {}
    ) {
        eventHandler.preProcess.add(EventHandlerBuilder(level, eventType, block).apply(builder).build())
    }

    /**
     * 后置处理器
     */
    fun after(
        builder: EventHandlerBuilder<E>.() -> Unit = {},
        level: Int = 0,
        block: suspend E.(CatEventHandlerExtendContext<E>) -> Unit = {}
    ) {
        eventHandler.postProcess.add(EventHandlerBuilder(level, eventType, block).apply(builder).build())
    }

    fun catch(
        throws: Array<Class<out Throwable>>,
        builder: EventHandlerBuilder<E>.() -> Unit = {},
        level: Int = 0,
        block: suspend E.(CatEventHandlerExtendContext<E>) -> Unit
    ) {
        val handler = EventHandlerBuilder(level, eventType, block).apply(builder).build()
        handler.filter.add(EventFilterHandlerBuilder<E>(level) {
            // 判断上下文内异常为指定异常子类
            exception?.let {
                throws.any { it.isAssignableFrom(it::class.java) }
            } ?: false
        }.build())
        eventHandler.exceptionHandle.add(handler)
    }

    /**
     * 设置执行完成后继续执行下面的执行器
     *
     * @param state true 则继续执行 false 则不继续执行
     *
     */
    fun executeThenContinue(state: Boolean = false) {
        after {
            it.stopHandler = state.not()
        }
    }

    fun builder(block: EventHandlerBuilder<E>.() -> Unit) {
        buildBlocks.add(block)
    }

    fun build(): EventHandler<E, CatEventHandlerExtendContext<E>> {
        buildBlocks.forEach {
            it(this)
        }

        return eventHandler
    }
}

/**
 * 构建一个事件处理器
 */
inline fun <reified E : Event> buildEventHandler(
    noinline builder: EventHandlerBuilder<E>.() -> Unit = {},
    level: Int = 0,
    noinline handler: suspend E.(CatEventHandlerExtendContext<E>) -> Unit = {}
): EventHandler<E, CatEventHandlerExtendContext<E>> {
    return EventHandlerBuilder(level, E::class.java, handler).apply {
        builder()

        typeHandler(MessageChainToMessageContent)
        typeHandler(MessageContentToString)
        typeHandler(MessageChainToString)
    }.build()
}

/**
 * 等待下一个事件
 */
inline fun <reified E : Event> Event.waitNextEvent(
    noinline builder: EventHandlerBuilder<E>.() -> Unit = {},
    noinline handler: suspend E.(CatEventHandlerExtendContext<E>) -> Unit = {}
): EventHandler<E, CatEventHandlerExtendContext<E>> {
    var handlerEntity: EventHandler<E, CatEventHandlerExtendContext<E>>? = null
    handlerEntity = buildEventHandler<E>(
        level = Int.MIN_VALUE,
        builder = {
            builder()
            after {
                handlerEntity?.let { EventManager.unregisterHandler(it) }
            }
        }
    ) {
        try {
            handler(it)
        } catch (e: Exception) {
            LoggerFactory.getLogger("waitNextEvent").error("传入方法运行异常", e)
        }
    }
    // 注册事件处理器
    EventManager.registerHandler(handlerEntity)

    return handlerEntity
}

/**
 * 等待相同来源相同发送人的下一条消息
 */
suspend inline fun <reified E : MessageEvent> MessageEvent.waitNextMsg(
    timeout: Long = 0L
): E {
    val sourceEvent = this
    val block: suspend () -> E = {
        suspendCancellableCoroutine { continuation ->
            waitNextEvent<E>({
                filter {
                    event.bot.id == sourceEvent.bot.id
                            && event.target.id == sourceEvent.target.id
                            && event.sender.id == sourceEvent.sender.id
                }
            }) {
                continuation.resume(this)
            }
        }
    }
    return if (timeout > 0L) withTimeout(timeout) {
        suspendCancellableCoroutine { continuation ->
            val handler = waitNextEvent<E>({
                filter {
                    event.bot.id == sourceEvent.bot.id
                            && event.target.id == sourceEvent.target.id
                            && event.sender.id == sourceEvent.sender.id
                }
            }) {
                continuation.resume(this)
            }

            continuation.invokeOnCancellation {
                LoggerFactory.getLogger("waitNextMsg").warn("卸载等待器 $handler")
                EventManager.unregisterHandler(handler)
            }
        }
    } else block()
}

/**
 * 等待一个群的下一条消息
 */
suspend inline fun GroupMessageEvent.waitNextGroupMsg(
    timeout: Long = 0L
): GroupMessageEvent {
    val sourceEvent = this
    val block: suspend () -> GroupMessageEvent = {
        suspendCancellableCoroutine { continuation ->
            waitNextEvent<GroupMessageEvent>({
                filter {
                    event.bot.id == sourceEvent.bot.id
                            && event.target.id == sourceEvent.target.id
                }
            }) {
                continuation.resume(this)
            }
        }
    }
    return if (timeout > 0L) withTimeout(timeout) {
        suspendCancellableCoroutine { continuation ->
            val handler = waitNextEvent<GroupMessageEvent>({
                filter {
                    event.bot.id == sourceEvent.bot.id
                            && event.target.id == sourceEvent.target.id
                }
            }) {
                continuation.resume(this)
            }

            continuation.invokeOnCancellation {
                LoggerFactory.getLogger("waitNextGroupMsg").warn("卸载等待器 $handler")
                EventManager.unregisterHandler(handler)
            }
        }
    } else block()
}

/**
 * 等待群消息回复
 */
suspend inline fun GroupMessageEvent.waitGroupNextReplyMsg(
    noinline builder: EventHandlerBuilder<GroupMessageEvent>.() -> Unit = {},
    timeout: Long = 0L,
): GroupMessageEvent {
    val sourceEvent = this

    val block: suspend () -> GroupMessageEvent = {
        suspendCancellableCoroutine { continuation ->
            waitNextEvent<GroupMessageEvent>({
                filter {
                    event.bot.id == sourceEvent.bot.id
                            && event.target.id == sourceEvent.target.id
                            && event.message[Reply]?.messageId == sourceEvent.message.messageId
                }
                builder()
            }) {
                continuation.resume(this)
            }
        }
    }
    return if (timeout > 0L) withTimeout(timeout) {
        suspendCancellableCoroutine { continuation ->
            val handler = waitNextEvent<GroupMessageEvent>({
                filter {
                    event.bot.id == sourceEvent.bot.id
                            && event.target.id == sourceEvent.target.id
                            && event.message[Reply]?.messageId == sourceEvent.message.messageId
                }
                builder()
            }) {
                continuation.resume(this)
            }

            continuation.invokeOnCancellation {
                LoggerFactory.getLogger("waitGroupNextReplyMsg").warn("卸载等待器 $handler")
                EventManager.unregisterHandler(handler)
            }
        }
    } else block()
}

/**
 * 等待指定的群消息回复
 * 只会触发一次
 */
suspend inline fun GroupMessageEvent.waitGroupNextReplyMsg(
    noinline builder: EventHandlerBuilder<GroupMessageEvent>.() -> Unit = {},
    timeout: Long = 0L,
    noinline block: suspend GroupMessageEvent.(CatEventHandlerExtendContext<GroupMessageEvent>) -> Unit
) {
    val sourceEvent = this

    var handler: EventHandler<GroupMessageEvent, CatEventHandlerExtendContext<GroupMessageEvent>>? = null

    val waitBlock: suspend () -> Unit = {
        handler = waitNextEvent<GroupMessageEvent>({
            filter {
                event.bot.id == sourceEvent.bot.id
                        && event.target.id == sourceEvent.target.id
                        && event.message[Reply]?.messageId == sourceEvent.message.messageId
            }
            builder()
        }, block)
    }

    if (timeout > 0L) runCatching {
        withTimeout(timeout) {
            waitBlock()
        }
    }.getOrElse {
        LoggerFactory.getLogger("waitGroupNextReplyMsg").warn("卸载等待器 $handler")
        handler?.let {
            EventManager.unregisterHandler(it)
            handler = null
        }

        if ((it is TimeoutCancellationException).not()) {
            throw it
        }
    } else waitBlock()
}

inline fun <reified E : Event> buildCatchHandler(
    throws: Array<Class<out Throwable>>,
    noinline builder: EventHandlerBuilder<E>.() -> Unit = {},
    level: Int = 0,
    noinline block: suspend E.(CatEventHandlerExtendContext<E>) -> Unit = {}
): EventHandler<E, CatEventHandlerExtendContext<E>> {
    val handler = EventHandlerBuilder(level, E::class.java, block).apply {
        builder()

        typeHandler(MessageChainToMessageContent)
        typeHandler(MessageContentToString)
        typeHandler(MessageChainToString)
    }.build()
    handler.filter.add(EventFilterHandlerBuilder<E>(level) {
        // 判断上下文内异常为指定异常子类
        exception?.let {
            throws.any { it.isAssignableFrom(it::class.java) }
        } ?: false
    }.build())

    return handler
}

inline fun <reified E : Event, reified T : Throwable> buildCatchHandler(
    noinline builder: EventHandlerBuilder<E>.() -> Unit = {},
    level: Int = 0,
    noinline block: suspend E.(CatEventHandlerExtendContext<E>) -> Unit = {}
) = buildCatchHandler<E>(
    level = level,
    throws = arrayOf(T::class.java),
    builder = {
        builder()

        typeHandler(MessageChainToMessageContent)
        typeHandler(MessageContentToString)
        typeHandler(MessageChainToString)
    },
    block = block
)
package org.sereinfish.catcat.framework.eventhandler.extend.build

import org.sereinfish.cat.frame.context.TypeParser
import org.sereinfish.cat.frame.event.Event
import org.sereinfish.cat.frame.event.handler.EventHandler
import org.sereinfish.catcat.framework.eventhandler.extend.handler.CatEventHandlerExtendContext
import org.sereinfish.catcat.framework.eventhandler.extend.handler.SimpleEventHandler

/**
 * 事件处理器构建
 */
class EventHandlerBuilder<E: Event>(
    val level: Int = 0,
    val eventType: Class<out Event>,
    val handler: suspend E.(CatEventHandlerExtendContext<E>) -> Unit
) {
    private val eventHandler = SimpleEventHandler(level, handler)
    var isProcessingComplete = true

    init {
        // 添加事件类型筛选
        eventHandler.filter.add(buildEventFilterHandler {
            eventType.isAssignableFrom(event::class.java)
        })
    }

    fun <T> typeHandler(
        match: (Any, output: Class<*>) -> Boolean,
        builder: TypeParserBuilder<T>.(E) -> Unit = {},
        cast: (Any) -> T
    ) {
        before(-100) {
            it.addTypeParser(TypeParserBuilder<T>(match, cast).apply {
                builder(this@before)
            }.build())
        }
    }

    fun <T> typeHandler(parser: TypeParser<T>) {
        before(-100) {
            it.addTypeParser(parser)
        }
    }

    /**
     * 添加筛选器
     */
    fun filter(
        level: Int = 0,
        builder: EventFilterHandlerBuilder<E>.() -> Unit = {},
        filter: suspend CatEventHandlerExtendContext<E>.() -> Boolean
    ) {
        eventHandler.filter.add(EventFilterHandlerBuilder(level, filter).apply(builder).build())
    }

    /**
     * 添加前置处理器
     */
    fun before(
        level: Int = 0,
        builder: EventHandlerBuilder<E>.() -> Unit = {},
        block: suspend E.(CatEventHandlerExtendContext<E>) -> Unit
    ) {
        eventHandler.preProcess.add(EventHandlerBuilder(level, eventType, block).apply(builder).build())
    }

    /**
     * 后置处理器
     */
    fun after(
        level: Int = 0,
        builder: EventHandlerBuilder<E>.() -> Unit = {},
        block: suspend E.(CatEventHandlerExtendContext<E>) -> Unit
    ) {
        eventHandler.postProcess.add(EventHandlerBuilder(level, eventType, block).apply(builder).build())
    }

    fun catch(
        level: Int = 0,
        throws: Array<Class<out Throwable>>,
        builder: EventHandlerBuilder<E>.() -> Unit = {},
        block: suspend E.(CatEventHandlerExtendContext<E>) -> Unit
    ){
        val handler = EventHandlerBuilder(level, eventType, block).apply(builder).build()
        handler.filter.add(EventFilterHandlerBuilder<E>(level){
            // 判断上下文内异常为指定异常子类
            exception?.let {
                throws.any { it.isAssignableFrom(it::class.java) }
            } ?: false
        }.build())
        eventHandler.exceptionHandle.add(handler)
    }

    fun build(): EventHandler<E, CatEventHandlerExtendContext<E>> {
        return eventHandler
    }
}

/**
 * 构建一个事件处理器
 */
inline fun <reified E: Event> buildEventHandler(
    level: Int = 0,
    noinline builder: EventHandlerBuilder<E>.() -> Unit = {},
    noinline handler: suspend E.(CatEventHandlerExtendContext<E>) -> Unit
): EventHandler<E, CatEventHandlerExtendContext<E>> {
    return EventHandlerBuilder(level, E::class.java, handler).apply(builder).build()
}

inline fun <reified E: Event> buildCatchHandler(
    level: Int = 0,
    throws: Array<Class<out Throwable>>,
    noinline builder: EventHandlerBuilder<E>.() -> Unit = {},
    noinline block: suspend E.(CatEventHandlerExtendContext<E>) -> Unit
): EventHandler<E, CatEventHandlerExtendContext<E>> {
    val handler = EventHandlerBuilder<E>(level, E::class.java, block).apply(builder).build()
    handler.filter.add(EventFilterHandlerBuilder<E>(level){
        // 判断上下文内异常为指定异常子类
        exception?.let {
            throws.any { it.isAssignableFrom(it::class.java) }
        } ?: false
    }.build())

    return handler
}

inline fun <reified E: Event, reified T: Throwable> buildCatchHandler(
    level: Int = 0,
    noinline builder: EventHandlerBuilder<E>.() -> Unit = {},
    noinline block: suspend E.(CatEventHandlerExtendContext<E>) -> Unit
) = buildCatchHandler<E>(level, arrayOf(T::class.java), builder, block)
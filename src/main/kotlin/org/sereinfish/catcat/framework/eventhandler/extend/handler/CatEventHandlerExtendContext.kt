package org.sereinfish.catcat.framework.eventhandler.extend.handler

import org.sereinfish.cat.frame.context.TypeParser
import org.sereinfish.cat.frame.context.property.ContextOrElseProperty
import org.sereinfish.cat.frame.context.property.ContextOrNullProperty
import org.sereinfish.cat.frame.context.property.ContextOrPutProperty
import org.sereinfish.cat.frame.context.property.valueOrPut
import org.sereinfish.cat.frame.event.Event
import org.sereinfish.cat.frame.event.EventHandlerContext

class CatEventHandlerExtendContext<E: Event>(
    event: E,
    eventHandlerContext: CatEventHandlerPropertyContext<E>,
) : EventHandlerContext<E>(event) {
    // 处理器的上下文
    val eventHandlerContext by valueOrPut { eventHandlerContext }

    override fun <T> contextOrElseProperty(default: (String) -> T): ContextOrElseProperty<T> {
        return ContextOrElseProperty(this, default)
    }

    override fun <T> contextOrNullProperty(): ContextOrNullProperty<T> {
        return ContextOrNullProperty(this)
    }

    override fun <T> contextOrPutProperty(default: (String) -> T): ContextOrPutProperty<T> {
        return ContextOrPutProperty(this, default)
    }

    fun addTypeParser(parser: TypeParser<*>): Boolean {
        return typeParser.add(parser)
    }
}
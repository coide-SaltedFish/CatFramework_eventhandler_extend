package org.sereinfish.catcat.framework.eventhandler.extend.build

import org.sereinfish.cat.frame.event.Event
import org.sereinfish.cat.frame.event.EventHandlerContext
import org.sereinfish.cat.frame.event.handler.EventHandler
import org.sereinfish.catcat.framework.eventhandler.extend.handler.CatEventHandlerExtendContext
import org.sereinfish.catcat.framework.eventhandler.extend.handler.filter.EventFilterHandler

class EventFilterHandlerBuilder<E: Event>(
    val level: Int = 0,
    val filter: suspend CatEventHandlerExtendContext<E>.() -> Boolean
) {
    private val filterHandler = EventFilterHandler(level, filter)

    fun build(): EventHandler<E, CatEventHandlerExtendContext<E>> {
        return filterHandler
    }
}

fun <E: Event> buildEventFilterHandler(
    level: Int = 0,
    builder: EventFilterHandlerBuilder<E>.() -> Unit = {},
    filter: CatEventHandlerExtendContext<E>.() -> Boolean
) = EventFilterHandlerBuilder(level, filter).apply(builder).build()
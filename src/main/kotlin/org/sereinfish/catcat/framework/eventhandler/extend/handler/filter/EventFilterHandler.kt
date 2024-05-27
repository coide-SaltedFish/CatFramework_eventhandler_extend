package org.sereinfish.catcat.framework.eventhandler.extend.handler.filter

import org.sereinfish.cat.frame.event.Event
import org.sereinfish.catcat.framework.eventhandler.extend.handler.AbstractEventHandler
import org.sereinfish.catcat.framework.eventhandler.extend.handler.CatEventHandlerExtendContext

class EventFilterHandler<E: Event>(
    level: Int = 0,
    val filterHandler: suspend CatEventHandlerExtendContext<E>.() -> Boolean,
): AbstractEventHandler<E>(level) {
    override suspend fun process(context: CatEventHandlerExtendContext<E>) {
        context.result = filterHandler.invoke(context)
    }
}
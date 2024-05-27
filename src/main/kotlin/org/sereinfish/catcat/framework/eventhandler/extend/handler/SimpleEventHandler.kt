package org.sereinfish.catcat.framework.eventhandler.extend.handler

import org.sereinfish.cat.frame.event.Event
import org.sereinfish.cat.frame.event.EventHandlerContext

class SimpleEventHandler<E: Event>(
    level: Int = 0,
    val handler: suspend E.(CatEventHandlerExtendContext<E>) -> Unit
): AbstractEventHandler<E>(level) {

    override suspend fun process(context: CatEventHandlerExtendContext<E>) {
        context.event.handler(context)
    }

    override fun getContext(event: E): EventHandlerContext<E> {
        return CatEventHandlerExtendContext(event)
    }
}
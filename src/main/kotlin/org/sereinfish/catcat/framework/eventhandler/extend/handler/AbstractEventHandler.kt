package org.sereinfish.catcat.framework.eventhandler.extend.handler

import org.sereinfish.cat.frame.context.Context
import org.sereinfish.cat.frame.event.Event
import org.sereinfish.cat.frame.event.EventHandlerChain
import org.sereinfish.cat.frame.event.EventHandlerContext
import org.sereinfish.cat.frame.event.handler.EventHandler
import org.sereinfish.cat.frame.event.handler.FilterInvokerChain
import org.sereinfish.cat.frame.event.invoker.Invoker
import org.sereinfish.cat.frame.event.invoker.InvokerChain

abstract class AbstractEventHandler<E: Event>(
    override val level: Int = 0
): EventHandler<E, CatEventHandlerExtendContext<E>> {
    override val context = CatEventHandlerPropertyContext(this)

    override val exceptionHandle = EventHandlerChain<E, CatEventHandlerExtendContext<E>>()
    override val filter: FilterInvokerChain<EventHandler<E, CatEventHandlerExtendContext<E>>, CatEventHandlerExtendContext<E>> = FilterInvokerChain()

    override val preProcess = EventHandlerChain<E, CatEventHandlerExtendContext<E>>()
    override val postProcess = EventHandlerChain<E, CatEventHandlerExtendContext<E>>()
}
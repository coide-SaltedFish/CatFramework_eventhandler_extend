package org.sereinfish.catcat.framework.eventhandler.extend.handler

import org.sereinfish.cat.frame.PluginInfo
import org.sereinfish.cat.frame.event.Event
import org.sereinfish.cat.frame.event.EventHandlerContext
import java.lang.reflect.Method

class SimpleEventHandler<E: Event>(
    level: Int = 0,
    pluginInfo: PluginInfo? = null,
    buildMethod: Method? = null,
    val handler: suspend E.(CatEventHandlerExtendContext<E>) -> Unit
): AbstractEventHandler<E>(level) {
    init {
        buildMethod?.let {
            context["buildMethod"] = it
            context["pluginInfo"] = pluginInfo
        }
    }

    override suspend fun process(context: CatEventHandlerExtendContext<E>) {
        context.event.handler(context)
    }

    override fun getContext(event: E): EventHandlerContext<E> {
        return CatEventHandlerExtendContext(event, context)
    }
}
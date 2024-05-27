package org.sereinfish.catcat.framework.eventhandler.extend.router.handle

import org.catcat.sereinfish.qqbot.universal.abstraction.layer.events.message.MessageEvent
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.MessageRouterBuilder
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.RouterContext
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.SimpleMessageRouterBuilder
import org.sereinfish.cat.frame.event.handler.EventHandler
import org.sereinfish.catcat.framework.eventhandler.extend.build.EventFilterHandlerBuilder
import org.sereinfish.catcat.framework.eventhandler.extend.handler.CatEventHandlerExtendContext

class RouterBuilder<E: MessageEvent>(
    val level: Int,
    val block: MessageRouterBuilder.(CatEventHandlerExtendContext<E>) -> Unit
) {

    fun build(): EventHandler<E, CatEventHandlerExtendContext<E>> {
        return EventFilterHandlerBuilder(level){
            val context = RouterContext(event)
            SimpleMessageRouterBuilder().apply { block(this@EventFilterHandlerBuilder) }.build().match(context).also {
                context.mergeTo(this)
            }
        }.build()
    }
}
package org.sereinfish.catcat.framework.eventhandler.extend.router.handle

import org.catcat.sereinfish.qqbot.universal.abstraction.layer.events.message.MessageEvent
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.MessageChain
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.MessageFactory
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.MessageReceipt
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.MessageRouter
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.MessageRouterBuilder
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.extend.*
import org.sereinfish.cat.frame.event.Event
import org.sereinfish.cat.frame.event.EventHandlerContext
import org.sereinfish.cat.frame.event.handler.EventHandler
import org.sereinfish.cat.frame.utils.logger
import org.sereinfish.catcat.framework.eventhandler.extend.build.*
import org.sereinfish.catcat.framework.eventhandler.extend.handler.CatEventHandlerExtendContext
import org.sereinfish.catcat.framework.eventhandler.extend.handler.HandlerListEntity
import org.sereinfish.catcat.framework.eventhandler.extend.handler.HandlerListEntityImpl

/**
 * 路由构建
 */
class RoutersBuilder<E: MessageEvent>(
    val level: Int,
    private val eventType: Class<E>
) {
    private val handlerListEntity = HandlerListEntityImpl()
    private val filters = ArrayList<EventHandler<E, CatEventHandlerExtendContext<E>>>()
    private val befores = ArrayList<EventHandler<E, CatEventHandlerExtendContext<E>>>()
    private val afters = ArrayList<EventHandler<E, CatEventHandlerExtendContext<E>>>()
    private val catchs = ArrayList<EventHandler<E, CatEventHandlerExtendContext<E>>>()

    fun filter(
        level: Int = 0,
        builder: EventFilterHandlerBuilder<E>.() -> Unit = {},
        filter: suspend CatEventHandlerExtendContext<E>.() -> Boolean
    ) {
        filters.add(EventFilterHandlerBuilder(level, filter).apply(builder).build())
    }

    /**
     * 添加前置处理器
     */
    fun before(
        level: Int = 0,
        builder: EventHandlerBuilder<E>.() -> Unit = {},
        block: suspend E.(CatEventHandlerExtendContext<E>) -> Unit
    ) {
        befores.add(EventHandlerBuilder(level, eventType, block).apply(builder).build())
    }

    /**
     * 后置处理器
     */
    fun after(
        level: Int = 0,
        builder: EventHandlerBuilder<E>.() -> Unit = {},
        block: suspend E.(CatEventHandlerExtendContext<E>) -> Unit
    ) {
        afters.add(EventHandlerBuilder(level, eventType, block).apply(builder).build())
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
        catchs.add(handler)
    }

    infix fun RouterBuilder<E>.send(block: suspend E.(CatEventHandlerExtendContext<E>) -> MessageChain) {
        val handler = EventHandlerBuilder(level, eventType){
            val message = block(this, it)
            val messageReceipt = (this as MessageEvent).sendMessage(message)
            it["messageReceipt"] = messageReceipt
        }.build()
        handler.filter.add(this.build())
        handlerListEntity.add(handler as EventHandler<Event, EventHandlerContext<Event>>)
    }

    infix fun RouterBuilder<E>.send(text: String) = send {
        buildMessage {
            + text
        }
    }

    infix fun Regex.send(block: suspend E.(CatEventHandlerExtendContext<E>) -> MessageChain) = routers {
        + regex(this@send) + end()
    }.send(block)

    infix fun Regex.send(text: String) = routers {
        + regex(this@send) + end()
    }.send(text)

    infix fun String.send(block: suspend E.(CatEventHandlerExtendContext<E>) -> MessageChain) = routers {
        + text(this@send) + end()
    }.send(block)

    infix fun String.send(text: String) = routers {
        + text(this@send) + end()
    }.send(text)

    infix fun RouterBuilder<E>.sendMessage(builder: MessageFactory.(CatEventHandlerExtendContext<E>) -> Unit) =
        send { bot.messageFactory().apply { builder(it) } .build() }
    infix fun String.sendMessage(builder: MessageFactory.(CatEventHandlerExtendContext<E>) -> Unit) =
        send { bot.messageFactory().apply { builder(it) } .build() }
    infix fun Regex.sendMessage(builder: MessageFactory.(CatEventHandlerExtendContext<E>) -> Unit) =
        send { bot.messageFactory().apply { builder(it) } .build() }

    infix fun RouterBuilder<E>.reply(block: suspend E.(CatEventHandlerExtendContext<E>) -> MessageChain) {
        val handler = EventHandlerBuilder(level, eventType){
            val message = block(this, it)
            val messageReceipt = (this as MessageEvent).reply(message)
            it["messageReceipt"] = messageReceipt
        }.build()
        handler.filter.add(this.build())
        handlerListEntity.add(handler as EventHandler<Event, EventHandlerContext<Event>>)
    }

    infix fun RouterBuilder<E>.reply(text: String) = reply {
        buildMessage {
            + text
        }
    }

    infix fun Regex.reply(block: suspend E.(CatEventHandlerExtendContext<E>) -> MessageChain) = routers {
        + regex(this@reply) + end()
    }.reply(block)

    infix fun Regex.reply(text: String) = routers {
        + regex(this@reply) + end()
    }.reply(text)

    infix fun String.reply(block: suspend E.(CatEventHandlerExtendContext<E>) -> MessageChain) = routers {
        + text(this@reply) + end()
    }.reply(block)

    infix fun String.reply(text: String) = routers {
        + text(this@reply) + end()
    }.reply(text)

    infix fun RouterBuilder<E>.replyMessage(builder: MessageFactory.(CatEventHandlerExtendContext<E>) -> Unit) =
        reply {  bot.messageFactory().apply { builder(it) } .build()  }
    infix fun String.replyMessage(builder: MessageFactory.(CatEventHandlerExtendContext<E>) -> Unit) =
        reply {  bot.messageFactory().apply { builder(it) } .build()  }
    infix fun Regex.replyMessage(builder: MessageFactory.(CatEventHandlerExtendContext<E>) -> Unit) =
        reply {  bot.messageFactory().apply { builder(it) } .build()  }

    infix fun RouterBuilder<E>.atReply(block: suspend E.(CatEventHandlerExtendContext<E>) -> MessageChain) {
        val handler = EventHandlerBuilder(level, eventType){
            val message = block(this, it)
            val messageReceipt = (this as MessageEvent).let {
                it.reply(it.buildMessage {
                    + at(it.sender.id)
                    + text(" ")
                    + message
                })
            }

            it["messageReceipt"] = messageReceipt
        }.build()
        handler.filter.add(this.build())
        handlerListEntity.add(handler as EventHandler<Event, EventHandlerContext<Event>>)
    }

    infix fun RouterBuilder<E>.atReply(text: String) = atReply {
        buildMessage {
            + text
        }
    }

    infix fun Regex.atReply(block: suspend E.(CatEventHandlerExtendContext<E>) -> MessageChain) = routers {
        + regex(this@atReply) + end()
    }.atReply(block)

    infix fun Regex.atReply(text: String) = routers {
        + regex(this@atReply) + end()
    }.atReply(text)

    infix fun String.atReply(block: suspend E.(CatEventHandlerExtendContext<E>) -> MessageChain) = routers {
        + text(this@atReply) + end()
    }.atReply(block)

    infix fun String.atReply(text: String) = routers {
        + text(this@atReply) + end()
    }.atReply(text)

    infix fun RouterBuilder<E>.atReplyMessage(builder: MessageFactory.(CatEventHandlerExtendContext<E>) -> Unit) =
        atReply {  bot.messageFactory().apply { builder(it) } .build()  }
    infix fun String.atReplyMessage(builder: MessageFactory.(CatEventHandlerExtendContext<E>) -> Unit) =
        atReply {  bot.messageFactory().apply { builder(it) } .build()  }
    infix fun Regex.atReplyMessage(builder: MessageFactory.(CatEventHandlerExtendContext<E>) -> Unit) =
        atReply {  bot.messageFactory().apply { builder(it) } .build()  }

    infix fun String.handle(block: suspend E.(CatEventHandlerExtendContext<E>) -> Unit) =
        routers {
            + text(this@handle) + end()
        }.handle(block)

    infix fun Regex.handle(block: suspend E.(CatEventHandlerExtendContext<E>) -> Unit) =
        routers {
            + regex(this@handle) + end()
        }.handle(block)

    infix fun RouterBuilder<E>.handle(block: suspend E.(CatEventHandlerExtendContext<E>) -> Unit) {
        val handler = EventHandlerBuilder(level, eventType, block).build()
        handler.filter.add(this.build())
        handlerListEntity.add(handler as EventHandler<Event, EventHandlerContext<Event>>)
    }

    fun routers(block: MessageRouterBuilder.(CatEventHandlerExtendContext<E>) -> Unit): RouterBuilder<E> {
        return RouterBuilder(0, block)
    }

    /**
     * 构建路由
     */
    fun build(): HandlerListEntity {
        handlerListEntity.forEach { handler ->
            filters.forEach {
                handler.filter.add(it as EventHandler<Event, EventHandlerContext<Event>>)
            }
            befores.forEach {
                handler.preProcess.add(it as EventHandler<Event, EventHandlerContext<Event>>)
            }

            afters.forEach {
                handler.postProcess.add(it as EventHandler<Event, EventHandlerContext<Event>>)
            }

            catchs.forEach {
                handler.exceptionHandle.add(it as EventHandler<Event, EventHandlerContext<Event>>)
            }

            handler.postProcess.add(EventHandlerBuilder<E>(level, eventType){
                it.stopHandler = true
            }.build() as EventHandler<Event, EventHandlerContext<Event>>)
        }

        return handlerListEntity
    }
}

inline fun <reified E: MessageEvent> buildRouters(
    level: Int = 0,
    block: RoutersBuilder<E>.() -> Unit
) = RoutersBuilder(level, E::class.java).apply { block() }.build()
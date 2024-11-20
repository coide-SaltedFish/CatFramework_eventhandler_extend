package org.sereinfish.catcat.framework.eventhandler.extend.handler

import org.sereinfish.cat.frame.PluginInfo
import org.sereinfish.cat.frame.context.Context
import org.sereinfish.cat.frame.context.TypeParser
import org.sereinfish.cat.frame.context.property.ContextOrElseProperty
import org.sereinfish.cat.frame.context.property.ContextOrNullProperty
import org.sereinfish.cat.frame.context.property.ContextOrPutProperty
import org.sereinfish.cat.frame.event.Event
import org.sereinfish.cat.frame.event.handler.EventHandler
import java.lang.reflect.Method
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class CatEventHandlerPropertyContext<T: Event>(
    eventHandler: EventHandler<T, CatEventHandlerExtendContext<T>>,
    override val data: ConcurrentHashMap<String, Any?> = ConcurrentHashMap(),
    override var typeParser: Vector<TypeParser<*>> = Vector()
) : Context {

    init {
        this["uuid"] = UUID.randomUUID().toString()
        this["eventHandler"] = eventHandler
    }

    val buildMethod: Method? get() = this["buildMethod"]?.let { it as? Method }
    val pluginInfo: PluginInfo? get() = this["pluginInfo"]?.let { it as? PluginInfo }

    val uuid: String get() =  this["uuid"] as String
    val eventHandler: EventHandler<T, CatEventHandlerExtendContext<T>>
        get() = this["eventHandler"] as EventHandler<T, CatEventHandlerExtendContext<T>>


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
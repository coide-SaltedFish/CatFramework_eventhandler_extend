package org.sereinfish.catcat.framework.eventhandler.extend.handler

import org.sereinfish.cat.frame.context.getOrNull
import org.sereinfish.cat.frame.event.Event
import org.sereinfish.cat.frame.event.EventHandlerContext
import org.sereinfish.cat.frame.event.EventManager
import org.sereinfish.cat.frame.event.handler.EventHandler
import org.sereinfish.cat.frame.plugin.PluginManager
import org.sereinfish.cat.frame.utils.logger
import org.sereinfish.catcat.framework.eventhandler.extend.utils.PackageUtils

object PluginHandlerManager {
    private val logger = logger()
    private val _handlers = ArrayList<EventHandler<Event, EventHandlerContext<Event>>>()
    val handlers: List<EventHandler<Event, EventHandlerContext<Event>>> get() = _handlers

    init {
        logger.info("已扫描载入 ${scanPlugin()} 个事件处理器")
    }

    /**
     * 扫描插件，并且初始化事件处理器
     */
    private fun scanPlugin(): Int {
        var count = 0
        PluginManager.plugins.forEach { (_, pluginInfo) ->
            val classLoader = pluginInfo.classLoader
            pluginInfo.config.getOrNull<String>("plugin.event.scanPackage")?.let { packet ->
                logger.info("开始扫描插件[${pluginInfo.id}]的包[$packet]")
                // 尝试扫描包
                PackageUtils.scanJar(pluginInfo.jarFile, packet).mapNotNull {
                    try {
                        classLoader.loadClass(it)
                    } catch (e: Exception) {
                        logger.error("加载插件[${pluginInfo.id}]类失败：$it", e)
                        null
                    }
                }.forEach { clazz ->
                    count += loadHandler(clazz)
                }
            }
        }
        return count
    }

    /**
     * 加载事件处理器
     */
    private fun loadHandler(clazz: Class<*>): Int {
        var count = 0
        val before = ArrayList<EventHandler<Event, EventHandlerContext<Event>>>()
        val after= ArrayList<EventHandler<Event, EventHandlerContext<Event>>>()
        val catch = ArrayList<EventHandler<Event, EventHandlerContext<Event>>>()

        // 判断是否实现了CatEvent接口
        if (CatEvent::class.java.isAssignableFrom(clazz)) {
            val obj = clazz.getConstructor().newInstance()
            // 扫描内部方法，获取Before、After、Catch注解下方法实例
            clazz.declaredMethods.forEach { method ->
                when {
                    method.isAnnotationPresent(CatEvent.Before::class.java) -> {
                        // 判断函数返回类型为EventHandler<*, *>
                        if (method.returnType.isAssignableFrom(EventHandler::class.java)) {
                            // 根据函数类型执行函数，兼容suspend和一般函数
                            if (method.parameters.isNotEmpty()) {
                                logger.error("处理器[${clazz.name}.${method.name}]加载失败，无法完成实例化，参数过多：${method.parameterCount}")
                            }else {
                                val handler = method.invoke(obj) as EventHandler<*, *>
                                before.add(handler as EventHandler<Event, EventHandlerContext<Event>>)
                            }
                        }
                    }

                    method.isAnnotationPresent(CatEvent.After::class.java) -> {
                        // 判断函数返回类型为EventHandler<*, *>
                        if (method.returnType.isAssignableFrom(EventHandler::class.java)) {
                            // 根据函数类型执行函数，兼容suspend和一般函数
                            if (method.parameters.isNotEmpty()) {
                                logger.error("处理器[${clazz.name}.${method.name}]加载失败，无法完成实例化，参数过多：${method.parameterCount}")
                            }else {
                                val handler = method.invoke(obj) as EventHandler<*, *>
                                after.add(handler as EventHandler<Event, EventHandlerContext<Event>>)
                            }
                        }
                    }

                    method.isAnnotationPresent(CatEvent.Catch::class.java) -> {
                        // 判断函数返回类型为EventHandler<*, *>
                        if (method.returnType.isAssignableFrom(EventHandler::class.java)) {
                            // 根据函数类型执行函数，兼容suspend和一般函数
                            if (method.parameters.isNotEmpty()) {
                                logger.error("处理器[${clazz.name}.${method.name}]加载失败，无法完成实例化，参数过多：${method.parameterCount}")
                            }else {
                                val handler = method.invoke(obj) as EventHandler<*, *>
                                catch.add(handler as EventHandler<Event, EventHandlerContext<Event>>)
                            }
                        }
                    }
                }
            }
            // 扫描内部方法，获取Handler注解下方法实例，并且加载为完整事件处理器
            clazz.declaredMethods.forEach { method ->
                if (method.isAnnotationPresent(CatEvent.Handler::class.java)){
                    // 判断函数返回类型为EventHandler<*, *>
                    if (method.returnType.isAssignableFrom(EventHandler::class.java)) {
                        // 根据函数类型执行函数，兼容suspend和一般函数
                        if (method.parameters.isNotEmpty()) {
                            logger.error("处理器[${clazz.name}.${method.name}]加载失败，无法完成实例化，参数过多：${method.parameterCount}")
                        }else {
                            val handler = method.invoke(obj) as EventHandler<Event, EventHandlerContext<Event>>

                            before.forEach {
                                handler.preProcess.add(it)
                            }
                            after.forEach {
                                handler.postProcess.add(it)
                            }
                            catch.forEach {
                                handler.exceptionHandle.add(it)
                            }

                            EventManager.registerHandler(handler)
                            _handlers.add(handler)

                            count ++
                            logger.info("注册扫描处理器：${clazz.name}.${method.name}")
                        }
                    } else if (method.returnType.isAssignableFrom(HandlerListEntity::class.java)){ // 如果返回是一个处理器列表
                        if (method.parameters.isNotEmpty()) {
                            logger.error("处理器[${clazz.name}.${method.name}]加载失败，无法完成实例化，参数过多：${method.parameterCount}")
                        }else {
                            val handlers = method.invoke(obj) as HandlerListEntity

                            handlers.forEachIndexed { index, eventHandler ->
                                before.forEach {
                                    eventHandler.preProcess.add(it)
                                }
                                after.forEach {
                                    eventHandler.postProcess.add(it)
                                }
                                catch.forEach {
                                    eventHandler.exceptionHandle.add(it)
                                }

                                EventManager.registerHandler(eventHandler)
                                _handlers.add(eventHandler)

                                count ++
                                logger.info("注册扫描处理器：${clazz.name}.${method.name}[$index]")
                            }
                        }
                    } else logger.warn(
                        "${clazz.name}.${method.name}" +
                                "(${method.parameters.joinToString { it.name }})加载失败，" +
                                "已标记为处理器，但返回类型错误：${method.returnType.name}"
                    )
                }
            }
        }

        return count
    }
}
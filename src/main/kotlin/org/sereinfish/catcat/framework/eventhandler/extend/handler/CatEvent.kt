package org.sereinfish.catcat.framework.eventhandler.extend.handler

import kotlin.reflect.KClass

interface CatEvent {

    /**
     * 注解Before，具有属性level
     * 只在实现了CatEvent的对象有效
     * 注解在运行时生效，可注解方法
     */
    @Target(AnnotationTarget.FUNCTION)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Before

    @Target(AnnotationTarget.FUNCTION)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Handler

    @Target(AnnotationTarget.FUNCTION)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class After

    @Target(AnnotationTarget.FUNCTION)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Catch(
//        vararg val value: KClass<out Throwable>
    )
}
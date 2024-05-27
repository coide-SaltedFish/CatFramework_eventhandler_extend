package org.sereinfish.catcat.framework.eventhandler.extend.build

import org.sereinfish.cat.frame.context.TypeParser

class TypeParserBuilder<T>(
    val match: (Any, output: Class<*>) -> Boolean,
    val cast: (Any) -> T
) {
    fun build() = object : TypeParser<T> {
        override fun cast(any: Any): T {
            return this@TypeParserBuilder.cast(any)
        }

        override fun match(any: Any, output: Class<*>): Boolean {
            return this@TypeParserBuilder.match(any, output)
        }

    }
}
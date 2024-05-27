package org.sereinfish.catcat.framework.eventhandler.extend.handler.type.parser

import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.MessageContent
import org.sereinfish.cat.frame.context.TypeParser

open class MessageContentToString: TypeParser<String> {
    companion object: MessageContentToString()

    override fun cast(any: Any): String {
        any as MessageContent
        return any.contentString()
    }

    override fun match(any: Any, output: Class<*>): Boolean {
        return any is MessageContent && output.isAssignableFrom(String::class.java)
    }
}
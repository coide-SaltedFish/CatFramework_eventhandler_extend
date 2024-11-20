package org.sereinfish.catcat.framework.eventhandler.extend.handler.type.parser

import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.MessageChain
import org.sereinfish.cat.frame.context.TypeParser

open class MessageChainToString: TypeParser<String> {
    companion object: MessageChainToString()

    override fun cast(any: Any): String {
        return (any as MessageChain).contentString()
    }

    override fun match(any: Any, output: Class<*>): Boolean {
        return any is MessageChain
    }
}
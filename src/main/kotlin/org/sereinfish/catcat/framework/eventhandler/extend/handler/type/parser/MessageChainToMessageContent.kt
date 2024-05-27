package org.sereinfish.catcat.framework.eventhandler.extend.handler.type.parser

import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.MessageChain
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.MessageContent
import org.sereinfish.cat.frame.context.TypeParser
import org.sereinfish.cat.frame.utils.nonNull

open class MessageChainToMessageContent: TypeParser<MessageContent> {
    companion object: MessageChainToMessageContent()

    override fun cast(any: Any): MessageContent {
        return (any as MessageChain).single() as MessageContent
    }

    override fun match(any: Any, output: Class<*>): Boolean {
        return any is MessageChain && any.singleOrNull().nonNull()
    }
}
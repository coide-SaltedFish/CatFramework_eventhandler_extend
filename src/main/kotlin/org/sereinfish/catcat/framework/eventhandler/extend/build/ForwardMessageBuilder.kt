package org.sereinfish.catcat.framework.eventhandler.extend.build

import org.catcat.sereinfish.qqbot.universal.abstraction.layer.contact.User
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.Message
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.element.Forward
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.forward.ForwardMessageFactory
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.utils.UniversalId

class ForwardMessageBuilder(
    val factory: ForwardMessageFactory
) {

    infix fun String.id(id: UniversalId) = UserInfo(this, id)

    infix fun User.say(message: Message) = factory.add(factory.node(id, name, message))

    /**
     * 新增消息
     */
    infix fun UserInfo.say(message: Message) = factory.add(factory.node(id, name, message))

    data class UserInfo(val name: String, val id: UniversalId)
}

fun ForwardMessageFactory.build(block: ForwardMessageBuilder.() -> Unit): Forward {
    ForwardMessageBuilder(this).block()
    return build()
}
package org.sereinfish.catcat.framework.eventhandler.extend.router

import org.catcat.sereinfish.qqbot.universal.abstraction.layer.events.message.GroupMessageEvent
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.MessageRouter
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.MessageRouterBuilder
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.extend.at
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.extend.or
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.extend.text

/**
 * 可以同时匹配 At 类型的 @Bot 以及字符类型的 @Bot
 */
fun MessageRouterBuilder.atBotOrText(event: GroupMessageEvent): MessageRouter =
    event.group.members[event.bot.id]?.let {
        or(
            at(event.bot),
            text("@${it.cardNameOrRemarkNameOrNickName}"),
            text("@${event.bot.name}"),
            text("@${event.bot.id.encodeToString()}")
        )
    } ?: at(event.bot)
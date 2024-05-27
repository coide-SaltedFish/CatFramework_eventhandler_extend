package org.sereinfish.catcat.framework.eventhandler.extend.router

import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.element.PlantText
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.MessageRouter
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.MessageRouterBuilder
import org.catcat.sereinfish.qqbot.universal.abstraction.layer.message.router.RouterContext
import org.sereinfish.catcat.framework.eventhandler.extend.utils.CnNumberFactory

class CnNumberRouter: MessageRouter {
    override fun parser(context: RouterContext): Boolean {
        // 读取字符串
        // 尝试整合多个文本元素
        var i = 0

        val textMessage = buildString {
            for (element in context.tempMessage){
                if (element is PlantText) {
                    append(element.text)
                    i++
                } else break
            }
        }
        // 尝试正则匹配
        var mstr = "" // 待匹配文本
        var matchStr = "" // 匹配的文本
        for (c in textMessage) {
            mstr += c
            // 匹配
            if (CnNumberFactory.isCnNumber(mstr)){
                matchStr = mstr
            }
        }
        if (matchStr.isEmpty()) return false

        return if (matchStr == textMessage){
            context.tempHandleMessage.addAll(context.tempMessage.subList(0, i))
            context.tempMessage.subList(0, i).clear()
            true
        }else if (textMessage.startsWith(matchStr)){
            val str = textMessage.removePrefix(matchStr)
            val pt = object : PlantText {
                override val text: String = str
                override fun encode(): Any {
                    error("该元素为临时消息元素，无法完成序列化")
                }
            }
            context.tempHandleMessage.add(object : PlantText {
                override val text: String = matchStr
                override fun encode(): Any {
                    error("该元素为临时消息元素，无法完成序列化")
                }
            })
            context.tempMessage.subList(0, i).clear()
            context.tempMessage.insertElementAt(pt, 0)
            true
        }else false
    }
}

fun MessageRouterBuilder.cnNumber() = CnNumberRouter()
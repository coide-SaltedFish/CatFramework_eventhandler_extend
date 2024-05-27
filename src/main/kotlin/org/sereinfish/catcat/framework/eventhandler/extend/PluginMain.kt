package org.sereinfish.catcat.framework.eventhandler.extend

import org.sereinfish.cat.frame.plugin.Plugin
import org.sereinfish.catcat.framework.eventhandler.extend.handler.PluginHandlerManager

object PluginMain: Plugin {
    override fun close() {
    }

    override fun start() {
        logger.info("已引入猫猫事件处理器扩展")
        runCatching {
            Class.forName("org.catcat.sereinfish.qqbot.universal.abstraction.layer.PluginMain")
        }.getOrElse {
            logger.warn("QQ机器人统一抽象接口层未引入，可能会导致在使用相关扩展时出现异常")
        }
        PluginHandlerManager
        logger.info("事件处理管理器加载完成")
    }
}
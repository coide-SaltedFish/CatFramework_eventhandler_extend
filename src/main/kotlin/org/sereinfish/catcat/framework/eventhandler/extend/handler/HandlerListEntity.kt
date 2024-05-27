package org.sereinfish.catcat.framework.eventhandler.extend.handler

import org.sereinfish.cat.frame.event.Event
import org.sereinfish.cat.frame.event.EventHandlerContext
import org.sereinfish.cat.frame.event.handler.EventHandler

interface HandlerListEntity: List<EventHandler<Event, EventHandlerContext<Event>>>

class HandlerListEntityImpl: HandlerListEntity, ArrayList<EventHandler<Event, EventHandlerContext<Event>>>()
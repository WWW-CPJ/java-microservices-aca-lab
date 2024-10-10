/*
 * Copyright 2002-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.api.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.samples.petclinic.api.model.ChatMessage;
import org.springframework.samples.petclinic.api.service.ChatService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for handling chat-related functionality.
 */
@Controller
public class ChatController {

    @Value("${petclinic.agent.name:petclinic}")
    private String agentName;

    private final Logger logger = LoggerFactory.getLogger(ChatController.class);

	private final ChatService chatService;

	public ChatController(ChatService chatService) {
		this.chatService = chatService;
	}

    @GetMapping("/chat.html")
    public String chatPage() {
        return "chat/chat";
    }

	/**
	 * Registers a user for chat.
	 * <p>
	 * @param chatMessage: contains the sender's information.
     * @param headerAccessor: used to access session attributes.
     * @return registered chat message.
	 */
	@MessageMapping("/chat.register")
	@SendTo("/topic/public")
	public ChatMessage register(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
		headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
		return chatMessage;
	}

	/**
	 * Sends a chat message to all connected users.
	 * <p>
	 * @param chatMessage: the chat message to be sent.
     * @return the response chat message.
	 */
	@MessageMapping("/chat.send")
	@SendTo("/topic/public")
	public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
        logger.info("Sending chat message from {}: {}", chatMessage.getSender(), chatMessage.getContent());
        var responseMessage = chatService.chat(chatMessage.getSender(), chatMessage.getContent());
        logger.info("Received chat message response: {}", responseMessage);

		chatMessage.setContent(responseMessage);
		chatMessage.setSender(agentName);
		return chatMessage;
	}

}
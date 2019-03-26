package com.verizon.chatbotservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.verizon.chatbotservice.config.MessagingApplicationConfig;
import com.verizon.chatbotservice.dto.ChatRequest;
import com.verizon.chatbotservice.dto.DialogFlowResponse;
import com.verizon.chatbotservice.service.MessageSender;
import com.verizon.chatbotservice.util.ApplicationConstant;
import com.verizon.chatbotservice.util.DetectIntent;

@RestController
@RequestMapping("/api/service")
public class ChatController {

	private static final Logger log = LoggerFactory.getLogger(ChatController.class);

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private MessagingApplicationConfig applicationConfig;

	@Autowired
	private MessageSender messageSender;

	@PostMapping("/message/plus/chat/{interactionId}")
	public ResponseEntity<?> chatService(@RequestBody ChatRequest chatResquest, @PathVariable("interactionId") final String interactionId) {
		try {
			messageSender.sendMessage(rabbitTemplate, applicationConfig.getAppExchange(), applicationConfig.getAppRoutingKey(), chatResquest);
			return new ResponseEntity<String>(ApplicationConstant.IN_QUEUE, HttpStatus.OK);
		} catch (Exception ex) {
			log.error("Exception occurred while sending message to the queue. Exception= {}", ex);
			return new ResponseEntity<String>(ApplicationConstant.MESSAGE_QUEUE_SEND_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/dialogflow/chat")
	public ResponseEntity<?> dialogFlowChat(@RequestBody ChatRequest chatResquest) {
		try {
			return new ResponseEntity<DialogFlowResponse>(DetectIntent.detectIntent(chatResquest.getQuestion()), HttpStatus.OK);
		} catch (Exception ex) {
			log.error("Exception occurred while sending message to the queue. Exception= {}", ex);
			return new ResponseEntity<String>(ApplicationConstant.MESSAGE_QUEUE_SEND_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

}

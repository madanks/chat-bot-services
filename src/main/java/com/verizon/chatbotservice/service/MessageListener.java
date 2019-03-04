package com.verizon.chatbotservice.service;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import com.verizon.chatbotservice.config.MessagingApplicationConfig;
import com.verizon.chatbotservice.dto.ChatRequest;
import com.verizon.chatbotservice.util.ApplicationConstant;
import com.verizon.chatbotservice.util.DetectIntent;

/**
 * Message Listener for RabbitMQ
 */
@Service
public class MessageListener {

	private static final Logger log = LoggerFactory.getLogger(MessageListener.class);

	@Autowired
	MessagingApplicationConfig applicationConfig;

	/**
	 * Message listener for app1
	 * 
	 * @param UserDetails a user defined object used for deserialization of message
	 */
	@RabbitListener(queues = "${app.queue.name}")
	public void receiveMessageForApp(final ChatRequest data) {

		log.info("Received message: {} from app queue.", data);

		try {

			log.info("Making DialogFlow call to identify intent ");
			// TODO: Code to make REST call
			String sessionId = UUID.randomUUID().toString();
			DetectIntent.detectIntentTexts(ApplicationConstant.DIALOG_FLOW_AGENT, data.getQuestion(), sessionId, ApplicationConstant.DIALOG_FLOW_LANG);
			log.info("<< Exiting receiveMessageForApp() after API call.");

		} catch (HttpClientErrorException ex) {

			if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
				log.info("Delay...");
				try {
					Thread.sleep(ApplicationConstant.MESSAGE_RETRY_DELAY);
				} catch (InterruptedException e) {
				}
				log.info("Throwing exception so that message will be requed in the queue.");
				// Note: Typically Application specific exception should be thrown below
				throw new RuntimeException();
			} else {
				throw new AmqpRejectAndDontRequeueException(ex);
			}
		} catch (Exception e) {

			log.error("Internal server error occurred in API call. Bypassing message requeue {}", e);
			throw new AmqpRejectAndDontRequeueException(e);
		}
	}
}

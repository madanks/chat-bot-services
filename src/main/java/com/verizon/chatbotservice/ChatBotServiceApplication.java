package com.verizon.chatbotservice;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;

import com.verizon.chatbotservice.config.MessagingApplicationConfig;

@EnableRabbit
@SpringBootApplication
public class ChatBotServiceApplication extends SpringBootServletInitializer implements RabbitListenerConfigurer {

	@Autowired
	private MessagingApplicationConfig applicationConfig;

	public MessagingApplicationConfig getApplicationConfig() {
		return applicationConfig;
	}

	public void setApplicationConfig(MessagingApplicationConfig applicationConfig) {
		this.applicationConfig = applicationConfig;
	}

	public static void main(String[] args) {
		SpringApplication.run(ChatBotServiceApplication.class, args);
	}

	@Override
	public void configureRabbitListeners(final RabbitListenerEndpointRegistrar registrar) {
		registrar.setMessageHandlerMethodFactory(messageHandlerMethodFactory());
	}

	@Bean
	public DefaultMessageHandlerMethodFactory messageHandlerMethodFactory() {
		DefaultMessageHandlerMethodFactory factory = new DefaultMessageHandlerMethodFactory();
		factory.setMessageConverter(consumerJackson2MessageConverter());
		return factory;
	}

	@Bean
	public MappingJackson2MessageConverter consumerJackson2MessageConverter() {
		return new MappingJackson2MessageConverter();
	}

	@Bean
	public Jackson2JsonMessageConverter producerJackson2MessageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	/* Bean for rabbitTemplate */
	@Bean
	public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory) {
		final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
		rabbitTemplate.setMessageConverter(producerJackson2MessageConverter());
		return rabbitTemplate;
	}

	/* Binding between Exchange and Queue using routing key */
	@Bean
	public Binding declareBindingApp2() {
		return BindingBuilder.bind(getAppQueue()).to(getAppExchange()).with(getApplicationConfig().getAppRoutingKey());
	}

	/* Creating a bean for the Message queue */
	@Bean
	public Queue getAppQueue() {
		return new Queue(getApplicationConfig().getAppQueue());
	}

	/* Creating a bean for the Message queue Exchange */
	@Bean
	public TopicExchange getAppExchange() {
		return new TopicExchange(getApplicationConfig().getAppExchange());
	}
	
	@Override
	 protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
	  return application.sources(ChatBotServiceApplication.class);
	 }

}

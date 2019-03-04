package com.verizon.chatbotservice.dto;

import java.io.Serializable;

public class ChatRequest implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String question;

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	@Override
	public String toString() {
		return "ChatRequest [question=" + question + "]";
	}
	
	

}

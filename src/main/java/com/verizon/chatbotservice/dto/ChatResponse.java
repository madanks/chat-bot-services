package com.verizon.chatbotservice.dto;

import java.io.Serializable;

public class ChatResponse implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String answer;

	public ChatResponse() {

	}

	public ChatResponse(String answer) {
		this.answer = answer;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

	@Override
	public String toString() {
		return "ChatResponse [answer=" + answer + "]";
	}

}

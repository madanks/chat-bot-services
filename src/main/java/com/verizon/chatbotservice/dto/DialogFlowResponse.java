package com.verizon.chatbotservice.dto;

import java.util.Map;

public class DialogFlowResponse {

	private String queryText;
	private String action;
	private Map<String, Object> parameters;
	private String fulfillmentText;
	private String intent;
	private float intentConfidence;
	private String language;

	public DialogFlowResponse() {

	}

	public DialogFlowResponse(String queryText, String action, Map<String, Object> parameters, String fulfillmentText, String intent, float intentConfidence, String language) {
		super();
		this.queryText = queryText;
		this.action = action;
		this.parameters = parameters;
		this.fulfillmentText = fulfillmentText;
		this.intent = intent;
		this.intentConfidence = intentConfidence;
		this.language = language;
	}

	public String getQueryText() {
		return queryText;
	}

	public void setQueryText(String queryText) {
		this.queryText = queryText;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}

	public String getFulfillmentText() {
		return fulfillmentText;
	}

	public void setFulfillmentText(String fulfillmentText) {
		this.fulfillmentText = fulfillmentText;
	}

	public String getIntent() {
		return intent;
	}

	public void setIntent(String intent) {
		this.intent = intent;
	}

	public float getIntentConfidence() {
		return intentConfidence;
	}

	public void setIntentConfidence(float intentConfidence) {
		this.intentConfidence = intentConfidence;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	@Override
	public String toString() {
		return "DialogFlowResponse [queryText=" + queryText + ", action=" + action + ", parameters=" + parameters + ", fulfillmentText=" + fulfillmentText + ", intent=" + intent + ", intentConfidence=" + intentConfidence + ", language=" + language + "]";
	}

}

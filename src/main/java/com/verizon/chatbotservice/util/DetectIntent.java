package com.verizon.chatbotservice.util;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.dialogflow.v2.AudioEncoding;
import com.google.cloud.dialogflow.v2.DetectIntentRequest;
import com.google.cloud.dialogflow.v2.DetectIntentResponse;
import com.google.cloud.dialogflow.v2.InputAudioConfig;
import com.google.cloud.dialogflow.v2.QueryInput;
import com.google.cloud.dialogflow.v2.QueryResult;
import com.google.cloud.dialogflow.v2.SessionName;
import com.google.cloud.dialogflow.v2.SessionsClient;
import com.google.cloud.dialogflow.v2.SessionsSettings;
import com.google.cloud.dialogflow.v2.TextInput;
import com.google.cloud.dialogflow.v2.TextInput.Builder;
import com.google.protobuf.ByteString;
import com.google.protobuf.Value;
import com.verizon.chatbotservice.dto.DialogFlowResponse;

public class DetectIntent {

	private static final Logger log = LoggerFactory.getLogger(DetectIntent.class);

	/**
	 * Returns the result of detect intent with texts as inputs.
	 *
	 * Using the same `session_id` between requests allows continuation of the conversation.
	 * 
	 * @param projectId    Project/Agent Id.
	 * @param texts        The text intents to be detected based on what a user says.
	 * @param sessionId    Identifier of the DetectIntent session.
	 * @param languageCode Language code of the query.
	 */
	public static void detectIntentTexts(String projectId, String text, String sessionId, String languageCode) throws Exception {
		// Instantiates a client
		try (SessionsClient sessionsClient = SessionsClient.create()) {
			// Set the session name using the sessionId (UUID) and projectID (my-project-id)
			SessionName session = SessionName.of(projectId, sessionId);
			log.info("Session Path: " + session.toString());

			// Set the text (hello) and language code (en-US) for the query
			Builder textInput = TextInput.newBuilder().setText(text).setLanguageCode(languageCode);

			// Build the query with the TextInput
			QueryInput queryInput = QueryInput.newBuilder().setText(textInput).build();

			// Performs the detect intent request
			DetectIntentResponse response = sessionsClient.detectIntent(session, queryInput);

			// Display the query result
			QueryResult queryResult = response.getQueryResult();

			log.info("====================");
			log.info(response.toString());
			System.out.format("Query Text: '%s'\n", queryResult.getQueryText());
			System.out.format("Detected Intent: %s (confidence: %f)\n", queryResult.getIntent().getDisplayName(), queryResult.getIntentDetectionConfidence());
			System.out.format("Fulfillment Text: '%s'\n", queryResult.getFulfillmentText());

		}
	}

	/**
	 * Returns the result of detect intent with an audio file as input.
	 *
	 * Using the same `session_id` between requests allows continuation of the conversation.
	 * 
	 * @param projectId     Project/Agent Id.
	 * @param audioFilePath Path to the audio file.
	 * @param sessionId     Identifier of the DetectIntent session.
	 * @param languageCode  Language code of the query.
	 */
	public static void detectIntentAudio(String projectId, String audioFilePath, String sessionId, String languageCode) throws Exception {
		// Instantiates a client
		try (SessionsClient sessionsClient = SessionsClient.create()) {
			// Set the session name using the sessionId (UUID) and projectID (my-project-id)
			SessionName session = SessionName.of(projectId, sessionId);
			log.info("Session Path: " + session.toString());

			// Note: hard coding audioEncoding and sampleRateHertz for simplicity.
			// Audio encoding of the audio content sent in the query request.
			AudioEncoding audioEncoding = AudioEncoding.AUDIO_ENCODING_LINEAR_16;
			int sampleRateHertz = 16000;

			// Instructs the speech recognizer how to process the audio content.
			InputAudioConfig inputAudioConfig = InputAudioConfig.newBuilder().setAudioEncoding(audioEncoding) // audioEncoding = AudioEncoding.AUDIO_ENCODING_LINEAR_16
					.setLanguageCode(languageCode) // languageCode = "en-US"
					.setSampleRateHertz(sampleRateHertz) // sampleRateHertz = 16000
					.build();

			// Build the query with the InputAudioConfig
			QueryInput queryInput = QueryInput.newBuilder().setAudioConfig(inputAudioConfig).build();

			// Read the bytes from the audio file
			byte[] inputAudio = Files.readAllBytes(Paths.get(audioFilePath));

			// Build the DetectIntentRequest
			DetectIntentRequest request = DetectIntentRequest.newBuilder().setSession(session.toString()).setQueryInput(queryInput).setInputAudio(ByteString.copyFrom(inputAudio)).build();

			// Performs the detect intent request
			DetectIntentResponse response = sessionsClient.detectIntent(request);

			// Display the query result
			QueryResult queryResult = response.getQueryResult();
			log.info("====================");
			System.out.format("Query Text: '%s'\n", queryResult.getQueryText());
			System.out.format("Detected Intent: %s (confidence: %f)\n", queryResult.getIntent().getDisplayName(), queryResult.getIntentDetectionConfidence());
			System.out.format("Fulfillment Text: '%s'\n", queryResult.getFulfillmentText());
		}
	}

	public static DialogFlowResponse detectIntent(String text) throws Exception {
		GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream("/usr/local/dialogflow/testagent-7edbe-2b6ea9ccd971.json"));
		SessionsSettings.Builder sessionBuilder = SessionsSettings.newBuilder();
		SessionsSettings sessionSetting = sessionBuilder.setCredentialsProvider(FixedCredentialsProvider.create(credentials)).build();
		try (SessionsClient sessionsClient = SessionsClient.create(sessionSetting)) {
			SessionName session = SessionName.of(ApplicationConstant.DIALOG_FLOW_AGENT, UUID.randomUUID().toString());
			log.info("Session Path: " + session.toString());
			Builder textInput = TextInput.newBuilder().setText(text).setLanguageCode("en-us");
			QueryInput queryInput = QueryInput.newBuilder().setText(textInput).build();
			DetectIntentResponse response = sessionsClient.detectIntent(session, queryInput);
			QueryResult queryResult = response.getQueryResult();
			log.info("====================>> " + queryResult);
			Map<String, Object> parameters = new HashMap<String, Object>();
			Map<String, Value> parMap = queryResult.getParameters().getFieldsMap();
			for (String key : parMap.keySet()) {
				String val = parMap.get(key).getStringValue();
				if (val != null && !val.isEmpty()) {
					parameters.put(key, val);
				} else {
					parameters.put(key, parMap.get(key).getNumberValue());
				}
			}

			return new DialogFlowResponse(queryResult.getQueryText(), queryResult.getAction(), parameters, queryResult.getFulfillmentText(), queryResult.getIntent().getDisplayName(), queryResult.getIntentDetectionConfidence(), queryResult.getLanguageCode());
		}
	}

}

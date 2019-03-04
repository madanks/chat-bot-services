package com.verizon.chatbotservice.util;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.cloud.dialogflow.v2.AudioEncoding;
import com.google.cloud.dialogflow.v2.DetectIntentRequest;
import com.google.cloud.dialogflow.v2.DetectIntentResponse;
import com.google.cloud.dialogflow.v2.InputAudioConfig;
import com.google.cloud.dialogflow.v2.QueryInput;
import com.google.cloud.dialogflow.v2.QueryResult;
import com.google.cloud.dialogflow.v2.SessionName;
import com.google.cloud.dialogflow.v2.SessionsClient;
import com.google.cloud.dialogflow.v2.TextInput;
import com.google.cloud.dialogflow.v2.TextInput.Builder;
import com.google.protobuf.ByteString;
import com.verizon.chatbotservice.service.MessageListener;

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

}

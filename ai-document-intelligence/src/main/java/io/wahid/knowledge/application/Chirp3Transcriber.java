package io.wahid.knowledge.application;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.speech.v2.AutoDetectDecodingConfig;
import com.google.cloud.speech.v2.RecognitionConfig;
import com.google.cloud.speech.v2.RecognizeRequest;
import com.google.cloud.speech.v2.RecognizeResponse;
import com.google.cloud.speech.v2.SpeechClient;
import com.google.cloud.speech.v2.SpeechRecognitionResult;
import com.google.cloud.speech.v2.SpeechSettings;
import com.google.protobuf.ByteString;

import java.io.IOException;
import java.io.InputStream;

public class Chirp3Transcriber {

    private static final String PROJECT_ID = "alert-cursor-476219-s1";
    private static final String REGION = "us";

    GoogleCredentials credentials;
    SpeechSettings settings;

    public Chirp3Transcriber() throws IOException {
        this.credentials = GoogleCredentials.getApplicationDefault()
                .createScoped("https://www.googleapis.com/auth/cloud-platform");
        this.settings = SpeechSettings.newBuilder()
                .setEndpoint(REGION + "-speech.googleapis.com:443")
                .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                .build();
        System.out.println(credentials.getClass().getName());
        System.out.println(System.getenv("GOOGLE_APPLICATION_CREDENTIALS_V2"));
    }

    public String transcribe(InputStream in) throws IOException {

        /*credentials.refreshIfExpired();

        System.out.println(
                credentials.refreshAccessToken().getTokenValue()
        );*/
        StringBuilder transcript = new StringBuilder();
        try (SpeechClient speechClient = SpeechClient.create(settings)) {
            byte[] audioBytes = in.readAllBytes();

            RecognitionConfig config = RecognitionConfig.newBuilder()
                    .setAutoDecodingConfig(AutoDetectDecodingConfig.newBuilder().build())
                    .addLanguageCodes("bn-BD")
                    .setModel("chirp_3")
                    .build();

            RecognizeRequest request = RecognizeRequest.newBuilder()
                    .setRecognizer(String.format("projects/%s/locations/%s/recognizers/_", PROJECT_ID, REGION))
                    .setConfig(config)
                    .setContent(ByteString.copyFrom(audioBytes))
                    .build();

            RecognizeResponse response = speechClient.recognize(request);

            for (SpeechRecognitionResult result : response.getResultsList()) {
                if (!result.getAlternativesList().isEmpty()) {
                    System.out.println(result.getAlternatives(0).getTranscript());
                    transcript.append(result.getAlternatives(0).getTranscript());
                }
            }
        }
        return transcript.toString();
    }
}
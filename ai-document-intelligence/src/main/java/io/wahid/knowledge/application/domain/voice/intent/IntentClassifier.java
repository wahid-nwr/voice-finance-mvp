package io.wahid.knowledge.application.domain.voice.intent;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.wahid.knowledge.api.AudioServlet;
import io.wahid.knowledge.infrastructure.config.AppConfig;
import io.wahid.knowledge.infrastructure.llms.LLMClient;
import io.wahid.knowledge.infrastructure.llms.ollama.impl.OllamaLLMClient;
import org.apache.commons.lang3.StringUtils;

import java.util.logging.Level;
import java.util.logging.Logger;

public class IntentClassifier {
    private static final Logger LOGGER = Logger.getLogger(IntentClassifier.class.getName());
    private static final ObjectMapper MAPPER = new ObjectMapper();
    LLMClient llmClient;

    public IntentClassifier() {
        this.llmClient = new OllamaLLMClient(AppConfig.ollamaBaseUrl(), AppConfig.llmModel());
    }

    public IntentResponse classify(String objective) throws Exception {
        LOGGER.log(Level.INFO, "Classifier objective:{0}", objective);
        String prompt =  """
                You are an intent classifier.
                
                Valid intents:
             
                CREATE_CUSTOMER
                SEARCH_CUSTOMER
                CREATE_LOAN
                RECORD_PAYMENT
                OUT_OF_SCOPE
                
                Schema:
                {
                  "intent": "CREATE_CUSTOMER | SEARCH_CUSTOMER | CREATE_LOAN | RECORD_PAYMENT | OUT_OF_SCOPE",
                  "arguments": MAP of details"
                }
                                    
                Examples:
                
                "নতুন কাস্টমার যোগ করুন"
                → CREATE_CUSTOMER
                
                "নতুন গ্রাহক তৈরি করুন"
                → CREATE_CUSTOMER
                
                "রহিমকে খুঁজে বের করুন"
                → SEARCH_CUSTOMER
                
                "নতুন ঋণ আবেদন তৈরি করুন"
                → CREATE_LOAN

                Return JSON only.
                User command:
                "%s"
        """.formatted(objective);

        String json = llmClient.generate(prompt);
        if (StringUtils.isEmpty(json)) return null;
        json = extractJson(json);
        LOGGER.log(Level.INFO, "Classifier intent:{0}", json);
        return MAPPER.readValue(json, IntentResponse.class);
    }

    private static String extractJson(String text) {
        return text
                .replaceFirst("(?i)^\\s*```json\\s*", "")
                .replaceFirst("^\\s*```\\s*", "")
                .replaceFirst("\\s*```\\s*$", "")
                .trim();
    }
}

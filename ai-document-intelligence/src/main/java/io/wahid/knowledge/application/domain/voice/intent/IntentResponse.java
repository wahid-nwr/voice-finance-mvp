package io.wahid.knowledge.application.domain.voice.intent;

import java.util.Collections;
import java.util.Map;

public class IntentResponse {
    private String intent;
    private Map<String, Object> arguments = Collections.emptyMap();

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public Map<String, Object> getArguments() {
        return arguments;
    }

    public void setArguments(Map<String, Object> arguments) {
        this.arguments = arguments;
    }
}
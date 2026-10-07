package io.wahid.knowledge.application.domain.weather.insights;

import io.wahid.knowledge.application.core.insight.InsightGenerator;
import io.wahid.knowledge.application.core.insight.InsightResult;
import io.wahid.knowledge.application.core.retrieval.vector.VectorSearcher;
import io.wahid.knowledge.domain.query.Query;

import java.util.List;

public interface AnswerGenerator extends InsightGenerator<Query, InsightResult> {

    String generateAnswer(
            String question,
            List<VectorSearcher.SearchResult> context
    ) throws Exception;
}

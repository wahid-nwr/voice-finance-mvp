package io.wahid.knowledge.infrastructure.health;

public record HealthStatus(
        boolean ollamaUp,
        boolean qdrantUp
) {
    public boolean isHealthy() {
        return ollamaUp && qdrantUp;
    }
}

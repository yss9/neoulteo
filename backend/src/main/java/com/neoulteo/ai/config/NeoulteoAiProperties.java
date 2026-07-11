package com.neoulteo.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "neoulteo.ai")
public class NeoulteoAiProperties {
    private SpringAi springAi = new SpringAi();
    private Rag rag = new Rag();
    private Weather weather = new Weather();
    private TravelSearch travelSearch = new TravelSearch();

    public SpringAi getSpringAi() {
        return springAi;
    }

    public void setSpringAi(SpringAi springAi) {
        this.springAi = springAi;
    }

    public Rag getRag() {
        return rag;
    }

    public void setRag(Rag rag) {
        this.rag = rag;
    }

    public Weather getWeather() {
        return weather;
    }

    public void setWeather(Weather weather) {
        this.weather = weather;
    }

    public TravelSearch getTravelSearch() {
        return travelSearch;
    }

    public void setTravelSearch(TravelSearch travelSearch) {
        this.travelSearch = travelSearch;
    }

    public static class SpringAi {
        private boolean enabled;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class Rag {
        private int topK = 5;
        private int maxDocuments = 0;
        private int lexicalCandidateLimit = 80;

        public int getTopK() {
            return topK;
        }

        public void setTopK(int topK) {
            this.topK = topK;
        }

        public int getMaxDocuments() {
            return maxDocuments;
        }

        public void setMaxDocuments(int maxDocuments) {
            this.maxDocuments = maxDocuments;
        }

        public int getLexicalCandidateLimit() {
            return lexicalCandidateLimit;
        }

        public void setLexicalCandidateLimit(int lexicalCandidateLimit) {
            this.lexicalCandidateLimit = lexicalCandidateLimit;
        }
    }

    public static class Weather {
        private String apiKey;

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
    }

    public static class TravelSearch {
        private String apiKey;

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
    }
}

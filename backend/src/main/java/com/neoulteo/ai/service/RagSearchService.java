package com.neoulteo.ai.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.neoulteo.ai.config.NeoulteoAiProperties;
import com.neoulteo.ai.resource.RagAttractionDocument;
import com.neoulteo.ai.resource.RagSourceResource;
import com.neoulteo.domain.attraction.mapper.AttractionMapper;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

@Service
public class RagSearchService {
    private static final List<Set<String>> RELATED_TERM_GROUPS = List.of(
            Set.of("물놀이", "수영", "해수욕", "해수욕장", "워터파크", "계곡", "바다", "해변", "해안"),
            Set.of("바다", "해변", "해수욕장", "해안", "오션", "항구", "섬"),
            Set.of("산", "등산", "트레킹", "숲", "자연", "계곡", "둘레길", "산책"),
            Set.of("아이", "어린이", "가족", "키즈", "체험", "놀이터", "교육"),
            Set.of("실내", "박물관", "미술관", "전시", "문화", "공연", "체험관"),
            Set.of("맛집", "음식", "식당", "카페", "먹거리", "시장", "디저트"),
            Set.of("야경", "밤", "전망", "전망대", "일몰", "노을", "뷰"),
            Set.of("역사", "유적", "유물", "궁", "고궁", "문화재", "사찰", "절"),
            Set.of("쇼핑", "시장", "거리", "상점", "몰", "상가"),
            Set.of("힐링", "휴식", "조용", "한적", "공원", "정원", "산책"),
            Set.of("액티비티", "레포츠", "스포츠", "자전거", "캠핑", "낚시", "체험"));

    private final AttractionMapper attractionMapper;
    private final ObjectProvider<EmbeddingModel> embeddingModelProvider;
    private final NeoulteoAiProperties properties;
    private final List<IndexedRagDocument> index = new ArrayList<>();
    private final Map<Integer, float[]> documentEmbeddingCache = new LinkedHashMap<>();
    private boolean indexed;

    public RagSearchService(AttractionMapper attractionMapper,
            ObjectProvider<EmbeddingModel> embeddingModelProvider,
            NeoulteoAiProperties properties) {
        this.attractionMapper = attractionMapper;
        this.embeddingModelProvider = embeddingModelProvider;
        this.properties = properties;
    }

    public synchronized List<RagSourceResource> search(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        ensureIndexed();
        if (index.isEmpty()) {
            return List.of();
        }

        List<ScoredRagDocument> lexicalCandidates = lexicalCandidates(query, lexicalCandidateLimit());
        EmbeddingModel embeddingModel = embeddingModelProvider.getIfAvailable();
        if (properties.getSpringAi().isEnabled() && embeddingModel != null) {
            try {
                if (lexicalCandidates.isEmpty()) {
                    return List.of();
                }
                float[] queryEmbedding = embeddingModel.embed(query);
                return lexicalCandidates.stream()
                        .map(candidate -> toSource(candidate.document(),
                                combinedScore(candidate.score(),
                                        embeddingScore(embeddingModel, queryEmbedding, candidate.document()))))
                        .filter(source -> source.score() > 0)
                        .sorted(Comparator.comparingDouble(RagSourceResource::score).reversed())
                        .limit(topK())
                        .toList();
            } catch (RuntimeException ignored) {
                return lexicalSearch(query);
            }
        }

        return lexicalCandidates.stream()
                .map(candidate -> toSource(candidate.document(), candidate.score()))
                .limit(topK())
                .toList();
    }

    private void ensureIndexed() {
        if (indexed) {
            return;
        }

        indexed = true;
        int maxDocuments = properties.getRag().getMaxDocuments();
        List<RagAttractionDocument> documents =
                attractionMapper.findRagAttractionDocuments(maxDocuments > 0 ? maxDocuments : null);
        for (RagAttractionDocument document : documents) {
            String content = document.toSearchText();
            if (content.isBlank()) {
                continue;
            }
            index.add(new IndexedRagDocument(document, content));
        }
    }

    private List<RagSourceResource> lexicalSearch(String query) {
        return lexicalCandidates(query, topK()).stream()
                .map(candidate -> toSource(candidate.document(), candidate.score()))
                .toList();
    }

    private List<ScoredRagDocument> lexicalCandidates(String query, int limit) {
        String normalizedQuery = normalize(query);
        Set<String> expandedTerms = expandedTerms(normalizedQuery);

        return index.stream()
                .map(item -> new ScoredRagDocument(item, lexicalScore(normalizedQuery, expandedTerms, item.content)))
                .filter(item -> item.score() > 0)
                .sorted(Comparator.comparingDouble(ScoredRagDocument::score).reversed())
                .limit(limit)
                .toList();
    }

    private double lexicalScore(String normalizedQuery, Set<String> expandedTerms, String content) {
        String normalizedContent = normalize(content);
        double score = normalizedContent.contains(normalizedQuery) ? 2.0 : 0.0;
        for (String term : expandedTerms) {
            if (!term.isBlank() && normalizedContent.contains(term)) {
                score += normalizedQuery.contains(term) ? 0.6 : 0.35;
            }
        }
        return score;
    }

    private double embeddingScore(EmbeddingModel embeddingModel, float[] queryEmbedding, IndexedRagDocument item) {
        try {
            float[] documentEmbedding = documentEmbeddingCache.computeIfAbsent(
                    item.document().getContentId(),
                    ignored -> embeddingModel.embed(item.content()));
            return cosine(queryEmbedding, documentEmbedding);
        } catch (RuntimeException ignored) {
            return 0.0;
        }
    }

    private double combinedScore(double lexicalScore, double embeddingScore) {
        return lexicalScore + Math.max(embeddingScore, 0.0) * 2.0;
    }

    private Set<String> expandedTerms(String normalizedQuery) {
        Set<String> terms = new HashSet<>(Arrays.asList(normalizedQuery.split("\\s+")));
        for (Set<String> group : RELATED_TERM_GROUPS) {
            if (group.stream().anyMatch(normalizedQuery::contains)) {
                terms.addAll(group);
            }
        }
        return terms;
    }

    private int topK() {
        return Math.max(properties.getRag().getTopK(), 1);
    }

    private int lexicalCandidateLimit() {
        return Math.max(properties.getRag().getLexicalCandidateLimit(), topK());
    }

    private RagSourceResource toSource(IndexedRagDocument item, double score) {
        RagAttractionDocument document = item.document;
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("contentId", document.getContentId());
        metadata.put("areaCode", document.getAreaCode());
        metadata.put("sigunguCode", document.getSigunguCode());
        metadata.put("contentTypeName", document.getContentTypeName());
        return new RagSourceResource(document.getTitle(), "ATTRACTION", item.content, score, metadata);
    }

    private double cosine(float[] left, float[] right) {
        if (left == null || right == null || left.length == 0 || left.length != right.length) {
            return 0.0;
        }

        double dot = 0.0;
        double leftNorm = 0.0;
        double rightNorm = 0.0;
        for (int i = 0; i < left.length; i++) {
            dot += left[i] * right[i];
            leftNorm += left[i] * left[i];
            rightNorm += right[i] * right[i];
        }
        if (leftNorm == 0.0 || rightNorm == 0.0) {
            return 0.0;
        }
        return dot / (Math.sqrt(leftNorm) * Math.sqrt(rightNorm));
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").trim();
    }

    private record IndexedRagDocument(RagAttractionDocument document, String content) {
    }

    private record ScoredRagDocument(IndexedRagDocument document, double score) {
    }
}

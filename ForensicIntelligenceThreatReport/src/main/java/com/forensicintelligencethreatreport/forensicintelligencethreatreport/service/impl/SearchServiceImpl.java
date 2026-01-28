package com.forensicintelligencethreatreport.forensicintelligencethreatreport.service.impl;

import ai.djl.translate.TranslateException;
import co.elastic.clients.elasticsearch._types.KnnQuery;
import co.elastic.clients.elasticsearch._types.KnnSearch;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.dto.AdvancedSearchRequestDTO;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.dto.BasicSearchRequestDTO;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.dto.SearchResultDTO;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.exceptionhandling.exception.MalformedQueryException;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.indexmodel.DummyIndex;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.service.interfaces.SearchService;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.util.VectorizationUtil;
//import joptsimple.internal.Strings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.common.unit.Fuzziness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchServiceImpl implements SearchService {

    private final ElasticsearchOperations elasticsearchTemplate;

    @Override
    public Page<SearchResultDTO> basicSearch(BasicSearchRequestDTO request, Pageable pageable) {
        log.info("Executing basic search");

        Query query = buildBasicQuery(request);
        return executeQuery(query, pageable);
    }

    @Override
    public Page<SearchResultDTO> advancedSearch(AdvancedSearchRequestDTO request, Pageable pageable) {
        log.info("Executing advanced search with {} expressions", request.expressions().size());

        Query query = buildBooleanQuery(request);
        return executeQuery(query, pageable);
    }

    @Override
    public Page<SearchResultDTO> knnSearch(String query, Pageable pageable) {
        log.info("Executing KNN search for: {}", query);

        try {
            float[] embedding = VectorizationUtil.getEmbedding(query);
            Query knnQuery = buildKnnQuery(embedding);
            return executeQuery(knnQuery, pageable);
        } catch (TranslateException e) {
            log.error("Vectorization failed: {}", e.getMessage());
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    @Override
    public Page<SearchResultDTO> phraseSearch(String phrase, Pageable pageable) {
        log.info("Executing phrase search for: {}", phrase);

        Query query = BoolQuery.of(q -> q
                .should( sb -> sb.matchPhrase(m -> m.field("content_sr").query(phrase)))
                .should( sb -> sb.matchPhrase(m -> m.field("content_en").query(phrase)))
                .should( sb -> sb.matchPhrase(m -> m.field("title").query(phrase)))
        )._toQuery();
        return executeQuery(query, pageable);
    }

    @Override
    public Page<SearchResultDTO> fullTextSearch(String text, Pageable pageable) {
        log.info("Executing full-text search for: {}", text);

        Query query = BoolQuery.of(q -> q
                .should( sb -> sb.match(m -> m.field("content_sr").query(text).boost(2.0f)))
                .should( sb -> sb.match(m -> m.field("content_en").query(text)))
                .should(sb -> sb.match(m -> m.field("title").query(text).boost(1.5f)))
                .should(sb -> sb.matchPhrase(m -> m.field("forensician_name").query(text))) // Dodato
                .should(sb -> sb.matchPhrase(m -> m.field("organization").query(text)))     // Dodato
                .should(sb -> sb.matchPhrase(m -> m.field("malware_name").query(text)))      // Dodato
        )._toQuery();
        return executeQuery(query, pageable);
    }

    private Query buildBasicQuery(BasicSearchRequestDTO request) {
        return BoolQuery.of(q -> {
//                .must(mq -> {
//                    if (request.forensicianName() != null && !request.forensicianName().isEmpty()) {
//                        mq.match(m -> m.field("title")
//                                .fuzziness(Fuzziness.ONE.asString())
//                                .query(request.forensicianName()));
//                    }
//                    return mq;
//                })
//                .should(
//                        sb -> request.hashValue() != null && !request.hashValue().isEmpty()
//                                ? sb.term(t -> t.field("hash_md5").value(request.hashValue()))
//                                : sb.matchAll(m -> m))
//
//                .should(        sb -> request.hashValue() != null && !request.hashValue().isEmpty()
//                                ? sb.term(t -> t.field("hash_sha256").value(request.hashValue()))
//                                : sb.matchAll(m -> m))
//
//                .should(        sb -> request.threatLevel() != null && !request.threatLevel().isEmpty()
//                                ? sb.term(t -> t.field("threat_level").value(request.threatLevel()))
//                                : sb.matchAll(m -> m))
//
//                .should(        sb -> request.organization() != null && !request.organization().isEmpty()
//                                ? sb.match(m -> m.field("organization")
//                                .fuzziness(Fuzziness.ONE.asString())
//                                .query(request.organization()))
//                                : sb.matchAll(m -> m))
//
//                .should(        sb -> request.malwareName() != null && !request.malwareName().isEmpty()
//                                ? sb.match(m -> m.field("malware_name")
//                                .fuzziness(Fuzziness.ONE.asString())
//                                .query(request.malwareName()))
//                                : sb.matchAll(m -> m))
//
//        )._toQuery();

            if (request.searchText() != null && !request.searchText().isEmpty()) {
                q.should(s -> s.multiMatch(m -> m
                        .fields("title", "content_sr", "content_en", "malware_name")
                        .query(request.searchText())
                        .fuzziness("AUTO")
                ));
            }

        if (request.forensicianName() != null && !request.forensicianName().isEmpty()) {
            q.should(s -> s.match(m -> m.field("forensician_name").query(request.forensicianName()).fuzziness("1")));
        }

        if (request.organization() != null && !request.organization().isEmpty()) {
            q.should(s -> s.match(m -> m.field("organization").query(request.organization()).fuzziness("1")));
        }

        if (request.malwareName() != null && !request.malwareName().isEmpty()) {
            q.should(s -> s.match(m -> m.field("malware_name").query(request.malwareName()).fuzziness("1")));
        }

        if (request.hashValue() != null && !request.hashValue().isEmpty()) {
            // Hash vrednosti su obično MD5 ili SHA256, tražimo ih u oba polja
            q.should(s -> s.term(t -> t.field("hash_md5").value(request.hashValue())));
            q.should(s -> s.term(t -> t.field("hash_sha256").value(request.hashValue())));
        }

        if (request.threatLevel() != null && !request.threatLevel().isEmpty()) {
            q.should(s -> s.term(t -> t.field("threat_level").value(request.threatLevel().toLowerCase())));
        }

        // KLJUČNA STAVKA: Ako je bar nešto uneto, mora da se poklopi bar jedan uslov.
        // Bez ovoga, ako su sva polja prazna, ES bi mogao vratiti ništa ili sve zavisno od konteksta.
        q.minimumShouldMatch("1");
        return q;
    })._toQuery();
    }

    private Query buildBooleanQuery(AdvancedSearchRequestDTO request) {
        List<String> expressions = request.expressions();
        List<String> operators = request.operators();

        if (expressions.isEmpty()) {
            throw new MalformedQueryException("No search expressions provided");
        }
        Query currentQuery = buildExpressionQuery(expressions.get(0));

        for (int i = 0; i < operators.size() && i + 1 < expressions.size(); i++) {
            String operator = operators.get(i).toUpperCase();
            Query nextQuery = buildExpressionQuery(expressions.get(i + 1));

            final Query finalCurrent = currentQuery;
            final Query finalNext = nextQuery;

            if ("AND".equals(operator)) {
                currentQuery = BoolQuery.of(q -> q.must(finalCurrent)
                        .must(finalNext)
                )._toQuery();
            } else if ("OR".equals(operator)) {
                currentQuery = BoolQuery.of(q -> q
                        .should(finalCurrent)
                        .should(finalNext)
                )._toQuery();
            } else if ("NOT".equals(operator)) {
                currentQuery = BoolQuery.of(q -> q
                        .must(finalCurrent)
                        .mustNot(finalNext)
                )._toQuery();
            }
        }
        return currentQuery;
    }

    private Query buildExpressionQuery(String expression) {
        if (!expression.contains(":")) {
            throw new MalformedQueryException("Expression must be in format 'field:value'");
        }
        String[] parts = expression.split(":", 2);
        String field = parts[0].trim();
        String value = parts[1].trim();
        return BoolQuery.of(q -> q.must(m -> m.match(
                ma -> ma.field(mapFieldName(field))
                        .fuzziness(Fuzziness.ONE.asString())
                        .query(value)
        )))._toQuery();
    }

    private Query buildKnnQuery(float[] embedding) {
        List<Float> floatList = new ArrayList<>();
        for (float v : embedding) {
            floatList.add(v);
        }
        return co.elastic.clients.elasticsearch._types.query_dsl.Query.of(q -> q
                .knn(k -> k
                        .field("vectorizedContent")
                        .queryVector(floatList)
                        .k(10)
                        .numCandidates(100)
                )
        );
    }

    private Page<SearchResultDTO> executeQuery(Query query, Pageable pageable) {
        NativeQuery nativeQuery = new NativeQueryBuilder()
                .withQuery(query)
                .withPageable(pageable)
                .build();
        SearchHits<DummyIndex> searchHits = elasticsearchTemplate.search(
                nativeQuery,
                DummyIndex.class,
                IndexCoordinates.of("dummy_index")
        );
        List<SearchResultDTO> results = searchHits.stream()
                .map(this::convertToSearchResult)
                .collect(Collectors.toList());
        return new PageImpl<>(results, pageable, searchHits.getTotalHits());
    }

    private SearchResultDTO convertToSearchResult(SearchHit<DummyIndex> hit) {
        DummyIndex index = hit.getContent();
        float score =Float.isNaN(hit.getScore())? 0.0f:hit.getScore();
        return new SearchResultDTO(
                index.getId(),
                index.getTitle(),
                index.getForensicianName(), // forensicianName
                index.getOrganization(), // organizatio
                index.getMalwareName(), // malware name
                index.getThreatLevel(), // hashMD5
                index.getHashMd5(), // hashSHA256
                index.getHashSha256(),
                truncateContent(index.getContentSr() != null ? index.getContentSr() : index.getContentEn()),
                getHighlightedContent(hit),
                (double) score
        );
    }

    private String truncateContent(String content) {        if (content == null) return "";
        if (content.length() > 200) {
            return content.substring(0, 200) + "...";
        }
        return content;
    }

    private Map<String, String> getHighlightedContent(SearchHit<DummyIndex> hit) {
        Map<String, String> highlighted = new HashMap<>();
        if (hit.getHighlightFields() != null) {
            hit.getHighlightFields().forEach((field, values) -> {
                if (!values.isEmpty()) {
                    highlighted.put(field, String.join(" ... ", values));
                }
            });
        }
        return highlighted;
    }

    private String mapFieldName(String userInput) {
        if (userInput == null) return "content_sr";
        return switch (userInput.toLowerCase().trim()) {
            case "title", "naslov", "naslovu","наслов","наслову" -> "title";
            case "content_sr", "sadrzaj","sadržaj", "садржај"-> "content_sr";
            case "malware","вирус","претња","virus","malver","малвер" -> "malware_name";
            case "content_en","english","engleski","енглески"->"content_en";
            case "forensician","forenzicar","forenzičar","форензичар"->"forensician_name";
            case "ornanization","organizacija","органитација" -> "organization";
            case "level","nivo","ниво" -> "threat_level";
            default -> userInput;
        };
    }

//////////////////////////////////////////////////////////////////////////////////////////////////////////////

//    @Override
//    public Page<DummyIndex> simpleSearch(List<String> keywords, Pageable pageable, boolean isKNN) {
//
//        if (isKNN) {
//            try {
//                return searchByVector(VectorizationUtil.getEmbedding(Strings.join(keywords, " ")));
//            } catch (TranslateException e) {
//                log.error("Vectorization failed");
//                return Page.empty();
//            }
//        }
//        var searchQueryBuilder =
//                new NativeQueryBuilder().withQuery(buildSimpleSearchQuery(keywords))
//                        .withPageable(pageable);
//
//        return runQuery(searchQueryBuilder.build());
//    }

//    public Page<DummyIndex> searchByVector(float[] queryVector) {
//        List<Float> floatList = new ArrayList<>();
//        for (float v : queryVector) {
//            floatList.add(v);
//        }
//
//        var knnQuery = new KnnQuery.Builder()
//                .field("vectorizedContent")
//                .queryVector(floatList)
//                .numCandidates(100)
//                .k(10)
//                .boost(10.0f)
//                .build();
//
//        // ISPRAVKA 1: Koristimo listu jer neki builderi to traže,
//        // i proveravamo naziv metode (verovatno je withKnnQuery ali je builder zbunjen)
//        NativeQuery searchQuery = new NativeQueryBuilder()
//                .withKnnSearches((List<KnnSearch>) knnQuery)
//                .withMaxResults(5)
//                .withPageable(Pageable.ofSize(5))
//                .build();
//
//        // ISPRAVKA 2: Eksplicitno kastovanje da izbegnemo "Ambiguous call"
//        var searchHits = elasticsearchTemplate.search(
//                (org.springframework.data.elasticsearch.core.query.Query) searchQuery,
//                DummyIndex.class,
//                IndexCoordinates.of("dummy_index")
//        );
//
//        // ISPRAVKA 3: getPageable() izvlačimo sigurnijim putem
//        var searchHitsPaged = SearchHitSupport.searchPageFor(searchHits, searchQuery.getPageable());
//
//        return (Page<DummyIndex>) SearchHitSupport.unwrapSearchHits(searchHitsPaged);
//    }
//
//    private Page<DummyIndex> runQuery(NativeQuery searchQuery) {
//        // Eksplicitno kastovanje ovde rešava "Ambiguous method call"
//        var searchHits = elasticsearchTemplate.search(
//                (org.springframework.data.elasticsearch.core.query.Query) searchQuery,
//                DummyIndex.class,
//                IndexCoordinates.of("dummy_index")
//        );
//
//        var searchHitsPaged = SearchHitSupport.searchPageFor(searchHits, searchQuery.getPageable());
//
//        return (Page<DummyIndex>) SearchHitSupport.unwrapSearchHits(searchHitsPaged);
//    }


//    @Override
//    public Page<DummyIndex> advancedSearch(List<String> expression, Pageable pageable) {
//        if (expression.size() != 3) {
//            throw new MalformedQueryException("Search query malformed.");
//        }
//
//        String operation = expression.get(1);
//        expression.remove(1);
//        var searchQueryBuilder =
//                new NativeQueryBuilder().withQuery(buildAdvancedSearchQuery(expression, operation))
//                        .withPageable(pageable);
//
//        return runQuery(searchQueryBuilder.build());
//    }

//    private Query buildSimpleSearchQuery(List<String> tokens) {
//        return BoolQuery.of(q -> q.must(mb -> mb.bool(b -> {
//            tokens.forEach(token -> {
//                // Term Query - simplest
//                // Matches documents with exact term in "title" field
//                b.should(sb -> sb.term(m -> m.field("title").value(token)));
//
//                // Terms Query
//                // Matches documents with any of the specified terms in "title" field
////                var terms = new ArrayList<>(List.of("dummy1", "dummy2"));
////                var titleTerms = new TermsQueryField.Builder()
////                    .value(terms.stream().map(FieldValue::of).toList())
////                    .build();
////                b.should(sb -> sb.terms(m -> m.field("title").terms(titleTerms)));
//
//                // Match Query - full-text search with fuzziness
//                // Matches documents with fuzzy matching in "title" field
//                b.should(sb -> sb.match(
//                        m -> m.field("title").fuzziness(Fuzziness.ONE.asString()).query(token)));
//
//                // Match Query - full-text search in other fields
//                // Matches documents with full-text search in other fields
//                b.should(sb -> sb.match(m -> m.field("content_sr").query(token).boost(0.5f)));
//                b.should(sb -> sb.match(m -> m.field("content_en").query(token)));
//
//                // Wildcard Query - unsafe
//                // Matches documents with wildcard matching in "title" field
//                b.should(sb -> sb.wildcard(m -> m.field("title").value("*" + token + "*")));
//
//                // Regexp Query - unsafe
//                // Matches documents with regular expression matching in "title" field
////                b.should(sb -> sb.regexp(m -> m.field("title").value(".*" + token + ".*")));
//
//                // Boosting Query - positive gives better score, negative lowers score
//                // Matches documents with boosted relevance in "title" field
//                b.should(sb -> sb.boosting(bq -> bq.positive(m -> m.match(ma -> ma.field("title").query(token)))
//                        .negative(m -> m.match(ma -> ma.field("description").query(token)))
//                        .negativeBoost(0.5f)));
//
//                // Match Phrase Query - useful for exact-phrase search
//                // Matches documents with exact phrase match in "title" field
//                b.should(sb -> sb.matchPhrase(m -> m.field("title").query(token)));
//
//                // Fuzzy Query - similar to Match Query with fuzziness, useful for spelling errors
//                // Matches documents with fuzzy matching in "title" field
//                b.should(sb -> sb.match(
//                        m -> m.field("title").fuzziness(Fuzziness.ONE.asString()).query(token)));
//
//                // Range query - not applicable for dummy index, searches in the range from-to
//
//                // More Like This query - finds documents similar to the provided text
////                b.should(sb -> sb.moreLikeThis(mlt -> mlt
////                    .fields("title")
////                    .like(like -> like.text(token))
////                    .minTermFreq(1)
////                    .minDocFreq(1)));
//            });
//            return b;
//        })))._toQuery();
//    }
//
//    private Query buildAdvancedSearchQuery(List<String> operands, String operation) {
//        return BoolQuery.of(q -> q.must(mb -> mb.bool(b -> {
//            var field1 = operands.get(0).split(":")[0];
//            var value1 = operands.get(0).split(":")[1];
//            var field2 = operands.get(1).split(":")[0];
//            var value2 = operands.get(1).split(":")[1];
//
//            switch (operation) {
//                case "AND":
//                    b.must(sb -> sb.match(
//                            m -> m.field(field1).fuzziness(Fuzziness.ONE.asString()).query(value1)));
//                    b.must(sb -> sb.match(m -> m.field(field2).query(value2)));
//                    break;
//                case "OR":
//                    b.should(sb -> sb.match(
//                            m -> m.field(field1).fuzziness(Fuzziness.ONE.asString()).query(value1)));
//                    b.should(sb -> sb.match(m -> m.field(field2).query(value2)));
//                    break;
//                case "NOT":
//                    b.must(sb -> sb.match(
//                            m -> m.field(field1).fuzziness(Fuzziness.ONE.asString()).query(value1)));
//                    b.mustNot(sb -> sb.match(m -> m.field(field2).query(value2)));
//                    break;
//            }
//            return b;
//        })))._toQuery();
//    }

//    private Page<DummyIndex> runQuery(NativeQuery searchQuery) {
//
//        var searchHits = elasticsearchTemplate.search(searchQuery, DummyIndex.class,
//            IndexCoordinates.of("dummy_index"));
//
//        var searchHitsPaged = SearchHitSupport.searchPageFor(searchHits, searchQuery.getPageable());
//
//        return (Page<DummyIndex>) SearchHitSupport.unwrapSearchHits(searchHitsPaged);
//    }
}

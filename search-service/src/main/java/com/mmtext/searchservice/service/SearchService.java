package com.mmtext.searchservice.service;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;

import com.mmtext.searchservice.esdocument.MovieDocument;
import com.mmtext.searchservice.esdocument.ShowDocument;
import com.mmtext.searchservice.esdocument.TheaterDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SearchService {

    private static final Logger log = LoggerFactory.getLogger(SearchService.class);
    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    /** 🔎 Fuzzy search by movie title */
    @Cacheable(value = "movies", key = "#keyword")
    public List<MovieDocument> searchMovies(String keyword) {
        log.info("Fetching from Elasticsearch (no cache) for keyword: {}", keyword);
        Query query = Query.of(q -> q
                .multiMatch(m -> m
                        .query(keyword)
                        .fields("title", "genre", "language")
                        .boost(2.0f)
                        .fuzziness("AUTO")
                )
        );

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(query)
                .build();

        return elasticsearchOperations.search(nativeQuery, MovieDocument.class)
                .map(SearchHit::getContent)
                .toList();
    }

    /** 🏠 Theaters near a geo point */
    @Cacheable(value = "theaters", key = "#lat + '-' + #lon + '-' + #distance")
    public List<TheaterDocument> searchTheatersNear(double lat, double lon, String distance) {
        Query geoQuery = Query.of(q -> q
                .geoDistance(g -> g
                        .field("address.location")
                        .location(l -> l
                                .latlon(ll -> ll
                                        .lat(lat)
                                        .lon(lon)
                                )
                        )
                        .distance(distance)
                )
        );

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(geoQuery)
                .build();

        return elasticsearchOperations.search(nativeQuery, TheaterDocument.class)
                .map(SearchHit::getContent)
                .toList();
    }

    /** 🎭 Find theaters showing a given movie */
    @Cacheable(value = "theaters", key = "'movie-' + #movieTitle")
    public List<TheaterDocument> searchTheatersByMovie(String movieTitle) {
        Query nestedQuery = Query.of(q -> q
                .nested(n -> n
                        .path("shows.movie")
                        .query(nq -> nq
                                .match(m -> m
                                        .field("shows.movie.title")
                                        .query(FieldValue.of(movieTitle))
                                )
                        )
                        .scoreMode(co.elastic.clients.elasticsearch._types.query_dsl.ChildScoreMode.Avg)
                )
        );

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(nestedQuery)
                .build();

        return elasticsearchOperations.search(nativeQuery, TheaterDocument.class)
                .map(SearchHit::getContent)
                .toList();
    }

    /** 💰 Filter shows by ticket price and movie */
    @Cacheable(value = "shows", key = "#movieTitle + '-' + #minPrice + '-' + #maxPrice")
    public List<ShowDocument> searchShows(String movieTitle, int minPrice, int maxPrice) {
        Query matchQuery = Query.of(q -> q
                .match(m -> m
                        .field("movie.title")
                        .query(FieldValue.of(movieTitle))
                )
        );

        Query rangeQuery = Query.of(q -> q
                .range(r -> r
                        .number(n -> n
                                .field("ticketPrice")
                                .gte((double) minPrice)
                                .lte((double) maxPrice)
                        )
                )
        );

        Query boolQuery = Query.of(q -> q
                .bool(b -> b
                        .must(matchQuery)
                        .filter(rangeQuery)
                )
        );

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(boolQuery)
                .build();

        return elasticsearchOperations.search(nativeQuery, ShowDocument.class)
                .map(SearchHit::getContent)
                .toList();
    }

    /** 🔮 Autocomplete for movie titles */
    @Cacheable(value = "movies", key = "'autocomplete-' + #prefix")
    public List<MovieDocument> autocompleteMovie(String prefix) {
        Query prefixQuery = Query.of(q -> q
                .prefix(p -> p
                        .field("title.keyword")
                        .value(prefix.toLowerCase())
                )
        );

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(prefixQuery)
                .build();

        return elasticsearchOperations.search(nativeQuery, MovieDocument.class)
                .map(SearchHit::getContent)
                .toList();
    }

    @Cacheable(value = "movies",
            key = "'advanced-' + #keyword + '-' + #language + '-' + #genre + '-' + #minRating + '-' + #sortBy + '-' + #desc")
    public List<MovieDocument> searchMoviesAdvanced(String keyword, String language, String genre,
                                                    Double minRating, String sortBy, boolean desc) {

        List<Query> mustQueries = new ArrayList<>();
        List<Query> filterQueries = new ArrayList<>();

        if (keyword != null && !keyword.isEmpty()) {
            mustQueries.add(Query.of(q -> q.multiMatch(m -> m
                    .query(keyword)
                    .fields("title^3.0", "genre^1.5", "language")
                    .fuzziness("AUTO")
            )));
        }

        if (language != null) {
            filterQueries.add(Query.of(q -> q.term(t -> t
                    .field("language.keyword")
                    .value(language)
            )));
        }

        if (genre != null) {
            filterQueries.add(Query.of(q -> q.term(t -> t
                    .field("genre.keyword")
                    .value(genre)
            )));
        }

        if (minRating != null) {
            filterQueries.add(Query.of(q -> q.range(r -> r
                    .number(n -> n
                            .field("rating")
                            .gte(minRating)
                    )
            )));
        }

        Query boolQuery = Query.of(q -> q.bool(b -> {
            if (!mustQueries.isEmpty()) b.must(mustQueries);
            if (!filterQueries.isEmpty()) b.filter(filterQueries);
            if (mustQueries.isEmpty() && filterQueries.isEmpty()) {
                b.must(Query.of(q2 -> q2.matchAll(ma -> ma)));
            }
            return b;
        }));

        NativeQuery nativeQuery = new NativeQuery(boolQuery);

        if (sortBy != null && !sortBy.isEmpty()) {
            Sort.Direction direction = desc ? Sort.Direction.DESC : Sort.Direction.ASC;
            nativeQuery.addSort(Sort.by(new Sort.Order(direction, sortBy)));
        }

        return elasticsearchOperations.search(nativeQuery, MovieDocument.class)
                .map(SearchHit::getContent)
                .toList();
    }

    /** 📅 Filter shows within a given time range */
    @Cacheable(value = "shows", key = "'daterange-' + #start + '-' + #end")
    public List<ShowDocument> searchShowsByDateRange(OffsetDateTime start, OffsetDateTime end) {
        Query rangeQuery = Query.of(q -> q
                .range(r -> r
                        .date(d -> d
                                .field("showTime")
                                .gte(start.toString())
                                .lte(end.toString())
                        )
                )
        );

        Query boolQuery = Query.of(q -> q
                .bool(b -> b.filter(rangeQuery))
        );

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(boolQuery)
                .build();

        return elasticsearchOperations.search(nativeQuery, ShowDocument.class)
                .map(SearchHit::getContent)
                .toList();
    }

    /** ⏰ Shows near user location within next 2 hours - NOT CACHED (time-sensitive) */
    public List<ShowDocument> searchShowsNearNow(double lat, double lon, String distanceKm) {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime nextTwoHours = now.plusHours(2);

        Query timeRangeQuery = Query.of(q -> q.range(r -> r.date(d -> d
                .field("showTime")
                .gte(now.toString())
                .lte(nextTwoHours.toString())
        )));

        Query geoQuery = Query.of(q -> q.geoDistance(g -> g
                .field("theater.address.location")
                .location(l -> l.latlon(ll -> ll.lat(lat).lon(lon)))
                .distance(distanceKm)
        ));

        Query boolQuery = Query.of(q -> q.bool(b -> b.filter(timeRangeQuery, geoQuery)));

        NativeQuery nativeQuery = new NativeQuery(boolQuery);
        nativeQuery.addSort(Sort.by(Sort.Order.asc("showTime")));

        return elasticsearchOperations.search(nativeQuery, ShowDocument.class)
                .map(SearchHit::getContent)
                .toList();
    }

    /** 🔥 Trending movies based on number of shows */
    @Cacheable(value = "trending", key = "'movies'")
    public List<MovieDocument> trendingMovies() {
        Query matchAllQuery = Query.of(q -> q.matchAll(m -> m));

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(matchAllQuery)
                .withPageable(PageRequest.of(0, 10))
                .build();

        return elasticsearchOperations.search(nativeQuery, MovieDocument.class)
                .map(SearchHit::getContent)
                .toList();
    }

    /** ⚖️ Weighted multi-field search (for best ranking) */
    @Cacheable(value = "movies", key = "'weighted-' + #text")
    public List<MovieDocument> weightedSearch(String text) {
        Query query = Query.of(q -> q
                .multiMatch(m -> m
                        .query(text)
                        .fields("title^4.0", "genre^2.0", "language^1.0")
                        .type(TextQueryType.BestFields)
                        .fuzziness("AUTO")
                )
        );

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(query)
                .build();

        return elasticsearchOperations.search(nativeQuery, MovieDocument.class)
                .map(SearchHit::getContent)
                .toList();
    }

    // Cache eviction methods - call these when data changes
    @CacheEvict(value = "movies", allEntries = true)
    public void evictMoviesCache() {
        // Evict all movie cache entries
    }

    @CacheEvict(value = "theaters", allEntries = true)
    public void evictTheatersCache() {
        // Evict all theater cache entries
    }

    @CacheEvict(value = "shows", allEntries = true)
    public void evictShowsCache() {
        // Evict all show cache entries
    }

    @CacheEvict(value = {"movies", "theaters", "shows", "trending"}, allEntries = true)
    public void evictAllCaches() {
        // Evict all cache entries
    }
}
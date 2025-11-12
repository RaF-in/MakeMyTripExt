package com.mmtext.searchconsumerservice.repo;

import com.mmtext.searchconsumerservice.esdocument.MovieDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieSearchRepository extends ElasticsearchRepository<MovieDocument, String> {}
package com.mmtext.searchconsumerservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.mmtext.searchconsumerservice.esdocument.MovieDocument;
import com.mmtext.searchconsumerservice.repo.MovieSearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MovieConsumeService {
    private static final Logger log = LoggerFactory.getLogger(MovieConsumeService.class);
    @Autowired
    MovieSearchRepository movieSearchRepository;

    public void saveOrUpdateMovie(MovieDocument movieDocument) {
        try {

            // Save the document to Elasticsearch (save handles both create and update if ID exists)
            movieSearchRepository.save(movieDocument);

            log.info("Successfully saved/updated MovieDocument with ID: {}", movieDocument.getId());

        } catch (Exception e) {
            log.error("Error saving movie document: {}", movieDocument, e);
            // Handle error appropriately
        }
    }

    public void deleteMovie(JsonNode movieDataJson) {
        // Extract the ID from the 'before' state of the deleted record
        if (movieDataJson.has("id")) {
            Long movieId = movieDataJson.get("id").asLong();
//            movieSearchRepository.deleteById(movieId);
            log.info("Successfully deleted MovieDocument with ID: {}", movieId);
        } else {
            log.warn("Cannot delete movie: ID field not found in 'before' payload.");
        }
    }
}

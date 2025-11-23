package com.mmtext.searchconsumerservice.listeners;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmtext.searchconsumerservice.esdocument.HotelDocument;
import com.mmtext.searchconsumerservice.esdocument.MovieDocument;
import com.mmtext.searchconsumerservice.interfaces.OutboxEventHandler;
import com.mmtext.searchconsumerservice.service.HotelConsumeService;
import com.mmtext.searchconsumerservice.service.MovieConsumeService;
import com.mmtext.searchconsumerservice.service.OutboxEventProcessor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaEventListeners {
    private static final Logger log = LoggerFactory.getLogger(KafkaEventListeners.class);
    private final ObjectMapper objectMapper;
    private final MovieConsumeService movieConsumeService;
    private final HotelConsumeService hotelConsumeService;

    public KafkaEventListeners(ObjectMapper objectMapper, MovieConsumeService movieConsumeService, HotelConsumeService hotelConsumeService) {
        this.objectMapper = objectMapper;
        this.movieConsumeService = movieConsumeService;
        this.hotelConsumeService = hotelConsumeService;
    }

    @KafkaListener(topics = "mmtext.public.movie_outbox_events", groupId = "movie-consumer-group")
    public void listenMovies(ConsumerRecord<String, String> record) throws Exception {
        OutboxEventProcessor<MovieDocument> processor = new OutboxEventProcessor<>(
                objectMapper,
                MovieDocument.class,
                new OutboxEventHandler<MovieDocument>() {
                    @Override
                    public void saveOrUpdate(MovieDocument document) {
                        movieConsumeService.saveOrUpdateMovie(document);
                    }

                    @Override
                    public void delete(JsonNode beforeNode) {
                        movieConsumeService.deleteMovie(beforeNode);
                    }
                }
        );
        processor.process(record);
    }

    @KafkaListener(topics = "mmtext.public.hotel_outbox_events", groupId = "hotel-consumer-group")
    public void listenHotels(ConsumerRecord<String, String> record) throws Exception {
        OutboxEventProcessor<HotelDocument> processor = new OutboxEventProcessor<>(
                objectMapper,
                HotelDocument.class,
                new OutboxEventHandler<HotelDocument>() {
                    @Override
                    public void saveOrUpdate(HotelDocument document) {
                        hotelConsumeService.saveOrUpdateHotel(document);
                    }

                    @Override
                    public void delete(JsonNode beforeNode) {
                        hotelConsumeService.deleteHotel(beforeNode);
                    }
                }
        );
        processor.process(record);
    }
}

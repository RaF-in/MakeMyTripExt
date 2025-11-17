package com.mmtext.supplierpollingservice.config;

import com.mmtext.supplierpollingservice.poller.AirlineSupplierPoller;
import com.mmtext.supplierpollingservice.poller.BusSupplierPoller;
import com.mmtext.supplierpollingservice.poller.HotelSupplierPoller;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableScheduling
public class ReactivePollingConfig {

    /**
     * Configure WebClient with production-grade settings
     * - Connection pooling
     * - Timeout configuration
     * - Memory buffer limits
     */
    @Bean
    public WebClient.Builder webClientBuilder() {

        // Connection pool configuration
        ConnectionProvider connectionProvider = ConnectionProvider.builder("supplier-pool")
                .maxConnections(500)               // Max connections across all suppliers
                .maxIdleTime(Duration.ofSeconds(20))
                .maxLifeTime(Duration.ofSeconds(60))
                .pendingAcquireTimeout(Duration.ofSeconds(10))
                .evictInBackground(Duration.ofSeconds(30))
                .build();

        // HTTP client with timeouts
        HttpClient httpClient = HttpClient.create(connectionProvider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .responseTimeout(Duration.ofSeconds(15))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(15, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(10, TimeUnit.SECONDS))
                );

        // Increase memory buffer for large responses
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                .build();

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(strategies);
    }
//
//    /**
//     * Define supplier configurations
//     * In production: load from database or config service
//     */
//    @Bean
//    public PollingConfig airlineSupplierConfig() {
//        return PollingConfig.airlineDefault(
//                "airline-amadeus-1",
//                "https://api.amadeus.com" // Mock URL
//        );
//    }
//
//    @Bean
//    public PollingConfig hotelSupplierConfig() {
//        return PollingConfig.hotelDefault(
//                "hotel-booking-1",
//                "https://api.booking.com" // Mock URL
//        );
//    }
//
//    @Bean
//    public PollingConfig busSupplierConfig() {
//        PollingConfig config = new PollingConfig();
//        config.setSupplierId("bus-amadeus-1");
//        config.setSupplierType(SupplierType.BUS);
//        config.setBaseUrl("https://api.redbus.in");
//        config.setNormalIntervalMs(180000L); // 3 min
//        config.setPeakIntervalMs(60000L);    // 1 min
//        config.setMaxRetries(3);
//        config.setTimeoutMs(8000L);
//        config.setSupportsEtag(false);
//        config.setSupportsIfModifiedSince(true);
//        return config;
//    }

    /**
     * Create poller instances
     */
    @Bean
    public AirlineSupplierPoller airlinePoller(
            WebClient.Builder builder, PollingConfig airlineSupplierConfig) {
        return new AirlineSupplierPoller(builder, airlineSupplierConfig);
    }

    @Bean
    public HotelSupplierPoller hotelPoller(
            WebClient.Builder builder, PollingConfig hotelSupplierConfig) {
        return new HotelSupplierPoller(builder, hotelSupplierConfig);
    }

    @Bean
    public BusSupplierPoller busPoller(
            WebClient.Builder builder, PollingConfig busSupplierConfig) {
        return new BusSupplierPoller(builder, busSupplierConfig);
    }
}
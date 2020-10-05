package com.backbase.stream.product.configuration;

import com.backbase.dbs.arrangement.integration.ApiClient;
import com.backbase.dbs.arrangement.integration.api.ArrangementsApi;
import com.backbase.stream.config.BackbaseStreamConfigurationProperties;
import com.backbase.stream.product.service.ArrangementService;
import com.backbase.stream.webclient.DbsWebClientConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.text.DateFormat;
import lombok.AllArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Access Control Configuration.
 */
@Configuration
@EnableConfigurationProperties(BackbaseStreamConfigurationProperties.class)
@Import(DbsWebClientConfiguration.class)
@AllArgsConstructor
public class ProductConfiguration {

    private final BackbaseStreamConfigurationProperties backbaseStreamConfigurationProperties;

    @Bean
    public ArrangementService arrangementService(ApiClient accountsApiClient) {
        return new ArrangementService(new ArrangementsApi(accountsApiClient));
    }

    @Bean
    public ApiClient accountsApiClient(WebClient dbsWebClient, ObjectMapper objectMapper, DateFormat dateFormat) {
        ApiClient apiClient = new ApiClient(dbsWebClient, objectMapper, dateFormat);
        apiClient.setBasePath(backbaseStreamConfigurationProperties.getDbs().getArrangementManagerBaseUrl());
        return apiClient;
    }


}

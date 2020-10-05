package com.backbase.stream.product.service;

import com.backbase.dbs.arrangement.integration.ApiClient;
import com.backbase.dbs.arrangement.integration.api.ArrangementsApi;
import com.backbase.dbs.arrangement.integration.model.ArrangementItemResponseBody;
import com.backbase.dbs.arrangement.integration.model.BatchResponseItemExtended;
import com.backbase.dbs.arrangement.integration.model.ExternalLegalEntityIds;
import com.backbase.dbs.arrangement.integration.model.PostArrangement;
import com.backbase.dbs.arrangement.integration.model.PutArrangement;
import com.backbase.stream.product.exception.ArrangementCreationException;
import com.backbase.stream.product.exception.ArrangementUpdateException;
import com.backbase.stream.product.mapping.ProductMapper;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Manage Products (In DBS Called Arrangements).
 */
@SuppressWarnings("WeakerAccess")
@Slf4j
public class ArrangementService {

    private final ArrangementsApi arrangementsApi;
    private final ProductMapper productMapper = Mappers.getMapper(ProductMapper.class);

    public ArrangementService(ArrangementsApi arrangementsApi) {
        this.arrangementsApi = arrangementsApi;
    }

    public Mono<PostArrangement> createArrangement(PostArrangement arrangementItemPost) {
        return arrangementsApi.postArrangements(arrangementItemPost)
            .onErrorMap(WebClientResponseException.class, throwable -> new ArrangementCreationException(throwable, "Failed to post arrangements"))
            .doOnError(WebClientResponseException.class, throwable -> log.error("Failed to create arrangement: {}\n{}", arrangementItemPost.getId(), throwable.getResponseBodyAsString()))
            .doOnNext(arrangementAddedResponse -> log.info("Added arrangement with internal Id: {}", arrangementAddedResponse.getId()))
            .thenReturn(arrangementItemPost);
    }

    public Mono<PutArrangement> updateArrangement(PutArrangement arrangemenItemBase) {
        log.info("Updating Arrangement: {}", arrangemenItemBase.getId());
        if (arrangemenItemBase.getDebitCards() == null)
            arrangemenItemBase.setDebitCards(Collections.emptyList());
        return arrangementsApi.putArrangements(arrangemenItemBase)
            .doOnNext(aVoid -> log.info("Updated Arrangement: {}", arrangemenItemBase.getId())).map(aVoid -> arrangemenItemBase)
            .onErrorResume(WebClientResponseException.class, throwable ->
                Mono.error(new ArrangementUpdateException(throwable, "Failed to update Arrangement: " + arrangemenItemBase.getId())))
            .thenReturn(arrangemenItemBase);

    }

    /**
     * Upsert list of arrangements using DBS batch upsert API.
     *
     * @param arrangementItems list of arrangements to be upserted.
     * @return flux of response items.
     */
    public Flux<BatchResponseItemExtended> upsertBatchArrangements(List<PostArrangement> arrangementItems) {
        return arrangementsApi.postBatch(arrangementItems)
            .map(r -> {
                log.info("Batch Arrangement update result for arrangementId: {}, resourceId: {}, action: {}, result: {}", r.getArrangementId(), r.getResourceId(), r.getAction(), r.getStatus());
                // Check if any failed, then fail everything.
                if (!BatchResponseItemExtended.StatusEnum._200.equals(r.getStatus())) {
                    throw new IllegalStateException("Batch arrangement update failed: " + r.getResourceId());
                }
                return r;
            })
            .onErrorResume(WebClientResponseException.class, throwable ->
                Mono.error(new ArrangementUpdateException(throwable, "Batch arrangement update failed: " + arrangementItems)));
    }

    public Flux<ArrangementItemResponseBody> getArrangementByExternalId(List<String> externalId) {
        return arrangementsApi.getArrangements(externalId);
    }


    public Mono<ArrangementItemResponseBody> getArrangementByExternalId(String externalId) {
        return getArrangementByExternalId(Collections.singletonList(externalId))
            .onErrorResume(WebClientResponseException.NotFound.class, ex -> {
                log.info("Arrangement: {} not found", externalId);
                return Mono.empty();})
            .singleOrEmpty();
    }

    /**
     * Delete arrangement identified by External ID.
     *
     * @param arrangementExternalId arrangement external identifier
     * @return external identifier of removed arrangement.
     */
    public Mono<String> deleteArrangementByExternalId(String arrangementExternalId) {
        log.debug("Removing Arrangement with external id {}", arrangementExternalId);
        return arrangementsApi.deleteIdById(arrangementExternalId)
            .thenReturn(arrangementExternalId);
    }

    /**
     * Assign Arrangement with specified Legal Entities.
     *
     * @param arrangementExternalId    external id of Arrangement.
     * @param legalEntitiesExternalIds list of Legal Entities external identifiers.
     * @return Mono<Void>
     */
    public Mono<Void> addLegalEntitiesForArrangement(String arrangementExternalId, List<String> legalEntitiesExternalIds) {
        log.debug("Attaching Arrangement {} to Legal Entities: {}", arrangementExternalId, legalEntitiesExternalIds);
        return arrangementsApi.postLegalentitiesById(arrangementExternalId, new ExternalLegalEntityIds().ids(legalEntitiesExternalIds));
    }

    /**
     * Detach specified Arrangement from Legal Entities.
     *
     * @param arrangementExternalId  arrangement external identifier.
     * @param legalEntityExternalIds List of Legal Entities identified by external identifier.
     * @return Mono<Void>
     */
    public Mono<Void> removeLegalEntityFromArrangement(String arrangementExternalId, List<String> legalEntityExternalIds) {
        log.debug("Removing Arrangement {} from Legal Entities {}", arrangementExternalId, legalEntityExternalIds);
        // TODO: Very ugly, but seems like BOAT doesn't generate body for DELETE requests. Not sure it is incorrect though..
        ApiClient apiClient = arrangementsApi.getApiClient();
        return apiClient.invokeAPI(
            "/arrangements/{externalArrangementId}/legalentities",
            HttpMethod.DELETE,
            Collections.singletonMap("externalArrangementId", arrangementExternalId),
            new LinkedMultiValueMap<>(),
            Collections.singletonMap("ids", legalEntityExternalIds),
            new HttpHeaders(),
            new LinkedMultiValueMap<>(),
            new LinkedMultiValueMap<>(),
            apiClient.selectHeaderAccept(new String[]{"application/json"}),
            apiClient.selectHeaderContentType(new String[]{}),
            new String[]{},
            new ParameterizedTypeReference<Void>() {
            }
        );
    }

}

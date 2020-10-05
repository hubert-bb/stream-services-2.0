package com.backbase.stream.bootstrap.service;

import com.backbase.stream.LegalEntitySaga;
import com.backbase.stream.LegalEntityTask;
import com.backbase.stream.bootstrap.configuration.BootstrapTaskConfiguration;
import com.backbase.stream.configuration.LegalEntitySagaConfiguration;
import com.backbase.stream.legalentity.model.LegalEntity;
import com.backbase.stream.productcatalog.ProductCatalogService;
import com.backbase.stream.productcatalog.configuration.ProductCatalogServiceConfiguration;
import com.backbase.stream.productcatalog.model.ProductCatalog;
import com.backbase.stream.worker.model.StreamTask;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;


@Slf4j
@EnableConfigurationProperties({BootstrapTaskConfiguration.class})
@Import({
    LegalEntitySagaConfiguration.class,
    ProductCatalogServiceConfiguration.class
})
@RequiredArgsConstructor
@Service
public class BootstrapService {

    private final LegalEntitySaga legalEntitySaga;
    private final ProductCatalogService productCatalogService;
    private final BootstrapTaskConfiguration bootstrapTaskConfiguration;


    public void setupLegalEntityHieararchy() {
        log.info("Setting up Root Legal Entity Hierarchy");
        LegalEntity legalEntity = bootstrapTaskConfiguration.getLegalEntity();
        if (legalEntity == null) {
            log.error("Failed to load Legal Entity Structure");
            System.exit(1);
        } else {
            log.info("Bootstrapping Root Legal Entity Structure: {}", legalEntity.getName());
            List<LegalEntity> aggregates = Collections.singletonList(bootstrapTaskConfiguration.getLegalEntity());

            Flux.fromIterable(aggregates)
                .map(LegalEntityTask::new)
                .flatMap(legalEntitySaga::executeTask)
                .doOnNext(StreamTask::logSummary)
                .doOnError(throwable -> {
                    log.error("Failed to setup Legal Entity Hierarchy", throwable);
                })

                .collectList()
                .block();
            log.info("Finished bootstrapping Legal Entity Structure");
        }
    }
    
    public void setupProductCatalog() {
        ProductCatalog productCatalog = bootstrapTaskConfiguration.getProductCatalog();
        if (productCatalog != null) {
            log.info("Setting up Product Catalog");
            productCatalogService.setupProductCatalog(productCatalog);
        } else {
            log.info("No Product Catalog Configured");
        }
        log.info("Finished setting up Product Catalog");

    }

}
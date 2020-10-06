package com.backbase.stream.productcatalog;


import com.backbase.dbs.arrangement.integration.ApiClient;
import com.backbase.dbs.arrangement.integration.api.ProductKindsApi;
import com.backbase.dbs.arrangement.integration.api.ProductsApi;
import com.backbase.dbs.arrangement.integration.model.Id;
import com.backbase.dbs.arrangement.integration.model.NewProductKindItem;
import com.backbase.dbs.arrangement.integration.model.NewProductKindItemPut;
import com.backbase.dbs.arrangement.integration.model.ProductItemResponseBody;
import com.backbase.dbs.arrangement.integration.model.ProductKindId;
import com.backbase.stream.legalentity.model.ProductCatalog;
import com.backbase.stream.legalentity.model.ProductKind;
import com.backbase.stream.legalentity.model.ProductType;
import com.backbase.stream.productcatalog.mapper.ProductCatalogMapper;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactive Product Catalog Service allowing to setup a complete Product Catalog in a single call.
 */
@Slf4j
public class ReactiveProductCatalogService {

    private final ProductsApi productsApi;
    private final ProductKindsApi productKindsApi;
    private final ProductCatalogMapper productCatalogMapper = Mappers.getMapper(ProductCatalogMapper.class);

    public ReactiveProductCatalogService(ApiClient accountPresentationClient) {
        this.productsApi = new ProductsApi(accountPresentationClient);
        this.productKindsApi = new ProductKindsApi(accountPresentationClient);
    }

    /**
     * Get Product Catalog from DBS.
     *
     * @return Product Catalog
     */
    public Mono<ProductCatalog> getProductCatalog() {
        Flux<ProductKind> productKindsFlux = getProductKindFlux();

        Flux<ProductItemResponseBody> products = productsApi.getProducts(null, null);
        Flux<ProductType> productTypesFlux = products.map(productCatalogMapper::toStream);

        return Mono.zip(productKindsFlux.collectList(), productTypesFlux.collectList())
            .map(tuple -> new ProductCatalog().productTypes(tuple.getT2()).productKinds(tuple.getT1()))
            .doOnNext(productCatalog -> log.info("Created Product Catalog"));
    }

    private Flux<ProductKind> getProductKindFlux() {
        return productKindsApi.getProductKinds(null, null, null)
            .map(productCatalogMapper::toStream);
    }

    /**
     * Setup Product Catalog from Aggregate.
     *
     * @param productCatalog Product Catalog
     * @return Completed Product Catalog
     */
    public Mono<ProductCatalog> setupProductCatalog(ProductCatalog productCatalog) {
        return getProductCatalog().flatMap(existingProductCatalog -> {

            List<ProductKind> newProductKinds = productCatalog.getProductKinds().stream()
                .filter(newProductKind -> existingProductCatalog.getProductKinds().stream()
                    .noneMatch(productKind ->
                        productKind.getExternalKindId().equals(newProductKind.getExternalKindId())))
                .collect(Collectors.toList());
            List<ProductType> newProductTypes = productCatalog.getProductTypes().stream()
                .filter(newProductType -> existingProductCatalog.getProductTypes().stream()
                    .noneMatch(productType ->
                        productType.getId().equals(newProductType.getId())))
                .collect(Collectors.toList());

            Flux<ProductKind> productKindFlux = storeProductKinds(newProductKinds)
                .mergeWith(getProductKindFlux());

            // Ensure products kinds are created first
            return productKindFlux.collectList().flatMap(productKinds -> {
                return createProductTypes(newProductTypes, productKinds).collectList().flatMap(productTypes -> {
                    productCatalog.setProductTypes(productTypes);
                    return Mono.just(productCatalog);
                });
            });
        });
    }


    /**
     * Setup Product Catalog from Aggregate.
     *
     * @param productCatalog Product Catalog
     * @return Completed Product Catalog
     */
    public Mono<ProductCatalog> updateExistingProductCatalog(ProductCatalog productCatalog) {
        return getProductCatalog().flatMap(existingProductCatalog -> {

            List<ProductKind> existingProductKinds = productCatalog.getProductKinds().stream()
                .filter(existingProductKind -> existingProductCatalog.getProductKinds().stream()
                    .anyMatch(productKind ->
                        productKind.getExternalKindId().equals(existingProductKind.getExternalKindId())))
                .collect(Collectors.toList());
            List<ProductType> existingProductTypes = productCatalog.getProductTypes().stream()
                .filter(existingProductType -> existingProductCatalog.getProductTypes().stream()
                    .anyMatch(productType ->
                        productType.getId().equals(existingProductType.getId())))
                .collect(Collectors.toList());

            Flux<ProductKind> existingProductKindFlux = updateProductKind(existingProductKinds)
                .map(productCatalogMapper::toStream)
                .mergeWith(getProductKindFlux());

            return existingProductKindFlux.collectList().flatMap(
                productKinds -> updateProductTypes(existingProductTypes, productKinds).collectList().flatMap(productTypes -> Mono.just(productCatalog))
            );
        });
    }


    private Flux<NewProductKindItemPut> updateProductKind(List<ProductKind> productKinds) {
        log.info("Updating Product Type1: {}", productKinds);
        return Flux.fromIterable(productKinds)
            .map(productCatalogMapper::toIntegrationPut)
            .flatMap(this::updateProductKind);
    }

    private Mono<NewProductKindItemPut> updateProductKind(NewProductKindItemPut productKind) {
        log.info("Updating Product Type2: {}", productKind.getKindName());
        return productKindsApi.putProductKinds(productKind)
            .doOnError(WebClientResponseException.BadRequest.class, e ->
                log.error("Bad Request Storing Product Kind: {} \n[{}]: {}\nResponse: {}", productKind, Objects.requireNonNull(e.getRequest()).getMethod(), e.getRequest().getURI(), e.getResponseBodyAsString())
            )
            .doOnError(WebClientResponseException.class, e ->
                log.error("Bad Request Product Kind: {} \n[{}]: {}\nResponse: {}", productKind, Objects.requireNonNull(e.getRequest()).getMethod(), e.getRequest().getURI(), e.getResponseBodyAsString())
            ).map(actual -> productKind);
    }

    private Flux<ProductType> createProductTypes(List<ProductType> productTypes, List<ProductKind> productKinds) {
        return Flux.fromIterable(productTypes)
            .flatMap(productType -> createProductType(productType, productKinds));
    }

    private Flux<ProductType> updateProductTypes(List<ProductType> productTypes, List<ProductKind> productKinds) {
        return Flux.fromIterable(productTypes)
            .flatMap(productType -> updateProductType(productType, productKinds));
    }

    public Mono<ProductType> updateProductType(ProductType productType, List<ProductKind> productKinds) {
        Mono<Void> productIdMono = Mono.just(productType)
            .map(productCatalogMapper::toPresentation)
            .map(productItem -> {
                log.info("Updating Product Type: {}", productItem.getProductTypeName());
                ProductKind productKind = getProductKind(productType, productKinds);
                productItem.setId(productKind.getExternalKindId());
                productItem.setProductKindName(productKind.getKindName());
                productItem.setProductTypeName(productType.getTypeName());
                return productItem;
            })
            .flatMap(
                productItem ->
                    productsApi.putProducts(productItem)
                        .doOnError(WebClientResponseException.BadRequest.class, e -> log.error("Bad Request Storing Product Type: {} \n[{}]: {}\nResponse: {}", productItem, Objects.requireNonNull(e.getRequest()).getMethod(), e.getRequest().getURI(), e.getResponseBodyAsString()))
                        .doOnError(WebClientResponseException.class, e -> log.error("Bad Request Storing Product Type: {} \n[{}]: {}\nResponse: {}", productItem, Objects.requireNonNull(e.getRequest()).getMethod(), e.getRequest().getURI(), e.getResponseBodyAsString())));

        return Mono.zip(Mono.just(productType), productIdMono, this::handelUpdateProductTypeResult);
    }


    public Mono<ProductType> createProductType(ProductType productType, List<ProductKind> productKinds) {

        Mono<Id> productIdMono = Mono.just(productType)
            .map(productCatalogMapper::toIntegration)
            .map(productItem -> {
                log.info("Creating Product Type: {}", productItem.getProductTypeName());
                ProductKind productKind = getProductKind(productType, productKinds);
                productItem.setId(productKind.getExternalKindId());
                productItem.setProductKindName(productKind.getKindName());
                productItem.setProductTypeName(productType.getTypeName());
                return productItem;
            })
            .flatMap(
                productItem ->
                    productsApi.postProducts(productItem)
                        .doOnError(WebClientResponseException.BadRequest.class, e -> log.error("Bad Request Storing Product Type: {} \n[{}]: {}\nResponse: {}", productItem, Objects.requireNonNull(e.getRequest()).getMethod(), e.getRequest().getURI(), e.getResponseBodyAsString()))
                        .doOnError(WebClientResponseException.class, e -> log.error("Bad Request Storing Product Type: {} \n[{}]: {}\nResponse: {}", productItem, Objects.requireNonNull(e.getRequest()).getMethod(), e.getRequest().getURI(), e.getResponseBodyAsString())));

        return Mono.zip(Mono.just(productType), productIdMono, this::handelStoreProductTypeResult);
    }

    private ProductKind getProductKind(ProductType productType, List<ProductKind> productKinds) {
        return productKinds.stream()
            .filter(kind -> productType.getId().equals(kind.getExternalKindId()))
            .findFirst()
            .orElseThrow(() -> new NullPointerException("Cannot cretae Product Type with out a valid Product Kind"));
    }


    private ProductType handelStoreProductTypeResult(ProductType productType, Id productId) {
        log.info("Product Type: {} created with: {}", productType.getProductTypeName(), productId.getId());
        return productType;
    }

    private ProductType handelUpdateProductTypeResult(ProductType productType, Object productIdMono) {
        log.info("Product Type: {} updated.", productType.getProductTypeName());
        return productType;
    }

    private Flux<ProductKind> storeProductKinds(List<ProductKind> productKinds) {
        return Flux.fromIterable(productKinds)
            .map(productCatalogMapper::toIntegration)
            .flatMap(this::storeProductKind);
    }


    private Mono<ProductKind> storeProductKind(NewProductKindItem productKind) {
        Mono<ProductKindId> productKindIdMono = productKindsApi.postProductKinds(productKind)
            .doOnError(WebClientResponseException.BadRequest.class, e ->
                log.error("Bad Request Storing Product Kind: {} \n[{}]: {}\nResponse: {}", productKind, Objects.requireNonNull(e.getRequest()).getMethod(), e.getRequest().getURI(), e.getResponseBodyAsString())
            )
            .doOnError(WebClientResponseException.class, e ->
                log.error("Bad Request Product Kind: {} \n[{}]: {}\nResponse: {}", productKind, Objects.requireNonNull(e.getRequest()).getMethod(), e.getRequest().getURI(), e.getResponseBodyAsString())
            );
        return Mono.zip(Mono.just(productKind), productKindIdMono, this::handleStoreResult)
            .map(productCatalogMapper::toStream);
    }

    private NewProductKindItem handleStoreResult(NewProductKindItem productKindItem, ProductKindId productKindId) {
        log.info("Product Kind: {} created with: {}", productKindItem.getKindName(), productKindId);
        return productKindItem;
    }


    public Mono<ProductType> getProductTypeByExternalId(String productTypeExternalId) {
        log.info("Get Product Type: {}", productTypeExternalId);
        return productsApi.getProducts(Collections.singletonList(productTypeExternalId), null)
            .doOnNext(productItem -> log.info("Found product: {} for id: {}", productItem.getProductTypeName(), productTypeExternalId))
            .doOnError(WebClientResponseException.class, ex -> {
                log.error("Failed to get product type by external id: {}. Response: {}", productTypeExternalId, ex.getResponseBodyAsString());
            })
            .onErrorResume(WebClientResponseException.NotFound.class, ex -> {
                log.info("No product type found with id: {}", productTypeExternalId);
                return Mono.empty();
            })
            .singleOrEmpty()
            .map(productCatalogMapper::toStream);
    }
}

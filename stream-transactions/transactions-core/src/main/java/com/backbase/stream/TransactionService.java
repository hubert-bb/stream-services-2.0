package com.backbase.stream;

import com.backbase.dbs.transaction.integration.api.TransactionsApi;
import com.backbase.dbs.transaction.integration.model.ArrangementItem;
import com.backbase.dbs.transaction.integration.model.SchemasTransactionPost;
import com.backbase.dbs.transaction.integration.model.TransactionItem;
import com.backbase.dbs.transaction.integration.model.TransactionPostResponse;
import com.backbase.dbs.transaction.integration.model.TransactionsDeleteRequestBody;
import com.backbase.dbs.transaction.integration.model.TransactionsGet;
import com.backbase.dbs.transaction.integration.model.TransactionsPatchRequestBody;
import com.backbase.stream.transaction.TransactionTask;
import com.backbase.stream.transaction.TransactionUnitOfWorkExecutor;
import com.backbase.stream.transaction.TransactionsQuery;
import com.backbase.stream.worker.model.UnitOfWork;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Main Transaction Ingestion Service. Supports Retry and back pressure and controller number of transactions to ingest
 * per second.
 */
@Slf4j
public class TransactionService {

    private final TransactionsApi transactionsApi;
    private final TransactionUnitOfWorkExecutor transactionTaskExecutor;

    public TransactionService(TransactionsApi transactionsApi, TransactionUnitOfWorkExecutor transactionTaskExecutor) {
        this.transactionTaskExecutor = transactionTaskExecutor;
        this.transactionsApi = transactionsApi;
    }

    /**
     * Upsert Transactions.
     *
     * @param transactions Unbounded list of Transactions
     * @return Ingestion Transactions IDs
     */
    public Flux<TransactionPostResponse> processTransactions(Flux<SchemasTransactionPost> transactions) {
        Flux<UnitOfWork<TransactionTask>> unitOfWorkFlux = transactionTaskExecutor.prepareUnitOfWork(transactions);
        return unitOfWorkFlux.flatMap(transactionTaskExecutor::executeUnitOfWork)
            .flatMap(this::getTransactionIdsFlux);
    }

    private Flux<TransactionPostResponse> getTransactionIdsFlux(UnitOfWork<TransactionTask> unitOfWork) {
        Stream<TransactionPostResponse> transactionIdsStream = unitOfWork.getStreamTasks().stream()
            .map(TransactionTask::getResponse)
            .flatMap(Collection::stream);
        return Flux.fromStream(transactionIdsStream);
    }


    /**
     * Retrieve latest transactions for an Arrangement.
     *
     * @param arrangementId external productId
     * @param size          number of transactions to return.
     * @return List of transactions
     */
    public Flux<TransactionItem> getLatestTransactions(String arrangementId, int size) {
        TransactionsQuery transactionsQuery = new TransactionsQuery();
        transactionsQuery.setArrangementId(arrangementId);
        transactionsQuery.setSize(size);
        return getTransactionsFlux(transactionsQuery)
            .onErrorResume(WebClientResponseException.NotFound.class, ex -> {
                log.info("No transactions found for: {} message: {}", arrangementId, ex.getResponseBodyAsString());
                return Flux.empty();
            }).doOnError(Throwable.class, ex -> log.error("Error: {}", ex.getMessage(), ex));
    }


    /**
     * Remove transactions from DBS.
     *
     * @param transactionItemDelete Body of transactions to delete
     * @return Void if successful.
     */
    @SuppressWarnings("WeakerAccess")
    public Mono<Void> deleteTransactions(Flux<TransactionsDeleteRequestBody> transactionItemDelete) {
        return transactionItemDelete
            .collectList()
            .flatMap(transactionsApi::postDelete);

    }

    /**
     * Get list of transactions.
     *
     * @param transactionsQuery Transaction Query
     * @return A list of transactions
     */
    public Flux<TransactionItem> getTransactionsFlux(TransactionsQuery transactionsQuery) {
        return getTransactions(transactionsQuery)
            .flatMapMany(transactionsGet -> Flux.fromIterable(transactionsGet.getTransactionItems()));
    }

    public Mono<TransactionsGet> getTransactions(TransactionsQuery transactionsQuery) {
        return transactionsApi.getTransactions(
            transactionsQuery.getAmountGreaterThan(),
            transactionsQuery.getAmountLessThan(),
            transactionsQuery.getBookingDateGreaterThan(),
            transactionsQuery.getBookingDateLessThan(),
            transactionsQuery.getTypes(),
            transactionsQuery.getDescription(),
            transactionsQuery.getReference(),
            transactionsQuery.getTypeGroups(),
            transactionsQuery.getCounterPartyName(),
            transactionsQuery.getCounterPartyAccountNumber(),
            transactionsQuery.getCreditDebitIndicator(),
            transactionsQuery.getCategory(),
            transactionsQuery.getCategories(),
            transactionsQuery.getBillingStatus(),
            transactionsQuery.getCurrency(),
            transactionsQuery.getNotes(),
            transactionsQuery.getId(),
            transactionsQuery.getArrangementId(),
            transactionsQuery.getArrangementsIds(),
            transactionsQuery.getFromCheckSerialNumber(),
            transactionsQuery.getToCheckSerialNumber(),
            transactionsQuery.getCheckSerialNumbers(),
            transactionsQuery.getQuery(),
            transactionsQuery.getFrom(),
            transactionsQuery.getCursor(),
            transactionsQuery.getSize(),
            transactionsQuery.getOrderBy(),
            transactionsQuery.getDirection(),
            transactionsQuery.getSecDirection());
    }

    /**
     * Update Transactions  with a new category or billing status.
     *
     * @param transactionItems Updated category and billing status fields
     * @return empty mono on completion
     */
    public Mono<Void> patchTransactions(Flux<TransactionsPatchRequestBody> transactionItems) {
        return transactionItems
            .collectList()
            .flatMap(transactionsApi::patchTransactions);
    }

    /**
     * Trigger refresh action for transactions.
     *
     * @param arrangementItems Arrangement ids for which to retrieve new transactions
     * @return empty mono on completion
     */
    public Mono<Void> postRefresh(Flux<ArrangementItem> arrangementItems) {
        return arrangementItems
            .collectList()
            .flatMap(transactionsApi::postRefresh);
    }

}

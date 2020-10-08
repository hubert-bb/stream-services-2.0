package com.backbase.stream.transaction;

import com.backbase.dbs.transaction.integration.api.TransactionsApi;
import com.backbase.dbs.transaction.integration.model.SchemasTransactionPost;
import com.backbase.dbs.transaction.integration.model.TransactionPost;
import com.backbase.dbs.transaction.integration.model.TransactionPostResponse;
import com.backbase.stream.worker.StreamTaskExecutor;
import com.backbase.stream.worker.exception.StreamTaskException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Slf4j
public class TransactionTaskExecutor implements StreamTaskExecutor<TransactionTask> {

    private final TransactionsApi transactionsApi;

    public TransactionTaskExecutor(TransactionsApi transactionsApi) {
        this.transactionsApi = transactionsApi;
    }

    @Override
    public Mono<TransactionTask> executeTask(TransactionTask streamTask) {
        List<SchemasTransactionPost> data = streamTask.getData();
        String ids = streamTask.getData().stream().map(SchemasTransactionPost::getId).collect(Collectors.joining(","));
        log.info("Post {} transactions: ", data.size());
        return transactionsApi.postTransactions(data)
            .onErrorResume(WebClientResponseException.class, throwable -> {
                streamTask.error("transactions", "post", "failed", ids, null, throwable, throwable.getResponseBodyAsString(), "Failed to ingest transactions");
                return Mono.error(new StreamTaskException(streamTask, throwable, "Failed to Ingest Transactions: " + throwable.getResponseBodyAsString()));
            })
            .collectList()
            .map(transactionIds -> {
                streamTask.error("transactions", "post", "success", ids, transactionIds.stream().map(TransactionPostResponse::getId).collect(Collectors.joining(",")), "Ingested Transactions");
                streamTask.setResponse(transactionIds);
                return streamTask;
            });
    }

    @Override
    public Mono<TransactionTask> rollBack(TransactionTask streamTask) {
        return Mono.just(streamTask);
    }

}

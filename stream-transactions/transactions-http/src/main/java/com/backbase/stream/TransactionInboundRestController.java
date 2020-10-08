package com.backbase.stream;

import com.backbase.dbs.transaction.integration.controller.api.TransactionsApi;
import com.backbase.dbs.transaction.integration.model.ArrangementItem;
import com.backbase.dbs.transaction.integration.model.SchemasTransactionPost;
import com.backbase.dbs.transaction.integration.model.TransactionPostResponse;
import com.backbase.dbs.transaction.integration.model.TransactionsDeleteRequestBody;
import com.backbase.dbs.transaction.integration.model.TransactionsGet;
import com.backbase.dbs.transaction.integration.model.TransactionsPatchRequestBody;
import com.backbase.stream.transaction.TransactionTask;
import com.backbase.stream.transaction.TransactionUnitOfWorkExecutor;
import com.backbase.stream.transaction.TransactionsQuery;
import com.backbase.stream.worker.model.UnitOfWork;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
@AllArgsConstructor
public class TransactionInboundRestController implements TransactionsApi {

    private final TransactionService transactionService;
    private final TransactionUnitOfWorkExecutor transactionUnitOfWorkExecutor;


    @Override
    public Mono<ResponseEntity<TransactionsGet>> getTransactions(@Valid BigDecimal amountGreaterThan, @Valid BigDecimal amountLessThan, @Valid String bookingDateGreaterThan, @Valid String bookingDateLessThan, @Valid List<String> types, @Valid String description, @Valid String reference, @Valid List<String> typeGroups, @Valid String counterPartyName, @Valid String counterPartyAccountNumber, @Valid String creditDebitIndicator, @Valid String category, @Valid List<String> categories, @Valid String billingStatus, @Valid String currency, @Valid Integer notes, @Valid String id, @Valid String arrangementId, @Valid List<String> arrangementsIds, @Valid BigDecimal fromCheckSerialNumber, @Valid BigDecimal toCheckSerialNumber, @Valid List<BigDecimal> checkSerialNumbers, @Valid String query, @Valid Integer from, @Valid String cursor, @Valid Integer size, @Valid String orderBy, @Valid String direction, @Valid String secDirection, ServerWebExchange exchange) {
        TransactionsQuery transactionsQuery = new TransactionsQuery(amountGreaterThan, amountLessThan,
            bookingDateGreaterThan, bookingDateLessThan, types, description, reference, typeGroups, counterPartyName,
            counterPartyAccountNumber, creditDebitIndicator, category, categories, billingStatus, currency, notes, id,
            arrangementId, arrangementsIds, fromCheckSerialNumber, toCheckSerialNumber, checkSerialNumbers, query, from,
            cursor, size, orderBy, direction, secDirection);
        Mono<com.backbase.dbs.transaction.integration.model.TransactionsGet> transactions = transactionService.getTransactions(transactionsQuery);
        return transactions
            .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Void>> patchTransactions(
        @Valid Flux<TransactionsPatchRequestBody> transactionsPatchRequestBody, ServerWebExchange exchange) {
        return transactionService.patchTransactions(transactionsPatchRequestBody)
            .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Void>> postDelete(
        @Valid Flux<TransactionsDeleteRequestBody> transactionsDeleteRequestBody, ServerWebExchange exchange) {
        return transactionService.deleteTransactions(transactionsDeleteRequestBody)
            .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Void>> postRefresh(@Valid Flux<ArrangementItem> arrangementItem,
                                                  ServerWebExchange exchange) {
        return transactionService.postRefresh(arrangementItem)
            .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Flux<TransactionPostResponse>>> postTransactions(@Valid Flux<SchemasTransactionPost> schemasTransactionPost, ServerWebExchange exchange) {

        Flux<UnitOfWork<TransactionTask>> unitOfWorkFlux = transactionUnitOfWorkExecutor
            .prepareUnitOfWork(schemasTransactionPost);

        Flux<TransactionPostResponse> transactionPostResponseFlux = unitOfWorkFlux
            .flatMap(transactionUnitOfWorkExecutor::executeUnitOfWork)
            .flatMap(unitOfWork -> Flux.fromStream(unitOfWork.getStreamTasks().stream()
                .map(TransactionTask::getResponse)
                .flatMap(Collection::stream)));

        return Mono.just(ResponseEntity.ok(transactionPostResponseFlux));
    }


}

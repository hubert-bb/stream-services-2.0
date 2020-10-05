package com.backbase.stream.limit;

import com.backbase.dbs.limit.integration.api.LimitsApi;
import com.backbase.dbs.limit.integration.model.IngestedLimit;
import com.backbase.stream.worker.StreamTaskExecutor;
import com.backbase.stream.worker.exception.StreamTaskException;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public class LimitsSaga implements StreamTaskExecutor<LimitsTask> {

    public static final String LIMIT = "limit";
    public static final String CREATE = "create";
    public static final String SUCCESS = "success";
    public static final String ERROR = "error";
    public static final String CREATED_SUCCESSFULLY = "Limit created successfully";
    public static final String FAILED_TO_INGEST_LIMITS = "Failed to ingest limits";
    private final LimitsApi limitsApi;

    @Override
    public Mono<LimitsTask> executeTask(LimitsTask limitsTask) {
        IngestedLimit item = limitsTask.getData();


        log.info("Started ingestion of transactions for user {}", item.getUserBBID());
        return limitsApi.putLimits(Collections.singletonList(item))
                .map(limitIngestionReport -> {
                    limitsTask.setResponse(limitIngestionReport);
                    limitsTask.info(LIMIT, CREATE, SUCCESS, item.getUserBBID(), limitIngestionReport.getIngestionStats().toString(), CREATED_SUCCESSFULLY);
                    return limitsTask;
                })
                .onErrorResume(throwable -> {
                    limitsTask.error(LIMIT, CREATE, ERROR, item.getUserBBID(), null, throwable, "Failed to ingest limit " + throwable.getMessage(), FAILED_TO_INGEST_LIMITS);
                    return Mono.error(new StreamTaskException(limitsTask, throwable, FAILED_TO_INGEST_LIMITS));
                });

    }

    @Override
    public Mono<LimitsTask> rollBack(LimitsTask limitsTask) {
        return null;
    }
}

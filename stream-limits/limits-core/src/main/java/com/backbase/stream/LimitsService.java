package com.backbase.stream;

import com.backbase.dbs.limit.integration.api.LimitsApi;
import com.backbase.dbs.limit.integration.model.IngestedLimit;
import com.backbase.dbs.limit.integration.model.LimitIngestionReport;
import com.backbase.dbs.user.integration.api.UsersApi;
import com.backbase.stream.limit.LimitsSaga;
import com.backbase.stream.limit.LimitsTask;
import com.backbase.stream.limit.LimitsUnitOfWorkExecutor;
import com.backbase.stream.worker.model.UnitOfWork;
import reactor.core.publisher.Flux;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class LimitsService {

    private LimitsSaga limitsSaga;
    private LimitsApi limitsApi;
    private LimitsUnitOfWorkExecutor limitsUnitOfWorkExecutor;

    public LimitsService(
            LimitsSaga limitsSaga,
            LimitsApi limitsApi,
            UsersApi usersApi,
            LimitsUnitOfWorkExecutor limitsUnitOfWorkExecutor
    ) {
        this.limitsSaga = limitsSaga;
        this.limitsApi = limitsApi;
        this.limitsUnitOfWorkExecutor = limitsUnitOfWorkExecutor;
    }

    public Flux<LimitIngestionReport> createUserLimits(Flux<IngestedLimit> items) {
        Flux<UnitOfWork<LimitsTask>> unitOfWorkFlux = limitsUnitOfWorkExecutor.prepareUnitOfWork(items);
        return unitOfWorkFlux.flatMap(limitsUnitOfWorkExecutor::executeUnitOfWork).flatMap(limitsTaskUnitOfWork -> {
            Stream<LimitIngestionReport> stream = limitsTaskUnitOfWork.getStreamTasks().stream().map(LimitsTask::getResponse);
            return Flux.fromStream(stream);
        });
    }

    private boolean isUUID(String input) {
        Pattern pattern = Pattern.compile("/^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/");
        return pattern.matcher(input).matches();
    }
}

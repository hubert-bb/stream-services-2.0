package com.backbase.stream.limit;

import com.backbase.dbs.limit.integration.model.IngestedLimit;
import com.backbase.dbs.limit.integration.model.LimitIngestionReport;
import com.backbase.stream.worker.model.StreamTask;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class LimitsTask extends StreamTask {

    public LimitsTask(String unitOfWorkId, IngestedLimit data) {
        super(unitOfWorkId);
        this.data = data;
    }

    private IngestedLimit data;
    private LimitIngestionReport response;

    @Override
    public String getName() {
        return "limit";
    }
}

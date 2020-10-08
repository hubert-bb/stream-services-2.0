package com.backbase.stream.transaction;

import com.backbase.dbs.transaction.integration.model.SchemasTransactionPost;
import com.backbase.dbs.transaction.integration.model.TransactionPost;
import com.backbase.dbs.transaction.integration.model.TransactionPostResponse;
import com.backbase.stream.worker.model.StreamTask;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TransactionTask extends StreamTask {

    public TransactionTask(String unitOfWorkId, List<SchemasTransactionPost> data) {
        super(unitOfWorkId);
        this.data = data;
    }

    private List<SchemasTransactionPost> data;
    private List<TransactionPostResponse> response;

    @Override
    public String getName() {
        return "transaction";
    }
}

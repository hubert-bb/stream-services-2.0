package com.backbase.stream.bootstrap.configuration;

import com.backbase.dbs.limit.service.model.CreateLimitRequest;
import com.backbase.stream.legalentity.model.LegalEntity;
import com.backbase.stream.productcatalog.model.ProductCatalog;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bootstrap", ignoreInvalidFields = true)
@Data
@NoArgsConstructor
public class BootstrapTaskConfiguration {

    /**
     * The Root Legal Entity including Subsidiaries
     */
    private LegalEntity legalEntity;

    /**
     * The Product Catalog to setup
     */
    private ProductCatalog productCatalog;

    private CreateLimitRequest globalUser;

    private CreateLimitRequest globalLegalEntity;

    private CreateLimitRequest globalServiceAgreement;

}


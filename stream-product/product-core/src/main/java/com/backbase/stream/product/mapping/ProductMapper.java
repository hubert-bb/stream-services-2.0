package com.backbase.stream.product.mapping;

import com.backbase.dbs.arrangement.integration.model.PostArrangement;
import com.backbase.dbs.arrangement.integration.model.PutArrangement;
import com.backbase.dbs.arrangement.integration.model.Unit;
import com.backbase.stream.legalentity.model.AvailableBalance;
import com.backbase.stream.legalentity.model.BaseProduct;
import com.backbase.stream.legalentity.model.BookedBalance;
import com.backbase.stream.legalentity.model.CreditCard;
import com.backbase.stream.legalentity.model.CreditLimit;
import com.backbase.stream.legalentity.model.CurrentAccount;
import com.backbase.stream.legalentity.model.DebitCard;
import com.backbase.stream.legalentity.model.InterestPaymentFrequencyUnit;
import com.backbase.stream.legalentity.model.InvestmentAccount;
import com.backbase.stream.legalentity.model.LegalEntity;
import com.backbase.stream.legalentity.model.LegalEntityReference;
import com.backbase.stream.legalentity.model.Loan;
import com.backbase.stream.legalentity.model.PrincipalAmount;
import com.backbase.stream.legalentity.model.Product;
import com.backbase.stream.legalentity.model.SavingsAccount;
import com.backbase.stream.legalentity.model.TermDeposit;
import com.backbase.stream.legalentity.model.TermUnit;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.ValueMapping;
import org.mapstruct.ValueMappings;
import org.springframework.util.StringUtils;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
@SuppressWarnings({"squid:S1710"})
public interface ProductMapper {


    @Mapping(source = ProductMapperConstants.EXTERNAL_ID, target = ProductMapperConstants.EXTERNAL_ARRANGEMENT_ID)
    @Mapping(source = ProductMapperConstants.PRODUCT_TYPE_EXTERNAL_ID, target = ProductMapperConstants.EXTERNAL_PRODUCT_ID)
    @Mapping(source = ProductMapperConstants.LEGAL_ENTITIES, target = ProductMapperConstants.EXTERNAL_LEGAL_ENTITY_IDS)
    @Mapping(source = "state.externalStateId", target = ProductMapperConstants.EXTERNAL_STATE_ID)
    PostArrangement toIntegration(Product product);


    @Mapping(source = ProductMapperConstants.EXTERNAL_ID, target = ProductMapperConstants.EXTERNAL_ARRANGEMENT_ID)
    @Mapping(source = ProductMapperConstants.PRODUCT_TYPE_EXTERNAL_ID, target = ProductMapperConstants.EXTERNAL_PRODUCT_ID)
    @Mapping(source = ProductMapperConstants.LEGAL_ENTITIES, target = ProductMapperConstants.EXTERNAL_LEGAL_ENTITY_IDS)
    @Mapping(source = "state.externalStateId", target = ProductMapperConstants.EXTERNAL_STATE_ID)
    PostArrangement toIntegration(BaseProduct product);


    @Mapping(source = ProductMapperConstants.EXTERNAL_ID, target = ProductMapperConstants.EXTERNAL_ARRANGEMENT_ID)
    @Mapping(source = ProductMapperConstants.PRODUCT_TYPE_EXTERNAL_ID, target = ProductMapperConstants.EXTERNAL_PRODUCT_ID)
    @Mapping(source = ProductMapperConstants.LEGAL_ENTITIES, target = ProductMapperConstants.EXTERNAL_LEGAL_ENTITY_IDS)
    @InheritConfiguration
    @Mapping(source = "debitCardsItems", target = "debitCards")
    PostArrangement toIntegration(CurrentAccount currentAccount);


    @Mapping(source = ProductMapperConstants.EXTERNAL_ID, target = ProductMapperConstants.EXTERNAL_ARRANGEMENT_ID)
    @Mapping(source = ProductMapperConstants.PRODUCT_TYPE_EXTERNAL_ID, target = ProductMapperConstants.EXTERNAL_PRODUCT_ID)
    @Mapping(source = ProductMapperConstants.LEGAL_ENTITIES, target = ProductMapperConstants.EXTERNAL_LEGAL_ENTITY_IDS)
    @InheritConfiguration
    PostArrangement toIntegration(SavingsAccount savingsAccount);


    @Mapping(source = ProductMapperConstants.EXTERNAL_ID, target = ProductMapperConstants.EXTERNAL_ARRANGEMENT_ID)
    @Mapping(source = ProductMapperConstants.PRODUCT_TYPE_EXTERNAL_ID, target = ProductMapperConstants.EXTERNAL_PRODUCT_ID)
    @Mapping(source = ProductMapperConstants.LEGAL_ENTITIES, target = ProductMapperConstants.EXTERNAL_LEGAL_ENTITY_IDS)
    @InheritConfiguration
    PostArrangement toIntegration(DebitCard debitCard);


    @Mapping(source = ProductMapperConstants.EXTERNAL_ID, target = ProductMapperConstants.EXTERNAL_ARRANGEMENT_ID)
    @Mapping(source = ProductMapperConstants.PRODUCT_TYPE_EXTERNAL_ID, target = ProductMapperConstants.EXTERNAL_PRODUCT_ID)
    @Mapping(source = ProductMapperConstants.LEGAL_ENTITIES, target = ProductMapperConstants.EXTERNAL_LEGAL_ENTITY_IDS)
    @InheritConfiguration
    PostArrangement toIntegration(CreditCard creditCard);

    @Mapping(source = ProductMapperConstants.EXTERNAL_ID, target = ProductMapperConstants.EXTERNAL_ARRANGEMENT_ID)
    @Mapping(source = ProductMapperConstants.PRODUCT_TYPE_EXTERNAL_ID, target = ProductMapperConstants.EXTERNAL_PRODUCT_ID)
    @Mapping(source = ProductMapperConstants.LEGAL_ENTITIES, target = ProductMapperConstants.EXTERNAL_LEGAL_ENTITY_IDS)
    @InheritConfiguration
    PostArrangement toIntegration(TermDeposit termDeposit);

    @Mapping(source = ProductMapperConstants.EXTERNAL_ID, target = ProductMapperConstants.EXTERNAL_ARRANGEMENT_ID)
    @Mapping(source = ProductMapperConstants.PRODUCT_TYPE_EXTERNAL_ID, target = ProductMapperConstants.EXTERNAL_PRODUCT_ID)
    @Mapping(source = ProductMapperConstants.LEGAL_ENTITIES, target = ProductMapperConstants.EXTERNAL_LEGAL_ENTITY_IDS)
    @InheritConfiguration
    PostArrangement toIntegration(InvestmentAccount investmentAccount);


    @Mapping(source = ProductMapperConstants.EXTERNAL_ID, target = ProductMapperConstants.EXTERNAL_ARRANGEMENT_ID)
    @Mapping(source = ProductMapperConstants.PRODUCT_TYPE_EXTERNAL_ID, target = ProductMapperConstants.EXTERNAL_PRODUCT_ID)
    @Mapping(source = ProductMapperConstants.LEGAL_ENTITIES, target = ProductMapperConstants.EXTERNAL_LEGAL_ENTITY_IDS)
    @InheritConfiguration
    PostArrangement toIntegration(Loan loan);


    @Mapping(source = ProductMapperConstants.EXTERNAL_ARRANGEMENT_ID, target = ProductMapperConstants.EXTERNAL_ID)
    @Mapping(source = ProductMapperConstants.EXTERNAL_PRODUCT_ID, target = ProductMapperConstants.PRODUCT_TYPE_EXTERNAL_ID)
    @Mapping(source = ProductMapperConstants.LEGAL_ENTITY_IDS, target = ProductMapperConstants.LEGAL_ENTITIES)
    @Mapping(source = ProductMapperConstants.ID, target = ProductMapperConstants.INTERNAL_ID)
    @InheritConfiguration
    Product mapCustomProduct(PostArrangement arrangementItem);

    @Mapping(source = ProductMapperConstants.EXTERNAL_ARRANGEMENT_ID, target = ProductMapperConstants.EXTERNAL_ID)
    @Mapping(source = ProductMapperConstants.EXTERNAL_PRODUCT_ID, target = ProductMapperConstants.PRODUCT_TYPE_EXTERNAL_ID)
    @Mapping(source = ProductMapperConstants.LEGAL_ENTITY_IDS, target = ProductMapperConstants.LEGAL_ENTITIES)
    @Mapping(source = ProductMapperConstants.ID, target = ProductMapperConstants.INTERNAL_ID)
    CurrentAccount mapCurrentAccount(PostArrangement product);

    @Mapping(source = ProductMapperConstants.EXTERNAL_ARRANGEMENT_ID, target = ProductMapperConstants.EXTERNAL_ID)
    @Mapping(source = ProductMapperConstants.EXTERNAL_PRODUCT_ID, target = ProductMapperConstants.PRODUCT_TYPE_EXTERNAL_ID)
    @Mapping(source = ProductMapperConstants.LEGAL_ENTITY_IDS, target = ProductMapperConstants.LEGAL_ENTITIES)
    @Mapping(source = ProductMapperConstants.ID, target = ProductMapperConstants.INTERNAL_ID)
    SavingsAccount mapSavingAccount(PostArrangement product);

    @Mapping(source = ProductMapperConstants.EXTERNAL_ARRANGEMENT_ID, target = ProductMapperConstants.EXTERNAL_ID)
    @Mapping(source = ProductMapperConstants.EXTERNAL_PRODUCT_ID, target = ProductMapperConstants.PRODUCT_TYPE_EXTERNAL_ID)
    @Mapping(source = ProductMapperConstants.LEGAL_ENTITY_IDS, target = ProductMapperConstants.LEGAL_ENTITIES)
    @Mapping(source = ProductMapperConstants.ID, target = ProductMapperConstants.INTERNAL_ID)
    DebitCard mapDebitCard(PostArrangement product);

    @Mapping(source = ProductMapperConstants.EXTERNAL_ARRANGEMENT_ID, target = ProductMapperConstants.EXTERNAL_ID)
    @Mapping(source = ProductMapperConstants.EXTERNAL_PRODUCT_ID, target = ProductMapperConstants.PRODUCT_TYPE_EXTERNAL_ID)
    @Mapping(source = ProductMapperConstants.LEGAL_ENTITY_IDS, target = ProductMapperConstants.LEGAL_ENTITIES)
    @Mapping(source = ProductMapperConstants.ID, target = ProductMapperConstants.INTERNAL_ID)
    CreditCard mapCreditCard(PostArrangement product);

    @Mapping(source = ProductMapperConstants.EXTERNAL_ARRANGEMENT_ID, target = ProductMapperConstants.EXTERNAL_ID)
    @Mapping(source = ProductMapperConstants.EXTERNAL_PRODUCT_ID, target = ProductMapperConstants.PRODUCT_TYPE_EXTERNAL_ID)
    @Mapping(source = ProductMapperConstants.LEGAL_ENTITY_IDS, target = ProductMapperConstants.LEGAL_ENTITIES)
    @Mapping(source = ProductMapperConstants.ID, target = ProductMapperConstants.INTERNAL_ID)
    Loan mapLoan(PostArrangement product);

    @Mapping(source = ProductMapperConstants.EXTERNAL_ARRANGEMENT_ID, target = ProductMapperConstants.EXTERNAL_ID)
    @Mapping(source = ProductMapperConstants.EXTERNAL_PRODUCT_ID, target = ProductMapperConstants.PRODUCT_TYPE_EXTERNAL_ID)
    @Mapping(source = ProductMapperConstants.LEGAL_ENTITY_IDS, target = ProductMapperConstants.LEGAL_ENTITIES)
    @Mapping(source = ProductMapperConstants.ID, target = ProductMapperConstants.INTERNAL_ID)
    TermDeposit mapTermDeposit(PostArrangement product);

    @Mapping(source = ProductMapperConstants.EXTERNAL_ARRANGEMENT_ID, target = ProductMapperConstants.EXTERNAL_ID)
    @Mapping(source = ProductMapperConstants.EXTERNAL_PRODUCT_ID, target = ProductMapperConstants.PRODUCT_TYPE_EXTERNAL_ID)
    @Mapping(source = ProductMapperConstants.LEGAL_ENTITY_IDS, target = ProductMapperConstants.LEGAL_ENTITIES)
    @Mapping(source = ProductMapperConstants.ID, target = ProductMapperConstants.INTERNAL_ID)
    InvestmentAccount mapInvestmentAccount(PostArrangement product);


    default BookedBalance mapBookedBalance(BigDecimal bigDecimal) {
        return new BookedBalance().amount(bigDecimal);
    }

    default AvailableBalance mapAvailable(BigDecimal bigDecimal) {
        return new AvailableBalance().amount(bigDecimal);
    }

    default PrincipalAmount mapPrincipal(BigDecimal bigDecimal) {
        return new PrincipalAmount().amount(bigDecimal);
    }

    default CreditLimit mapCreditLimit(BigDecimal bigDecimal) {
        return new CreditLimit().amount(bigDecimal);
    }

    default LegalEntity mapLegalEntity(String legalEntityId) {
        return new LegalEntity().externalId(legalEntityId);
    }

    default OffsetDateTime map(String s) {
        if (StringUtils.isEmpty(s)) {
            return null;
        } else {
            try {
                return OffsetDateTime.parse(s, ProductMapperConstants.formatter);
            } catch (java.time.format.DateTimeParseException e) {
                return OffsetDateTime.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm'Z'"));
            }
        }
    }

    /**
     * Convert Object to BigDecimal.
     *
     * @param value wrapped value
     * @return BigDecimal
     */
    default BigDecimal map(BookedBalance value) {
        if (value != null) {
            return value.getAmount();
        } else {
            return null;
        }
    }

    /**
     * Convert Object to BigDecimal.
     *
     * @param value wrapped value
     * @return BigDecimal
     */
    default BigDecimal map(AvailableBalance value) {
        if (value != null) {
            return value.getAmount();
        } else {
            return null;
        }
    }

    /**
     * Convert Object to BigDecimal.
     *
     * @param value wrapped value
     * @return BigDecimal
     */
    default BigDecimal map(PrincipalAmount value) {
        if (value != null) {
            return value.getAmount();
        } else {
            return null;
        }
    }

    /**
     * Convert Object to BigDecimal.
     *
     * @param value wrapped value
     * @return BigDecimal
     */
    default BigDecimal map(CreditLimit value) {
        if (value != null) {
            return value.getAmount();
        } else {
            return null;
        }
    }

    /**
     * Convert Object to BigDecimal.
     *
     * @param offsetDateTime wrapped value
     * @return BigDecimal
     */
    default String map(OffsetDateTime offsetDateTime) {
        if (offsetDateTime != null) {
            return offsetDateTime.toString();
        } else {
            return null;
        }
    }

    @ValueMappings({
        @ValueMapping(source = "QUARTERLY", target = MappingConstants.NULL),
        @ValueMapping(source = ProductMapperConstants.DAILY, target = ProductMapperConstants.D),
        @ValueMapping(source = ProductMapperConstants.WEEKLY, target = ProductMapperConstants.W),
        @ValueMapping(source = ProductMapperConstants.MONTHLY, target = ProductMapperConstants.M),
        @ValueMapping(source = ProductMapperConstants.YEARLY, target = ProductMapperConstants.Y)
    })
    Unit map(TermUnit termUnit);

    @ValueMappings({
        @ValueMapping(source = "QUARTERLY", target = MappingConstants.NULL),
        @ValueMapping(source = ProductMapperConstants.DAILY, target = ProductMapperConstants.D),
        @ValueMapping(source = ProductMapperConstants.WEEKLY, target = ProductMapperConstants.W),
        @ValueMapping(source = ProductMapperConstants.MONTHLY, target = ProductMapperConstants.M),
        @ValueMapping(source = ProductMapperConstants.YEARLY, target = ProductMapperConstants.Y)
    })
    Unit map(InterestPaymentFrequencyUnit interestPaymentFrequencyUnit);

    @ValueMappings({
        @ValueMapping(source = ProductMapperConstants.D, target = ProductMapperConstants.DAILY),
        @ValueMapping(source = ProductMapperConstants.W, target = ProductMapperConstants.WEEKLY),
        @ValueMapping(source = ProductMapperConstants.M, target = ProductMapperConstants.MONTHLY),
        @ValueMapping(source = ProductMapperConstants.Y, target = ProductMapperConstants.YEARLY)
    })
    TermUnit map(Unit unit);

    default java.util.List<java.lang.String> mapLegalEntityId(java.util.List<com.backbase.stream.legalentity.model.LegalEntityReference> value) {
        if (value != null) {
            return value.stream().map(LegalEntityReference::getExternalId).collect(Collectors.toList());
        } else {
            return null;
        }
    }


    default List<LegalEntityReference> mapLegalEntityReference(List<String> value) {
        if (value != null) {
            return value.stream().map(id -> new LegalEntityReference().externalId(id)).collect(Collectors.toList());
        } else {
            return null;
        }
    }


    @ValueMappings({
        @ValueMapping(source = ProductMapperConstants.D, target = ProductMapperConstants.DAILY),
        @ValueMapping(source = ProductMapperConstants.W, target = ProductMapperConstants.WEEKLY),
        @ValueMapping(source = ProductMapperConstants.M, target = ProductMapperConstants.MONTHLY),
        @ValueMapping(source = ProductMapperConstants.Y, target = ProductMapperConstants.YEARLY)
    })
    InterestPaymentFrequencyUnit mapInterestPayment(Unit unit);


    PutArrangement toPutArrangement(PostArrangement arrangementItemPost);
}

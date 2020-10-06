package com.backbase.stream.product.mapping;

import java.time.format.DateTimeFormatter;
import lombok.experimental.UtilityClass;

@UtilityClass
class ProductMapperConstants {

    private static final String YYYY_MM_DD_T_HH_MM_SS_SSSXX = "yyyy-MM-dd'T'HH:mm:ss.SSSXX";
    static final DateTimeFormatter formatter =
        DateTimeFormatter.ofPattern(ProductMapperConstants.YYYY_MM_DD_T_HH_MM_SS_SSSXX);
    static final String DAILY = "DAILY";
    static final String WEEKLY = "WEEKLY";
    static final String MONTHLY = "MONTHLY";
    static final String YEARLY = "YEARLY";
    static final String D = "D";
    static final String W = "W";
    static final String M = "M";
    static final String Y = "Y";
    static final String LEGAL_ENTITIES = "legalEntities";
    static final String LEGAL_ENTITY_IDS = "legalEntityIds";
    static final String ID = "id";

}

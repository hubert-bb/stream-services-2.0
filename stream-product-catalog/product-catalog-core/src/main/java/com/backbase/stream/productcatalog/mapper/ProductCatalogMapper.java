package com.backbase.stream.productcatalog.mapper;

import com.backbase.dbs.accounts.presentation.service.model.*;
import com.backbase.dbs.arrangement.integration.model.IntegrationProductKindItem;
import com.backbase.dbs.arrangement.integration.model.NewProductKindItem;
import com.backbase.dbs.arrangement.integration.model.NewProductKindItemPut;
import com.backbase.dbs.arrangement.integration.model.ProductItem;
import com.backbase.dbs.arrangement.integration.model.ProductItemResponseBody;
import com.backbase.stream.legalentity.model.ProductKind;
import com.backbase.stream.legalentity.model.ProductType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface ProductCatalogMapper {

    ProductItem toIntegration(ProductType source);

    ProductItem toPresentation(ProductType source);

    ProductType toStream(ProductItemResponseBody source);

    ProductKind toStream(NewProductKindItem source);

    ProductKind toStream(NewProductKindItemPut source);

    ProductKind toStream(IntegrationProductKindItem source);

    NewProductKindItemPut toIntegrationPut(ProductKind source);

    NewProductKindItem toIntegration(ProductKind source);
}

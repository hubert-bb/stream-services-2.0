package com.backbase.stream.mapper;

import com.backbase.dbs.user.integration.model.UserItem;
import com.backbase.dbs.user.integration.model.UserItemGet;
import com.backbase.dbs.user.presentation.service.model.CreateUser;
import com.backbase.dbs.user.presentation.service.model.GetUserById;
import com.backbase.stream.legalentity.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface UserMapper {


    User toStream(UserItemGet userItem);

    @Mapping(source = "legalEntityId", target = "legalEntityExternalId")
    UserItem toPresentation(User user);
}

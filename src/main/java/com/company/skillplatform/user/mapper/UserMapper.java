package com.company.skillplatform.user.mapper;

import com.company.skillplatform.user.dto.MyProfileResponse;
import com.company.skillplatform.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "skills", ignore = true)
    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    MyProfileResponse toMyProfileResponse(User user);
}


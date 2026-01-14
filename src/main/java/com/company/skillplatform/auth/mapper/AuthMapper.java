package com.company.skillplatform.auth.mapper;

import com.company.skillplatform.auth.dto.RegisterResponse;
import com.company.skillplatform.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    RegisterResponse toAuthResponse(User user, String roleName);
}


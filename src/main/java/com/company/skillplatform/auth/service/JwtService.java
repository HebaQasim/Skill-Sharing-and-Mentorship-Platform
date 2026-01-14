package com.company.skillplatform.auth.service;

import io.jsonwebtoken.Claims;

import java.util.Set;
import java.util.UUID;

public interface JwtService {

    String generate(UUID userId, String email, Set<String> roles);

    Claims parseClaims(String token);

}
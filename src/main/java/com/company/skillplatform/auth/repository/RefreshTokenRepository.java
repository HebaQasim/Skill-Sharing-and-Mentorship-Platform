package com.company.skillplatform.auth.repository;

import com.company.skillplatform.auth.entity.RefreshToken;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<RefreshToken> findByHashedTokenAndDeviceId(String hashedToken, String deviceId);




    @Modifying
    @Query("update RefreshToken t set t.revoked = true where t.user.id = :userId and t.deviceId = :deviceId")
    void revokeByUserAndDevice(UUID userId, String deviceId);

    @Modifying
    @Query("update RefreshToken t set t.revoked = true where t.user.id = :userId")
    void revokeAllByUser(UUID userId);
}

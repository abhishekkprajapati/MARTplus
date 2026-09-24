package com.ShopSphere.repository;

import com.ShopSphere.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {

    Optional<OtpVerification> findTopByTargetAndPurposeOrderByIdDesc(String target, String purpose);

    Optional<OtpVerification> findByChallengeId(String challengeId);

    Optional<OtpVerification> findByChallengeIdAndPurpose(String challengeId, String purpose);

    long countByTargetAndCreatedAtAfter(String target, LocalDateTime after);
}

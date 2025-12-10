package com.example.ecompoc.giftcard.repository;

import com.example.ecompoc.giftcard.model.GiftCard;
import com.example.ecompoc.giftcard.model.GiftCardStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * JPA repository for GiftCard entities
 */
@Repository
public interface GiftCardRepository extends JpaRepository<GiftCard, String> {
    
    /**
     * Find gift card by code
     */
    Optional<GiftCard> findByCode(String code);
    
    /**
     * Find gift card by code with pessimistic lock (for concurrent redemption)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT g FROM GiftCard g WHERE g.code = :code")
    Optional<GiftCard> findByCodeWithLock(@Param("code") String code);
    
    /**
     * Find all gift cards for a purchaser
     */
    List<GiftCard> findByPurchaserId(String purchaserId);
    
    /**
     * Find all gift cards by purchaser email
     */
    List<GiftCard> findByPurchaserEmail(String purchaserEmail);
    
    /**
     * Find all gift cards by recipient email
     */
    List<GiftCard> findByRecipientEmail(String recipientEmail);
    
    /**
     * Find gift cards by status
     */
    List<GiftCard> findByStatus(GiftCardStatus status);
    
    /**
     * Find active gift cards for a user
     */
    List<GiftCard> findByPurchaserIdAndStatus(String purchaserId, GiftCardStatus status);
    
    /**
     * Find gift cards expiring soon (for expiration warnings)
     */
    @Query("SELECT g FROM GiftCard g WHERE g.status = :status AND g.expirationDate BETWEEN :startDate AND :endDate")
    List<GiftCard> findExpiringSoon(@Param("status") GiftCardStatus status, 
                                    @Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find expired gift cards that haven't been updated yet
     */
    @Query("SELECT g FROM GiftCard g WHERE g.status = :status AND g.expirationDate < :now")
    List<GiftCard> findExpiredCards(@Param("status") GiftCardStatus status, 
                                    @Param("now") LocalDateTime now);
    
    /**
     * Check if code exists
     */
    boolean existsByCode(String code);
}

package com.example.ecompoc.giftcard.dto;

import com.example.ecompoc.giftcard.model.GiftCard;
import com.example.ecompoc.giftcard.model.GiftCardTransaction;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper utility for converting between GiftCard entities and DTOs
 */
@Component
public class GiftCardMapper {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    /**
     * Convert GiftCard entity to GiftCardResponse DTO
     */
    public GiftCardResponse toResponse(GiftCard giftCard) {
        if (giftCard == null) {
            return null;
        }
        
        GiftCardResponse response = new GiftCardResponse();
        response.setGiftCardId(giftCard.getGiftCardId());
        response.setCode(giftCard.getCode());
        response.setAmount(giftCard.getAmount());
        response.setBalance(giftCard.getBalance());
        response.setStatus(giftCard.getStatus() != null ? giftCard.getStatus().name() : null);
        response.setPurchaserId(giftCard.getPurchaserId());
        response.setPurchaserEmail(giftCard.getPurchaserEmail());
        response.setRecipientEmail(giftCard.getRecipientEmail());
        response.setRecipientName(giftCard.getRecipientName());
        response.setPersonalMessage(giftCard.getPersonalMessage());
        response.setDesign(giftCard.getDesign());
        response.setPurchaseDate(giftCard.getPurchaseDate() != null ? 
            giftCard.getPurchaseDate().format(DATE_FORMATTER) : null);
        response.setExpirationDate(giftCard.getExpirationDate() != null ? 
            giftCard.getExpirationDate().format(DATE_FORMATTER) : null);
        response.setScheduledDeliveryDate(giftCard.getScheduledDeliveryDate() != null ? 
            giftCard.getScheduledDeliveryDate().format(DATE_FORMATTER) : null);
        
        return response;
    }
    
    /**
     * Convert list of GiftCard entities to list of GiftCardResponse DTOs
     */
    public List<GiftCardResponse> toResponseList(List<GiftCard> giftCards) {
        return giftCards.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Convert GiftCardTransaction entity to GiftCardTransactionResponse DTO
     */
    public GiftCardTransactionResponse toTransactionResponse(GiftCardTransaction transaction) {
        if (transaction == null) {
            return null;
        }
        
        GiftCardTransactionResponse response = new GiftCardTransactionResponse();
        response.setTransactionId(transaction.getTransactionId());
        response.setGiftCardId(transaction.getGiftCardId());
        response.setTransactionType(transaction.getTransactionType() != null ? 
            transaction.getTransactionType().name() : null);
        response.setAmount(transaction.getAmount());
        response.setOrderId(transaction.getOrderId());
        response.setDescription(transaction.getDescription());
        response.setCreatedAt(transaction.getCreatedAt() != null ? 
            transaction.getCreatedAt().format(DATE_FORMATTER) : null);
        
        return response;
    }
    
    /**
     * Convert list of GiftCardTransaction entities to list of GiftCardTransactionResponse DTOs
     */
    public List<GiftCardTransactionResponse> toTransactionResponseList(List<GiftCardTransaction> transactions) {
        return transactions.stream()
            .map(this::toTransactionResponse)
            .collect(Collectors.toList());
    }
}

package com.example.ecompoc.loyalty.scheduler;

import com.example.ecompoc.loyalty.model.*;
import com.example.ecompoc.loyalty.repository.LoyaltyAccountRepository;
import com.example.ecompoc.loyalty.repository.LoyaltyTransactionRepository;
import com.example.ecompoc.loyalty.service.LoyaltyEmailService;
import com.example.ecompoc.loyalty.service.LoyaltyExpirationService;
import com.example.ecompoc.loyalty.service.LoyaltyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Scheduled job for processing loyalty point expirations and birthday/anniversary bonuses
 */
@Component
public class LoyaltyExpirationScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(LoyaltyExpirationScheduler.class);
    
    private final LoyaltyTransactionRepository transactionRepository;
    private final LoyaltyAccountRepository accountRepository;
    private final LoyaltyExpirationService expirationService;
    private final LoyaltyEmailService emailService;
    private final LoyaltyService loyaltyService;
    
    @Value("${loyalty.expiration.warning-days:30,7,1}")
    private List<Integer> warningDays;
    
    public LoyaltyExpirationScheduler(LoyaltyTransactionRepository transactionRepository,
                                     LoyaltyAccountRepository accountRepository,
                                     LoyaltyExpirationService expirationService,
                                     LoyaltyEmailService emailService,
                                     LoyaltyService loyaltyService) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.expirationService = expirationService;
        this.emailService = emailService;
        this.loyaltyService = loyaltyService;
    }
    
    /**
     * Process expiring points and send warnings
     * Runs daily at midnight
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void processExpirations() {
        logger.info("Starting loyalty point expiration processing");
        
        LocalDateTime now = LocalDateTime.now();
        
        // Process expired points (FIFO)
        List<LoyaltyTransaction> expiredTransactions = transactionRepository
            .findExpiringTransactions(now);
        
        // Group by account and process FIFO
        expiredTransactions.stream()
            .collect(java.util.stream.Collectors.groupingBy(LoyaltyTransaction::getAccountId))
            .forEach((accountId, transactions) -> {
                // Sort by creation date (FIFO)
                transactions.sort((t1, t2) -> t1.getCreatedAt().compareTo(t2.getCreatedAt()));
                
                LoyaltyAccount account = accountRepository.findById(accountId)
                    .orElse(null);
                
                if (account == null) {
                    return;
                }
                
                int totalExpired = 0;
                for (LoyaltyTransaction transaction : transactions) {
                    if (transaction.getExpirationDate() != null && 
                        transaction.getExpirationDate().isBefore(now) &&
                        transaction.getTransactionType() == TransactionType.EARNED &&
                        transaction.getPoints() > 0) {
                        
                        // Create expiration transaction
                        String expirationId = UUID.randomUUID().toString();
                        LoyaltyTransaction expirationTransaction = new LoyaltyTransaction(
                            expirationId,
                            accountId,
                            account.getUserId(),
                            TransactionType.EXPIRED,
                            -transaction.getPoints(),
                            ActivityType.EXPIRED
                        );
                        expirationTransaction.setDescription(
                            String.format("Points expired: %d points", transaction.getPoints()));
                        
                        totalExpired += transaction.getPoints();
                        
                        // Update account
                        account.setCurrentPoints(Math.max(0, account.getCurrentPoints() - transaction.getPoints()));
                        account.setUpdatedAt(now);
                        
                        transactionRepository.save(expirationTransaction);
                    }
                }
                
                if (totalExpired > 0) {
                    accountRepository.save(account);
                    emailService.sendPointExpiredEmail(account.getUserId(), totalExpired);
                    logger.info("Expired {} points for account {}", totalExpired, accountId);
                }
            });
        
        // Send expiration warnings
        for (Integer days : warningDays) {
            LocalDateTime warningDate = now.plusDays(days);
            List<LoyaltyTransaction> expiring = transactionRepository.findExpiringTransactions(warningDate);
            
            expiring.stream()
                .collect(java.util.stream.Collectors.groupingBy(LoyaltyTransaction::getAccountId))
                .forEach((accountId, transactions) -> {
                    LoyaltyAccount account = accountRepository.findById(accountId)
                        .orElse(null);
                    
                    if (account == null) {
                        return;
                    }
                    
                    int totalExpiring = transactions.stream()
                        .mapToInt(LoyaltyTransaction::getPoints)
                        .sum();
                    
                    LocalDateTime earliestExpiration = transactions.stream()
                        .map(LoyaltyTransaction::getExpirationDate)
                        .filter(d -> d != null)
                        .min(LocalDateTime::compareTo)
                        .orElse(null);
                    
                    if (earliestExpiration != null) {
                        emailService.sendPointExpirationWarning(account.getUserId(), totalExpiring, earliestExpiration);
                        logger.debug("Sent {} day expiration warning to account {}", days, accountId);
                    }
                });
        }
        
        logger.info("Completed loyalty point expiration processing");
    }
    
    /**
     * Process birthday and anniversary bonuses
     * Runs daily at 1 AM
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void processBirthdayAnniversaryBonuses() {
        logger.info("Starting birthday and anniversary bonus processing");
        
        // This would check user birthdays and enrollment anniversaries
        // For now, this is a placeholder - would need user birthdate field
        // Implementation would check if today matches user's birthday month
        // and if enrollment anniversary matches today
        
        logger.info("Completed birthday and anniversary bonus processing");
    }
}

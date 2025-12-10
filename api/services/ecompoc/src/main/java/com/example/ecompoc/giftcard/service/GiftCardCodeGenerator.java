package com.example.ecompoc.giftcard.service;

import com.example.ecompoc.giftcard.repository.GiftCardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

/**
 * Service for generating unique gift card codes
 */
@Service
public class GiftCardCodeGenerator {
    
    private static final Logger logger = LoggerFactory.getLogger(GiftCardCodeGenerator.class);
    
    // Characters to use (avoiding confusing characters: 0, O, 1, I)
    private static final String CHARACTERS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final int CODE_LENGTH = 16;
    private static final int MAX_RETRIES = 10;
    
    private final GiftCardRepository giftCardRepository;
    private final SecureRandom random;
    
    @Autowired
    public GiftCardCodeGenerator(GiftCardRepository giftCardRepository) {
        this.giftCardRepository = giftCardRepository;
        this.random = new SecureRandom();
    }
    
    /**
     * Generate a unique gift card code
     * Format: XXXX-XXXX-XXXX-XXXX (16 characters total, grouped in 4s)
     * 
     * @return Unique gift card code
     */
    public String generateUniqueCode() {
        int retries = 0;
        Set<String> attemptedCodes = new HashSet<>();
        
        while (retries < MAX_RETRIES) {
            String code = generateCode();
            
            // Check if code already exists
            if (!giftCardRepository.existsByCode(code) && !attemptedCodes.contains(code)) {
                logger.debug("Generated unique gift card code: {}", code);
                return code;
            }
            
            attemptedCodes.add(code);
            retries++;
            logger.warn("Generated duplicate code, retrying (attempt {}/{})", retries, MAX_RETRIES);
        }
        
        throw new RuntimeException("Failed to generate unique gift card code after " + MAX_RETRIES + " attempts");
    }
    
    /**
     * Generate a random gift card code
     * 
     * @return Gift card code in format XXXX-XXXX-XXXX-XXXX
     */
    private String generateCode() {
        StringBuilder code = new StringBuilder();
        
        for (int i = 0; i < CODE_LENGTH; i++) {
            if (i > 0 && i % 4 == 0) {
                code.append('-');
            }
            int index = random.nextInt(CHARACTERS.length());
            code.append(CHARACTERS.charAt(index));
        }
        
        return code.toString();
    }
    
    /**
     * Validate gift card code format
     * 
     * @param code Code to validate
     * @return true if format is valid
     */
    public boolean isValidFormat(String code) {
        if (code == null || code.length() != 19) { // 16 chars + 3 hyphens
            return false;
        }
        
        // Check format: XXXX-XXXX-XXXX-XXXX
        String pattern = "^[23456789ABCDEFGHJKLMNPQRSTUVWXYZ]{4}-[23456789ABCDEFGHJKLMNPQRSTUVWXYZ]{4}-[23456789ABCDEFGHJKLMNPQRSTUVWXYZ]{4}-[23456789ABCDEFGHJKLMNPQRSTUVWXYZ]{4}$";
        return code.matches(pattern);
    }
}

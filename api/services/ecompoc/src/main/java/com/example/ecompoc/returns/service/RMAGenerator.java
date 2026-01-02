package com.example.ecompoc.returns.service;

import com.example.ecompoc.returns.repository.ReturnRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Service for generating unique RMA (Return Merchandise Authorization) numbers
 * Format: RMA-YYYYMMDD-XXXXX (date prefix + 5-digit sequence)
 */
@Service
public class RMAGenerator {
    
    private static final Logger logger = LoggerFactory.getLogger(RMAGenerator.class);
    
    private static final String RMA_PREFIX = "RMA";
    private static final int SEQUENCE_LENGTH = 5;
    private static final int MAX_RETRIES = 10;
    
    private final ReturnRepository returnRepository;
    private final SecureRandom random;
    
    @Autowired
    public RMAGenerator(ReturnRepository returnRepository) {
        this.returnRepository = returnRepository;
        this.random = new SecureRandom();
    }
    
    /**
     * Generate a unique RMA number
     * Format: RMA-YYYYMMDD-XXXXX
     * 
     * @return Unique RMA number
     */
    public String generateUniqueRMA() {
        int retries = 0;
        
        while (retries < MAX_RETRIES) {
            String rmaNumber = generateRMA();
            
            // Check if RMA number already exists
            if (!returnRepository.existsByRmaNumber(rmaNumber)) {
                logger.debug("Generated unique RMA number: {}", rmaNumber);
                return rmaNumber;
            }
            
            retries++;
            logger.warn("Generated duplicate RMA number, retrying (attempt {}/{})", retries, MAX_RETRIES);
        }
        
        throw new RuntimeException("Failed to generate unique RMA number after " + MAX_RETRIES + " attempts");
    }
    
    /**
     * Generate an RMA number
     * Format: RMA-YYYYMMDD-XXXXX
     * 
     * @return RMA number
     */
    private String generateRMA() {
        // Get current date in YYYYMMDD format
        String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        // Generate 5-digit random sequence
        StringBuilder sequence = new StringBuilder();
        for (int i = 0; i < SEQUENCE_LENGTH; i++) {
            sequence.append(random.nextInt(10));
        }
        
        return String.format("%s-%s-%s", RMA_PREFIX, datePrefix, sequence.toString());
    }
    
    /**
     * Validate RMA number format
     * 
     * @param rmaNumber RMA number to validate
     * @return true if format is valid
     */
    public boolean isValidFormat(String rmaNumber) {
        if (rmaNumber == null || rmaNumber.length() != 18) { // RMA-YYYYMMDD-XXXXX = 18 chars
            return false;
        }
        
        // Check format: RMA-YYYYMMDD-XXXXX
        String pattern = "^RMA-\\d{8}-\\d{5}$";
        return rmaNumber.matches(pattern);
    }
}


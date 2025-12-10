# Test Plan: SCRUM-20 - Implement BCrypt or Argon2 Password Hashing

## 1. 📝 Story Summary & Core Objective

**Story:** Implement secure password hashing using BCrypt or Argon2 to replace any existing plain text or weak password storage mechanisms.

**Core Objective:** Replace plaintext password storage with industry-standard secure password hashing (BCrypt or Argon2) to protect user credentials in the event of a database breach.

**User Problem Solved:** Currently, passwords are stored in plaintext, which poses a critical security risk. If the database is compromised, all user passwords would be immediately exposed. Secure password hashing ensures that even if the database is breached, passwords cannot be easily recovered.

**Security Impact:** 
- **Critical Security Enhancement**: Protects user credentials from database breaches
- **Compliance**: Meets industry standards for password storage (OWASP, NIST)
- **Risk Mitigation**: Reduces impact of potential data breaches

---

## 2. ✅ Acceptance Criteria (AC) Test Cases

### **AC 1: Passwords are hashed using BCryptPasswordEncoder (or Argon2PasswordEncoder) before storing in the database**

**Given** a user registration request with a plaintext password  
**When** the user is created  
**Then** the password must be hashed using BCryptPasswordEncoder (or Argon2PasswordEncoder) before storing in the database  
**And** the stored password must NOT match the plaintext password  
**And** the stored password must start with the BCrypt identifier (`$2a$`, `$2b$`, or `$2y$`) or Argon2 identifier (`$argon2id$`)

#### Test Cases:

* **Test Case 1.1:** New user registration hashes password
  * **Description:** Create a new user with password "SecurePassword123" - verify password is hashed before saving
  * **Expected Result:** Password stored in database is a BCrypt/Argon2 hash (starts with `$2a$`, `$2b$`, `$2y$`, or `$argon2id$`), NOT plaintext "SecurePassword123"
  * **Automated Test Type:** Unit test (UserService), Integration test (Postman - verify database state)

* **Test Case 1.2:** Password hash format validation
  * **Description:** Verify stored password follows BCrypt format: `$2a$10$...` (60 characters) or Argon2 format: `$argon2id$v=19$m=65536,t=3,p=4$...`
  * **Expected Result:** Stored password matches expected hash format and length
  * **Automated Test Type:** Unit test (UserService), Integration test (Postman)

* **Test Case 1.3:** Different passwords produce different hashes
  * **Description:** Create two users with different passwords - verify hashes are different
  * **Expected Result:** User1 password hash ≠ User2 password hash (even if passwords are similar)
  * **Automated Test Type:** Unit test (UserService)

* **Test Case 1.4:** Same password produces different hashes (salt verification)
  * **Description:** Create two users with identical passwords - verify hashes are different (due to unique salts)
  * **Expected Result:** User1 password hash ≠ User2 password hash (even with same password)
  * **Automated Test Type:** Unit test (UserService)

* **Test Case 1.5:** Password hashing configuration uses appropriate strength/cost parameters
  * **Description:** Verify BCrypt strength is set to 10-12 (or Argon2 parameters are appropriate)
  * **Expected Result:** PasswordEncoder configured with strength >= 10 for BCrypt, or appropriate memory/time/parallelism for Argon2
  * **Automated Test Type:** Unit test (Configuration), Integration test

---

### **AC 2: Password comparison uses BCryptPasswordEncoder.matches() (or equivalent for Argon2) instead of plain text comparison**

**Given** a user login request with email and password  
**When** the authentication process validates credentials  
**Then** the system must use `BCryptPasswordEncoder.matches()` (or `Argon2PasswordEncoder.matches()`) to compare the provided password with the stored hash  
**And** authentication must succeed when password matches  
**And** authentication must fail when password does not match

#### Test Cases:

* **Test Case 2.1:** Successful authentication with correct password
  * **Description:** User logs in with correct password - authentication succeeds
  * **Expected Result:** LoginResponse returned with success message and token
  * **Automated Test Type:** Unit test (UserService.authenticate), Integration test (Postman), E2E test (Selenium)

* **Test Case 2.2:** Failed authentication with incorrect password
  * **Description:** User logs in with incorrect password - authentication fails
  * **Expected Result:** AuthenticationException thrown with "Invalid email or password" message
  * **Automated Test Type:** Unit test (UserService.authenticate), Integration test (Postman)

* **Test Case 2.3:** Password comparison uses BCryptPasswordEncoder.matches()
  * **Description:** Verify authentication method calls `passwordEncoder.matches(plaintext, hash)` instead of `equals()`
  * **Expected Result:** Code uses `passwordEncoder.matches()` for password comparison
  * **Automated Test Type:** Unit test (UserService - verify method calls), Code review

* **Test Case 2.4:** Authentication works with existing hashed passwords
  * **Description:** User created before migration logs in - authentication succeeds
  * **Expected Result:** Login succeeds for users with hashed passwords
  * **Automated Test Type:** Integration test (Postman), Migration test

* **Test Case 2.5:** Case-sensitive password validation
  * **Description:** User password is "Password123" - login with "password123" fails
  * **Expected Result:** Authentication fails (BCrypt/Argon2 are case-sensitive)
  * **Automated Test Type:** Unit test (UserService), Integration test (Postman)

* **Test Case 2.6:** Special characters in passwords work correctly
  * **Description:** User password contains special characters like "P@ssw0rd!#$" - login succeeds
  * **Expected Result:** Authentication succeeds with special characters
  * **Automated Test Type:** Unit test (UserService), Integration test (Postman)

---

### **AC 3: All existing password storage is migrated to use the new hashing mechanism**

**Given** existing users with plaintext passwords in the database  
**When** the migration script runs  
**Then** all plaintext passwords must be hashed using BCrypt/Argon2  
**And** no plaintext passwords remain in the database  
**And** migrated users can still authenticate successfully

#### Test Cases:

* **Test Case 3.1:** Migration script hashes all existing plaintext passwords
  * **Description:** Run migration script on database with 10 users having plaintext passwords - verify all passwords are hashed
  * **Expected Result:** All 10 passwords converted to BCrypt/Argon2 hashes, no plaintext passwords remain
  * **Automated Test Type:** Migration test (Database script), Integration test

* **Test Case 3.2:** Already hashed passwords are not re-hashed
  * **Description:** Database contains mix of plaintext and already-hashed passwords - migration only processes plaintext
  * **Expected Result:** Plaintext passwords hashed, already-hashed passwords unchanged
  * **Automated Test Type:** Migration test (Database script)

* **Test Case 3.3:** Migrated users can authenticate successfully
  * **Description:** User with migrated password logs in - authentication succeeds
  * **Expected Result:** Login succeeds with same password as before migration
  * **Automated Test Type:** Integration test (Postman), Migration test

* **Test Case 3.4:** Migration is idempotent
  * **Description:** Run migration script twice - second run doesn't modify already-hashed passwords
  * **Expected Result:** No errors, passwords remain correctly hashed
  * **Automated Test Type:** Migration test (Database script)

* **Test Case 3.5:** Migration handles edge cases (null, empty, whitespace passwords)
  * **Description:** Database contains users with null/empty/whitespace passwords - migration handles gracefully
  * **Expected Result:** Migration completes without errors, edge cases logged or handled appropriately
  * **Automated Test Type:** Migration test (Database script)

* **Test Case 3.6:** Migration rollback capability
  * **Description:** Migration fails partway through - verify rollback restores original state
  * **Expected Result:** Database restored to pre-migration state, no partial migrations
  * **Automated Test Type:** Migration test (Database script - rollback verification)

---

### **AC 4: Password hashing configuration is properly set up with appropriate strength/cost parameters**

**Given** the application configuration  
**When** the PasswordEncoder bean is created  
**Then** BCrypt strength must be set to 10-12 (or Argon2 parameters must be appropriate)  
**And** the configuration must be documented  
**And** the configuration must be easily adjustable for different environments

#### Test Cases:

* **Test Case 4.1:** BCrypt strength parameter is configured correctly
  * **Description:** Verify BCryptPasswordEncoder is configured with strength >= 10
  * **Expected Result:** PasswordEncoder bean created with strength parameter 10-12
  * **Automated Test Type:** Unit test (Configuration class), Integration test

* **Test Case 4.2:** Argon2 parameters are configured correctly (if using Argon2)
  * **Description:** Verify Argon2PasswordEncoder configured with appropriate memory/time/parallelism
  * **Expected Result:** Argon2PasswordEncoder configured with memory >= 65536, time >= 3, parallelism >= 4
  * **Automated Test Type:** Unit test (Configuration class), Integration test

* **Test Case 4.3:** Configuration is environment-aware
  * **Description:** Different environments (dev, test, prod) can have different strength parameters
  * **Expected Result:** Configuration allows environment-specific parameters via properties
  * **Automated Test Type:** Unit test (Configuration class), Integration test

* **Test Case 4.4:** Configuration is documented
  * **Description:** Verify configuration properties are documented in application.properties/yml or README
  * **Expected Result:** Password hashing configuration documented with recommended values
  * **Automated Test Type:** Documentation review

---

### **AC 5: Tests are updated to verify password hashing and comparison functionality**

**Given** existing unit and integration tests  
**When** password hashing is implemented  
**Then** all tests must be updated to work with hashed passwords  
**And** new tests must verify password hashing functionality  
**And** all tests must pass

#### Test Cases:

* **Test Case 5.1:** UserServiceTest updated for password hashing
  * **Description:** Update UserServiceTest.createUser() to verify password is hashed
  * **Expected Result:** Test verifies stored password is a hash, not plaintext
  * **Automated Test Type:** Unit test (UserServiceTest)

* **Test Case 5.2:** UserServiceTest.authenticate() updated for password comparison
  * **Description:** Update UserServiceTest.authenticate() to use passwordEncoder.matches()
  * **Expected Result:** Test uses passwordEncoder for comparison, not equals()
  * **Automated Test Type:** Unit test (UserServiceTest)

* **Test Case 5.3:** New test: PasswordEncoderTest created
  * **Description:** Create new test class to verify PasswordEncoder functionality
  * **Expected Result:** Test class verifies: hash generation, password matching, salt uniqueness
  * **Automated Test Type:** Unit test (PasswordEncoderTest) - **CREATE NEW**

* **Test Case 5.4:** Integration tests updated for hashed passwords
  * **Description:** Update UserManagementIntegrationTest to work with hashed passwords
  * **Expected Result:** Integration tests pass with hashed passwords
  * **Automated Test Type:** Integration test (UserManagementIntegrationTest)

* **Test Case 5.5:** Postman tests updated for hashed passwords
  * **Description:** Update Postman collection to verify password hashing in responses
  * **Expected Result:** Postman tests pass, verify passwords are hashed in database
  * **Automated Test Type:** Integration test (Postman)

* **Test Case 5.6:** All existing tests pass after implementation
  * **Description:** Run full test suite - all tests must pass
  * **Expected Result:** 100% test pass rate
  * **Automated Test Type:** Full test suite execution

---

## 3. 🔒 Security Test Cases

### **Security Test 1: Password Hash Verification**

* **Test Case S1.1:** Verify passwords cannot be recovered from hashes
  * **Description:** Extract password hash from database - verify it cannot be easily reversed
  * **Expected Result:** Hash cannot be reversed to original password without brute force
  * **Automated Test Type:** Security test (Manual verification)

* **Test Case S1.2:** Verify password hashes are not exposed in API responses
  * **Description:** Call GET /api/users/{id} - verify password field is not included in response
  * **Expected Result:** UserResponse does not contain password field
  * **Automated Test Type:** Integration test (Postman), Unit test (UserResponse DTO)

* **Test Case S1.3:** Verify password hashes are not logged
  * **Description:** Enable debug logging during user creation - verify password hash is not logged
  * **Expected Result:** No password hashes appear in application logs
  * **Automated Test Type:** Security test (Log review)

### **Security Test 2: Timing Attack Prevention**

* **Test Case S2.1:** Authentication timing is consistent for invalid passwords
  * **Description:** Measure authentication time for invalid passwords - verify consistent timing (prevents timing attacks)
  * **Expected Result:** Authentication time is consistent regardless of password correctness
  * **Automated Test Type:** Security test (Performance test)

### **Security Test 3: Password Strength**

* **Test Case S3.1:** Verify password hashing works with various password lengths
  * **Description:** Test passwords of length 8, 16, 32, 64, 128 characters - all hash successfully
  * **Expected Result:** All password lengths hash successfully
  * **Automated Test Type:** Unit test (PasswordEncoderTest)

* **Test Case S3.2:** Verify password hashing works with Unicode characters
  * **Description:** Test password with Unicode characters (e.g., "Pässwörd🔒") - hashes successfully
  * **Expected Result:** Unicode passwords hash and authenticate successfully
  * **Automated Test Type:** Unit test (PasswordEncoderTest), Integration test (Postman)

---

## 4. 🔄 Migration Test Cases

### **Migration Test 1: Database Migration**

* **Test Case M1.1:** Migration script executes successfully
  * **Description:** Run migration script on test database with existing users - script completes without errors
  * **Expected Result:** Migration completes successfully, all passwords hashed
  * **Automated Test Type:** Migration test (Database script)

* **Test Case M1.2:** Migration preserves user data integrity
  * **Description:** Verify user IDs, emails, names unchanged after migration
  * **Expected Result:** All user data except passwords remains unchanged
  * **Automated Test Type:** Migration test (Database verification)

* **Test Case M1.3:** Migration handles large datasets
  * **Description:** Run migration on database with 10,000+ users - completes in reasonable time
  * **Expected Result:** Migration completes within acceptable time (e.g., < 30 minutes for 10K users)
  * **Automated Test Type:** Performance test (Migration script)

---

## 5. ⚠️ Edge Cases & Error Handling

### **Edge Case 1: Null/Empty Passwords**

* **Test Case E1.1:** Null password handling
  * **Description:** Attempt to create user with null password - appropriate error thrown
  * **Expected Result:** ValidationException or IllegalArgumentException thrown
  * **Automated Test Type:** Unit test (UserService), Integration test (Postman)

* **Test Case E1.2:** Empty password handling
  * **Description:** Attempt to create user with empty password - appropriate error thrown
  * **Expected Result:** ValidationException thrown (password validation should reject empty passwords)
  * **Automated Test Type:** Unit test (UserService), Integration test (Postman)

### **Edge Case 2: Very Long Passwords**

* **Test Case E2.1:** Very long password handling
  * **Description:** Create user with password length 1000 characters - hashes successfully
  * **Expected Result:** Password hashed successfully (BCrypt supports up to 72 bytes, Argon2 supports longer)
  * **Automated Test Type:** Unit test (PasswordEncoderTest)

### **Edge Case 3: Concurrent User Creation**

* **Test Case E3.1:** Concurrent user creation with same password
  * **Description:** Create 10 users concurrently with same password - all hashed correctly
  * **Expected Result:** All users created successfully with different hashes (due to unique salts)
  * **Automated Test Type:** Concurrent test (UserService)

---

## 6. 📊 Automated Test Strategy (Test Pyramid)

### Unit Tests (Foundation - 70% coverage target)

#### Backend Unit Tests:
- ✅ **UserServiceTest** - **UPDATE EXISTING** - Update createUser() and authenticate() tests
  - Test cases: AC1.1-AC1.4, AC2.1-AC2.6, E1.1-E1.2
- ❌ **PasswordEncoderTest** - **CREATE NEW** - Test password hashing and comparison
  - Test cases: AC1.2, AC2.3, S3.1-S3.2, E2.1
- ❌ **PasswordConfigTest** - **CREATE NEW** - Test PasswordEncoder configuration
  - Test cases: AC4.1-AC4.3
- ✅ **UserControllerTest** - **UPDATE EXISTING** - Verify password not exposed in responses
  - Test cases: S1.2

#### Frontend Unit Tests:
- ✅ **UserForm.test.tsx** - **UPDATE EXISTING** - Verify password input handling
  - Test cases: Verify password field is password type, not exposed in form state

### Integration Tests (Middle Layer - 25% coverage target)

#### Postman Integration Tests:
- ✅ **User Creation Test** - **UPDATE EXISTING** - Verify password is hashed in database
  - Test cases: AC1.1, AC1.2, S1.2
- ✅ **User Login Test** - **UPDATE EXISTING** - Verify authentication with hashed passwords
  - Test cases: AC2.1, AC2.2, AC2.5, AC2.6
- ❌ **Password Hashing Verification Test** - **CREATE NEW** - Verify password hashing in database
  - Test cases: AC1.1-AC1.4, AC3.1-AC3.3

#### Database Integration Tests:
- ✅ **UserManagementIntegrationTest** - **UPDATE EXISTING** - Verify end-to-end flow
  - Test cases: AC1.1, AC2.1, AC2.2

### E2E Tests (Top Layer - 5% coverage target)

#### Selenium E2E Tests:
- ✅ **E2EWorkflowTest** - **UPDATE EXISTING** - Verify user creation and login flow
  - Test cases: AC1.1, AC2.1, AC2.2
- ❌ **SCRUM20PasswordHashingTest** - **CREATE NEW** - E2E test for password security
  - Test cases: Verify password not exposed in UI, login works with hashed passwords

---

## 7. 📋 Test Execution Checklist

### Phase 1: Unit Tests (Foundation)
- [ ] Backend: UserServiceTest - **UPDATE EXISTING** - All test cases pass
- [ ] Backend: PasswordEncoderTest - **CREATE NEW** - All test cases pass
- [ ] Backend: PasswordConfigTest - **CREATE NEW** - All test cases pass
- [ ] Backend: UserControllerTest - **UPDATE EXISTING** - All test cases pass
- [ ] Frontend: UserForm.test.tsx - **UPDATE EXISTING** - Component tests pass

### Phase 2: Integration Tests
- [ ] Postman: Update user creation test - Verify password hashing
- [ ] Postman: Update user login test - Verify authentication with hashed passwords
- [ ] Postman: Create password hashing verification test
- [ ] Database: UserManagementIntegrationTest - **UPDATE EXISTING** - All test cases pass

### Phase 3: Migration Tests
- [ ] Migration script: Test on development database
- [ ] Migration script: Test on test database with existing users
- [ ] Migration script: Verify migrated users can authenticate
- [ ] Migration script: Test rollback capability

### Phase 4: Security Tests
- [ ] Security: Verify passwords cannot be recovered from hashes
- [ ] Security: Verify password hashes not exposed in API responses
- [ ] Security: Verify password hashes not logged
- [ ] Security: Test timing attack prevention
- [ ] Security: Test various password lengths and Unicode characters

### Phase 5: E2E Tests
- [ ] Selenium: E2EWorkflowTest - **UPDATE EXISTING** - All test cases pass
- [ ] Selenium: SCRUM20PasswordHashingTest - **CREATE NEW** - All test cases pass

### Phase 6: Regression Tests
- [ ] Run full test suite - All tests pass
- [ ] Verify no existing functionality broken
- [ ] Performance test - Verify no performance degradation

---

## 8. 🎯 Test Data Requirements

### Test Users for Password Hashing:
- User 1: Password "SecurePassword123" (standard password)
- User 2: Password "SecurePassword123" (same password, different hash expected)
- User 3: Password "P@ssw0rd!#$" (special characters)
- User 4: Password "VeryLongPassword123456789012345678901234567890" (long password)
- User 5: Password "Pässwörd🔒" (Unicode characters)

### Test Scenarios:
- New user registration (AC1)
- User login with correct password (AC2)
- User login with incorrect password (AC2)
- Migration of existing users (AC3)

---

## 9. ⚠️ Risks & Mitigation

### Risk 1: Migration Failure
**Area:** Database migration script  
**Risk:** Migration script fails partway through, leaving database in inconsistent state  
**Mitigation:** 
- Implement transaction-based migration with rollback capability
- Test migration on copy of production data first
- Verify migration script is idempotent

### Risk 2: Authentication Breakage
**Area:** User authentication flow  
**Risk:** Existing users cannot authenticate after password hashing implementation  
**Mitigation:** 
- Thoroughly test authentication with hashed passwords
- Ensure migration script runs before deployment
- Have rollback plan ready

### Risk 3: Performance Impact
**Area:** Password hashing operations  
**Risk:** BCrypt/Argon2 hashing may slow down user registration and authentication  
**Mitigation:** 
- Use appropriate strength/cost parameters (BCrypt strength 10-12 is acceptable)
- Performance test user creation and authentication endpoints
- Monitor response times in production

### Risk 4: Test Failures
**Area:** Existing test suite  
**Risk:** Existing tests fail because they expect plaintext passwords  
**Mitigation:** 
- Update all tests to work with hashed passwords
- Use test fixtures with pre-hashed passwords
- Verify all tests pass before deployment

---

## 10. 📈 Success Criteria

### Must Have (Blocking):
- ✅ All passwords hashed using BCrypt/Argon2 before storage
- ✅ Password comparison uses BCryptPasswordEncoder.matches() / Argon2PasswordEncoder.matches()
- ✅ All existing passwords migrated to hashed format
- ✅ All unit tests pass
- ✅ All integration tests pass
- ✅ Migration script executes successfully
- ✅ No plaintext passwords remain in database

### Should Have (Important):
- ✅ Password hashing configuration documented
- ✅ E2E tests updated and passing
- ✅ Security tests pass
- ✅ Performance tests show acceptable response times

### Nice to Have (Optional):
- ✅ Password strength validation (future enhancement)
- ✅ Password reset functionality (future enhancement)
- ✅ Password expiration policy (future enhancement)

---

## 11. 📝 Notes

- **Current State:** Passwords are stored as plaintext (see `UserService.java` line 90: `user.getPassword().equals(request.getPassword())`)
- **Target State:** All passwords hashed using BCrypt or Argon2
- **Migration Required:** Yes - existing users need password migration
- **Breaking Changes:** Yes - authentication flow changes, tests need updates
- **Dependencies:** Spring Security BCryptPasswordEncoder or Argon2PasswordEncoder

---

**QA Status:** ⚠️ **NOT STARTED** - Implementation pending  
**Recommendation:** Implement password hashing following this test plan, then execute all test phases before production deployment



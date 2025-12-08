# Dubbo RPC Flow - Account Synchronization

## Overview
Hệ thống sử dụng Apache Dubbo để đồng bộ metadata tài khoản từ `customer-service` sang `account-service` sau khi tạo CIF trên Core Banking System.

---

## Architecture

```
┌─────────────────────┐
│  Customer Service   │ (Consumer)
│  Port: 8082         │
└──────────┬──────────┘
           │ Dubbo RPC
           │ (Hessian2 Serialization)
           │
           ↓ Nacos Registry
           │ (klb-bank namespace)
           │ (banking-services group)
           │
┌──────────┴──────────┐
│  Account Service    │ (Provider)
│  Port: 8083         │
│  Dubbo: -1 (random) │
└─────────────────────┘
```

---

## Components

### 1. Shared Contract (common-api module)

#### DTOs (`com.example.commonapi.dto.account`)

**AccountSyncRequest.java**
```java
@Data
@Builder
public class AccountSyncRequest implements Serializable {
    private String accountNumber;      // Số tài khoản từ Core Banking
    private String customerId;         // UUID khách hàng từ Customer Service
    private String cifNumber;          // CIF từ Core Banking
    private String accountType;        // "CHECKING", "SAVINGS", "CREDIT"
    private String currency;           // "VND", "USD", "EUR", "JPY"
    private String status;             // "ACTIVE", "DORMANT", "FROZEN", "CLOSED"
    private BigDecimal balance;        // Số dư ban đầu
    private LocalDateTime openedAt;    // Ngày mở tài khoản
}
```

**AccountSyncResult.java**
```java
@Data
@Builder
public class AccountSyncResult implements Serializable {
    private boolean success;           // Trạng thái đồng bộ
    private String message;            // Thông báo chi tiết
    private String accountId;          // UUID tài khoản (nếu thành công)
}
```

#### Dubbo Interface (`com.example.commonapi.dubbo`)

**AccountSyncDubboService.java**
```java
public interface AccountSyncDubboService {
    AccountSyncResult syncAccountMetadata(AccountSyncRequest request);
}
```

---

### 2. Provider - Account Service

#### Implementation (`com.example.accountservice.events.producer`)

**AccountSyncDubboServiceImpl.java**
```java
@DubboService(version = "1.0.0", group = "banking-services")
@RequiredArgsConstructor
@Slf4j
public class AccountSyncDubboServiceImpl implements AccountSyncDubboService {
    
    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public AccountSyncResult syncAccountMetadata(AccountSyncRequest request) {
        // 1. Check idempotency
        if (accountRepository.existsByAccountNumber(request.getAccountNumber())) {
            // Trả về thành công nếu đã tồn tại
            Account existing = accountRepository.findByAccountNumber(...)
            return AccountSyncResult.builder()
                .success(true)
                .message("Account already synced (idempotent)")
                .accountId(existing.getAccountId())
                .build();
        }

        // 2. Create account entity by type
        Account account = createAccountByType(request);
        
        // 3. Map metadata
        account.setAccountNumber(request.getAccountNumber());
        account.setCustomerId(request.getCustomerId());
        account.setCifNumber(request.getCifNumber());
        account.setAccountType(AccountType.valueOf(request.getAccountType()));
        account.setCurrency(Currency.valueOf(request.getCurrency()));
        account.setStatus(AccountStatus.valueOf(request.getStatus()));
        account.setOpenedDate(request.getOpenedAt() != null ? request.getOpenedAt() : LocalDateTime.now());

        // 4. Persist
        Account saved = accountRepository.save(account);
        
        return AccountSyncResult.builder()
            .success(true)
            .message("Account metadata synced successfully")
            .accountId(saved.getAccountId())
            .build();
    }

    private Account createAccountByType(AccountSyncRequest request) {
        return switch (AccountType.valueOf(request.getAccountType())) {
            case CHECKING -> new CheckingAccount();
            case SAVINGS -> {
                SavingsAccount savings = new SavingsAccount();
                savings.setInterestRate(new BigDecimal("0.035"));  // 3.5%
                savings.setTermMonths(6);
                yield savings;
            }
            case CREDIT -> {
                CreditAccount credit = new CreditAccount();
                credit.setCreditLimit(new BigDecimal("5000000"));  // 5M VND
                credit.setAvailableCredit(new BigDecimal("5000000"));
                credit.setStatementDate(25);
                credit.setPaymentDueDate(15);
                yield credit;
            }
        };
    }
}
```

#### Configuration (`application.properties`)
```properties
# Dubbo Provider Settings
dubbo.application.name=account-service
dubbo.application.version=1.0.0

# Nacos Registry
dubbo.registry.address=nacos://localhost:8848
dubbo.registry.parameters.namespace=klb-bank
dubbo.registry.parameters.group=banking-services

# Protocol (-1 = random port to avoid conflicts)
dubbo.protocol.name=dubbo
dubbo.protocol.port=-1
dubbo.protocol.serialization=hessian2
```

---

### 3. Consumer - Customer Service

#### Consumer Wrapper (`com.example.customerservice.events.consumer`)

**AccountSyncConsumer.java**
```java
@Component
@Slf4j
public class AccountSyncConsumer {

    @DubboReference(
        version = "1.0.0", 
        group = "banking-services", 
        check = false,           // Không check provider khi khởi động
        timeout = 5000           // Timeout 5s
    )
    private AccountSyncDubboService accountSyncDubboService;

    public AccountSyncResult syncAccountMetadata(AccountSyncRequest request) {
        log.info("Syncing account metadata via Dubbo for accountNumber: {}", 
            request.getAccountNumber());
        
        try {
            // RPC call to account-service
            AccountSyncResult result = accountSyncDubboService.syncAccountMetadata(request);
            
            if (result.isSuccess()) {
                log.info("Successfully synced: accountNumber={}, accountId={}", 
                    request.getAccountNumber(), result.getAccountId());
            } else {
                log.warn("Sync returned success=false: {}", result.getMessage());
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("Dubbo RPC failed: accountNumber={}", 
                request.getAccountNumber(), e);
            throw new RuntimeException("Dubbo account sync failed", e);
        }
    }
}
```

#### Service Integration (`com.example.customerservice.service.impl`)

**CustomerServiceImpl.java** (excerpt)
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService {
    
    private final AccountSyncConsumer accountSyncConsumer;

    @Override
    @Transactional
    public CustomerResponse registerCustomer(CustomerRegisterRequest registerDto) {
        String authProviderId = null;
        String coreCustomerId = null;
        String accountNumber = null;

        try {
            // Step 1: Create user in Keycloak
            authProviderId = keycloakService.createUser(registerDto);

            // Step 2: Create CIF in Core Banking
            CreateCoreCustomerResponse coreResponse = coreBankingFeignClient.createCif(...);
            coreCustomerId = coreResponse.getCoreCustomerId();
            accountNumber = coreResponse.getAccountNumber();

            // Step 3: Save customer in local DB
            Customer savedCustomer = customerRepository.save(customer);

            // Step 4: Sync account metadata to AccountService via Dubbo
            if (accountNumber != null && !accountNumber.isBlank()) {
                try {
                    AccountSyncRequest syncRequest = AccountSyncRequest.builder()
                        .accountNumber(accountNumber)
                        .customerId(savedCustomer.getCustomerId())
                        .cifNumber(coreResponse.getCifNumber())
                        .accountType("CHECKING")
                        .currency("VND")
                        .status("ACTIVE")
                        .balance(BigDecimal.ZERO)
                        .openedAt(LocalDateTime.now())
                        .build();
                    
                    AccountSyncResult syncResult = accountSyncConsumer.syncAccountMetadata(syncRequest);
                    
                    if (syncResult.isSuccess()) {
                        log.info("Account synced: accountId={}", syncResult.getAccountId());
                    } else {
                        log.warn("Sync failed: {}", syncResult.getMessage());
                    }
                } catch (Exception e) {
                    // Log but don't fail registration
                    log.error("Failed to sync account, but registration completed", e);
                }
            }

            return customerMapper.toResponse(savedCustomer);

        } catch (Exception e) {
            // Rollback logic...
            throw new CustomerRegistrationException(...);
        }
    }
}
```

#### Configuration (`application.properties`)
```properties
# Dubbo Consumer Settings
dubbo.application.name=customer-service
dubbo.application.version=1.0.0

# Nacos Registry
dubbo.registry.address=nacos://localhost:8848
dubbo.registry.parameters.namespace=klb-bank
dubbo.registry.parameters.group=banking-services

# Consumer Config
dubbo.consumer.timeout=5000
dubbo.consumer.retries=2
dubbo.consumer.check=false

# Scan for @DubboReference
dubbo.scan.base-packages=com.example.customerservice.events.producer

# Serialization
dubbo.protocol.serialization=hessian2
```

---

## Flow Diagram

### Registration Flow với Dubbo Sync

```
┌────────────────────────────────────────────────────────────────────┐
│                       Customer Registration                         │
└────────────────────────────────────────────────────────────────────┘

1. Client Request
   POST /api/customers/register
   
   ↓
   
2. CustomerServiceImpl.registerCustomer()
   
   ├─► Step 1: Keycloak User Creation
   │   - Create user với phone/email
   │   - Assign USER role
   │   - Return authProviderId
   │
   ├─► Step 2: Core Banking CIF Creation
   │   - Feign call to core-banking-service
   │   - Create CIF + CASA account
   │   - Return cifNumber + accountNumber
   │
   ├─► Step 3: Save Customer Locally
   │   - Persist customer entity
   │   - Link authProviderId + cifNumber
   │   - Return customerId
   │
   └─► Step 4: Dubbo Account Sync
       │
       ├─► AccountSyncConsumer.syncAccountMetadata()
       │   │
       │   ├─► Build AccountSyncRequest
       │   │   - accountNumber (from Core Banking)
       │   │   - customerId (from local DB)
       │   │   - cifNumber (from Core Banking)
       │   │   - accountType: "CHECKING"
       │   │   - currency: "VND"
       │   │   - status: "ACTIVE"
       │   │
       │   └─► Dubbo RPC Call
       │       │
       │       ↓ Network (Hessian2 serialization)
       │       │
       │       ┌───────────────────────────────────────┐
       │       │  Account Service (Provider)           │
       │       └───────────────────────────────────────┘
       │       │
       │       ├─► AccountSyncDubboServiceImpl.syncAccountMetadata()
       │       │   │
       │       │   ├─► Check Idempotency
       │       │   │   - existsByAccountNumber()?
       │       │   │   - Yes → Return existing accountId
       │       │   │   - No → Continue
       │       │   │
       │       │   ├─► Create Account Entity
       │       │   │   - Switch by accountType
       │       │   │   - CheckingAccount / SavingsAccount / CreditAccount
       │       │   │
       │       │   ├─► Map Metadata
       │       │   │   - Convert String → Enum (AccountType, Currency, Status)
       │       │   │   - Set customerId, cifNumber, dates
       │       │   │
       │       │   ├─► Persist to account_db
       │       │   │   - @Transactional
       │       │   │   - Save to accounts table
       │       │   │
       │       │   └─► Return AccountSyncResult
       │       │       - success: true
       │       │       - accountId: UUID
       │       │       - message: "Account metadata synced successfully"
       │       │
       │       ↓ Network response
       │
       └─► Log Result
           - Success → Log accountId
           - Failure → Warn but continue (non-blocking)

   ↓
   
3. Return CustomerResponse to Client
```

---

## Error Handling

### Provider Side (Account Service)

#### 1. Idempotency
```java
// Nếu accountNumber đã tồn tại → trả về success ngay lập tức
if (accountRepository.existsByAccountNumber(request.getAccountNumber())) {
    return AccountSyncResult.builder()
        .success(true)
        .message("Account already synced (idempotent)")
        .accountId(existing.getAccountId())
        .build();
}
```

#### 2. Exception Handling
```java
catch (Exception e) {
    log.error("Failed to sync account: {}", request.getAccountNumber(), e);
    return AccountSyncResult.builder()
        .success(false)
        .message("Sync failed: " + e.getMessage())
        .build();
}
```

#### 3. Enum Validation
```java
// Nếu accountType/currency/status không hợp lệ
// → IllegalArgumentException sẽ được catch và trả về success=false
AccountType.valueOf(request.getAccountType())  // Throws nếu không hợp lệ
```

---

### Consumer Side (Customer Service)

#### 1. Non-blocking Failure
```java
try {
    AccountSyncResult syncResult = accountSyncConsumer.syncAccountMetadata(syncRequest);
    if (!syncResult.isSuccess()) {
        log.warn("Sync failed: {}", syncResult.getMessage());
    }
} catch (Exception e) {
    // Chỉ log, không làm fail registration
    log.error("Failed to sync account, but registration completed", e);
}
```

#### 2. Dubbo Timeout & Retry
```properties
# application.properties
dubbo.consumer.timeout=5000    # 5 giây timeout
dubbo.consumer.retries=2       # Retry 2 lần nếu timeout
dubbo.consumer.check=false     # Không check provider khi start
```

---

## Data Flow

### Request Example
```json
{
  "accountNumber": "1234567890123456",
  "customerId": "550e8400-e29b-41d4-a716-446655440000",
  "cifNumber": "CIF20231205001",
  "accountType": "CHECKING",
  "currency": "VND",
  "status": "ACTIVE",
  "balance": 0,
  "openedAt": "2025-12-08T10:30:00"
}
```

### Success Response
```json
{
  "success": true,
  "message": "Account metadata synced successfully",
  "accountId": "660e8400-e29b-41d4-a716-446655440001"
}
```

### Failure Response
```json
{
  "success": false,
  "message": "Sync failed: Invalid account type: INVALID_TYPE",
  "accountId": null
}
```

---

## Database Schema

### Account Service - `account_db`

#### Base Table: `accounts`
```sql
CREATE TABLE accounts (
    account_id VARCHAR(36) PRIMARY KEY,
    account_number VARCHAR(20) UNIQUE NOT NULL,
    customer_id VARCHAR(36) NOT NULL,
    cif_number VARCHAR(50) NOT NULL,
    account_type VARCHAR(20) NOT NULL,  -- CHECKING, SAVINGS, CREDIT
    status VARCHAR(20) NOT NULL,        -- ACTIVE, DORMANT, FROZEN, CLOSED
    currency VARCHAR(10) NOT NULL,      -- VND, USD, EUR, JPY
    opened_date DATETIME NOT NULL,
    closed_date DATETIME,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);
```

#### Joined Tables (Inheritance)
```sql
-- Tài khoản thanh toán
CREATE TABLE checking_accounts (
    account_id VARCHAR(36) PRIMARY KEY,
    FOREIGN KEY (account_id) REFERENCES accounts(account_id)
);

-- Tài khoản tiết kiệm
CREATE TABLE savings_accounts (
    account_id VARCHAR(36) PRIMARY KEY,
    interest_rate DECIMAL(8,5) NOT NULL,  -- 0.03500
    term_months INT NOT NULL,             -- 6, 12, 24...
    FOREIGN KEY (account_id) REFERENCES accounts(account_id)
);

-- Tài khoản tín dụng
CREATE TABLE credit_accounts (
    account_id VARCHAR(36) PRIMARY KEY,
    credit_limit DECIMAL(19,4) NOT NULL,      -- 5000000.0000
    available_credit DECIMAL(19,4) NOT NULL,
    statement_date INT NOT NULL,              -- 25
    payment_due_date INT NOT NULL,            -- 15
    FOREIGN KEY (account_id) REFERENCES accounts(account_id)
);
```

---

## Testing Checklist

### Unit Tests

- [ ] **AccountSyncDubboServiceImpl**
  - [x] Idempotency: existing account returns success
  - [x] Create CHECKING account successfully
  - [x] Create SAVINGS account với interest rate
  - [x] Create CREDIT account với credit limit
  - [x] Invalid accountType throws exception
  - [x] Exception handling returns success=false

- [ ] **AccountSyncConsumer**
  - [x] Successful RPC call logs accountId
  - [x] Failed RPC throws RuntimeException
  - [x] Timeout handling

### Integration Tests

- [ ] **End-to-End Registration Flow**
  - [ ] Nacos registry running
  - [ ] account-service provider registered
  - [ ] customer-service consumer can discover
  - [ ] Dubbo RPC completes successfully
  - [ ] Account persisted in account_db
  - [ ] Customer persisted in customer_db

---

## Troubleshooting

### 1. Provider Not Found
```
No provider available for AccountSyncDubboService
```
**Giải pháp:**
- Kiểm tra Nacos console: `http://localhost:8848/nacos`
- Verify namespace: `klb-bank`
- Verify group: `banking-services`
- Restart account-service

### 2. Serialization Error
```
Hessian serialization failed
```
**Giải pháp:**
- Đảm bảo DTOs implement `Serializable`
- Check `serialVersionUID` matching
- Verify Lombok annotations (@Data, @Builder)

### 3. Timeout
```
Dubbo invoke timeout
```
**Giải pháp:**
- Tăng timeout: `dubbo.consumer.timeout=10000`
- Check network latency
- Review provider logs

### 4. Port Conflict
```
Address already in use
```
**Giải pháp:**
- Sử dụng `dubbo.protocol.port=-1` (random port)
- Hoặc chỉ định port cụ thể khác nhau cho mỗi service

---

## Dependencies

### common-api/pom.xml
```xml
<dependencies>
    <!-- Lombok for DTOs -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </dependency>
</dependencies>
```

### account-service/pom.xml
```xml
<dependencies>
    <!-- Common API -->
    <dependency>
        <groupId>com.example</groupId>
        <artifactId>common-api</artifactId>
    </dependency>
    
    <!-- Dubbo Spring Boot Starter -->
    <dependency>
        <groupId>org.apache.dubbo</groupId>
        <artifactId>dubbo-spring-boot-starter</artifactId>
    </dependency>
    
    <!-- Nacos Registry -->
    <dependency>
        <groupId>com.alibaba.nacos</groupId>
        <artifactId>nacos-client</artifactId>
    </dependency>
</dependencies>
```

### customer-service/pom.xml
```xml
<dependencies>
    <!-- Common API -->
    <dependency>
        <groupId>com.example</groupId>
        <artifactId>common-api</artifactId>
    </dependency>
    
    <!-- Dubbo Spring Boot Starter -->
    <dependency>
        <groupId>org.apache.dubbo</groupId>
        <artifactId>dubbo-spring-boot-starter</artifactId>
    </dependency>
    
    <!-- Nacos Registry -->
    <dependency>
        <groupId>com.alibaba.nacos</groupId>
        <artifactId>nacos-client</artifactId>
    </dependency>
</dependencies>
```

---

## Best Practices

### 1. Idempotency
✅ **Luôn check duplicate trước khi insert**
```java
if (accountRepository.existsByAccountNumber(request.getAccountNumber())) {
    return existing result;
}
```

### 2. Transaction Boundary
✅ **Provider method phải @Transactional**
```java
@Override
@Transactional
public AccountSyncResult syncAccountMetadata(AccountSyncRequest request)
```

### 3. Non-blocking Consumer
✅ **Không để Dubbo failure làm crash registration**
```java
try {
    accountSyncConsumer.syncAccountMetadata(syncRequest);
} catch (Exception e) {
    log.error("Sync failed, but continue", e);
}
```

### 4. Enum Validation
✅ **Validate enum values sớm**
```java
try {
    AccountType.valueOf(request.getAccountType());
} catch (IllegalArgumentException e) {
    return error result;
}
```

### 5. Timeout Configuration
✅ **Set timeout hợp lý**
```properties
dubbo.consumer.timeout=5000    # Không quá lớn (block caller)
dubbo.consumer.retries=2       # Retry hợp lý (tránh storm)
```

---

## Future Improvements

1. **Async Dubbo Call**: Sử dụng `CompletableFuture` để không block registration thread
2. **Event Sourcing**: Publish `AccountSyncEvent` thay vì RPC trực tiếp
3. **Circuit Breaker**: Thêm Resilience4j cho Dubbo consumer
4. **Metrics**: Expose Dubbo metrics qua Micrometer
5. **Distributed Tracing**: Integrate Sleuth/Zipkin cho Dubbo calls

---

## References

- **Apache Dubbo Docs**: https://dubbo.apache.org/
- **Nacos Registry**: https://nacos.io/
- **Spring Cloud Alibaba**: https://spring.io/projects/spring-cloud-alibaba
- **Hessian Serialization**: http://hessian.caucho.com/

---

*Last Updated: December 8, 2025*

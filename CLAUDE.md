# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Structure

This is a microservices banking application with the following structure:
- `backend/` - Contains Spring Boot microservices
  - `common-api/` - Shared module containing Dubbo service interfaces, DTOs, enums, and constants for inter-service communication
  - `customer-service/` - Customer management service with OAuth2 resource server, Keycloak integration, and Dubbo consumer/provider
  - `account-service/` - Account management service with Dubbo provider for account operations
  - `core-banking-service/` - Core banking operations with circuit breaker patterns and CIF management
- `frontend/` - Frontend application (currently empty)

## Technology Stack

**Backend Services:**
- Java 17 with Spring Boot 3.5.5
- Spring Security with OAuth2 support
- Spring Data JPA with MySQL
- **Apache Dubbo 3.2.15** for RPC between microservices with Nacos registry
- Spring WebFlux for reactive programming (core-banking-service)
- Circuit breaker with Resilience4J (core-banking-service)
- Spring Cloud 2025.0.0 (core-banking-service)
- Keycloak integration for identity and access management
- **MapStruct 1.6.3** for object mapping with Lombok integration
- Lombok for reducing boilerplate
- SpringDoc OpenAPI for API documentation
- Spring REST Docs for documentation generation
- WireMock for testing external dependencies (core-banking-service)

**Architecture Patterns:**

**Common-API Module (Shared Interfaces):**
- `service/` - Dubbo RPC service interfaces (AccountService, CustomerService)
- `dto/request/` - Serializable request DTOs for cross-service calls
- `dto/response/` - Serializable response DTOs for cross-service calls
- `enums/` - Shared enumerations (AccountType, AccountStatus, Currency, KycStatus, etc.)
- `constants/` - Service constants (Dubbo versions, timeouts, service names)
- `errorcode/` - Shared error code definitions

**Individual Service Architecture:**
Each service follows standard Spring Boot layered architecture:
- `controller/` - REST endpoints for external APIs
- `service/` - Business logic implementations (local interfaces)
- `repository/` - Data access layer
- `entity/` - JPA entities with proper indexing and constraints
- `dto/` - Local service DTOs with "Local" prefix (e.g., `OpenAccountLocalRequest`, `AccountLocalResponse`)
  - Request DTOs: End with `LocalRequest` suffix
  - Response DTOs: End with `LocalResponse` suffix
  - Avoids naming collision with common-api DTOs
- `adapter/` - **Dubbo adapters** implementing common-api interfaces, bridging local services with RPC
- `mapper/` - MapStruct interfaces for DTO conversions (Entity ↔ Local DTO ↔ Common DTO)
- `config/` - Configuration classes including security, CORS, Dubbo, and external service configs
- `exception/` - Custom exception handling with structured error responses
- `client/` - HTTP clients for external system integration (CoreBankingClient)

## Exception Handling Architecture

The application uses a structured exception handling approach:
- `BaseException` - Abstract base for all custom exceptions with ErrorCode support
- `ErrorCode` - Enum containing error codes, messages (in Vietnamese), and HTTP status codes
- `GlobalExceptionHandler` - Centralized exception handling with detailed error responses
- Service-specific exceptions like `CifException`, `BusinessException`, `ResourceNotFoundException`
- Standardized `ErrorResponse` format with tracing, field validation errors, and conditional stack traces

## Common Commands

### Building and Running Services

**Build a service:**
```bash
cd backend/[service-name]
./mvnw clean package
```

**Run a service:**
```bash
cd backend/[service-name]
./mvnw spring-boot:run
```

**Run tests:**
```bash
cd backend/[service-name]
./mvnw test
```

**Run specific test class:**
```bash
cd backend/[service-name]
./mvnw test -Dtest=ClassName
```

**Generate documentation:**
```bash
cd backend/[service-name]
./mvnw package
```
This will trigger the asciidoctor-maven-plugin to generate REST documentation.

**Check compilation without running tests:**
```bash
cd backend/[service-name]
./mvnw clean compile
```

**Run with specific profile:**
```bash
cd backend/[service-name]
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Service Architecture and Communication

**Service Ports:**
- Customer Service: `8082` - OAuth2 resource server with Keycloak integration
- Account Service: `8083` - OAuth2 authorization server, Dubbo provider (port 20882)
- Core Banking Service: `8088` - Reactive service with circuit breakers
- Keycloak: `8081` - Identity and access management
- Nacos Registry: `8848` - Service discovery and configuration management

**Database Configuration:**
- Customer Service: `customer_db` (port 3306, password: 123456)
- Account Service: `account_db` (port 3306, password: 123456)
- Core Banking Service: `core_banking_db` (port 3306, password: 123456)
- Keycloak: `keycloak` database (port 3309, password: 123456)

**Inter-service Communication Architecture:**

The project uses a **hybrid communication pattern**:

1. **Dubbo RPC (Internal Microservices):**
   - Customer Service ↔ Account Service: Dubbo RPC via Nacos registry
   - Uses binary protocol (Hessian2) for high performance
   - Service discovery, load balancing, and failover via Nacos (port 8848)
   - Common-API module defines shared interfaces and DTOs
   - `@DubboService` exposes services, `@DubboReference` consumes services
   - Configuration: `dubbo.registry.address=nacos://localhost:8848`

2. **HTTP REST (External Systems):**
   - All Services → External Core Banking System: HTTP via CoreBankingClient (http://localhost:9090/api)
   - Frontend → Backend Services: Standard REST APIs
   - Uses WebClient for reactive HTTP calls where needed

**When to Use Each:**
- **Dubbo RPC**: Internal microservice-to-microservice calls within cluster
- **HTTP REST**: External system integration, frontend APIs, third-party services

### Infrastructure

**Docker Compose:**
```bash
docker compose up -d
```
Starts infrastructure services:
- Keycloak (port 8081) - Identity and access management
- Keycloak MySQL database (port 3309)
- Nacos (port 8848, 9848, 9849) - Service registry and configuration management

**Verify infrastructure:**
```bash
# Check containers are running
docker ps

# Access Nacos console
# http://localhost:8848/nacos (username: nacos, password: nacos)

# Access Keycloak admin console
# http://localhost:8081/admin (username: admin, password: admin)
```

### Service-Specific Notes

**Customer Service:**
- OAuth2 resource server secured with Keycloak JWT tokens
- Role-based access control: USER role for `/api/customers/**`, ADMIN role for `/admin/**`
- Complete user registration and authentication:
  - Register: Creates user in Keycloak → Core Banking → Local DB (with rollback on failure)
  - Login: Issues JWT tokens via Keycloak OAuth2 password grant
  - Profile management: View and update customer information
- Dubbo provider exposing `CustomerService` RPC interface for customer validation
- Dubbo consumer calling `AccountService` for account operations
- Keycloak realm: `klb-bank`, client: `customer-service`
- Uses MapStruct for entity-DTO mapping with Lombok integration
- CORS configuration for cross-origin requests
- Comprehensive validation with custom error messages
- MySQL database: `customer_db`

**Core Banking Service:**
- CIF (Customer Information File) management system
- Reactive programming with WebFlux for non-blocking operations
- Circuit breaker patterns with Resilience4J for fault tolerance
- Structured exception handling with Vietnamese error messages
- Entity relationships: CIF_Master with proper indexing on cif_number, username, national_id
- WireMock integration for testing external dependencies
- SpringDoc OpenAPI for API documentation at `/swagger-ui.html`

**Account Service:**
- OAuth2 resource server with Keycloak JWT validation
- Complete account management REST API:
  - Open accounts (CHECKING, SAVINGS, CREDIT)
  - View account details and balances
  - List customer accounts
  - Update account information (credit limits, interest rates, term months)
  - Close accounts with validation (zero balance required)
- Dubbo provider exposing `AccountService` RPC interface (port 20882)
- Integrates with customer-service via Dubbo for customer validation
- Integrates with Core Banking system via HTTP for account registration
- Transaction management with pessimistic locking for updates
- Status history tracking for audit trail
- MySQL database: `account_db`

## Development Workflow

1. Each service is independently buildable and runnable
2. Services use Maven Wrapper (`mvnw`) - no global Maven installation required
3. All services include Spring Boot DevTools for hot reloading during development
4. API documentation is automatically generated via SpringDoc OpenAPI (available at `/swagger-ui.html`)
5. REST documentation is generated via Spring REST Docs during build process using AsciiDoctor
6. Exception handling follows structured approach with ErrorCode enums and centralized GlobalExceptionHandler

## Security Configuration

**Authentication & Authorization:**
- Customer Service uses OAuth2 Resource Server with Keycloak JWT validation
- JWT tokens are validated against Keycloak issuer: `http://localhost:8081/realms/klb-bank`
- Custom `KeycloakRoleConverter` for role mapping from JWT claims
- Role-based access: USER, ADMIN roles with different endpoint permissions

**CORS Configuration:**
- Customer Service has CORS enabled for cross-origin requests
- Configure allowed origins, methods, and headers as needed

## Data Architecture

**Core Banking Entities:**
- `CIF_Master` - Central customer information with unique constraints on cif_number, username, national_id
- Entity relationships with proper JPA indexing for performance
- Hibernate DDL auto-update enabled for development
- MySQL dialect optimized for version 8.0+

**Entity Lifecycle Management:**
- Version-based optimistic locking with `@Version`
- Automatic timestamp tracking with `@CreationTimestamp` and `@UpdateTimestamp`
- UUID-based primary keys using `@UuidGenerator`

## Testing Strategy

**Framework Support:**
- Spring Boot Test for integration testing
- Spring Security Test for authentication/authorization testing
- WireMock for external service mocking (core-banking-service)
- Spring REST Docs for API documentation generation from tests
- Reactor Test for reactive programming testing (core-banking-service)

## MapStruct Configuration

**Object Mapping Setup:**
- MapStruct 1.6.3 with Lombok integration across all services
- Annotation processor configuration ensures proper compilation order: Lombok → Lombok-MapStruct-Binding → MapStruct
- Component model set to Spring for dependency injection (auto-wired mappers)
- All DTOs use Lombok `@Data`, `@Builder` annotations

**Key Mappers:**

**customer-service:**
- `CustomerMapper` - Handles all customer-related mappings:
  - `toEntity()`: CustomerLocalRegisterRequest → Customer entity
  - `toResponse()`: Customer entity → CustomerResponse
  - `toLocalRegisterRequest()`: Common-API → Local DTO (for Dubbo)
  - `toCommonDetailsResponse()`: Local → Common-API DTO (for Dubbo)
- `AddressMapper` - Handles address conversions:
  - `toEntity()`: AddressLocalRequest → Address entity
  - `toLocalAddressRequest()`: Common-API → Local DTO
  - `toCommonAddressRequest()`: Local → Common-API DTO

**account-service:**
- `AccountMapper` - Complete mapping chain:
  - `toResponseDTO()`: Account entity → AccountLocalResponse
  - `toCommonResponse()`: AccountLocalResponse → AccountResponse (Enum → String)
  - `toLocalRequest()`: OpenAccountRequest → OpenAccountLocalRequest (String → Enum)
  - `toCommonListResponse()`: Local list → Common-API list
  - Automatic enum conversions and null-safe mappings

**Why MapStruct for Dubbo:**
- Zero runtime overhead (compile-time code generation)
- Type-safe conversions between local DTOs and common-api DTOs
- Automatic enum conversions (String ↔ Enum)
- Automatic null checks and collection mappings
- Required for adapters bridging local services with Dubbo RPC interfaces

## Dubbo RPC Architecture

**Provider Pattern (Exposing Services):**
Services exposing operations via Dubbo must implement:
1. **Adapter class** in `adapter/` package implementing common-api interface
2. Annotated with `@DubboService(version = "1.0.0", timeout = 5000, interfaceClass = CommonApiInterface.class)`
3. Delegates to local service implementation
4. Uses MapStruct mapper for DTO conversions (Local ↔ Common)
5. Configuration: `dubbo.scan.base-packages=com.example.[service].adapter`

**Consumer Pattern (Calling Remote Services):**
Services calling remote operations use:
1. `@DubboReference(version = "1.0.0", timeout = 5000)` annotation to inject remote service proxy
2. Calls common-api interface methods
3. Nacos registry resolves service location automatically
4. Configuration in application.properties:
   - `dubbo.registry.address=nacos://localhost:8848`
   - `dubbo.registry.parameters.namespace=klb-bank`
   - `dubbo.registry.parameters.group=banking-services`
   - `dubbo.consumer.timeout=5000`
   - `dubbo.consumer.check=false` (for development - allows startup without providers)

**Service Discovery Flow:**
1. Provider service starts → Registers with Nacos (service name + version + protocol port)
2. Consumer service starts → Subscribes to Nacos for required services
3. Consumer makes RPC call → Nacos returns available provider instances
4. Dubbo handles load balancing, failover, retry automatically

**Critical Implementation Pattern:**
```
REST Controller (external API)
    ↓
Local Service Interface (business logic contract)
    ↓
Local Service Implementation (business logic)
    ↓
Local Repository (database access)

FOR DUBBO EXPOSURE:
Dubbo Adapter (implements common-api interface)
    ↓ delegates to
Local Service Implementation
    ↓ uses MapStruct for DTO conversion
Common-API DTOs ↔ Local DTOs
    ↓ @DubboService annotation
Dubbo RPC Protocol (Hessian2) → Nacos Registry
```

**Common-API Module Role:**
- Contains ONLY interfaces, DTOs, enums, constants
- NO implementations, NO business logic
- Shared dependency across all microservices
- Defines the contract for RPC communication
- All DTOs must implement `Serializable`

## Troubleshooting

**Common Issues:**

**MapStruct Compilation Errors:**
- Ensure proper annotation processor order: Lombok → Lombok-MapStruct-Binding → MapStruct
- Clean and recompile: `./mvnw clean compile`
- Check for circular dependencies in mapper interfaces

**Dubbo Service Communication Failures:**
- **No provider available**: Ensure provider service started before consumer, check Nacos console for registered services
- **Version mismatch**: Verify `@DubboService` and `@DubboReference` use same version (1.0.0)
- **Namespace/group mismatch**: Ensure all services use same Nacos namespace (klb-bank) and group (banking-services)
- **Timeout exceptions**: Increase timeout in `@DubboReference(timeout = 10000)` or check business logic performance
- **Service not registering**: Verify `dubbo.scan.base-packages` includes adapter package

**HTTP Service Communication Failures:**
- Verify service security configurations allow inter-service calls
- Check service ports and URLs in application properties
- Ensure Core Banking Service security permits `/api/cif/**` endpoints

**Authentication Issues:**
- Verify Keycloak is running on port 8081
- Check JWT token validation and realm configuration
- Ensure role mappings match security configurations

**Database Connection Problems:**
- Start databases via Docker Compose: `docker-compose up -d`
- Verify MySQL is accessible on configured ports
- Check database names and credentials in application properties

## Development Environment Setup

**Prerequisites:**
- Java 17 or higher
- Docker and Docker Compose for infrastructure
- No global Maven installation required (uses wrapper)

**Initial Setup:**
1. Start infrastructure: `docker-compose up -d`
2. Build services: `cd backend/[service-name] && ./mvnw clean package`
3. Run services in order: Core Banking → Customer → Account
4. Access Swagger UI at `http://localhost:[port]/swagger-ui.html`

**Service Startup Order:**
1. **Infrastructure first**: `docker compose up -d` (Keycloak, MySQL, Nacos)
2. **Dubbo Providers**: Start services with `@DubboService` first
   - Account Service (port 8083, Dubbo port 20882) - Provider for account operations
3. **Dubbo Consumers**: Start services with `@DubboReference`
   - Customer Service (port 8082) - Consumer of account-service
4. **Optional**: Core Banking Service (port 8088) - Standalone reactive service

**Verification:**
- Check Nacos console: http://localhost:8848/nacos (nacos/nacos)
- Look for registered services in "Service Management" → "Service List"
- Provider services should show with Dubbo protocol port (20882)
- Check service health status in Nacos dashboard

## DTO Naming Conventions

**IMPORTANT**: This project uses a "Local" prefix pattern to avoid naming collisions between local service DTOs and common-api DTOs.

**Naming Pattern:**
- **Common-API DTOs** (for Dubbo RPC): No prefix
  - Examples: `OpenAccountRequest`, `AccountResponse`, `CustomerRegisterRequest`
  - Location: `common-api/dto/request` and `common-api/dto/response`
  - Use String for enums (for serialization compatibility)

- **Local Service DTOs** (for REST controllers): "Local" prefix
  - Examples: `OpenAccountLocalRequest`, `AccountLocalResponse`, `CustomerLocalRegisterRequest`
  - Location: `[service]/dto/request` and `[service]/dto/response`
  - Use actual Enum types for type safety

**Why This Pattern?**
- Avoids import collisions when both DTOs are used in the same file (adapters)
- Makes it explicit which layer the DTO belongs to
- Allows clear distinction in MapStruct mappings
- IDE auto-import works correctly without fully qualified names

**Example Usage in Adapter:**
```java
@DubboService
public class AccountServiceDubboAdapter implements com.example.commonapi.service.AccountService {

    @Override
    public AccountResponse openAccount(OpenAccountRequest request) {
        // Convert common-API DTO to local DTO
        OpenAccountLocalRequest localRequest = mapper.toLocalRequest(request);

        // Call local service
        AccountLocalResponse localResponse = localService.openAccount(localRequest);

        // Convert local DTO back to common-API DTO
        return mapper.toCommonResponse(localResponse);
    }
}
```

## API Endpoints

**Customer Service (Port 8082):**
- `POST /api/auth/register` - Register new customer (public)
- `POST /api/auth/login` - Login and get JWT tokens (public)
- `GET /api/customers/me` - Get authenticated customer info (USER role)
- `PATCH /api/customers/me` - Update customer information (USER role)
- `POST /api/customers/kyc/verify` - eKYC verification (USER role)

**Account Service (Port 8083):**
- `POST /api/accounts` - Open new account (USER role)
- `GET /api/accounts/my-accounts` - List customer accounts (USER role)
- `GET /api/accounts/{accountNumber}` - Get account details (USER role)
- `GET /api/accounts/{accountNumber}/balance` - Get account balance (USER role)
- `GET /api/accounts/{accountNumber}/status` - Check account status (USER role)
- `PATCH /api/accounts/{accountNumber}` - Update account info (USER role)
- `DELETE /api/accounts/{accountNumber}` - Close account (USER role)

**Core Banking Service (Port 8088):**
- `POST /api/cif/customers` - Create CIF customer
- `GET /api/cif/customers/{cifNumber}` - Get customer CIF details
- Additional CIF management endpoints

**Security Notes:**
- All `/api/auth/**` endpoints are public (no authentication required)
- All other `/api/**` endpoints require JWT authentication with USER role
- JWT token obtained from login must be included in `Authorization: Bearer <token>` header
- Customer ID is extracted from JWT token automatically for security
- Users can only access/modify their own accounts (ownership verified in service layer)
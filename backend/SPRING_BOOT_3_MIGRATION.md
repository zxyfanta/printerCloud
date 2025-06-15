# Spring Boot 3.x Migration Guide

## Issues Fixed

The `ClassNotFoundException: User` error was caused by incompatibility between Spring Boot 2.7.14 and Java 21, along with outdated javax imports.

## Changes Made

### 1. Updated Spring Boot Version
- Changed from Spring Boot 2.7.14 to 3.2.0 in `pom.xml`
- Spring Boot 3.x is required for Java 21 support

### 2. Updated Package Imports
Migrated from `javax.*` to `jakarta.*` packages:

#### Entity Classes
- `User.java`: `javax.persistence.*` → `jakarta.persistence.*`
- `PrintOrder.java`: `javax.persistence.*` → `jakarta.persistence.*`
- `SystemConfig.java`: `javax.persistence.*` → `jakarta.persistence.*`
- `PrintFile.java`: `javax.persistence.*` → `jakarta.persistence.*`
- `PriceConfig.java`: `javax.persistence.*` → `jakarta.persistence.*`

#### Service Classes
- `DataInitializationService.java`: `javax.annotation.PostConstruct` → `jakarta.annotation.PostConstruct`
- `UserService.java`: `javax.annotation.PostConstruct` → `jakarta.annotation.PostConstruct`

### 3. Updated Dependencies
- SpringDoc OpenAPI: Updated from `springdoc-openapi-ui:1.6.14` to `springdoc-openapi-starter-webmvc-ui:2.2.0`

## Additional Changes Needed

If you encounter more issues, check for these common Spring Boot 3.x migration items:

### 1. Validation Annotations
If using validation, update:
```java
// Old
import javax.validation.constraints.*;

// New
import jakarta.validation.constraints.*;
```

### 2. Servlet API
If using servlet classes, update:
```java
// Old
import javax.servlet.*;

// New
import jakarta.servlet.*;
```

### 3. Configuration Properties
Update any configuration that might reference old package names.

## How to Test

1. Clean and compile the project:
   ```bash
   mvn clean compile
   ```

2. Run the application:
   ```bash
   mvn spring-boot:run
   ```

3. Check that the application starts without the `ClassNotFoundException: User` error

4. Verify API endpoints work:
   - http://localhost:8082/swagger-ui.html (Swagger UI)
   - http://localhost:8082/h2-console (H2 Database Console)

## Rollback Plan

If issues persist, you can rollback by:
1. Reverting Spring Boot version to 2.7.14 in `pom.xml`
2. Changing all `jakarta.*` imports back to `javax.*`
3. Downgrading Java version to 17 or 11

However, the recommended approach is to fix any remaining compatibility issues rather than rollback.
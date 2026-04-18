# Claude Code Configuration for DummyDataGenerator

## Testing Commands
- **Unit Tests**: `mvn test`
- **Integration Tests**: `mvn verify -P integration-tests`
- **All Tests**: `mvn verify -P all-tests`

## Actuator Endpoints (Secured)
- **Health Check**: `curl -u admin:secure-admin-password-2025 http://localhost:8080/manage/health`
- **Application Info**: `curl -u admin:secure-admin-password-2025 http://localhost:8080/manage/info`
- **Metrics**: `curl -u admin:secure-admin-password-2025 http://localhost:8080/manage/metrics`
- **Environment**: `curl -u admin:secure-admin-password-2025 http://localhost:8080/manage/env`
- **Thread Dump**: `curl -u admin:secure-admin-password-2025 http://localhost:8080/manage/threaddump`

## Environment Variables
- **ACTUATOR_PASSWORD**: Override the default actuator password (recommended for production)

## Build Commands
- **Clean Build**: `mvn clean package`
- **Skip Tests**: `mvn clean package -DskipTests`

## Development Commands
- **Run Application**: `mvn spring-boot:run`
- **Run with Profile**: `mvn spring-boot:run -Dspring-boot.run.profiles=test`

## Docker Commands
- **Build Image**: `docker build -t ddg-app .`
- **Run Container**: `docker-compose up`

## Code Quality
- **Dependency Check**: `mvn dependency:tree`
- **Security Audit**: Check for OWASP dependency vulnerabilities

## Project Structure Notes
- Main application: `src/main/java/com/hisham/dummydatagenerator/`
- Configuration: `src/main/resources/application.properties`
- Controllers: `/controller/` - REST endpoints for database operations
- Generators: `/generator/` - Data generation logic
- Connectors: `/connectors/` - Database connectivity layer
- Schema: `/schema/` - Database introspection utilities
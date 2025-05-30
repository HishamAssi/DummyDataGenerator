# 🧪 DummyDataGenerator

**DummyDataGenerator** is a lightweight SaaS-ready Java Spring Boot application that connects to relational databases, introspects table schemas, and generates realistic dummy data to populate those tables. This is especially useful for testing, prototyping, and demos.

---

## 🚀 Features

- 🔌 Connects to PostgreSQL and SQL Server (other DBs coming soon)
- 🧠 Introspects table schema: columns, types, nullability
- 🤖 Generates dummy data based on SQL data types
- 📝 Inserts rows directly into your selected table OR into a kafka topic
- 🌐 REST API for easy integration
- 💡 Modular architecture for extensibility

---

## 🛠️ Technologies

- Java 17+
- Spring Boot
- Spring Data / JDBC
- PostgreSQL
- Maven
- RESTful API
- Docker (upcoming)
- Kubernetes (planned)

---

## 📦 Getting Started

### 1. **Clone the Project**

```bash
git clone https://github.com/yourusername/dummy-data-generator.git
cd dummy-data-generator
```

### 2. Configure PostgreSQL
Make sure PostgreSQL is running locally. Create a database and table:

```sql
CREATE DATABASE ddg_init;

\c ddg_init

CREATE TABLE public.test_table (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255),
    age INT,
    birthdate DATE
);
```
### 3. Configure Application Properties
Edit `src/main/resources/application.properties`:

```
spring.application.name=DummyDataGenerator

# PostgreSQL config
spring.datasource.url=jdbc:postgresql://localhost:5432/ddg_init
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver

# Hibernate (optional)
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Server settings
server.port=8080
server.servlet.context-path=/api
```

### 🧪 Running the App
With Maven:
```bash
mvn spring-boot:run
```
Or in IntelliJ:
* Open DummyDataGeneratorApplication.java 
* Right-click → Run

Once running, Tomcat should start at `http://localhost:8080/api`

## 📡 API Usage
### 🧠 1. Get Table Metadata
```pgsql
GET /api/schema/{schema}/{table}
```
Example:

```pgsql
GET http://localhost:8080/api/schema/public/test_table
```
Returns column names, types, and nullability info.

### 🧬 2. Generate & Insert Dummy Data
```pgsql
POST /api/data/{schema}/{table}?rows=100
```

Example:

```bash
POST http://localhost:8080/api/data/public/test_table?rows=100
```
Generates 100 rows of type-matched dummy data and inserts them into test_table.

## ⚙️ Data Types Supported (so far)
SQL Type	Generator Example
varchar	Random name string
text	Random UUID-style string
int4/int	Random integer [1, 10000]
date	Random date (2000–2022)

## 🔧 Roadmap
 CSV/JSON export instead of DB insert

 UI frontend with React

 Support more databases (MySQL, SQLite, Mongo)

 Docker & Kubernetes deployment

 Schema validation and error reporting

## 🤝 Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss improvements.

## 🚀 CI/CD Pipeline

This project uses GitHub Actions for continuous integration and deployment. The pipeline:

1. Builds and tests the application
2. Builds a Docker image
3. Pushes the image to GitHub Container Registry (ghcr.io)

### Pipeline Triggers
- On push to main branch
- On pull requests to main branch

### Image Tags
The Docker image is tagged with:
- Branch name
- PR number (for pull requests)
- Semantic version (if using tags)
- Git SHA

### Using the Container Registry

To use the container registry:

1. Authenticate to GitHub Container Registry:
```bash
echo $GITHUB_TOKEN | docker login ghcr.io -u USERNAME --password-stdin
```

2. Pull the image:
```bash
docker pull ghcr.io/USERNAME/dummy-data-generator:latest
```
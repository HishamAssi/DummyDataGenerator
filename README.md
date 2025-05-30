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

### Universal Connector Endpoints

All API requests require the following Parameters:

- `jdbcUrl`: Standard jdbc url used for connecting to Database
- `username`: Database username for the jdbcUrl
- `password`: Database password for the jdbcUrl
- `dbType`: Currently only supports "POSTGRESQL" and "SQLSERVER"

#### 1. Introspect Table Schema
```http
POST /api/universal/introspect
Content-Type: application/json

{
    "jdbcUrl": "jdbc:postgresql://localhost:5432/your_db",
    "username": "your_username",
    "password": "your_password",
    "dbType": "POSTGRESQL",
    "schema": "public",
    "table": "your_table"
}
```
Returns detailed table metadata including column names, types, and constraints.

Insert API requests have the option to go to a kafka topic by specifying the following parameters:
- `topic`: A kafka topic name, this will automatically switch the apply to the kafka topic while generating metadata from the source table.
- `kafkaConfig`: By default, the API will try to connect to localhost:9092 for the kafka topic but you can specify any bootstrap server and any other kafka configurations needed.

#### 2. Insert Dummy Data
```http
POST /api/universal/insert?row_count=100&tnx=1
Content-Type: application/json

{
    "jdbcUrl": "jdbc:postgresql://localhost:5432/your_db",
    "username": "your_username",
    "password": "your_password",
    "dbType": "POSTGRESQL",
    "schema": "public",
    "table": "your_table",
    "topic": "optional_kafka_topic",
    "kafkaConfig": {
        "bootstrapServers": "kafka:9092",
        "keySerializer": "org.apache.kafka.common.serialization.StringSerializer",
        "valueSerializer": "org.springframework.kafka.support.serializer.JsonSerializer",
        "additionalProperties": {
        "security.protocol": "SASL_SSL",
        "sasl.mechanism": "PLAIN",
        "sasl.jaas.config": "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"user\" password=\"pass\";"
        }
    }
}
```
Optional request Parameters for regular insert:
- `row_count`: Number of rows to generate (default: 100)
- `tnx`: Number of transactions to perform (default: 1)

#### 3. Insert Into All Tables
```http
POST /api/universal/insert-all
Content-Type: application/json

{
    "jdbcUrl": "jdbc:postgresql://localhost:5432/your_db",
    "username": "your_username",
    "password": "your_password",
    "dbType": "POSTGRESQL",
    "schema": "public",
    "rowsPerTable": 100,
    "includeTables": ["table1", "table2"],
    "ignoreTables": ["table3"],
    "topic": "optional_kafka_topic",
    "kafkaConfig": {
        "bootstrapServers": "kafka:9092",
        "keySerializer": "org.apache.kafka.common.serialization.StringSerializer",
        "valueSerializer": "org.springframework.kafka.support.serializer.JsonSerializer",
        "additionalProperties": {
        "security.protocol": "SASL_SSL",
        "sasl.mechanism": "PLAIN",
        "sasl.jaas.config": "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"user\" password=\"pass\";"
        }
    }
}
```

Optional Parameters for insert-all:
- `includeTables`: Instead of generating data for all tables in the schema, generate data only for this subset.
- `excludeTables`: Exclude these tables from the list of tables that data is being generated for, this can be used in tandem with includeTables.


### Response Examples

#### Introspect Response
```json
{
    "tableName": "your_table",
    "columns": [
        {
            "name": "id",
            "type": "INTEGER",
            "nullable": false,
            "primaryKey": true
        },
        {
            "name": "name",
            "type": "VARCHAR",
            "nullable": true,
            "primaryKey": false
        }
    ]
}
```

#### Insert Response
```json
"Inserted 1 transaction(s) with 100 dummy rows into your_table"
```

#### Insert All Response
```json
{
    "message": "Insert complete",
    "rowsInserted": {
        "table1": 100,
        "table2": 100
    }
}
```

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
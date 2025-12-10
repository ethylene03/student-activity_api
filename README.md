# student-activity-api

This is the backend repository for the student activity tracker project.

## Recommended IDE Setup

[IntelliJ IDEA](https://www.jetbrains.com/idea/) + [Spring Boot (Kotlin)](https://spring.io/guides/tutorials/spring-boot-kotlin).

## Project Setup

### Project Configuration
#### Database

Modify environment variables to include:
- `DB_PATH`
- `DB_USERNAME`

#### JWT

Modify environment variables to include:
- `JWT_SECRET`

### Project Commands

#### Build Project

```sh
./gradlew build
```

#### Run the Application

```sh
./gradlew bootRun
```

#### Clean and Build

```sh
./gradlew clean build
```

#### Run the JAR file after building

```sh
java -jar build/libs/your-app-name-version.jar
```

# Mini-Url

A fast and reliable URL shortener service built with Jakarta EE, Spring Data JPA, Spring MVC, Lombok, and Java 21.

---

## Features

- Shorten long URLs to compact, anonymized links
- Redirect users from short URLs to the original addresses
- RESTful API for integration with third-party applications
- Persistent storage with JPA and support for SQL databases
- Production-ready with Docker support
- Built-in input validation and exception handling
- Easily extendable for analytics, authentication, or UI

---

## Tech Stack

- **Java 21**
- **Jakarta EE**
- **Spring Boot** (with Spring Data JPA & Spring MVC)
- **Lombok**
- **Gradle** (build management)
- **Docker** (deployment)
- **H2/PostgreSQL/MySQL** (configurable DB)
- **JUnit** (testing)

---

## Getting Started

### Prerequisites

- Java 21+
- Gradle 8.x+
- Docker (optional, for containerized builds)

### Local Development

1. **Clone the repository:**
   ```sh
   git clone <your-repository-url>
   cd mini-url
   ```

2. **Build the project:**
   ```sh
   ./gradlew clean build
   ```

3. **Run the application:**
   ```sh
   ./gradlew bootRun
   ```
   The API will be available at: `http://localhost:8080`

4. **API Endpoints:**

    - **Shorten a URL**
      ```
      POST /api/shorten
      Content-Type: application/json
 
      {
        "url": "https://www.example.com"
      }
      ```
      **Response:**
      ```json
      {
        "shortUrl": "http://localhost:8080/abc123"
      }
      ```

    - **Redirect to original URL**
      ```
      GET /{shortUrl}
      ```
      This will issue an HTTP 302 redirect to the original link.

---

## Configuration

- **Database**:
    - Uses in-memory H2 database by default.
    - To use PostgreSQL/MySQL, update `src/main/resources/application.properties`:

      ```properties
      spring.datasource.url=jdbc:postgresql://localhost:5432/miniurl
      spring.datasource.username=youruser
      spring.datasource.password=yourpassword
      spring.jpa.hibernate.ddl-auto=update
      ```

- **Port**: Change `server.port=8080` in `application.properties` if needed.

---

## Docker

To run the service via Docker:

```docker
 docker build -t mini-url . docker run -p 8080:8080 mini-ur
```

---

## Contributing

1. Fork this repository
2. Create your feature branch (`git checkout -b feature/awesome-feature`)
3. Commit your changes (`git commit -m 'Add awesome feature'`)
4. Push to the branch (`git push origin feature/awesome-feature`)
5. Open a pull request

---

## License

This project is licensed under the MIT License.

---

## Contact

For issues, please open a ticket in the repository.

---

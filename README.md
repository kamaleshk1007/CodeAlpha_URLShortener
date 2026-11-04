# CodeAlpha_URLShortener

A simple URL shortener built with Java and Spring Boot, created for the CodeAlpha internship (Task 1).

## Features

- REST API that turns a long URL into a unique 7-character short code
- Database (H2, file-based) that stores each short code with its original URL, so links survive restarts
- Redirect route: opening the short URL sends you to the original URL (HTTP 302)
- Input validation: only valid http/https URLs are accepted
- Basic plain HTML frontend to shorten URLs from the browser

## Tech stack

- Java 17+
- Spring Boot (Spring Web, Spring Data JPA, Validation)
- H2 database
- Maven
- HTML, CSS and JavaScript (no frameworks)

## Getting started

**Prerequisite:** JDK 17 or newer.

```bash
git clone https://github.com/kamaleshk1007/CodeAlpha_URLShortener.git
cd CodeAlpha_URLShortener
./mvnw spring-boot:run
```

Then open http://localhost:8080 in your browser.

## API

### Shorten a URL

`POST /api/shorten`

Request body:

```json
{ "url": "https://www.example.com/some/very/long/link" }
```

Response (`201 Created`):

```json
{
  "shortCode": "aB3dE9x",
  "shortUrl": "http://localhost:8080/aB3dE9x",
  "originalUrl": "https://www.example.com/some/very/long/link"
}
```

An invalid URL returns `400 Bad Request` with `{ "error": "..." }`.

### Open a short URL

`GET /{shortCode}`

Redirects (`302 Found`) to the original URL. Returns `404 Not Found` if the code does not exist.

## How it works

1. The client sends a long URL to `POST /api/shorten`.
2. The service validates it, then generates a random 7-character Base62 code.
3. If the code already exists in the database, a new one is generated. A unique constraint on `short_code` also guards against collisions.
4. The code and original URL are saved, and the short URL is returned.
5. Opening `/{shortCode}` looks up the code and redirects to the original URL.

## Project structure

```
src/main/java/com/codealpha/urlshortener/
├── controller/   UrlController, GlobalExceptionHandler
├── service/      UrlShortenerService
├── repository/   UrlMappingRepository
└── model/        UrlMapping
src/main/resources/
├── static/       index.html (frontend)
└── application.properties
```

## Viewing the database

While the app is running, open http://localhost:8080/h2-console and use:

- JDBC URL: `jdbc:h2:file:./data/urlshortener`
- User: `sa`
- Password: ()
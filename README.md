# BFH Java Qualifier — Auto Submitter

This Spring Boot app:
1. Calls the webhook generator on startup
2. Picks the correct SQL (Q1 for odd, Q2 for even based on last two digits of regNo)
3. Submits the SQL to the returned webhook with the JWT access token

## How to run
```bash
mvn clean package
java -jar target/bfh-java-qualifier-1.0.0.jar
```

## Config (already filled)
- Name: Vikas J V
- RegNo: U25UV22T064063
- Email: vikasjv@gmail.com

Endpoints are set from the PDF prompt.

# Mnemosyne

## Overview

A Kotlin gRPC API

## Requirements

- PostgreSQL with Postgis
- Latest JDK

## Build and Run

```bash
make migrate
make run
```

`make migrate` will migrate the database schema into the Postgres database through the Flyway schema management tool.

You can run `MnemosyneApplication` from IntelliJ IDEA directly to start Spring Boot application as well.

## Dockerization

### Build Docker image

```
docker build -t seb7887/mnemosyne . -f Dockerfile
```

### Run Mnemosyne in Docker

```
docker run --rm -d -p 8080:8080 -p 6565:6565 --name mnemosyne seb7887/mnemosyne
```

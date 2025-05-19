# ⚡ VPP - Virtual Power Plant Microservices System

[![Build](https://img.shields.io/badge/build-passing-brightgreen.svg)]()
[![Tests](https://img.shields.io/badge/tests-passing-blue.svg)]()

**Virtual Power Plant (VPP)** is a microservices-based energy data api that processes and aggregates battery metadata. It uses Apache Kafka for event-driven communication between services and uses postgresql for storing the data. ( This proposed architecture is overkill for a small application. But, it is desiged keeping scalability in mind i.e. thousands of battery data will be added to the system every second)

---

## 🧰 Technologies Used

* Java 17
* Spring Boot
* Spring Cloud (Eureka for service discovery)
* Apache Kafka
* Docker + Docker Compose
* JPA/Hibernate + H2 Database
* Lombok
* Maven

---

## 🚀 How to Start the Application

To start the entire VPP microservices application stack (vpp-web, vpp-worker, Kafka, Zookeeper, service-discovery, postgres):

```bash
docker compose up --build
```

This will bring up:

* `vpp-web` (Producer)
* `vpp-worker` (Consumer)
* `Kafka broker + Zookeeper`
* `Eureka service registry`
* `Postgres`

---

## 🧪 Running Tests

To run unit and integration tests locally:

```bash
./mvnw verify
```

Or for specific modules:

```bash
./mvnw -pl vpp-web test
./mvnw -pl vpp-worker test
```

---

## 🧱 Microservices Architecture

The VPP system follows an event-driven architecture composed of the following services:

### 1. 🔋 vpp-web (Producer)

* Receives battery metadata (name, postcode, capacity).
* Publishes battery metadata as Kafka messages to a topic (e.g., `vpp_topic`).

### 2. 📊 Aggregator Service (Consumer)

* Subscribes to `vpp_topic` topic.
* Stores incoming data in postgres.
* Exposes aggregation API (average capacity, total capacity, sorted names).

### 3. 🛰️ Kafka Broker

* Acts as a message bus between producer and consumer.
* Backed by Zookeeper.

### 4. 🧭 Eureka Server (Service Registry)

* Discovers and registers microservices dynamically.
* Enables inter-service communication without hardcoded URLs.

---

## 🌐 REST APIs

### 🔋 Producer Service - `vpp-web`

| Method | Endpoint               | Description                       |
| ------ | ---------------------- | --------------------------------- |
| POST   | `/api/batteries`       | Create and publish battery data   |
| GET    | `/api/batteries`       | Get all stored battery records    |
| GET    | `/api/batteries/range` | Query batteries by postcode range and optionally with minWatt and maxWatt |

**Sample Payload**:

```json
[
  {
    "name": "Battery A",
    "postcode": 100,
    "capacity": 2000.0
  },
  {
    "name": "Battery B",
    "postcode": 150,
    "capacity": 2500.0
  }
]
```

### 📊 Aggregator Service - `vpp-worker`

| Method | Endpoint          | Description                               |
| ------ | ----------------- | ----------------------------------------- |
| GET    | `/api/batteries/range` | Get total/average capacity and names list. And, it is invoked from vpp-web only |

**Sample Response**:

```json
{
  "names": ["Battery A", "Battery B"],
  "totalCapacity": 4500.0,
  "averageCapacity": 2250.0
}
```

---

## 🐳 Docker Overview

The application uses the following Docker containers:

| Container Name         | Description                      |
| ------------------     | -------------------------------- |
| vpp-web                | Spring Boot app (Kafka producer) |
| vpp-worker             | Spring Boot app (Kafka consumer) |
| zookeeper              | Required for Kafka broker        |
| kafka                  | Kafka message broker             |
| service-discovery      | Spring Cloud service registry    |

---

## 📂 Repository Structure

```bash
.
├── vpp-service-discovery/         # Kafka producer (Spring Boot)
├── vpp-web/                       # Kafka consumer (Spring Boot)
├── vpp-worker/                    # Service discovery
├── docker-compose.yml             # Local orchestration
└── Dockerfile

```
---

## 📈 Future Enhancements

* Add Prometheus + Grafana for observability
* Secure inter-service communication with OAuth2
* Enable schema validation for Kafka messages (Avro/Schema Registry)

# MotorGuard ⚙️
> Real-time IoT-based Predictive Maintenance System for Industrial Motorss

[![ESP32](https://img.shields.io/badge/ESP32-Firmware-blue?logo=espressif)](https://www.espressif.com/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-61DAFB?logo=react)](https://react.dev/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-336791?logo=postgresql)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker)](https://www.docker.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## Overview

**MotorGuard** is a full-stack IoT system that continuously monitors the health of industrial motors using real-time sensor data. It detects faults before they cause failure by combining threshold-based alerting with vibration trend analysis — giving you a Warning before a motor reaches a Critical state.

Built as a **Microprocessor & Microcontroller System Design** sessional project, it integrates embedded firmware, a REST backend, a relational database, and a live web dashboard into a single cohesive system.

---

## System Architecture

```
Sensors (MPU6050 · DS18B20 · ACS712)
         │
         ▼
    ESP32 (Arduino C++)
    • Moving-average filter
    • Local threshold evaluation
    • HTTP POST every 5s
         │
         ▼  WiFi (local network)
Spring Boot Backend (Java 17)
    • REST API
    • Alert generation
    • Vibration trend analysis
         │
    ┌────┴────┐
    ▼         ▼
PostgreSQL   React Dashboard
(Docker)     • Live charts (Chart.js)
             • Alert panel
             • Motor status card
```

---

## Features

- 📡 **Real-time sensor acquisition** — vibration (MPU6050), temperature (DS18B20), current (ACS712) sampled every 5 seconds
- 🔴 **Three-level motor health classification** — NORMAL / WARNING / CRITICAL with LED indicators on hardware
- 📈 **Trend-based predictive alerting** — detects gradually rising vibration before it crosses the critical threshold
- 🔔 **Categorized alert system** — OVERHEAT, VIBRATION, OVERLOAD alerts with severity levels
- 📊 **Live React dashboard** — real-time charts, metric cards, alert panel with resolve action
- 🐘 **PostgreSQL with Docker** — containerized database with persistent storage
- 🔌 **RESTful API** — clean Spring Boot API consumed by both the ESP32 firmware and the React frontend
- ⚙️ **Per-motor configurable thresholds** — stored in database, no code change required

---

## Hardware Components

| Component | Role | Interface |
|---|---|---|
| ESP32 DevKit V1 | Main microcontroller + WiFi | — |
| MPU6050 | Vibration (3-axis accelerometer) | I²C (GPIO21/22) |
| DS18B20 | Temperature | 1-Wire (GPIO4) |
| ACS712 (5A) | Current sensing | Analog (GPIO34) |
| DC Motor + L298N | Demo load | External 12V supply |
| 3× LEDs (G/Y/R) | Local status indicators | GPIO2 / 5 / 18 |

---

## Tech Stack

### Embedded
- **Language:** Arduino C++
- **Board:** ESP32 DevKit V1
- **Libraries:** MPU6050, DallasTemperature, OneWire, ArduinoJson, HTTPClient

### Backend
- **Framework:** Spring Boot 3.2 (Java 17)
- **ORM:** Spring Data JPA + Hibernate
- **Database:** PostgreSQL 15 (Docker)
- **Build:** Maven

### Frontend
- **Framework:** React 18 + Vite
- **Charts:** Chart.js
- **Styling:** Inline CSS (no framework dependency)

---

## Project Structure

```
motor-guard/
├── firmware/
│   └── main.ino                  # ESP32 Arduino sketch
├── backend/
│   ├── src/main/java/com/predictive/maintenance/
│   │   ├── entity/               # Motor, SensorData, Alert, Threshold
│   │   ├── dto/                  # Request/Response DTOs
│   │   ├── repository/           # JPA repositories
│   │   ├── service/              # Business logic + alert engine
│   │   ├── controller/           # REST controllers
│   │   └── config/               # CORS + exception handler
│   └── src/main/resources/
│       └── application.properties
├── frontend/
│   └── src/
│       ├── App.jsx
│       ├── api/api.js
│       ├── hooks/useData.js
│       └── components/
│           ├── StatusCard.jsx
│           ├── MetricCard.jsx
│           ├── SensorChart.jsx
│           └── AlertPanel.jsx
├── docker-compose.yml
└── README.md
```

---

## Getting Started

### Prerequisites
- Java 17+
- Node.js 18+
- Docker Desktop
- Arduino IDE with ESP32 board support

### 1. Database

```bash
docker-compose up -d
# PostgreSQL starts on localhost:5432
# pgAdmin UI available at http://localhost:5050
```

### 2. Backend

```bash
cd backend
./mvnw spring-boot:run
# API available at http://localhost:8080
```

### 3. Register your motor (run once)

```bash
curl -X POST http://localhost:8080/api/motors \
  -H "Content-Type: application/json" \
  -d '{"name":"Motor 1","location":"Lab Bench"}'
# Note the returned id — set it as MOTOR_ID in firmware
```

### 4. Firmware

1. Open `firmware/main.ino` in Arduino IDE
2. Set your WiFi credentials and PC local IP:
```cpp
const char* WIFI_SSID     = "YOUR_WIFI_NAME";
const char* WIFI_PASSWORD = "YOUR_WIFI_PASSWORD";
const char* SERVER_URL    = "http://YOUR_PC_IP:8080/api/data";
const int   MOTOR_ID      = 1;
```
3. Upload to ESP32

### 5. Frontend

```bash
cd frontend
npm install
npm run dev
# Dashboard at http://localhost:3000
```

---

## API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/data` | Receive sensor reading from ESP32 |
| `GET` | `/api/data/{motorId}?limit=50` | Get recent readings |
| `GET` | `/api/data/{motorId}/range` | Get readings in time range |
| `GET` | `/api/alerts` | Get all active alerts |
| `GET` | `/api/alerts/{motorId}` | Get alerts for a motor |
| `PATCH` | `/api/alerts/{id}/resolve` | Mark alert as resolved |
| `POST` | `/api/motors` | Register a new motor |
| `GET` | `/api/motors` | List all motors |
| `GET` | `/api/motors/status/{motorId}` | Get motor health status |

---

## Alert Types

| Type | Trigger | Severity |
|---|---|---|
| OVERHEAT | Temperature ≥ 55°C (warn) / 70°C (critical) | WARNING / CRITICAL |
| VIBRATION | Vibration ≥ 2.0 m/s² (warn) / 5.0 m/s² (critical) | WARNING / CRITICAL |
| OVERLOAD | Current ≥ 3.0A (warn) / 4.5A (critical) | WARNING / CRITICAL |
| VIBRATION (Trend) | ≥80% of last 10 readings show rising vibration | WARNING (predictive) |

---

## Predictive Logic

MotorGuard uses two layers of fault detection:

**1. Threshold-based (reactive):**
Each incoming reading is compared against configurable per-motor thresholds stored in the database. Violations immediately generate an alert.

**2. Trend-based (predictive):**
The vibration trend algorithm fetches the last 10 readings and checks whether ≥ 80% of consecutive pairs are increasing. If so, a predictive warning is raised — flagging a degrading motor before any single reading reaches the critical threshold.

```
R = (1 / N-1) × Σ 𝟙[vᵢ > vᵢ₊₁]
If R ≥ 0.80 → Predictive VIBRATION alert generated
```

---

## Known Hardware Notes

- **ESP32 ADC noise:** ADC1 (GPIO34) is affected by WiFi radio switching. Mitigate with 500-sample oversampling + passive RC filter (10kΩ + 100nF) on the ACS712 output pin.
- **ACS712 calibration:** Measure actual zero-current VREF empirically — it depends on real supply voltage, not the assumed 1.65V nominal.
- **DS18B20 pull-up:** A 4.7kΩ resistor between the data line and 3.3V is mandatory for the sensor to respond.
- **ADC2 conflict:** Never use ADC2 pins (GPIO0, 2, 4, 12–15, 25–27) for analog reads when WiFi is active.

---

## License

MIT © 2024 — feel free to use for academic and personal projects.

---

## Acknowledgements

Built as a sessional project for **Microprocessor & Microcontroller System Design**. Inspired by the need for accessible, low-cost predictive maintenance tools in small industrial and educational settings.

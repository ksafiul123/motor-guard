/*
 * ============================================================
 *  Predictive Maintenance System — Real Hardware Firmware
 *  Board   : ESP32 DevKit V1 (38-pin)
 *  Sensors : MPU6050 (vibration) | DS18B20 (temp) | ACS712 (current)
 *  Target  : Spring Boot backend on local network (no ngrok needed)
 * ============================================================
 */

#include <WiFi.h>
#include <HTTPClient.h>
#include <Wire.h>
#include <OneWire.h>
#include <DallasTemperature.h>
#include <MPU6050.h>
#include <ArduinoJson.h>

// ─── CONFIG: Edit these before uploading ─────────────────────────────────────

// Your home/lab WiFi credentials
const char* WIFI_SSID     = "Safi's Redmi Note 9";
const char* WIFI_PASSWORD = "69696969";

// Your PC's local IP + Spring Boot port
// Find your PC IP: Windows → ipconfig | Linux/Mac → ifconfig
// Make sure PC and ESP32 are on the SAME WiFi network
const char* SERVER_URL = "http://10.94.74.169:8080/api/data";

// Motor ID — must match a row in your motors table
const int MOTOR_ID = 1;

// ─── PIN DEFINITIONS ─────────────────────────────────────────────────────────
#define DS18B20_PIN     4    // DS18B20 data line     → GPIO4
#define ACS712_PIN      34   // ACS712 output (analog) → GPIO34 (ADC1)
// MPU6050 uses I2C: SDA → GPIO21, SCL → GPIO22 (ESP32 default I2C pins)
// LEDs
#define LED_GREEN_PIN   2    // Normal  (onboard LED or external green)
#define LED_YELLOW_PIN  5    // Warning (external yellow LED)
#define LED_RED_PIN     18   // Critical (external red LED)

// ─── SENSOR THRESHOLDS ───────────────────────────────────────────────────────
const float TEMP_WARN       = 55.0;   // °C
const float TEMP_CRIT       = 70.0;   // °C
const float VIB_WARN        = 2.0;    // m/s²
const float VIB_CRIT        = 5.0;    // m/s²
const float CURRENT_WARN    = 3.0;    // Amperes
const float CURRENT_CRIT    = 4.5;    // Amperes

// ─── SAMPLING CONFIG ─────────────────────────────────────────────────────────
const int SAMPLE_COUNT     = 200;      // Readings averaged per cycle
const int SEND_INTERVAL_MS = 5000;    // POST to backend every 5 seconds

// ─── ACS712 CALIBRATION ──────────────────────────────────────────────────────
// ACS712-5A  → sensitivity = 0.185 V/A
// ACS712-20A → sensitivity = 0.100 V/A
// ACS712-30A → sensitivity = 0.066 V/A
// Change the value below to match your module
const float ACS712_SENSITIVITY = 0.185;  // V/A  (for 5A module)
float ACS712_VREF        = 1.65;   // Volts at 0A (Vcc/2 = 3.3/2)
const float CURRENT_NOISE_CUTOFF = 0.10;
// Note: ESP32 ADC reference is 3.3V (NOT 5V like Arduino)

// ─── OBJECTS ─────────────────────────────────────────────────────────────────
OneWire           oneWire(DS18B20_PIN);
DallasTemperature tempSensor(&oneWire);
MPU6050           mpu;

// ─── GLOBALS ─────────────────────────────────────────────────────────────────
String lastStatus = "NORMAL";
int    failedPosts = 0;

// =============================================================================
//  SETUP
// =============================================================================
void setup() {
  Serial.begin(115200);
  delay(1000);

  Serial.println("\n╔══════════════════════════════════════╗");
  Serial.println("║   Predictive Maintenance System      ║");
  Serial.println("║   Real Hardware Mode                 ║");
  Serial.println("╚══════════════════════════════════════╝\n");

  // LED setup
  pinMode(LED_GREEN_PIN,  OUTPUT);
  pinMode(LED_YELLOW_PIN, OUTPUT);
  pinMode(LED_RED_PIN,    OUTPUT);
  setLED("NORMAL");

  // DS18B20 init
  tempSensor.begin();
  int deviceCount = tempSensor.getDeviceCount();
  if (deviceCount == 0) {
    Serial.println("[ERROR] No DS18B20 found! Check wiring & 4.7kΩ pull-up resistor.");
  } else {
    Serial.printf("[OK]   DS18B20 found (%d device)\n", deviceCount);
  }

  // MPU6050 init
  Wire.begin(21, 22);  // SDA=21, SCL=22 (ESP32 default)
  mpu.initialize();
  mpu.setFullScaleAccelRange(MPU6050_ACCEL_FS_2);  // ±2g range
  mpu.setFullScaleGyroRange(MPU6050_GYRO_FS_250);  // ±250°/s

  if (mpu.testConnection()) {
    Serial.println("[OK]   MPU6050 connected");
  } else {
    Serial.println("[ERROR] MPU6050 not found! Check SDA/SCL wiring.");
  }

  // ADC setup for ACS712
  analogReadResolution(12);        // 12-bit ADC (0–4095)
  analogSetAttenuation(ADC_11db); // Full range: 0–3.3V

  Serial.println("[ACS712] Keep motor/current OFF for calibration...");
delay(2000);
calibrateACS712();

Serial.printf("[OK] ACS712 calibrated. Zero voltage = %.3f V\n", ACS712_VREF);

  Serial.println("[OK]   ACS712 ADC configured");

  // WiFi
  connectWiFi();

  Serial.println("\n[READY] Starting data acquisition loop...\n");
}

// =============================================================================
//  MAIN LOOP
// =============================================================================
void loop() {
  // Reconnect WiFi if dropped
  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("[WiFi] Connection lost. Reconnecting...");
    connectWiFi();
  }

  // Read sensors (averaged)
  float temperature = readTemperature();
  float vibration   = readVibration();
  float current     = readCurrent();

  // Evaluate motor health
  String status = evaluateStatus(temperature, vibration, current);

  // Update LED indicators
  setLED(status);

  // Log to Serial Monitor
  printReadings(temperature, vibration, current, status);

  // Send to Spring Boot backend
  bool success = sendData(temperature, vibration, current, status);
  if (!success) {
    failedPosts++;
    Serial.printf("[WARN] Failed posts in a row: %d\n", failedPosts);
  } else {
    failedPosts = 0;
  }

  lastStatus = status;
  delay(SEND_INTERVAL_MS);
}

// =============================================================================
//  WiFi
// =============================================================================
void connectWiFi() {
  Serial.printf("[WiFi] Connecting to '%s'", WIFI_SSID);
  WiFi.mode(WIFI_STA);
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);

  int attempts = 0;
  while (WiFi.status() != WL_CONNECTED && attempts < 40) {
    delay(500);
    Serial.print(".");
    attempts++;
  }

  if (WiFi.status() == WL_CONNECTED) {
    Serial.println(" Connected!");
    Serial.printf("[WiFi] IP Address : %s\n", WiFi.localIP().toString().c_str());
    Serial.printf("[WiFi] Signal     : %d dBm\n", WiFi.RSSI());
  } else {
    Serial.println("\n[ERROR] WiFi connection failed. Check SSID/password.");
    Serial.println("[INFO]  Will retry on next loop iteration.");
  }
}

// =============================================================================
//  SENSOR: Temperature — DS18B20
// =============================================================================
float readTemperature() {
  float sum   = 0.0;
  int   valid = 0;

  for (int i = 0; i < SAMPLE_COUNT; i++) {
    tempSensor.requestTemperatures();
    float t = tempSensor.getTempCByIndex(0);

    // DEVICE_DISCONNECTED_C = -127, filter it out
    if (t > -100.0 && t < 150.0) {
      sum += t;
      valid++;
    }
    delay(10);
  }

  if (valid == 0) {
    Serial.println("[ERROR] DS18B20 all readings invalid — check wiring!");
    return -999.0;
  }

  return sum / valid;
}

// =============================================================================
//  SENSOR: Vibration — MPU6050
//  Returns RMS vibration magnitude in m/s² (gravity removed)
// =============================================================================
float readVibration() {
  // Step 1: Calibrate zero offset (first reading, motor at rest)
  // This is a simple approach — for better accuracy, add a calibration
  // routine that runs once at startup.

  float sumSq = 0.0;

  for (int i = 0; i < SAMPLE_COUNT; i++) {
    int16_t ax, ay, az, gx, gy, gz;
    mpu.getMotion6(&ax, &ay, &az, &gx, &gy, &gz);

    // Convert raw values to m/s²
    // Sensitivity at ±2g range = 16384 LSB/g, 1g = 9.81 m/s²
    float accelX = (ax / 16384.0) * 9.81;
    float accelY = (ay / 16384.0) * 9.81;
    float accelZ = (az / 16384.0) * 9.81;

    // Remove static gravity component on Z axis
    // (assumes sensor is mounted flat/horizontal on motor)
    accelZ -= 9.81;

    // Compute magnitude of vibration vector
    float magnitude = sqrt(
      accelX * accelX +
      accelY * accelY +
      accelZ * accelZ
    );

    sumSq += magnitude * magnitude;
    delay(5);
  }

  // Return RMS value
  return sqrt(sumSq / SAMPLE_COUNT);
}

// =============================================================================
//  SENSOR: Current — ACS712
//  ESP32 ADC note: GPIO34 is input-only, supports ADC1 (safe to use)
// =============================================================================
void calibrateACS712() {
  long sum = 0;

  for (int i = 0; i < 500; i++) {
    sum += analogRead(ACS712_PIN);
    delay(2);
  }

  float avgRaw = sum / 500.0;
  ACS712_VREF = (avgRaw / 4095.0) * 3.3;
}

float readCurrent() {
  float sumCurrent = 0.0;

  for (int i = 0; i < SAMPLE_COUNT; i++) {
    int raw = analogRead(ACS712_PIN);
    float voltage = (raw / 4095.0) * 3.3;

    float amps = (voltage - ACS712_VREF) / ACS712_SENSITIVITY;

    sumCurrent += amps;
    delay(2);
  }

  float current = sumCurrent / SAMPLE_COUNT;

  if (abs(current) < CURRENT_NOISE_CUTOFF) {
    current = 0.0;
  }

  return abs(current);
}

// =============================================================================
//  STATUS EVALUATION — Threshold-based predictive logic
// =============================================================================
String evaluateStatus(float temp, float vib, float curr) {
  // Critical takes priority
  if (temp >= TEMP_CRIT || vib >= VIB_CRIT || curr >= CURRENT_CRIT) {
    return "CRITICAL";
  }
  // Warning next
  if (temp >= TEMP_WARN || vib >= VIB_WARN || curr >= CURRENT_WARN) {
    return "WARNING";
  }
  return "NORMAL";
}

// =============================================================================
//  LED INDICATOR
// =============================================================================
void setLED(String status) {
  digitalWrite(LED_GREEN_PIN,  status == "NORMAL"   ? HIGH : LOW);
  digitalWrite(LED_YELLOW_PIN, status == "WARNING"  ? HIGH : LOW);
  digitalWrite(LED_RED_PIN,    status == "CRITICAL" ? HIGH : LOW);
}

// =============================================================================
//  SERIAL MONITOR OUTPUT
// =============================================================================
void printReadings(float temp, float vib, float curr, String status) {
  Serial.println("─────────────────────────────────────");
  Serial.printf("  Temperature : %.2f °C\n",  temp);
  Serial.printf("  Vibration   : %.4f m/s²\n", vib);
  Serial.printf("  Current     : %.3f A\n",    curr);
  Serial.printf("  Status      : %s\n",        status.c_str());
  Serial.println("─────────────────────────────────────");
}

// =============================================================================
//  HTTP POST — Send JSON to Spring Boot
// =============================================================================
bool sendData(float temp, float vib, float curr, String status) {
  HTTPClient http;
  http.begin(SERVER_URL);
  http.addHeader("Content-Type", "application/json");
  http.setTimeout(5000);  // 5 second timeout

  // Build JSON payload
  StaticJsonDocument<256> doc;
  doc["motorId"]     = MOTOR_ID;
  doc["temperature"] = round(temp  * 100.0)   / 100.0;
  doc["vibration"]   = round(vib   * 10000.0) / 10000.0;
  doc["current"]     = round(curr  * 1000.0)  / 1000.0;
  doc["status"]      = status;

  String payload;
  serializeJson(doc, payload);

  Serial.printf("[HTTP] POST → %s\n", SERVER_URL);
  Serial.printf("[HTTP] Body : %s\n", payload.c_str());

  int httpCode = http.POST(payload);

  if (httpCode > 0) {
    Serial.printf("[HTTP] Response : %d %s\n",
      httpCode,
      httpCode == 200 ? "OK" :
      httpCode == 201 ? "Created" : "Other"
    );
    http.end();
    return true;
  } else {
    Serial.printf("[HTTP] Error: %s\n", http.errorToString(httpCode).c_str());
    Serial.println("[TIP]  Check: Is Spring Boot running? Correct IP in SERVER_URL?");
    http.end();
    return false;
  }
}

#include "esp_camera.h"
#include <WiFi.h>
#include "AsyncTCP.h"
#include "ESPAsyncWebServer.h"

#define CAMERA_MODEL_ESP32S3_EYE
#include "camera_pins.h"

// Credenciales de WiFi
const char* ssid = "DIGIFIBRA-33Ee";
const char* password = "PX9CkHXPUUfs";

// Servidor en puerto 82 para WebSocket
AsyncWebServer server(82);
AsyncWebSocket ws("/ws");

// Variable global para is_enrolling
extern int8_t is_enrolling; // Declarar como extern, definida en app_httpd.cpp

// Declarar funciones externas
extern void activateWorkshopMode();
extern void deactivateWorkshopMode();
void startCameraServer();
void setupLedFlash(int pin);

void onWsEvent(AsyncWebSocket *server, AsyncWebSocketClient *client, AwsEventType type, void *arg, uint8_t *data, size_t len) {
  if (type == WS_EVT_CONNECT) {
    Serial.println("Cliente WebSocket conectado");
    // Enviar el estado actual de is_enrolling al cliente al conectarse
    client->text(is_enrolling ? "registrar_usuario_activado" : "registrar_usuario_desactivado");
  } else if (type == WS_EVT_DISCONNECT) {
    Serial.println("Cliente WebSocket desconectado");
  } else if (type == WS_EVT_DATA) {
    AwsFrameInfo *info = (AwsFrameInfo*)arg;
    if (info->final && info->index == 0 && info->len == len && info->opcode == WS_TEXT) {
      data[len] = 0;
      String message = String((char*)data);
      Serial.printf("Mensaje WebSocket recibido: %s\n", message.c_str());
      
      // Manejar los mensajes para activar/desactivar el modo de registro
      if (message == "activar_registro") {
        Serial.println("Comparación con 'activar_registro' exitosa");
        is_enrolling = 1; // Activar el modo de registro
        Serial.println("Modo de registro activado (is_enrolling = 1)");
        ws.textAll("registrar_usuario_activado");
      } else if (message == "desactivar_registro") {
        Serial.println("Comparación con 'desactivar_registro' exitosa");
        is_enrolling = 0; // Desactivar el modo de registro
        Serial.println("Modo de registro desactivado (is_enrolling = 0)");
        ws.textAll("registrar_usuario_desactivado");
      } else if (message == "MODO_TALLER_ACTIVADO") {
        activateWorkshopMode();
      } else if (message == "MODO_TALLER_DESACTIVADO") {
        deactivateWorkshopMode();
      } else {
        Serial.printf("Mensaje no coincide con 'activar_registro', 'desactivar_registro', 'MODO_TALLER_ACTIVADO' ni 'MODO_TALLER_DESACTIVADO', recibido: %s\n", message.c_str());
      }
    }
  }
}

void setup() {
  Serial.begin(115200);
  Serial.setDebugOutput(true);
  Serial.println();

  camera_config_t config;
  config.ledc_channel = LEDC_CHANNEL_0;
  config.ledc_timer = LEDC_TIMER_0;
  config.pin_d0 = Y2_GPIO_NUM;
  config.pin_d1 = Y3_GPIO_NUM;
  config.pin_d2 = Y4_GPIO_NUM;
  config.pin_d3 = Y5_GPIO_NUM;
  config.pin_d4 = Y6_GPIO_NUM;
  config.pin_d5 = Y7_GPIO_NUM;
  config.pin_d6 = Y8_GPIO_NUM;
  config.pin_d7 = Y9_GPIO_NUM;
  config.pin_xclk = XCLK_GPIO_NUM;
  config.pin_pclk = PCLK_GPIO_NUM;
  config.pin_vsync = VSYNC_GPIO_NUM;
  config.pin_href = HREF_GPIO_NUM;
  config.pin_sccb_sda = SIOD_GPIO_NUM;
  config.pin_sccb_scl = SIOC_GPIO_NUM;
  config.pin_pwdn = PWDN_GPIO_NUM;
  config.pin_reset = RESET_GPIO_NUM;
  config.xclk_freq_hz = 20000000;
  config.frame_size = FRAMESIZE_240X240; // Mantener la resolución baja
  config.pixel_format = PIXFORMAT_JPEG;
  config.grab_mode = CAMERA_GRAB_WHEN_EMPTY;
  config.fb_location = CAMERA_FB_IN_PSRAM;
  config.jpeg_quality = 12;
  config.fb_count = 1;

  if (config.pixel_format == PIXFORMAT_JPEG) {
    if (psramFound()) {
      config.jpeg_quality = 10;
      config.fb_count = 2;
      config.grab_mode = CAMERA_GRAB_LATEST;
    } else {
      config.frame_size = FRAMESIZE_SVGA;
      config.fb_location = CAMERA_FB_IN_DRAM;
    }
  } else {
    config.frame_size = FRAMESIZE_240X240;
#if CONFIG_IDF_TARGET_ESP32S3
    config.fb_count = 2;
#endif
  }

#if defined(CAMERA_MODEL_ESP_EYE)
  pinMode(13, INPUT_PULLUP);
  pinMode(14, INPUT_PULLUP);
#endif

  esp_err_t err = esp_camera_init(&config);
  if (err != ESP_OK) {
    Serial.printf("Camera init failed with error 0x%x", err);
    return;
  }

  sensor_t *s = esp_camera_sensor_get();
  if (s->id.PID == OV3660_PID) {
    s->set_vflip(s, 1);
    s->set_brightness(s, 1);
    s->set_saturation(s, -2);
  }
  if (config.pixel_format == PIXFORMAT_JPEG) {
    s->set_framesize(s, FRAMESIZE_240X240);
  }

#if defined(CAMERA_MODEL_M5STACK_WIDE) || defined(CAMERA_MODEL_M5STACK_ESP32CAM)
  s->set_vflip(s, 1);
  s->set_hmirror(s, 1);
#endif

#if defined(CAMERA_MODEL_ESP32S3_EYE)
  s->set_vflip(s, 1);
#endif

#if defined(LED_GPIO_NUM)
  setupLedFlash(LED_GPIO_NUM);
#endif

  WiFi.begin(ssid, password);
  WiFi.setSleep(false);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("");
  Serial.println("WiFi connected");

  ws.onEvent(onWsEvent);
  server.addHandler(&ws);
  server.begin();

  startCameraServer();

  Serial.print("Camera Ready! Use 'http://");
  Serial.print(WiFi.localIP());
  Serial.println("' to connect");
}

void loop() {
  delay(10000);
}
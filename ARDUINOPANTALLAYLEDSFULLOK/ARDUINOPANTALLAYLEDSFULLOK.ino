#include <Wire.h>
#include <Adafruit_GFX.h>
#include <Adafruit_SSD1306.h>

#define SCREEN_WIDTH 128
#define SCREEN_HEIGHT 64
#define OLED_RESET -1 // Reset pin # (o -1 si no se usa)
Adafruit_SSD1306 display(SCREEN_WIDTH, SCREEN_HEIGHT, &Wire, OLED_RESET);

const int inputPin2 = 2;    // Pin para leer GPIO 2 (Modo Acceso)
const int inputPin4 = 4;    // Pin para leer GPIO 4 (Modo Taller)
const int relayPin = 3;     // Pin para controlar el relé (motor)
const int buttonPin = 5;    // Pin del pulsador P3
const int ledWhitePin = 6;  // Pin para el LED blanco
const int ledGreenPin = 7;  // Pin para el LED verde

int lastButtonState = HIGH; // Estado anterior del pulsador (pull-up)
bool motorState = LOW;      // Estado inicial del motor
int scrollOffset = 0;       // Offset para el desplazamiento del texto

void setup() {
  Serial.begin(115200);
  
  // Inicializar la pantalla con la dirección I2C 0x3C
  if (!display.begin(SSD1306_SWITCHCAPVCC, 0x3C)) {
    Serial.println(F("Error al inicializar la pantalla OLED"));
    for (;;); // Bucle infinito si falla
  }

  // Configurar texto
  display.setTextSize(1);      // Tamaño del texto
  display.setTextColor(SSD1306_WHITE); // Color blanco

  pinMode(inputPin2, INPUT);    // Pin 2 como entrada
  pinMode(inputPin4, INPUT);    // Pin 4 como entrada
  pinMode(relayPin, OUTPUT);    // Pin 3 como salida para el relé
  pinMode(buttonPin, INPUT_PULLUP); // Pin 5 como entrada con pull-up para el pulsador
  pinMode(ledWhitePin, OUTPUT); // Pin 6 como salida para el LED blanco
  pinMode(ledGreenPin, OUTPUT); // Pin 7 como salida para el LED verde
  digitalWrite(relayPin, LOW);  // Relé inicial desactivado (LOW para Active HIGH)
  digitalWrite(ledWhitePin, LOW); // LED blanco inicial apagado
  digitalWrite(ledGreenPin, LOW); // LED verde inicial apagado
  Serial.println("Arduino Uno listo");
}

void loop() {
  int statePin2 = digitalRead(inputPin2); // Leer estado del pin 2
  int statePin4 = digitalRead(inputPin4); // Leer estado del pin 4
  
  // Actualizar pantalla en cada iteración
  display.clearDisplay();
  
  // Primera línea: ARRANQUE AUTORIZADO si ESP32 está activa
  if (statePin2 == HIGH || statePin4 == HIGH) {
    digitalWrite(ledGreenPin, HIGH); // Encender LED verde
    int x1 = (SCREEN_WIDTH - (strlen("ARRANQUE AUTORIZADO") * 6)) / 2; // Centrar
    display.setCursor(x1, 0);
    display.println("ARRANQUE AUTORIZADO");
    Serial.println("LED verde encendido - Arranque Autorizado");
  } else {
    digitalWrite(ledGreenPin, LOW); // Apagar LED verde
    if (!motorState) {
      int x1 = (SCREEN_WIDTH - (strlen("SISTEMA APAGADO") * 6)) / 2; // Centrar
      display.setCursor(x1, 0);
      display.println("SISTEMA APAGADO");
    }
    Serial.println("LED verde apagado - Sistema Apagado");
  }
  
  // Tercera línea: MOTOR EN MARCHA o MOTOR APAGADO según motorState
  const char* motorText = motorState ? "MOTOR EN MARCHA" : "MOTOR APAGADO";
  int x2 = (SCREEN_WIDTH - (strlen(motorText) * 6)) / 2; // Centrar
  display.setCursor(x2, 16); // Tercera línea
  display.println(motorText);
  
  // Sexta línea: Scroll del texto
  const char* scrollText = "Carlos Fernandez Garcia TSA 2B I.E.S. Barajas";
  int textWidth = strlen(scrollText) * 6; // Ancho total del texto en píxeles
  int x3 = SCREEN_WIDTH - scrollOffset; // Posición inicial del texto
  display.setCursor(x3, 56); // Sexta línea
  display.println(scrollText);
  if (x3 + textWidth < 0) {
    scrollOffset = 0; // Reiniciar cuando el texto salga completamente
  } else {
    scrollOffset++; // Desplazar a la izquierda
  }
  
  display.display(); // Mostrar en la pantalla
  
  // Leer el estado del pulsador en D5
  int buttonState = digitalRead(buttonPin);
  Serial.print("Button State: ");
  Serial.println(buttonState);

  // Detectar flanco descendente del pulsador y verificar si la ESP32 permite activar el motor
  if (buttonState == LOW && lastButtonState == HIGH && (statePin2 == HIGH || statePin4 == HIGH)) {
    motorState = !motorState;           // Alternar estado del motor
    digitalWrite(relayPin, motorState ? HIGH : LOW); // Activar/desactivar relé (HIGH = activado)
    digitalWrite(ledWhitePin, motorState); // LED blanco refleja el estado del motor
    Serial.print("Motor cambiado a: ");
    Serial.println(motorState ? "ENCENDIDO" : "APAGADO");
  }

  // Si la ESP32 no está activa, apagar motor y LED blanco
  if (statePin2 == LOW && statePin4 == LOW) {
    motorState = LOW;
    digitalWrite(relayPin, LOW); // Desactivar relé (LOW)
    digitalWrite(ledWhitePin, LOW); // Apagar LED blanco
    Serial.println("ESP32 desactivada, motor apagado");
  }

  lastButtonState = buttonState; // Guardar estado actual del pulsador
  
  delay(10); // Retraso para controlar la velocidad del scroll
}
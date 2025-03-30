const int inputPin = 2;  // Pin para leer la señal del S3 (GPIO 2)
const int relayPin = 3;  // Pin para controlar el relé

void setup() {
  pinMode(inputPin, INPUT);   // Pin 2 como entrada para leer la señal del S3
  pinMode(relayPin, OUTPUT);  // Pin 3 como salida para el relé
  digitalWrite(relayPin, HIGH); // Estado inicial: relé desactivado (HIGH = apagado)
  Serial.begin(115200);
  Serial.println("Arduino Uno listo");
}

void loop() {
  int state = digitalRead(inputPin); // Leer el estado del pin 2 (HIGH o LOW)
  
  if (state == HIGH) {
    digitalWrite(relayPin, LOW); // Activar el relé (LOW = encendido)
    Serial.println("Relé activado (GPIO 2 en HIGH)");
  } else {
    digitalWrite(relayPin, HIGH); // Desactivar el relé (HIGH = apagado)
    Serial.println("Relé desactivado (GPIO 2 en LOW)");
  }
  
  delay(100); // Pequeño retraso para evitar lecturas rápidas
}
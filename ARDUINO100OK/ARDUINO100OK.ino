const int inputPin2 = 2;  // Pin para leer la señal del S3 (GPIO 2 - Modo Acceso)
const int inputPin4 = 4;  // Pin para leer la señal del S3 (GPIO 4 - Modo Taller)
const int relayPin = 3;   // Pin para controlar el relé

void setup() {
  pinMode(inputPin2, INPUT);   // Pin 2 como entrada para leer la señal del S3 (GPIO 2)
  pinMode(inputPin4, INPUT);   // Pin 4 como entrada para leer la señal del S3 (GPIO 4)
  pinMode(relayPin, OUTPUT);   // Pin 3 como salida para el relé
  digitalWrite(relayPin, HIGH); // Estado inicial: relé desactivado (HIGH = apagado)
  Serial.begin(115200);
  Serial.println("Arduino Uno listo");
}

void loop() {
  int statePin2 = digitalRead(inputPin2); // Leer el estado del pin 2 (HIGH o LOW)
  int statePin4 = digitalRead(inputPin4); // Leer el estado del pin 4 (HIGH o LOW)
  
  // Imprimir el estado de los pines para depuración
  Serial.print("Estado Pin 2: ");
  Serial.print(statePin2);
  Serial.print(" | Estado Pin 4: ");
  Serial.println(statePin4);
  
  if (statePin2 == HIGH || statePin4 == HIGH) {
    digitalWrite(relayPin, LOW); // Activar el relé (LOW = encendido)
    if (statePin2 == HIGH && statePin4 == HIGH) {
      Serial.println("Relé activado (GPIO 2 y GPIO 4 en HIGH)");
    } else if (statePin2 == HIGH) {
      Serial.println("Relé activado (GPIO 2 en HIGH)");
    } else {
      Serial.println("Relé activado (GPIO 4 en HIGH)");
    }
  } else {
    digitalWrite(relayPin, HIGH); // Desactivar el relé (HIGH = apagado)
    Serial.println("Relé desactivado (GPIO 2 y GPIO 4 en LOW)");
  }
  
  delay(100); // Pequeño retraso para evitar lecturas rápidas
}
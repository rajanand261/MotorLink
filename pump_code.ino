#include <SoftwareSerial.h>
SoftwareSerial Bluetooth(10, 9); // RX, TX
int Data;
int Prev_Data=0;
unsigned long previousMillis=0;        
long interval;
boolean is_allocated=false;
boolean first_run=true;
boolean marker=true; 

void setup() {
  Bluetooth.begin(57600);
  Serial.begin(9600);
  pinMode(13, OUTPUT);
  digitalWrite(13, LOW);
  Serial.println("Waiting for command...");
  }

void loop() {
  
   if (Bluetooth.available()){ //wait for data received
        Data=Bluetooth.read();
        Serial.println(Data);
        interval=Data*60000;
        is_allocated=true;

        if(0<Data<=200){
        digitalWrite(13, HIGH);
       }
     
       Serial.println("Btread block");
       if(first_run){
       Prev_Data=Data;
       first_run=false;
        }

       if(Data== 250){
       Bluetooth.println("2");
       is_allocated=false;
       first_run=true;
       marker=true;
       digitalWrite(13, LOW);
       }

        if(Data== 225){
       Bluetooth.println("4");
       is_allocated=false;
       first_run=true;
       marker=true;
       digitalWrite(13, LOW);
       }

       if(Data== 200){
       Bluetooth.println("3");
       is_allocated=false;
       marker=true;
       digitalWrite(13, HIGH);
       }

       if(Data<Prev_Data){
        Serial.println("our data less block");
        marker=true;
        }

       
     
     }
   
   if(is_allocated){
      
      Serial.println("e a");
      unsigned long currentMillis = millis();
      
      if(marker){
        previousMillis = currentMillis;
        marker=false;
        Bluetooth.println(1);
        }
      if (currentMillis - previousMillis >= interval){
        digitalWrite(13, LOW);
        is_allocated=false;
        marker=true;
        first_run=true;
        Serial.println("free allocation work doned");
     }
    }
delay(1000);
}

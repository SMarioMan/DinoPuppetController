/*
Dino puppet program
This program is deisgned to control motors and servos for an animatronic dino
*/

#include <stdlib.h>
#include <time.h>
#include <Servo.h>
#include <Wire.h>
#include <Adafruit_MotorShield.h>
#include "utility/Adafruit_MS_PWMServoDriver.h"

#define DEBUG 0

// Set the motor count to have the number of motors we want
// I set this to 4 because motors 1 and 2 aren't working, but we will eventually change this to 3
#define MOTOR_COUNT 4
#define SERVO_COUNT 2

// A list of commands that can be sent to the Arduino. At present, we can assign up to 256 distinct commands
const byte UP = 0;
const byte VERTSTOP = 1;
const byte DOWN = 2;
// Additional Up-Down movement
const byte UP2 = 3;
const byte VERTSTOP2 = 4;
const byte DOWN2 = 5;
// Side to side movement
const byte LEFT = 6;
const byte HORIZSTOP = 7;
const byte RIGHT = 8;
// Special servo actions
const byte MOUTH = 9;
const byte EYES = 10;
// Start listening for an int
const byte MOUTHstart = 100;
const byte EYEstart = 102;

// Create the motor shield object with the default I2C address
Adafruit_MotorShield AFMS = Adafruit_MotorShield();

// Our array of motors
Adafruit_DCMotor *motors[MOTOR_COUNT];

// Our array of servos
Servo servos[SERVO_COUNT];

int temp;

void setup() {
	
  // Initialize motors
  for(int i = 0; i < MOTOR_COUNT; i++) {
    motors[i] = AFMS.getMotor(i+1);
  }

  // Initialize servos
  for(int i = 0; i < SERVO_COUNT; i++) {
    servos[i].attach(i+9);  // attaches the servos to pins 9 and 10
  }
  
  Serial.begin(9600); // set up Serial library at 9600 bps
  Serial.println("Dino puppet program - Life finds a way");

  AFMS.begin(); // create with the default frequency 1.6KHz
  
  for(int i = 0; i < MOTOR_COUNT; i++) {
    motors[i]->setSpeed(128); // Set the speed to start, from 0 (off) to 255 (max speed)
    motors[i]->run(FORWARD);
    motors[i]->run(RELEASE); // turn on motor
  }
}

void loop() {
	
	//Serial.print("Running loop");

  if (Serial.available() > 0) {
    byte inByte = Serial.read();
		if(DEBUG) Serial.print(inByte);
    
    switch (inByte) {
    
    case UP:
    motors[2]->run(FORWARD);
    break;
    
    case DOWN: 
    motors[2]->run(BACKWARD);
    break;
		
		case VERTSTOP: 
    motors[2]->run(RELEASE);
    break;
    
    case LEFT: 
    motors[1]->run(FORWARD);
    break;
    
    case RIGHT: 
    motors[1]->run(BACKWARD);
    break;
		
		case HORIZSTOP: 
    motors[1]->run(RELEASE);
    break;
		
		case UP2:
    motors[0]->run(BACKWARD);
    break;
    
    case DOWN2: 
    motors[0]->run(BACKWARDFORWARD);
    break;
		
		case VERTSTOP2: 
    motors[0]->run(RELEASE);
    break;

    case MOUTH:
		servos[0].write(180);
		delay(500);
		servos[0].write(0);
    break;

    case EYES:
		temp = servos[1].read();
    servos[1].write(90);
		delay(200);
		servos[1].write(temp);
    break;
		
		case EYEstart:
		while(Serial.available() <= 0) {} // Wait for the next byte to arrive
		inByte = Serial.read();
		if(DEBUG) Serial.println((int)inByte);
		servos[1].write(inByte);
		break;
		
		case MOUTHstart:
		while(Serial.available() <= 0) {} // Wait for the next byte to arrive
		inByte = Serial.read();
		if(DEBUG) Serial.println((int)inByte);
		servos[0].write(inByte);
		break;
    
    default:
    break;
    
    }
  }
	
	//delay(1000);

}

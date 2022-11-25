#include <wiringPi.h>
#include <softPwm.h>
#include <stdio.h>

#define PWM_PIN 0

int main()
{
    wiringPiSetup();
    pinMode(PWM_PIN, OUTPUT);
    softPwmCreate(PWM_PIN, 0, 200);
    
    softPwmWrite(PWM_PIN, 5);
    delay(2000);
    softPwmWrite(PWM_PIN, 10);

    return 0;
}
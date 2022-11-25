#include <wiringpi.h>
#include <softpwm.h>
#include <stdio.h>
// 定义控制舵机的引脚为：GPIO1
#define PWM_PIN 1
int main(void)
{
    printf("wiringPi-C PWM test programn") ;
    wiringPiSetup();
    pinMode(PWM_PIN, PWM_OUTPUT); // 设置PWM输出
    pwmSetMode(PWM_MODE_MS); // 设置传统模式
    pwmSetClock(192); // 设置分频
    pwmSetRange(2000); // 设置周期分为2000步
    printf("当前方向: -90度n");
    pwmWrite(PWM_PIN, 50) ;
    delay(2000);
    printf("当前方向: -45度n");
    pwmWrite(PWM_PIN, 100) ;
    delay(2000);
    printf("当前方向: 0度n");
    pwmWrite(PWM_PIN, 150) ;
    delay(2000);
    printf("当前方向: 45度n");
    pwmWrite(PWM_PIN, 200) ;
    delay(2000);
    printf("当前方向: 90度n");
    pwmWrite(PWM_PIN, 250) ;
    delay(2000);

    return 0 ;
}
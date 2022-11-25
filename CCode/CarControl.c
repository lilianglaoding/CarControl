#include <wiringPi.h>
#include <softPwm.h>
#include <sys/types.h>          /* See NOTES */
#include <sys/socket.h>
#include <string.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <stdio.h>
#include <signal.h>
#include <pthread.h>

#define SERVER_PORT 12345
#define BACKLOG     10

#define STEER_PIN 1  // 舵机引脚
#define STEER1_PIN 4
#define STEER2_PIN 5
#define STEER3_PIN 6

#define STEER_0_DEGREE 190
#define STEER_ANGLE 25

int g_s1_stop = 0;
int g_s2_stop = 0;
int g_s3_stop = 0;
int g_s1_last_duty = 19;
int g_s2_last_duty = 19;
int g_s3_last_duty = 10;

// 马达从左到右、从前到后分别编号为A、B、C、D
// 小车马达平面图
//   A ---- B
//   |      |
//   |      |
//   |      |
//   C ---- D


#define MOTORA_PIN_1 21   // 马达A引脚1
#define MOTORA_PIN_2 22   // 马达A引脚2
#define MOTORB_PIN_1 23   // 马达B引脚1
#define MOTORB_PIN_2 24   // 马达B引脚2
#define MOTORC_PIN_1 25   // 马达C引脚1
#define MOTORC_PIN_2 26   // 马达C引脚2
#define MOTORD_PIN_1 27   // 马达D引脚1
#define MOTORD_PIN_2 28   // 马达D引脚2

// 0 for success, others failed
int raspi_init()
{
    if (wiringPiSetup() == -1) {
        printf("Setup wiringPi failed!\n");
        return 1;
    }

    pinMode(MOTORA_PIN_1, OUTPUT);
    pinMode(MOTORA_PIN_2, OUTPUT);
    digitalWrite(MOTORA_PIN_1, LOW);
    digitalWrite(MOTORA_PIN_2, LOW);
    pinMode(MOTORB_PIN_1, OUTPUT);
    pinMode(MOTORB_PIN_2, OUTPUT);
    digitalWrite(MOTORB_PIN_1, LOW);
    digitalWrite(MOTORB_PIN_2, LOW);
    pinMode(MOTORC_PIN_1, OUTPUT);
    pinMode(MOTORC_PIN_2, OUTPUT);
    digitalWrite(MOTORC_PIN_1, LOW);
    digitalWrite(MOTORC_PIN_2, LOW);
    pinMode(MOTORD_PIN_1, OUTPUT);
    pinMode(MOTORD_PIN_2, OUTPUT);
    digitalWrite(MOTORD_PIN_1, LOW);
    digitalWrite(MOTORD_PIN_2, LOW);

    //pinMode(STEER1_PIN, OUTPUT);
    softPwmCreate(STEER1_PIN, 0, 200);
    //pinMode(STEER2_PIN, OUTPUT);
    softPwmCreate(STEER2_PIN, 0, 200);
    //pinMode(STEER3_PIN, OUTPUT);
    softPwmCreate(STEER3_PIN, 0, 200);

    return 0;
}

void motorA_up()
{
    digitalWrite(MOTORA_PIN_1, HIGH);
    digitalWrite(MOTORA_PIN_2, LOW);
}

void motorA_down()
{
    digitalWrite(MOTORA_PIN_1, LOW);
    digitalWrite(MOTORA_PIN_2, HIGH);
}

void motorA_stop()
{
    digitalWrite(MOTORA_PIN_1, LOW);
    digitalWrite(MOTORA_PIN_2, LOW);
}

void motorB_up()
{
    digitalWrite(MOTORB_PIN_1, HIGH);
    digitalWrite(MOTORB_PIN_2, LOW);
}

void motorB_down()
{
    digitalWrite(MOTORB_PIN_1, LOW);
    digitalWrite(MOTORB_PIN_2, HIGH);
}

void motorB_stop()
{
    digitalWrite(MOTORB_PIN_1, LOW);
    digitalWrite(MOTORB_PIN_2, LOW);
}

void motorC_up()
{
    digitalWrite(MOTORC_PIN_1, HIGH);
    digitalWrite(MOTORC_PIN_2, LOW);
}

void motorC_down()
{
    digitalWrite(MOTORC_PIN_1, LOW);
    digitalWrite(MOTORC_PIN_2, HIGH);
}

void motorC_stop()
{
    digitalWrite(MOTORC_PIN_1, LOW);
    digitalWrite(MOTORC_PIN_2, LOW);
}

void motorD_up()
{
    digitalWrite(MOTORD_PIN_1, HIGH);
    digitalWrite(MOTORD_PIN_2, LOW);
}

void motorD_down()
{
    digitalWrite(MOTORD_PIN_1, LOW);
    digitalWrite(MOTORD_PIN_2, HIGH);
}

void motorD_stop()
{
    digitalWrite(MOTORD_PIN_1, LOW);
    digitalWrite(MOTORD_PIN_2, LOW);
}

void stop()
{
    motorA_stop();
    motorB_stop();
    motorC_stop();
    motorD_stop();
}

// direction:
/*
     5  6  7
      \ | /
       \|/
   4 ------- 0  
       /|\
      / | \
     3  2  1

     8 - left rotate
     9 - right rotate
*/

//void (*move_car)(int commant) == NULL;

void move_cs_mode(int command)
{
    switch (command)
    {
    case 0:
    case 1:
    case 7:
    case 9:
        motorB_down();
        motorD_down();
        motorA_up();
        motorC_up();
        break;
    case 3:
    case 4:
    case 5:
    case 8:
        motorB_up();
        motorD_up();
        motorA_down();
        motorC_down();
        break;
    case 6:
        motorB_up();
        motorD_up();
        motorA_up();
        motorC_up();
        break;
    case 2:
        motorB_down();
        motorD_down();
        motorA_down();
        motorC_down();
        break;
    default:
        stop();
        break;
    }
}

void move_akm_mode(int command)
{
    switch (command) {
        case 0:
        case 7:
            pwmWrite(STEER_PIN, STEER_0_DEGREE + STEER_ANGLE);
            motorA_up();
            motorB_up();
            motorC_up();
            motorD_up();
            break;
        case 1:
            pwmWrite(STEER_PIN, STEER_0_DEGREE + STEER_ANGLE);
            motorA_down();
            motorB_down();
            motorC_down();
            motorD_down();
            break;
        case 2:
            pwmWrite(STEER_PIN, STEER_0_DEGREE);
            motorA_down();
            motorB_down();
            motorC_down();
            motorD_down();
            break;
        case 3:
            pwmWrite(STEER_PIN, STEER_0_DEGREE - STEER_ANGLE);
            motorA_down();
            motorB_down();
            motorC_down();
            motorD_down();
            break;
        case 4:
        case 5:
            pwmWrite(STEER_PIN, STEER_0_DEGREE - STEER_ANGLE);
            motorA_up();
            motorB_up();
            motorC_up();
            motorD_up();
            break;
        case 6:
            pwmWrite(STEER_PIN, STEER_0_DEGREE);
            motorA_up();
            motorB_up();
            motorC_up();
            motorD_up();
            break;
        default:
            pwmWrite(STEER_PIN, STEER_0_DEGREE);
            stop();
            break;
    }
}

// direction:
/*
     5  6  7
      \ | /
       \|/
   4 ------- 0  
       /|\
      / | \
     3  2  1

     8 - left rotate
     9 - right rotate
     10 - FRONT_LEFT_ROTATE, // 沿前轴中点左旋
     11 - FRONT_RIGHT_ROTATE, // 沿前轴中点右旋
     12 - REAR_LEFT_ROTATE, // 沿后轴中点左旋
     13 - REAR_RIGHT_ROTATE, // 沿后轴中点右旋
*/


/*
    private enum Command {
        TURN_RIGHT, // 右 0
        TURN_DOWN_RIGHT, // 右下 1
        TURN_DOWN, // 下 2
        TURN_DOWN_LEFT, // 左下 3
        TURN_LEFT, // 左 4
        TURN_UP_LEFT, // 左上 5
        TURN_UP, // 上 6
        TURN_UP_RIGHT, // 右上 7
        LEFT_ROTATE, // 左转 8
        RIGHT_ROTATE, // 右转 9
        FRONT_LEFT_ROTATE, // 沿前轴中点左旋 10
        FRONT_RIGHT_ROTATE, // 沿前轴中点右旋 11
        REAR_LEFT_ROTATE, // 沿后轴中点左旋 12
        REAR_RIGHT_ROTATE, // 沿后轴中点右旋 13
        STOP, // 停止 14
        CS, // 差速模式 15
        AKM, // 阿克曼模式 16
        MKNM // 麦克纳姆模式 17
    } */

void move_mknm_mode(int command)
{
    switch (command)
    {
    case 0:
        motorA_up();
        motorD_up();
        motorB_down();
        motorC_down();
        break;
    case 1:
        motorA_stop();
        motorD_stop();
        motorB_down();
        motorC_down();
        break;
    case 2:
        motorA_down();
        motorD_down();
        motorB_down();
        motorC_down();
        break;
    case 3:
        motorB_stop();
        motorC_stop();
        motorA_down();
        motorD_down();
        break;
    case 4:
        motorA_down();
        motorD_down();
        motorB_up();
        motorC_up();
        break;
    case 5:
        motorA_stop();
        motorD_stop();
        motorB_up();
        motorC_up();
        break;
    case 6:
        motorA_up();
        motorD_up();
        motorB_up();
        motorC_up();
        break;
    case 7:
        motorB_stop();
        motorC_stop();
        motorA_up();
        motorD_up();
        break;
    case 8:
        motorA_down();
        motorB_up();
        motorC_down();
        motorD_up();
        break;
    case 9:
        motorA_up();
        motorB_down();
        motorC_up();
        motorD_down();
        break;
    case 10:
        motorA_stop();
        motorB_stop();
        motorC_down();
        motorD_up();
        break;
    case 11:
        motorA_stop();
        motorB_stop();
        motorC_up();
        motorD_down();
        break;
    case 12:
        motorA_up();
        motorB_down();
        motorC_stop();
        motorD_stop();
        break;
    case 13:
        motorA_down();
        motorB_up();
        motorC_stop();
        motorD_stop();
        break;
    case 14:
        motorA_stop();
        motorB_stop();
        motorC_stop();
        motorD_stop();
        break;
    default:
        stop();
        break;
    }
}

void steer1_up()
{
    int i = g_s1_last_duty;
    while (!g_s1_stop && i >= 13) {
        softPwmWrite(STEER1_PIN, i);
        delay(20);
        --i;
    }
    g_s1_last_duty = i;
}

void steer1_down()
{
    int i = g_s1_last_duty;
    while (!g_s1_stop && i <= 25) {
        softPwmWrite(STEER1_PIN, i);
        delay(20);
        ++i;
    }
    g_s1_last_duty = i;
}

void steer2_up()
{
    int i = g_s2_last_duty;
    while (!g_s2_stop && i >= 13) {
        softPwmWrite(STEER2_PIN, i);
        delay(20);
        --i;
    }
    g_s2_last_duty = i;
}

void steer2_down()
{
    int i = g_s2_last_duty;
    while (!g_s2_stop && i <= 25) {
        softPwmWrite(STEER2_PIN, i);
        delay(20);
        ++i;
    }
    g_s2_last_duty = i;
}

void steer3_in()
{
    int i = g_s3_last_duty;
    while (!g_s3_stop && i >= 5) {
        softPwmWrite(STEER3_PIN, i);
        delay(20);
        --i;
    }
    g_s3_last_duty = i;
}

void steer3_out()
{
    int i = g_s3_last_duty;
    while (!g_s3_stop && i <= 15) {
        SoftPwmWrite(STEER3_PIN, i);
        delay(20);
        ++i;
    }
    g_s3_last_duty = i;
}

int main()
{
    if (raspi_init() == 1) {
        return 0;
    }

    void (*move)(int) = move_mknm_mode;

    pthread_t t1;
    void *res = NULL;

    int iSocketServer;
	int iSocketClient;
	struct sockaddr_in tSocketServerAddr;
	struct sockaddr_in tSocketClientAddr;
	int iRet;
	int iAddrLen;
 
	int iRecvLen;
	unsigned char ucRecvBuf[1000];
 
	int iClientNum = -1;
 
	signal(SIGCHLD,SIG_IGN);
	
	iSocketServer = socket(AF_INET, SOCK_STREAM, 0);
	if (-1 == iSocketServer)
	{
		printf("socket error!\n");
		return -1;
	}
 
	tSocketServerAddr.sin_family      = AF_INET;
	tSocketServerAddr.sin_port        = htons(SERVER_PORT);  /* host to net, short */
 	tSocketServerAddr.sin_addr.s_addr = INADDR_ANY;
	memset(tSocketServerAddr.sin_zero, 0, 8);
	
	iRet = bind(iSocketServer, (const struct sockaddr *)&tSocketServerAddr, sizeof(struct sockaddr));
	if (-1 == iRet)
	{
		printf("bind error!\n");
		return -1;
	}
 
	iRet = listen(iSocketServer, BACKLOG);
	if (-1 == iRet)
	{
		printf("listen error!\n");
		return -1;
	}
 
	while (1)
	{
		iAddrLen = sizeof(struct sockaddr);
		iSocketClient = accept(iSocketServer, (struct sockaddr *)&tSocketClientAddr, &iAddrLen);
		if (-1 != iSocketClient)
		{
			iClientNum++;
			printf("Get connect from client %d : %s\n",  iClientNum, inet_ntoa(tSocketClientAddr.sin_addr));
			if (!fork())
			{
				/* 子进程的源码 */
				while (1)
				{
					/* 接收客户端发来的数据并显示出来 */
					iRecvLen = recv(iSocketClient, ucRecvBuf, 999, 0);
					if (iRecvLen <= 0)
					{
						close(iSocketClient);
						return -1;
					}
					else
					{
						ucRecvBuf[iRecvLen] = '\0';
						printf("Get Msg From Client %d: %s\n", iClientNum, ucRecvBuf);
                        int command = atoi(ucRecvBuf + (iRecvLen - 2));
                        printf("Command is %d\n", command);
                        switch (command)
                        {
                        case 15:
                            move = move_cs_mode;
                            break;
                        case 16:
                            pinMode(STEER_PIN, PWM_OUTPUT); // 设置PWM输出
                            pwmSetMode(PWM_MODE_MS); // 设置传统模式
                            pwmSetClock(192); // 设置分频
                            pwmSetRange(2000); // 设置周期分为2000步
                            pwmWrite(STEER_PIN, STEER_0_DEGREE);
                            move = move_akm_mode;
                            break;
                        case 17:
                            move = move_mknm_mode;
                            break;
                        case 18:
                            g_s1_stop = 0;
                            pthread_create(&t1, NULL, steer1_up, NULL);
                            break;
                        case 19:
                            g_s1_stop = 0;
                            pthread_create(&t1, NULL, steer1_down, NULL);
                            break;
                        case 20:
                            g_s1_stop = 1;
                            break;
                        case 21:
                            g_s2_stop = 0;
                            pthread_create(&t1, NULL, steer2_up, NULL);
                            break;
                        case 22:
                            g_s2_stop = 0;
                            pthread_create(&t1, NULL, steer2_down, NULL);
                            break;
                        case 23:
                            g_s2_stop = 1;
                            break;
                        case 24:
                            g_s3_stop = 0;
                            pthread_create(&t1, NULL, steer3_in, NULL);
                            break;
                        case 25:
                            g_s3_stop = 0;
                            pthread_create(&t1, NULL, steer3_out, NULL);
                            break;
                        case 26:
                            g_s3_stop = 1;
                            break;
                        default:
                            move(command);
                            break;
                        }
					}
				}				
			}
		}
	}

    pthread_join(t1, &res);
	close(iSocketServer);
	return 0;
}

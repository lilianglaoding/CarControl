#include <iostream>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <string.h>
#define PORT 8000
using namespace std;

int main() {
    int sockFd = 0;
    char buffer[1024] = {0};
    char* helloFromClient = "hello from client";
    struct sockaddr_in address;
    address.sin_family = AF_INET;
    inet_pton(AF_INET, "192.168.1.11", &address.sin_addr.s_addr);
    address.sin_port = htons(PORT);
    sockFd = socket(AF_INET, SOCK_STREAM, 0);
    

    connect(sockFd, (struct sockaddr*)&address, sizeof(address));
    send(sockFd, helloFromClient, strlen(helloFromClient), 0);
    printf("client sent\n");
    read(sockFd, buffer, 1024);
    printf("read message:%s\n", buffer);
    return 0;
}



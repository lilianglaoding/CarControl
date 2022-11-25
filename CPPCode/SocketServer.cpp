#include <iostream>
#include <sys/socket.h>
#include <netinet/in.h>
#include <unistd.h>
#include <string.h>
using namespace std;
#define PORT 8000

int main() {
    int sockFd, newSockFd, valread;
    int opt = 1;
    char buffer[1024] = {0};
    char* helloFromServer = "hello from server";
    struct sockaddr_in address;

    sockFd = socket(AF_INET, SOCK_STREAM, 0);
    setsockopt(sockFd, SOL_SOCKET, SO_REUSEADDR|SO_REUSEPORT, &opt, sizeof(opt));
    address.sin_family = AF_INET;
    address.sin_addr.s_addr = INADDR_ANY;
    address.sin_port = htons(PORT);
    int addrlen = sizeof(address);
    bind(sockFd, (struct sockaddr*)&address, addrlen);
    listen(sockFd, 3);
    newSockFd = accept(sockFd, (struct sockaddr*)&address, (socklen_t*)&addrlen);
    read(newSockFd, buffer, 1024);
    printf("receive: %s\n", buffer);
    send(newSockFd, helloFromServer, strlen(helloFromServer), 0);
    printf("server sent message\n");

    return 0;
}


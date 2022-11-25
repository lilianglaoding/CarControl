#include <thread>
#include <iostream>

void print()
{
    std::cout << "hello" << std::endl;
}

int main()
{
    std::thread th1(print);
    th1.join();
    
    return 0;
}
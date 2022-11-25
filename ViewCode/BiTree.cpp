#include <iostream>
#include <stdlib.h>
#include <stack>
#include <string>
#include <stdexcept>
#include <exception>

struct BiTree {
    int data;
    BiTree *lchild;
    BiTree *rchild;
};

struct StackNode {
    BiTree *link;
    int flag;
};

void DestroyBiTree(BiTree *bt)
{
    if (bt) {
        DestroyBiTree(bt->lchild);
        DestroyBiTree(bt->rchild);
        free(bt);
    }
}

void RPreOrder(BiTree *bt)
{
    if (bt) {
        std::cout << bt->data << " ";
        RPreOrder(bt->lchild);
        RPreOrder(bt->rchild);
    }
}

void RInOrder(BiTree *bt)
{
    if (bt) {
        RInOrder(bt->lchild);
        std::cout << bt->data << " ";
        RInOrder(bt->rchild);
    }
}

void RPostOrder(BiTree *bt)
{
    if (bt) {
        RPostOrder(bt->lchild);
        RPostOrder(bt->rchild);
        std::cout << bt->data << " ";
    }
}

void NRPreOrder(BiTree *bt)
{
    if (bt == NULL) {
        return;
    }

    BiTree *p = bt;
    std::stack<BiTree*> bTStack;
    while (!(p == NULL && bTStack.empty())) {
        while (p) {
            std::cout << p->data << " ";
            bTStack.push(p);
            p = p->lchild;
        }

        if (bTStack.empty()) {
            return;
        }

        BiTree *q = bTStack.top();
        bTStack.pop();
        p = q->rchild;
    }
}

void NRPreOrder1(BiTree *bt)
{
    if (bt == NULL) {
        return;
    }

    BiTree *p = bt;
    std::stack<BiTree*> bTStack;
    while (!(p == NULL && bTStack.empty())) {
        if (p) {
            std::cout << p->data << " ";
            bTStack.push(p);
            p = p->lchild;
        }
        else {
            BiTree *q = bTStack.top();
            bTStack.pop();
            p = q->rchild;
        } 
    }
}

void NRInOrder(BiTree *bt)
{
    if (bt == NULL) {
        return;
    }

    BiTree* p = bt;
    std::stack<BiTree*> bTStack;
    while (!(p == NULL && bTStack.empty())) {
        while (p) {
            bTStack.push(p);
            p = p->lchild;
        }
        
        if (bTStack.empty()) {
            return;
        }

        BiTree *q = bTStack.top();
        std::cout << q->data << " ";
        bTStack.pop();
        p = q->rchild;
    }
}

void NRInOrder1(BiTree *bt)
{
    if (bt == NULL) {
        return;
    }

    BiTree* p = bt;
    std::stack<BiTree*> bTStack;
    while (!(p == NULL && bTStack.empty())) {
        if (p) {
            bTStack.push(p);
            p = p->lchild;
        }
        else {
            BiTree *q = bTStack.top();
            std::cout << q->data << " ";
            bTStack.pop();
            p = q->rchild;
        }
    }
}

void NRPostOrder(BiTree *bt)
{
    if (bt == NULL) {
        return;
    }

    BiTree *p = bt;
    std::stack<StackNode> bTStack;
    while (!(p == NULL && bTStack.empty())) {
        while (p) {
            StackNode node;
            node.link = p;
            node.flag = 1;
            bTStack.push(node);
            p = p->lchild;
        }

        if (bTStack.empty()) {
            return;
        }

        StackNode topNode = bTStack.top();
        if (topNode.flag == 1) {
            p = topNode.link->rchild;
            topNode.flag = 2;
            bTStack.pop();
            bTStack.push(topNode);
        }
        else {
            std::cout << topNode.link->data << " ";
            bTStack.pop();
        }
    }
}

void NRPostOrder1(BiTree *bt)
{
    if (bt == NULL) {
        return;
    }

    BiTree *p = bt;
    std::stack<StackNode> bTStack;
    while (!(p == NULL && bTStack.empty())) {
        if (p) {
            StackNode node;
            node.link = p;
            node.flag = 1;
            bTStack.push(node);
            p = p->lchild;
        }
        else {
            StackNode &topNode = bTStack.top();
            if (topNode.flag == 1) {
                p = topNode.link->rchild;
                topNode.flag = 2;
                //bTStack.pop();
                //bTStack.push(topNode);
            }
            else {
                std::cout << topNode.link->data << " ";
                bTStack.pop();
            }
        }
    }
}

BiTree *RestoreCore(int* startPreOrder, int* endPreOrder, int* startInOrder, int* endInOrder)
{
    int rootValue = startPreOrder[0];
    BiTree* root = (BiTree *)malloc(sizeof(BiTree));
    if (root == NULL) {
        return NULL;
    }
    root->data = rootValue;
    root->lchild = NULL;
    root->rchild = NULL;

    if (startPreOrder == endPreOrder) {
        if (startInOrder == endInOrder && *startPreOrder == *startInOrder) {
            return root;
        }
        else {
            throw std::logic_error("invalid input");
        }
    }

    int leftLength = 0;
    int* inOrderPtr = startInOrder;
    while (inOrderPtr <= endInOrder && *inOrderPtr != rootValue) {
        ++inOrderPtr;
    }
    if (inOrderPtr <= endInOrder) {
        leftLength = inOrderPtr - startInOrder;
    }
    else {
        throw std::logic_error("invalid input");
    }

    if (leftLength > 0) {
        root->lchild = RestoreCore(startPreOrder + 1, startPreOrder + leftLength,
                                   startInOrder, inOrderPtr - 1);
    }
    if (endInOrder - inOrderPtr > 0) {
        root->rchild = RestoreCore(startPreOrder + leftLength + 1, endPreOrder,
                                   inOrderPtr + 1, endInOrder);
    }

    return root;
}

BiTree *RestoreBiTree(int* preOrder, int* inOrder, int length)
{
    if (preOrder == NULL || inOrder == NULL || length <= 0) {
        return NULL;
    }

    return RestoreCore(preOrder, preOrder + length - 1, inOrder, inOrder + length - 1);
}

int main()
{
    int preOrder[8] = {1, 2, 4, 7, 3, 5, 6, 8};
    int inOrder[8] = {4, 7, 2, 1, 5, 3, 8, 6};
    BiTree *bt = RestoreBiTree(preOrder, inOrder, 8);
    std::cout << "RPreOrder" << std::endl;
    RPreOrder(bt);
    std::cout << std::endl;
    std::cout << "NRPreOrder" << std::endl;
    NRPreOrder(bt);
    std::cout << std::endl;
    std::cout << "NRPreOrder1" << std::endl;
    NRPreOrder1(bt);
    std::cout << std::endl;
    std::cout << "RInOrder" << std::endl;
    RInOrder(bt);
    std::cout << std::endl;
    std::cout << "NRInOrder" << std::endl;
    NRInOrder(bt);
    std::cout << std::endl;
    std::cout << "NRInOrder1" << std::endl;
    NRInOrder1(bt);
    std::cout << std::endl;
    std::cout << "RPostOrder" << std::endl;
    RPostOrder(bt);
    std::cout << std::endl;
    std::cout << "NRPostOrder" << std::endl;
    NRPostOrder(bt);
    std::cout << std::endl;
    std::cout << "NRPostOrder1" << std::endl;
    NRPostOrder(bt);
    std::cout << std::endl;
    DestroyBiTree(bt);
}
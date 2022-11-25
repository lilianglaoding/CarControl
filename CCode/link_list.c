#include <stdio.h>
#include <stdlib.h>

struct node
{
	int data;
	struct node *next;
};

int create_list(struct node **head, int data)
{
	struct node *node;

	node = (struct node *)malloc(sizeof(struct node));
	if (node == NULL)
		return -1;
	
	node->data = data;
	node->next = *head;
	*head = node;

	return 0;
}

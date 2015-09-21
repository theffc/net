#include <sys/types.h>
#include <sys/socket.h>
#include <netdb.h>
#include <string.h>
#include <stdlib.h>
#include <stdio.h>

#define IP 1
#define PORT 2
#define BUFFER 1000000

// extern struct addrinfo hints;

struct addrinfo hints;

void net_error(const char *msg, const int status);


int main(int argc, char const *argv[])
{	
	memset(&hints, 0, sizeof(struct addrinfo));
	hints.ai_family = AF_UNSPEC;
	hints.ai_socktype = SOCK_STREAM;
	hints.ai_flags = AI_PASSIVE;

	char *port;
	const char http[] = "http";
	printf("%s = %d\n", argv[1], strlen(argv[1]));
	if (argc < 2 || argc > 3)
	{
		fprintf(stderr, "Invalid call!\nCall format should be:\n<program> <ip> [<port>]\n");
		exit(1);
	} else if (argc == 2) { // port not declared; default is http
		port = malloc(strlen(http) * sizeof(char));
		port = strcpy(port, http);
	} else { // port declared
		port = malloc(strlen(argv[PORT]) * sizeof(char));
		port = strcpy(port, argv[PORT]);
	}

	int status;
	struct addrinfo *ai;
	status = getaddrinfo(argv[IP], port, &hints, &ai);
	net_error("GETADDRINFO", status);

	// creates a socket for this application
	int sock;
	sock = socket(ai->ai_family, ai->ai_socktype, ai->ai_protocol);
	printf("Socket %d criado!\n", sock);
	// tries to connect to the server
	status = connect(sock, ai->ai_addr, ai->ai_addrlen);
	net_error("CONNECT", status);
	printf("Connection established!\n");
	
	// sends a message
	char msg[BUFFER];
	strcat(msg, "GET /index.html HTTP/1.1\r\nHost: ");
	strcat(msg, argv[IP]);
	strcat(msg, "\r\n\r\n");
	printf("\n*MESSAGE* (http)\n%s\n", msg );

	int by_sent = 0;
	int len = strlen(msg);
	char *aux = msg;
	char c;
	while (by_sent < len){
		aux = aux + by_sent;
		by_sent = send(sock, aux, len, 0);
		printf("bytes sent = %d\n", by_sent);
	}

	// receives a message
	// int recv(int sockfd, void *buf, int len, int flags);
	int by_recv = 1;
	char buff[BUFFER];
	int count = 0;
	while(by_recv != 0 && count < BUFFER){
		by_recv = recv(sock, &buff, BUFFER, 0);
		count += by_recv;
		printf("\n** Value received from recv() = %d\n", by_recv);
		printf("** Total bytes recv = %d\n\n\n", count);
		printf("%s\n", buff);
	}

	printf("\n\nMessage has ended; received %d bytes!\n", count);
	return 0;
}


		

void net_error(const char *msg, const int status)
{
	if (status)
	{
		fprintf(stderr, "%s error: %s\n", msg, gai_strerror(status));
		exit(1);
	}
	return;
}
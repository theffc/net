#include <sys/types.h>
#include <sys/socket.h>
#include <netdb.h>
#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include "sockets-util.c"

#define IP 1
#define PORT 2
#define BUFFER 1000000


void get_input(const int argc, char *argv[], char **ip, char **path, char **port)
{
	const char http[] = "http";
	
	printf("%s = %d\n", argv[IP], strlen(argv[IP]));
	if (argc < 2 || argc > 3)
	{
		fprintf(stderr, "Invalid call!\nCall format should be:\n<program> <ip> [<port>]\n");
		exit(1);
	} else if (argc == 2) { // port not declared; default is http
		*port = (char*) malloc(strlen(http) * sizeof(char));
		*port = strcpy(*port, http);
	} else { // port declared
		*port = (char*) malloc(strlen(argv[PORT]) * sizeof(char));
		*port = strcpy(*port, argv[PORT]);
	}

	char *tok;
	tok = strtok(argv[1], "/");
	*ip = (char*) malloc(strlen(tok) * sizeof(char));
	strcpy(*ip, tok);
	printf("IP = %s\n", *ip);

	tok = strtok(NULL, "");
	if (tok){
		*path = (char*) malloc(strlen(tok) * sizeof(char));
		strcpy(*path, tok);
	}
	else {
		*path = (char*) malloc(strlen("index.html") * sizeof(char));
		strcpy(*path, "index.html");
	}
	printf("Path = %s\n", *path);

	
}



int main(int argc, char const *argv[])
{	
	char *ip, *path, *port;
	get_input(argc, argv, &ip, &path, &port);
	printf("IP = %s\n", ip);
	printf("Path = %s\n", path);



	struct addrinfo *serv_info;
	serv_info = create_addrinfo(SOCK_STREAM, ip, port);
 
	int sock;
	sock = create_socket(serv_info, &connect);
	
// sends a message
	char msg[BUFFER];
	strcat(msg, "GET /index.html HTTP/1.1\r\nHost: ");
	strcat(msg, argv[IP]);
	strcat(msg, "\r\n\r\n");
	printf("\n*MESSAGE* (http)\n%s\n", msg );

	int by_sent = 0; // quantity of bytes already sent
	int msg_len = strlen(msg);
	char *aux = msg;
	while (by_sent < msg_len){
		aux = aux + by_sent;
		by_sent = send(sock, aux, msg_len, 0);
		printf("bytes sent = %d\n", by_sent);
	}

// receives a message
	// int recv(int sockfd, void *buf, int len, int flags);
	int by_recv = 1;// quatity of bytes already received
	char buff[BUFFER];
	int count = 0;// total of bytes received in the end
	while(by_recv > 0 && count < BUFFER){
		by_recv = recv(sock, &buff, BUFFER, 0);
		if(by_recv == 0 || by_recv == -1)
			break;
		count += by_recv;
		printf("\n** Value received from recv() = %d\n", by_recv);
		printf("** Total bytes recv = %d\n\n\n", count);
		printf("%s\n", buff);
		if(strstr(buff, "\r\n\r\n"))
			break;
	}


	printf("\n\nMessage has ended; received %d bytes!\n", count);
	return 0;
}


		

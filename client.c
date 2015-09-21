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

// handles errors, and print them
void net_error(const char *msg, const int status)
{
	if (status)
	{
		fprintf(stderr, "%s error: %s\n", msg, gai_strerror(status));
		exit(1);
	}
	return;
}

const char http[] = "http";
char *port;


void check_input(const int argc, char const *argv[])
{
	printf("%s = %d\n", argv[IP], strlen(argv[IP]));
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
}

int main(int argc, char const *argv[])
{	
	check_input(argc, argv);

// prepares the struct adrrinfo
	memset(&hints, 0, sizeof(struct addrinfo));
	hints.ai_family = AF_UNSPEC; // can be IPv4 or IPv6
	hints.ai_socktype = SOCK_STREAM; // tcp
	hints.ai_flags = AI_PASSIVE; // fill in my IP for me

	int status;
	struct addrinfo *serv_info;
	status = getaddrinfo(argv[IP], port, &hints, &serv_info);
	net_error("GETADDRINFO", status);

// go through every server addrinfo and tries to create a socket and a connection
	struct addrinfo *ai;
	int sock;
	int flag;
	for (ai = serv_info; ai != NULL; ai = ai->ai_next){
		// creates a socket for this application
		flag = 0;
		sock = socket(ai->ai_family, ai->ai_socktype, ai->ai_protocol);
		if (sock == -1){ // socket creation error
			flag = 1;
			continue;
		}
		printf("Socket %d criado!\n", sock);
		// tries to connect to the server
		if (connect(sock, ai->ai_addr, ai->ai_addrlen) == -1){
			close(sock);
			flag = 1;
			continue; // cant connect to this
		}
		printf("Connection established!\n");
		break;
	}
	if (flag){
		printf("Couldnt make a socket OR establish a connection\n");
		exit(1);
	}
	
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
		count += by_recv;
		printf("\n** Value received from recv() = %d\n", by_recv);
		printf("** Total bytes recv = %d\n\n\n", count);
		printf("%s\n", buff);
	}

	printf("\n\nMessage has ended; received %d bytes!\n", count);
	return 0;
}


		

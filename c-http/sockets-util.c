#include <sys/types.h>
#include <sys/socket.h>
#include <netdb.h>
#include <string.h>

#ifndef LOCALHOST
#define LOCALHOST "127.0.0.1"
#endif

// handles errors, and PRINT them
void net_error(const char *msg, const int status)
{
	if (status){
		fprintf(stderr, "%s error: %s\n", msg, gai_strerror(status));
		exit(1);
	}
	return;
}


struct addrinfo* create_addrinfo(int socktype, const char *ip, const char *port)
{
	struct addrinfo hints;
	// prepares the struct adrrinfo
	memset(&hints, 0, sizeof(struct addrinfo));
	hints.ai_family = AF_UNSPEC; // can be IPv4 or IPv6
	hints.ai_socktype = socktype; // tcp or udp
	hints.ai_flags = AI_PASSIVE; // fill in my IP for me

	int status;
	struct addrinfo *info;
	status = getaddrinfo(ip, port, &hints, &info);
	net_error("GETADDRINFO", status);

	return info;
}

// go through every server addrinfo and tries to create a socket, then tries to make a connection or a binding
int create_socket(struct addrinfo *addrinfo, int (*function)(int, const struct sockaddr* , socklen_t))
{
	struct addrinfo *ai;
	int sock;
	int flag;
	for (ai = addrinfo; ai != NULL; ai = ai->ai_next){
		// creates a socket for this application
		flag = 0;
		sock = socket(ai->ai_family, ai->ai_socktype, ai->ai_protocol);
		if (sock == -1){ // socket creation error
			flag = 1;
			continue;
		}
	// do the function passed by the caller to the addrinfos
		if (function(sock, ai->ai_addr, ai->ai_addrlen) == -1){
			close(sock);
			flag = 1;
			continue; // cant use the function to this
		}
		printf("Socket Creation Accomplished!\n");
		break;
	}
	if (flag){
		printf("Socket Creation Error!\n");
		sock = -1;
	}

	free(addrinfo);
	return sock;
}
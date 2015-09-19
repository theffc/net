#include "my-socket.c"
#include <string.h>

int int main(int argc, char const *argv[])
{
	char *server_name;
	server_name = argv[]

	return 0;
}

extern struct addrinfo hints;
int status;
struct addrinfo *ai;
status = getaddrinfo(NULL, "http", &hints, &ai);
if (status)
{
	fprintf(stderr, "getaddrinfo error: %s\n", gai_strerror(status));
	exit(1);
}

// creates a socket for this application
int sock;
sock = socket(ai->ai_family, ai->ai_socktype, ai->ai_protocol)

// tries to connect to the server
status = connect(sock, ai->ai_addr, ai->ai_addrlen)
if (status)
{
	fprintf(stderr, "connect error: %s\n", gai_strerror(status));
}
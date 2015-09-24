#include <sys/types.h>
#include <sys/socket.h>
#include <netdb.h>
#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include <sys/stat.h>

#define PATH 1
#define PORT 2
#define BUFFER 1000000
#define HTTP_SIZE 100

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

int check_msg(char *msg, size_t len, char *path, char *host)
{
	printf("Entering check_msg()\n");
	char sep[] = " \r\n";
	const char HOST[] = "Host: ";
	char *start;
	char *tok;

	tok = strtok(msg, sep);
	if(strcmp(tok, "GET")){// it isnt a GET request
		return 1;
	}
	tok = strtok(NULL, sep);
	strcat(path, tok);

	/*start = strstr(msg, HOST);
	strcpy(host, start + strlen(HOST));*/

	printf("Exiting check_msg()\n");
	return 0;
}

const char http[] = "http";
char *port;
void check_input(const int argc, char const *argv[])
{
	if (argc < 2 || argc > 3)
	{
		fprintf(stderr, "Invalid call!\nCall format should be:\n<program> <path> [<port>]\n");
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
	struct addrinfo *local_info;
	status = getaddrinfo(NULL, port, &hints, &local_info);
	net_error("GETADDRINFO", status);

// go through every server addrinfo and tries to create a socket and a connection
	struct addrinfo *ai;
	int sock;
	for (ai = local_info; ai != NULL; ai = ai->ai_next){
		// creates a socket for this application
		sock = socket(ai->ai_family, ai->ai_socktype, ai->ai_protocol);
		if (sock == -1) // socket creation error
			continue;
		printf("Socket %d criado!\n", sock);
		// tries to connect to the server
		if (bind(sock, ai->ai_addr, ai->ai_addrlen) == -1){
			close(sock);
			continue; // cant bind this
		}
		printf("Binding established!\n");
		break;
	}
	if (!ai){
		printf("Couldnt make a socket OR bind an address\n");
		exit(1);
	}
	freeaddrinfo(local_info); // all done with this structure
	
	//int listen(int sockfd, int backlog);
	int new_sock;
	while(1){
		struct sockaddr_storage client_addr;
		socklen_t addr_len;
		addr_len = sizeof(struct sockaddr_storage);

		status = listen(sock, 20);
		if (status == -1)
		{
			printf("Erro no listen!\n");
			exit(1);
		}
		new_sock = accept(sock,(struct sockaddr *)&client_addr, &addr_len);
		if(new_sock == -1){
			printf("Erro no Accept\n");
			exit(1);
		}

		// receives a message
		// int recv(int sockfd, void *buf, int len, int flags);
		int by_recv = 1;// quatity of bytes already received
		char buff[BUFFER];
		int count = 0;// total of bytes received in the end
		//while(by_recv > 0 && count < BUFFER){
			by_recv = recv(new_sock, &buff, BUFFER, 0);
			count += by_recv;
			printf("\n** Value received from recv() = %d\n", by_recv);
			printf("** Total bytes recv = %d\n\n\n", count);
			printf("%s\n", buff);
		
		printf("\n\nMessage has ended; received %d bytes!\n", count);

		char path[200];
		strcpy(path, argv[PATH]);
		char host[100];
		char answer[100000];
		FILE *fd;
		check_msg(buff, strlen(buff), path, host);
		fd = fopen(path, "r");
		if ( fd == NULL)
		{
			strcpy(answer, "HTTP/1.1 404 Not Found\r\nConnection: close\r\n");
			send(new_sock, answer, strlen(answer), 0);
			close(new_sock);
			continue;
		}
		printf("Has the file!\n");
		struct stat st;
		stat(path, &st);
		char *data;
		off_t lSize;
		size_t result;

		lSize = st.st_size;
		// obtain file size:
		/*printf("before seek\n");
		fseek(fd , 0 , SEEK_END);
		printf("before tell\n");
		lSize = ftell(fd);
		printf("after tell\n");
		rewind(fd);*/
		//rewind(fd);
		// allocate memory to contain the whole file:
		printf("Tamanho = %d\n", sizeof(char)*lSize );
		data = (char*) malloc (sizeof(char)*lSize);
		if (data == NULL) {fputs ("Memory error",stderr); exit (2);}
		printf("before copy\n");
		// copy the file into the data:
		result = fread (data, lSize, 1, fd);
		//if (result != lSize) {fputs ("Reading error\n",stderr); exit (3);}
		/* the whole file is now loaded in the memory data. */
		// sends the file
		printf("antes de mandar\n");
		// packet = (char*) malloc(sizeof(char)*lSize + HTTP_SIZE);
		// strcat(packet, "HTTP/1.1 200 OK\r\n");
		// strcat(packet, "Content-leng")
		send(new_sock, data, strlen(data), 0);
		// terminate
		fclose (fd);
		free (data);
	}
	//int accept(int sockfd, struct sockaddr *addr, socklen_t *addrlen);

	

	//printf("\n\nMessage has ended; received %d bytes!\n", count);
	return 0;
}


		
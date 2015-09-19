#include <sys/types.h>
#include <sys/socket.h>
#include <netdb.h>

##ifndef LOCALHOST
#define LOCALHOST "127.0.0.1"
#endif

struct addrinfo hints;
memset(&hints, 0, sizeof hints);
hints.ai_family = AF_UNSPEC;
hints.ai_socktype = SOCK_STREAM;
hints.ai_flags = AI_PASSIVE;


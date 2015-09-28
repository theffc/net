from socket import *
import sys
from argparse import *
import re

# Constantes para indexar a addrinfo tuple
FAMILY = 0
TYPE = 1
PROTO = 2
CANNONNAME = 3
SOCKADDR = 4
#----------------
BUFFERSIZE = 2147483648


# Parte de receber INPUT
ap = ArgumentParser()
ap.add_argument('host', help='<ip> + <path> of the server')
ap.add_argument('port', nargs='?', default='http', help='port in which this application will connect with')
args = ap.parse_args()
print (args)

dest = args.host.split('/', 1)
path = '/'
if(len(dest) == 2):
	path += dest[1]
ip = dest[0]
port = args.port
print (ip, path, port)
#------------------------------------------


# Criando e Conectando sockets
sock = None
erro = True
ai = getaddrinfo(ip, port, family=AF_UNSPEC, type=SOCK_STREAM, flags=AI_PASSIVE)
for x in ai:
	try:
		sock = socket(x[FAMILY], x[TYPE], x[PROTO])
		sock.connect(x[SOCKADDR])
		print('deu certo')
		break
	except:
		if sock:
			sock.close()
			sock = None
		print('entrou no except')
		continue
if not sock:
	print("Erro na criacao do socket!")
	exit()
#----------------------------------------------


# Enviando mensagens pelo socket criado
msg = ""
msg += "GET " + path + " HTTP/1.1\r\n" \
	   "Host: " + ip + "\r\n\r\n"
print(msg)
sock.send(msg.encode())
#-------------------------------------------


# Recebendo mensagens
len_re = re.compile("Content-Length: (?P<len>[0-9]+)")

buff = sock.recv(BUFFERSIZE)
# buff = buff.decode()
l = buff.split(b'\r\n\r\n', 1)
head = l[0]
body = (l[1] if len(l) > 1 else b'')
match = len_re.search(head.decode())
print(len(head))
print(len(body))
if match:# a msg possui um header Content-Length
	total_len = int(match.group('len'))
	print('Total = ', total_len)
	while len(body) < total_len:
		buff = sock.recv(total_len)
		# buff = buff.decode()
		body += buff
else:# a msg nao possui length; o client so vai parar de receber quando o servidor encerrar a conexao
	while len(buff) > 0:
		buff = sock.recv(BUFFERSIZE)
		# buff = buff.decode()
		body += buff

resp = head + body
print(len(resp))
# print(resp)


# def get_msg(sock):
# 	buff = sock.recv(BUFFSIZE)


	



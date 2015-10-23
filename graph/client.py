from socket import *
import argparse
import re
from urllib.parse import urlparse

from my_socket import *


# Parte de receber INPUT
ap = argparse.ArgumentParser()
ap.add_argument('url', help='URL')
ap.add_argument('port', nargs='?', default='http', help='port in which this application will connect with')
ap.add_argument('method', nargs='?', help='http method', default='GET')
args = ap.parse_args()
print(args)

# dest = args.host.split('/', 1)
# path = '/'
# if len(dest) == 2:
#     path += dest[1]
# ip = dest[0]
# port = args.port
# print (ip, path, port)

if "//" not in args.url:
    args.url = 'http://' + args.url
url = urlparse(args.url)
print(url)
print(url.hostname)
sock = make_socket(url.hostname, args.port, socket.socket.connect)
# sock = socket.create_connection((url.hostname, args.port))




request = make_request(url, args.method, url.query)
print(' --- REQUEST --- ')
print(request)


# Enviando mensagens pelo socket criado
sock.send(request.encode())
# -------------------------------------------

# Recebendo mensagens
response = rcv_msg(sock)
print(' --- RESPONSE ---')
print(response)

# def get_msg(sock):
# 	buff = sock.recv(BUFFSIZE)




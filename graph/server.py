import socket
from argparse import *
import re
from urllib.parse import *
import threading

from my_socket import *
from server_util import *

import sys
from io import StringIO

import pickle




# Parte de receber INPUT
ap = ArgumentParser()
ap.add_argument('path', help='URL')
ap.add_argument('port', nargs='?', default='http', help='port in which this server will listen to')
args = ap.parse_args()
print(args)

PATH = args.path
port = args.port



# Criando e fazendo Binding de sockets
listen_sock = make_socket(None, port, socket.socket.bind)
# --------------------------------------

listen_sock.listen(20)
while True:
    (client_sock, client_addr) = listen_sock.accept()
    client_handler(client_sock, client_addr)



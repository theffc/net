import socket
from argparse import *
import re
from urllib.parse import *
import threading
import os.path

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


if not os.path.exists(PATH):
    print("Invalid Path!")
    exit(1)

# Criando e fazendo Binding de sockets
sock_listen = make_socket(None, port, socket.socket.bind)
# --------------------------------------

sock_listen.listen(20)
while True:
    (sock_client, client_addr) = sock_listen.accept()
    client_handler(sock_client, client_addr)



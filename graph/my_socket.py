import socket
import re

# Constantes para indexar a addrinfo tuple
FAMILY = 0
TYPE = 1
PROTO = 2
CANNONNAME = 3
SOCKADDR = 4
# ----------------
BUFFERSIZE = 4028

version = ' HTTP/1.1'
new_line = '\r\n'
end = '\r\n\r\n'


def make_socket(host, port, function) -> socket.socket:
    sock = None
    ai = socket.getaddrinfo(host, port, family=socket.AF_UNSPEC, type=socket.SOCK_STREAM, flags=socket.AI_PASSIVE)
    for x in ai:
        try:
            sock = socket.socket(x[FAMILY], x[TYPE], x[PROTO])
            print('antes da funcao')
            function(sock, x[SOCKADDR])
            print('Socket established = ', sock)
            break
        except:
            if sock:
                sock.close()
                sock = None
            print('entrou no except')
            continue
    if not sock:
        print("Erro na criacao do socket!")
        exit(1)
    else:
        return sock


def make_request(url, method='GET', body='') -> str:
    msg = ''
    body = (body if body else url.query)

    host_header = "Host: " + url.hostname
    content_type = 'Content-Type: ' + 'application/x-www-form-urlencoded'
    content_length = 'Content-Length: ' + str(len(body))

    if method == 'GET':
        msg += "GET " + url.path + url.query + version + new_line + \
                host_header + end
    elif method == 'HEAD':
        msg += "HEAD " + url.path + url.query + version + new_line +\
                host_header + end
    elif method == 'POST':
        msg += "POST " + url.path + version + new_line + \
                host_header + new_line +\
                content_type + new_line +\
                content_length + end +\
                body
    elif method == 'PUT':
        msg += 'PUT ' + url.path + version + new_line + \
                host_header + new_line + \
                content_length + end + \
                body + end
    elif method == 'DELETE':
        msg += 'DELETE ' + url.path + version + new_line + \
                host_header + end
    else:
        print('Invalid method!')
        exit(1)
    return msg


def rcv_msg(sock) -> str:
    len_re = re.compile("Content-Length: (?P<len>[0-9]+)")

    buff = sock.recv(BUFFERSIZE)
    buff = buff.decode()
    head, body = get_head_body(buff)
    match = len_re.search(head)
    print('Size of HEAD = ', len(head))
    print('Size of BODY = ', len(body))
    if match:  # a msg possui um header Content-Length
        total_len = int(match.group('len'))
        print('Total size of msg = ', total_len)
        while len(body) < total_len:
            buff = sock.recv(total_len)
            buff = buff.decode()
            body += buff
    else:  # a msg nao possui length; o client so vai parar de receber quando o servidor encerrar a conexao
        while len(buff) > 0:
            buff = sock.recv(BUFFERSIZE)
            buff = buff.decode()
            body += buff
    resp = head + end + body
    return resp

def get_head_body(msg:str) -> tuple:
    end = '\r\n\r\n'
    # msg = msg.decode()
    l = msg.split(end, 1)
    head = l[0]
    body = (l[1] if len(l) > 1 else '')
    return head, body


def make_response(status, body='') -> str:
    msg = ''
    content_length = ('Content-Length: ' + str(len(body)) if body else '')
    if content_length:
        msg += version + ' ' + status + new_line + \
                content_length + end
    else:
        msg += version + ' ' + status + end

    return msg.encode()
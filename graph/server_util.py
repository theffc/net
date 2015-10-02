from my_socket import *
from urllib.parse import *
from graph import *


# Constantes para status da resposta
STATUS_OK = '202 OK'
STATUS_NOT_FOUND = '404 Not Found'
STAT_SERVER_ERROR = '500 Server Error'
# ----------------

LOCALHOST = socket.gethostname()


graphs = dict()


def client_handler(sock, addr):
    request = rcv_msg(sock)
    print(request)
    response = handle_request(request)
    if isinstance(response, str):
        response = response.encode()
    sock.send(response)
    print(graphs.popitem()[1].mostrar())
    sock.close()


def handle_request(request):
    head, body = get_head_body(request)
    line = head.split(' ', 2)
    method = line[0]
    url = line[1]
    url = LOCALHOST + url
    if "//" not in url:
        url = 'http://' + url
    url = urlparse(url)
    path = url.path
    # if url.path != PATH:
    #     msg = make_response(url, STATUS_NOT_FOUND)
    #     return msg
    # aux = url.path.split('/')
    # file = (aux[-1] if aux[-1] else aux[-2])

    if method == 'GET':
        if path not in graphs:
            msg = make_response(STATUS_NOT_FOUND)
            return msg
        body = graphs[path].mostrar()
        msg = make_response(STATUS_OK, body)
        return msg

    elif method == 'HEAD':
        if path in graphs:
            msg = make_response(STATUS_OK)
        else:
            msg = make_response(STATUS_NOT_FOUND)
        return msg

    elif method == 'DELETE':
        if path not in graphs:
            msg = make_response(STATUS_NOT_FOUND)
            return msg
        del graphs[path]
        return make_response(STATUS_OK, 'The graph *'+ path +'* has been deleted.')

    elif method == 'POST':
        qs = (url.query if url.query else body)
        if path not in graphs:
            graphs[path] = Grafo()
            if not qs:
                return make_response(STATUS_OK, 'Graph created.')
        query = parse_qs(qs, keep_blank_values=True)
        print(query)
        if not ('vertice' or 'edge') in query:
            msg = make_response(STAT_SERVER_ERROR)
            return msg
        if 'vertice' in query:
            for v in query['vertice']:
                graphs[path].newV(v)
        if 'edge' in query:
            for e in query['edge']:
                _l = e.split('-')
                v1, v2 = _l[0], _l[1]
                v1 = graphs[path].get_vertice(v1)
                v2 = graphs[path].get_vertice(v2)
                graphs[path].newA(v1, v2)
        msg = graphs[path].mostrar()
        return msg

    elif method == 'PUT':
        qs = (url.query if url.query else body)
        query = parse_qs(qs, keep_blank_values=True)
        if not ('vertice' or 'edge') in query:
            msg = make_response(STAT_SERVER_ERROR)
            return msg
        if 'vertice' in query:
            for v in query['vertice']:
                graphs[path].removeV(v)
        if 'edge' in query:
            for e in query['edge']:
                _l = e.split('-')
                v1, v2 = _l[0], _l[1]
                v1 = graphs[path].get_vertice(v1)
                v2 = graphs[path].get_vertice(v2)
                graphs[path].removeA(v1, v2)
        msg = graphs[path].mostrar()
        return msg

    else:
        print('Invalid method!')
        exit(1)
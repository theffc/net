<!-- MarkdownTOC -->

- [Causally Ordered Multicast][causally-ordered-multicast]
    - [Implementação][implementação]
        - [Inicialização (Main)][inicialização-main]
        - [Threads][threads]
            - [App][app]
            - [Receber mensagens][receber-mensagens]
            - [Enviar Mensagens][enviar-mensagens]
        - [Recursos Compartilhados][recursos-compartilhados]
            - [SendingQueue][sendingqueue]
            - [ReceivingQueue][receivingqueue]
            - [VectorClock][vectorclock]
- [Introduction][introduction]
    - [Total Order][total-order]
    - [Source Order or FIFO Order][source-order-or-fifo-order]
    - [Causal Order or Happened-before Order][causal-order-or-happened-before-order]
        - [Vector Clocks][vector-clocks]

<!-- /MarkdownTOC -->

-----------

# Causally Ordered Multicast

A intenção desse trabalho é utilizar ***vector clocks*** para conseguir comunicar, por meio de *multicast*, com pares distribuídos da aplicação, afim de estabelecer uma **ordenação causal** entre as mensagens trocadas.  

## Implementação

- Cada nó terá 3 threads:
    + uma representará a camada de aplicação, ela irá decidir aleatoriamente quando vai querer enviar uma mensagem.
    + uma para receber mensagens
    + uma para enviar mensagens
- Constante que define o tick padrão. Só que cada sistema randomiza uma alteração nesse tick


### Inicialização (Main)
- receber por linha de comando os seguintes parâmetros:
    + numNodes : quantidade de nós envolvidos no sistema
    + nodeID : identificador *exclusivo* do nó em questão; deve ser um número no intervalo (0, numNodes]
- criar o VectorClock e inicializá-lo com 0 em todos os campos
- criar e inciar a thread App
- criar e iniciar a camada Middleware

### Threads

#### App

Representa a camada de aplicação. Utiliza o Middleware para se comunicar com os outros nós. 

A cada tick, irá executar os seguintes passos:

- checar se existe alguma mensagem na fila de mensagens recebidas;
    + caso exista, irá printar essa mensagem
- decidir (aleatoriamente) se envia uma nova mensagem, ou fica quieto;
    + caso decida enviar, irá passar a mensagem pra thread Send


#### Receber mensagens

Chama o recv() do multicast; quando receber algo, irá executar os seguintes passos:

- Esperar o recebimento das mensagens, que ainda não chegaram, anteriores a essa, caso:
    + `myVC[sender] < msgVC[sender] - 1`
    + `myVC[i] < msgVC[i], i != sender`
- Ajustar o `myVC`:
    + `myVC[i] = max(myVC[i], msgVC[i])`
    + `myVC[sender] += 1`
- Colocar a mensagem recebida na fila de mensagens recebidas da aplicação


#### Enviar Mensagens
Verifica se há mensagens a serem enviadas na fila; se houver, executará os seguintes passos:

- `myVC[myID] += 1`
    + para realizar a operação acima, será necessário lockar o clock, pois trata-se de um read e de uma escrita. Caso fizesse primeiro a atualização e depois a leitura, ambos sincronizados, ainda tería-se o problema do valor ter sido atualizado novamente.
- crio a mensagem, colocando o `myVC` nela.
- envio a mensagem para os outros processos
- passo a mensagem para a aplicação


### Recursos Compartilhados

#### SendingQueue
utilizado pelas threads App e Send

#### ReceivingQueue
utilizado pelas threads App e Recv

#### VectorClock
utilizado pelas threads Send e Recv
- precisa ser inicializado com um tamanho igual ao número de processos distribuídos
- necessário implementar um método sincronizado que será utilizado pela thread Send:
    + incrementará o campo relativo ao nó em questão
    + retornará o VectorClock atualizado
- como a thread Recv não irá alterar o valor do nó em questão – apenas dos outros nós – ela não precisa se preocupar em lockar o clock.
    


------------
# Introduction

No contexto de sistemas distribuídos e programação paralela, pode ser necessário saber a ordem cronológica em que eventos ocorreram em um dado sistema. Assim, definimos a seguir algumas possíveis formas de ordenação temporais.

## Total Order

Acontece quando cada processo envolvido possue a mesma visão cronológica dos eventos que ocorreram em todos os processos.

In total ordering, if a message is delivered before another message
at one process, then the same order will be preserved at all processes.

## Source Order or FIFO Order

Garante que mensagens de um mesmo processo serão entregues em ordem para os outros processos. Ou seja, se uma mensagem foi enviada antes de outra mensagem por um mesmo processo, elas serão entregues aos outros processo nessa mesma ordem.

## Causal Order or Happened-before Order

Faz com que os eventos obedeçam á relação de causalidade definida por Lamport como Happened-before Relationship (**`->`**).

- If two events occurred at the same process `p`, then they occurred in the order in which `p` observes them.
- Whenever a message is sent between processes, the event of sending the message occurred before the event of receiving the message.

Se a ordenação causal for verdadeira entre dois eventos, ou seja, **`e1 -> e2`**, então pode-se afirmar que o "timestamp" de `e1` é **menor** que o de `e2`.

No entanto, pode ser interessante partir de `time(e1) < time(e2)`, para se chegar em `e1 -> e2`. Infelizmente, essa implicação lógica, na prática, é difícil de ser implementada. A técnica de **vector clocks** tenta garantir essa afirmação lógica para alguns casos.

#### Vector Clocks

A vector clock of a system of N processes is an array/vector of N logical clocks, one clock per process; a local "smallest possible values" copy of the global clock-array is kept in each process, with the following rules for clock updates:

- Initially all clocks are zero.

- Each time a process experiences an internal event, it increments its own logical clock in the vector by one.

- Each time a process prepares to send a message, it sends its entire vector along with the message being sent.

- Each time a process receives a message, it increments its own logical clock in the vector by one and updates each element in its vector by taking the maximum of the value in its own vector clock and the value in the vector in the received message (for every element).

##### Comparações

Igualdade: se todos os campos de dois vetores forem iguais.

Menor (**<**): um vetor clock é menor que outro, **sse**: 
    - todos os elementos de um forem menores ou iguais que os dos outros;
    - e os vetores forem diferentes entre si.

- Se o clock do evento `e1` for menor do que o de um outro evento `e2`, então podemos dizer que `e1` *happened-before* `e2`.
        c(e1) < c(e2) => e1 -> e2






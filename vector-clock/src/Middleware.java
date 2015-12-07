import java.net.NetworkInterface;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;

/**
 * Created by theffc on 05/12/15.
 */
public class Middleware {

    VectorClock vClock;
    int nodeID;
    int numOfNodes;

    private BlockingQueue<byte[]> recvQueue;
    private BlockingQueue<byte[]> sendQueue;


    public Middleware(int numOfNodes, int nodeID) {

        this.nodeID = nodeID;
        this.numOfNodes = numOfNodes;

        vClock = new VectorClock(numOfNodes, nodeID);
        recvQueue = new LinkedBlockingQueue<byte[]>();
        sendQueue = new LinkedBlockingQueue<byte[]>();

        Thread send, recv;
        send = new Thread(new CausalOrderSend(sendQueue));
        recv = new Thread(new CausalOrderRecv(recvQueue));

    }

    public void send(String msg) {
        //Byte byMsg = Byte.decode(msg);
        sendQueue.add(msg.getBytes());
    }

    // bloqueia até receber uma mensagem
    public String recv() {
        try {
            byte[] by = recvQueue.take();
            return Arrays.toString(by);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // caso não tenha mensagem, retorna imediatamente null(sem bloquear)
    public String poll() {
        byte[] by = recvQueue.poll();
        if (by != null) {
            return Arrays.toString(by);
        }
        return null;
    }

}

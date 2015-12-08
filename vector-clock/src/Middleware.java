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

    int nodeID;
    int numOfNodes;

    public BlockingQueue<byte[]> recvQueue;
    public BlockingQueue<byte[]> sendQueue;


    public Middleware(int numOfNodes, int nodeID) {

        this.nodeID = nodeID;
        this.numOfNodes = numOfNodes;

        VectorClock.init(numOfNodes, nodeID);
        recvQueue = new LinkedBlockingQueue<byte[]>();
        sendQueue = new LinkedBlockingQueue<byte[]>();

        new CausalOrderSend(sendQueue);
        new CausalOrderRecv(recvQueue);

    }

    public void send(String msg) {
        //Byte byMsg = Byte.decode(msg);
        sendQueue.add(msg.getBytes());
    }

    // bloqueia até receber uma mensagem
    public String recv() {
        try {
            byte[] by = recvQueue.take();
            return new String(by);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }

    // caso não tenha mensagem, retorna imediatamente null(sem bloquear)
    public String poll() {
        byte[] by = recvQueue.poll();
        if (by != null) {
            return new String(by);
        }
        return null;
    }

}

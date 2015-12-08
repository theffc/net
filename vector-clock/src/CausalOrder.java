import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Created by theffc on 06/12/15.
 */
public abstract class CausalOrder {

    public static InetAddress MCAST_GROUP;
    public static final int SLEEP_TIME = 2000;
    public static final int MCAST_PORT = 39052;
    public static final int MAX_BUF = 4096;

    protected BlockingQueue<byte[]> appQueue;
    protected MulticastSocket sock;
    //protected VectorClock vClock;

    public CausalOrder(BlockingQueue<byte[]> appQueue) {
        this.appQueue = appQueue;
        //this.vClock = vClock;

        try {
            MCAST_GROUP = InetAddress.getByName("224.224.224.224");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.concurrent.BlockingQueue;

/**
 * Created by theffc on 06/12/15.
 */
public class CausalOrderSend extends CausalOrder implements Runnable{

    public CausalOrderSend(BlockingQueue<byte[]> appQueue) {
        super(appQueue);

        try {
            this.sock = new MulticastSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Thread t = new Thread(this);
        t.setName("Sender");
        t.start();
    }

    @Override
    public void run() {
        while(true) {
            try {
                byte[] msg = appQueue.take();

                VectorClock vClock = VectorClock.makeUpdate();
                vClock.setMsg(msg);
                msg = vClock.toByteArray();

                DatagramPacket pckt = new DatagramPacket(msg, msg.length, MCAST_GROUP, MCAST_PORT);
                sock.send(pckt);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}


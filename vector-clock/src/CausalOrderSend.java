import java.io.IOException;
import java.net.DatagramPacket;
import java.util.concurrent.BlockingQueue;

/**
 * Created by theffc on 06/12/15.
 */
public class CausalOrderSend extends CausalOrder implements Runnable{

    public CausalOrderSend(BlockingQueue<byte[]> appQueue, VectorClock vClock) {
        super(appQueue, vClock);

        Thread t = new Thread(this);
        t.start();
    }

    @Override
    public void run() {
        while(true) {
            try {
                byte[] msg = appQueue.take();

                synchronized (vClock) {
                    vClock.update();
                    vClock.setMsg(msg);
                    msg = vClock.toByteArray();
                }
                DatagramPacket pckt = new DatagramPacket(msg, msg.length, Global.MCAST_GROUP, Global.MCAST_PORT);
                sock.send(pckt);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}


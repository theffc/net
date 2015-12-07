import java.io.IOException;
import java.net.DatagramPacket;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by theffc on 06/12/15.
 */
public class CausalOrderRecv extends CausalOrder implements Runnable {

    private Queue<VectorClock> waitQueue;

    public CausalOrderRecv(BlockingQueue<byte[]> appQueue, VectorClock vClock) {
        super(appQueue, vClock);

        waitQueue = new PriorityQueue<>();

        sock.connect(MCAST_GROUP, MCAST_PORT);

        Thread t = new Thread(this);
        t.start();
    }

    @Override
    public void run() {
        while (true) {
            byte[] msg = new byte[MAX_BUF];
            DatagramPacket pckt = new DatagramPacket(msg, MAX_BUF);
            try {
                sock.receive(pckt);
            } catch (IOException e) {
                e.printStackTrace();
            }

            VectorClock peerVClock = VectorClock.parseFromMsg(pckt.getData());
            int peerID = peerVClock.getNodeID();

            waitQueue.add(peerVClock);
            for (int i = 0; i < waitQueue.size(); i++) {
                VectorClock vc = waitQueue.peek();
                // decremento para facilitar a comparação
                vc.lowerClock(peerID);
                /* se o meu vClock ainda for menor que o do outro, tenho que continuar esperando os pacotes que ainda não chegaram */
                if ((vClock.compareTo(vc) == VectorClock.SMALLER)) {
                    break;
                }
                vc = waitQueue.remove();
                vClock.update(vc);
                appQueue.add(vc.getMsg());
            }// end for
        }// end while
    }// end run()

}


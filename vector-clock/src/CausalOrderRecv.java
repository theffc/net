import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by theffc on 06/12/15.
 */
public class CausalOrderRecv extends CausalOrder implements Runnable {

    public static Queue<VectorClock> waitQueue;

    public CausalOrderRecv(BlockingQueue<byte[]> appQueue) {
        super(appQueue);

        waitQueue = new PriorityQueue<>();

        try {
            this.sock = new MulticastSocket(MCAST_PORT);
            this.sock.joinGroup(MCAST_GROUP);
        } catch (IOException e) {
            e.printStackTrace();
        }


        // sock.connect(MCAST_GROUP, MCAST_PORT);

        Thread t = new Thread(this);
        t.setName("Receiver");
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

            VectorClock myVClock = VectorClock.getVClock();
            VectorClock peerVClock = VectorClock.parseFromMsg(pckt.getData());
            int peerID = peerVClock.getNodeID();

            // descarta pacotes de mim mesmo
            if (peerID == myVClock.getNodeID()) {
                continue;
            }
            /*
            System.out.println("AppQueue: " + appQueue);
            System.out.println("WaitQueue: " + waitQueue);
            System.out.println("PeerID: " + peerID + " \t MyID: " + myVClock.getNodeID());
            */

            waitQueue.add(peerVClock);
            if (waitQueue.size() > 2){
                System.out.println("*** WaitQueue: " + waitQueue);
            }
            for (int i = 0; i < waitQueue.size(); i++) {
                VectorClock vc = waitQueue.peek();
                // decremento para facilitar a comparação
                vc.lowerClock();
                /* se o myVClock ainda for menor, tenho que continuar esperando os pacotes que ainda não chegaram */
                if ((myVClock.compareTo(vc) == VectorClock.SMALLER)) {
                    break;
                }
                vc = waitQueue.remove();
                vc.upClock();
                myVClock.update(vc);
                // System.out.println("MyVClock: " + myVClock);
                appQueue.add(vc.getMsg());
            }// end for
        }// end while
    }// end run()

}


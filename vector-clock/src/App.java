import java.sql.Time;
import java.util.Date;
import java.util.Random;

/**
 * Created by theffc on 05/12/15.
 */
public class App implements Runnable {

    public static final int SLEEP_TIME = 2000;


    private Middleware mid;
    private Random rand;
    private int numSentMsg = 0;
    private int nodeID;

    public static void main(String[] args) {
        int numOfNodes = Integer.valueOf(args[0]);
        int nodeID = Integer.valueOf(args[1]);

        System.out.println(numOfNodes + " " + nodeID);

        new App(numOfNodes, nodeID);
    }

    public App(int numOfNodes, int nodeID) {
        this.mid = new Middleware(numOfNodes, nodeID);
        this.rand = new Random();
        this.nodeID = nodeID;
        Thread t = new Thread(this);
        t.start();
    }

    @Override
    public void run() {

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        while (numSentMsg < 6) {
            // receber uma mensagem (sem bloquear)
            String msg = mid.poll();
            while (msg != null) {
                System.out.println("RECEIVED: " + msg);
                msg = mid.poll();
            }
            // sorteio se envio uma mensagem
            int coin = rand.nextInt(2);
            if (coin == 1) {
                //int sleep = rand.nextInt(SLEEP_TIME);
                mid.send(this.makeMsg());
            }
            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }// end while
        VectorClock.printLog();
        System.out.println();
        System.out.println("RecvQueue: " + mid.recvQueue);
        System.out.println("SendQueue: " + mid.sendQueue);
        System.out.println("WaitQueue: " + CausalOrderRecv.waitQueue);

    }

    private String makeMsg() {
        numSentMsg++;
        Date date = new Date();
        Time time = new Time(date.getTime());

        String msg = "<<< Node " + nodeID + " : ";
        msg += "Msg *" + numSentMsg + "* ";
        msg += time + " >>>";
        System.out.println(msg);
        return msg;
    }

}

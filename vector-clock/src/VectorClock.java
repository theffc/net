import java.io.*;
import java.util.*;
import java.lang.Math;
import java.util.Collections;

/**
 * Created by theffc on 05/12/15.
 */
public class VectorClock implements Serializable, Comparable<VectorClock> {

    public static final int CONCURRENT = 42;
    public static final int SMALLER = -1;
    public static final int EQUALS = 0;
    public static final int GREATER = 1;

    // private static final String separators = "!@#$";

    private static transient List<VectorClock> vClocksLog = Collections.synchronizedList(new LinkedList<VectorClock>());

    private List<Integer> clocks;
    private int myNode;
    private byte[] msg;


// ----------- Constructors -------------

    public VectorClock(int size, int myNode, byte[] msg) {
        this.myNode = myNode;
        this.msg = msg;
        clocks = Collections.synchronizedList(new ArrayList<Integer>(size));
        for (int i = 0; i < size; i++) {
            clocks.add(0);
        }
    }


    /* public VectorClock(int size, int myNode, byte[] msg) {
        this.myNode = myNode;
        clocks = Collections.synchronizedList(new ArrayList<Integer>(size));
        for (int i = 0; i < size; i++) {
            clocks.set(i, 0);
        }
        this.msg = msg;
    }*/

    public VectorClock(VectorClock vc) {
        this.myNode = vc.myNode;
        this.clocks = Collections.synchronizedList(new ArrayList<Integer>(vc.clocks));
        this.msg = vc.msg;
    }

// --------- Getters ------------------

    public static synchronized VectorClock getVClock() {
        return vClocksLog.get(0);
    }

    public static void init(int size, int myNode) {
        VectorClock vc = new VectorClock(size, myNode, "Initial State!".getBytes());
        vClocksLog.add(0, vc);
    }

    public List<Integer> getClocks() {
        return clocks;
    }

    public int getNodeID() {
        return myNode;
    }

    public Integer getClock(int node) {
        return clocks.get(node);
    }

    public Integer getClock() {
        return clocks.get(myNode);
    }

    public byte[] getMsg() {
        return msg;
    }

    public static List<VectorClock> getLog() {
        return vClocksLog;
    }


    // -------- Updates -----------

    public void setMsg(byte[] msg) {
        this.msg = msg;
    }

    public void lowerClock() {
        clocks.set(myNode, clocks.get(myNode) - 1);
    }

    public void upClock() {
        clocks.set(myNode, clocks.get(myNode) + 1);
    }

    public synchronized static VectorClock makeUpdate() {
        VectorClock vc = getVClock();
        VectorClock newVC = new VectorClock(vc);
        int node = newVC.getNodeID();
        int up = newVC.getClock(node) + 1;
        newVC.clocks.set(node, up);
        vClocksLog.add(0, newVC);
        return newVC;
    }

    public synchronized void update(VectorClock peerVClock) {
        VectorClock vc = new VectorClock(this);
        vc.setMsg(peerVClock.getMsg());
        for (int i = 0; i < vc.clocks.size(); i++) {
            int max = Math.max(vc.clocks.get(i), peerVClock.getClock(i));
            vc.clocks.set(i, max);
        }
        vClocksLog.add(0, vc);
    }

    public synchronized void update() {
        int up = clocks.get(myNode) + 1;
        VectorClock newVC = new VectorClock(this);
        newVC.clocks.set(myNode, up);
        vClocksLog.add(0, newVC);
    }

//---------- Persistence --------------

    public synchronized byte[] toByteArray() {
        ByteArrayOutputStream baOut = new ByteArrayOutputStream();
        this.toStream(baOut);
        return baOut.toByteArray();
    }

    public synchronized void toStream(OutputStream out) {
        try {
            ObjectOutputStream objOut = new ObjectOutputStream(out);
            objOut.writeObject(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static VectorClock fromStream(InputStream in) {
        VectorClock newVClock = null;
        try {
            ObjectInputStream ois = new ObjectInputStream(in);
            newVClock = (VectorClock) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }
        //System.out.println("VClock fromStream() : " + newVClock + " \tMSG: " + newVClock.getMsg());
        return newVClock;
    }


    public static VectorClock parseFromMsg(byte[] bytesMsg) {
        VectorClock vClock;
        // System.out.println("BytesMsg: " + Arrays.toString(bytesMsg));
        ByteArrayInputStream in = new ByteArrayInputStream(bytesMsg);
        return VectorClock.fromStream(in);

        /* String msgClock;
        String msg = Arrays.toString(bytesMsg);
        int i = msg.indexOf(separators);

        msgClock = msg.substring(0,i);
        ByteArrayInputStream in = new ByteArrayInputStream(msgClock.getBytes());
        vClock = VectorClock.fromStream(in);

        msg = msg.substring(i);

        return new VClockAndMsg(vClock, msg.getBytes()); */
    }

    /* public enum Compare {
        CONCURRENT (42),
        SMALLER (-1),
        EQUAL (0),
        GREATER (1);

    }*/

    @Override
    public int compareTo(VectorClock vc) {
        boolean menor = false;
        boolean maior = false;
        boolean igual = false;
        Integer a, b;

        for (int i = 0; i < clocks.size(); i++) {
            a = this.getClock(i);
            b = vc.getClock(i);

            if (a == b) {
                igual = true;
            } else if (a > b) {
                maior = true;
            } else if (a < b) {
                menor = true;
            }
        }

        if (maior) {
            if (menor/*|| igual*/)
                return CONCURRENT;
            return GREATER;
        }
        if (menor)
            return SMALLER;
        // if (igual)
        return EQUALS;
    }

    @Override
    public String toString() {
        String s = "";
        //s = "VectorClock " + this.myNode + " :\t";
        s += " <clocks> " + this.clocks.toString();
        s += " \t <msg> " + new String(msg);
        return s;
    }

    public static void printLog() {
        VectorClock v = VectorClock.getVClock();
        System.out.println();
        System.out.println("********************");
        System.out.println(" \t\tLog of Node " + v.getNodeID());
        for (VectorClock vc : vClocksLog) {
            System.out.println(vc);
        }
    }
}
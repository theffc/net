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

    private static final String separators = "!@#$";


    private List<Integer> clocks;
    private int myNode;
    private transient byte[] msg = null;


// ----------- Constructors -------------

    public VectorClock(int size, int myNode) {
        this.myNode = myNode;
        clocks = new Collections.synchronizedList(new ArrayList(size));
        for (int i = 0; i < size; i++) {
            clocks.set(i, 0);
        }
    }

    public VectorClock(int size, int myNode, byte[] msg) {
        this.myNode = myNode;
        clocks = new Collections.synchronizedList(new ArrayList(size));
        for (int i = 0; i < size; i++) {
            clocks.set(i, 0);
        }
        this.msg = msg;
    }

// --------- Getters ------------------

    public List<Integer> getClocks() {
        return clocks;
    }

    public int getNodeID(){
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

    // -------- Updates -----------


    public void setMsg(byte[] msg) {
        this.msg = msg;
    }

    public void lowerClock(int node) {
        clocks.set(node, clocks.get(node)-1);
    }

    public synchronized void update(VectorClock peerVClock) {
        for (int i = 0; i < clocks.size(); i++) {
            int max = Math.max(clocks.get(i), peerVClock.getClock(i));
            clocks.set(i, max);
        }
        return this;
    }

    public synchronized List<Integer> update() {
        int up = clocks.get(myNode) + 1;
        clocks.set(myNode, up);
    }
    
//---------- Persistence --------------

    public synchronized byte[] toByteArray(){
        ByteArrayOutputStream baOut = new ByteArrayOutputStream();
        this.toStream(baOut);
        return baOut.toByteArray();
    }

    public synchronized void toStream(OutputStream out){
        try {
            ObjectOutputStream objOut = new ObjectOutputStream(out);
            objOut.writeObject(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static VectorClock fromStream(InputStream in){
        VectorClock newVClock = null;
        try {
            ObjectInputStream ois = new ObjectInputStream(in);
            newVClock = (VectorClock) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return newVClock;
    }

    public byte[] include(byte[] msg) {
        byte[] vclock = this.toByteArray();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            out.write(vclock);
            out.write(separators.getBytes());
            out.write(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return out.toByteArray();
    }

    public static VectorClock parseFromMsg(byte[] bytesMsg) {
        VectorClock vClock;
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
}

import java.net.InetAddress;

/**
 * Created by theffc on 05/12/15.
 */
public abstract class Global {

    public static final InetAddress MCAST_GROUP = InetAddress.getByName("224.224.224.224");

    public static final int SLEEP_TIME = 2000;
    public static final int MCAST_PORT = 39052;

    public static VectorClock vClock;
}

/**
 * Created by theffc on 07/12/15.
 */
public class VClockAndMsg {
    public byte[] msg;
    public VectorClock vClock;

    public VClockAndMsg(VectorClock vClock, byte[] msg) {
        this.msg = msg;
        this.vClock = vClock;
    }
}

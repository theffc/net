/**
 * Created by theffc on 05/12/15.
 */
public class App implements Runnable {

    private Middleware mid;

    public App() {
        this.mid = new Middleware();
    }

    @Override
    public void run() {

        while (true) {
            msg = mid.recv();
            if (msg) {
                System.out.print(msg);
            }
            //TODO Randomizar um numero
            if (number > 0.5) {
                mid.send(this.makeMsg());
            }

        }
    }

}

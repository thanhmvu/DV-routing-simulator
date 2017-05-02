package dvroutingsimulator;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Automatically advertise distance vector of the router every t seconds
 *
 * @author thanhvu
 */
public class AutoUpdater implements Runnable {

    private final Router r;
    private final Timer timer;
    private final long t;           // t seconds
    private final int n = 3;

    /**
     * Constructor for AutoUpdater
     *
     * @param router The router owning this thread
     */
    AutoUpdater(Router router) {
        this.r = router;
        this.timer = new Timer();
        this.t = 20; // t seconds
    }

    /**
     * Run this thread
     */
    public void run() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    r.advertiseDV();
                } catch (IOException ex) {
                    Logger.getLogger(AutoUpdater.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }, 0, t * 1000);
        
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run(){
                r.checkNeighborStatus();
            }
        }, 0, n * t * 1000);
    }

    /**
     * Stop this thread
     */
    public void stop() {
        timer.cancel();
    }
}

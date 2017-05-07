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
    private Timer timer;
    static final long T = 10;           // period between scheduled tasks, t seconds
    private long timeCnt;     // time count from beginning of thread to now

    /**
     * Constructor for AutoUpdater
     *
     * @param router The router owning this thread
     */
    AutoUpdater(Router router) {
        this.r = router;
        this.timeCnt = 0;
    }

    /**
     * Run this thread
     */
    @Override
    public void run() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    timeCnt += T;
                    r.advertiseDV();

                    
                    
                } catch (IOException ex) {
                    Logger.getLogger(AutoUpdater.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }, 0, T * 1000);
    }

    /**
     * Stop this thread
     */
    public void stop() {
        timer.cancel();
        timeCnt = 0;
    }
    
    public long getCurrentTime(){
        return timeCnt;
    }
}

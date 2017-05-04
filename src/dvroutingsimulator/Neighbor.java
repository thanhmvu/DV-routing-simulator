
package dvroutingsimulator;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Create a class to contain neighbor router, bundling neighbor information
 * together
 */
class Neighbor {

    private Address a;
    private DistanceVector dv;
    private int w;
    private Timer timer;
    private int n = 3;
    private final long t = 20;
    private final Router r;

    /**
     * Create a neighbor
     *
     * @param a Address of the neighbor
     * @param weight The weight of the neighbor
     */
    Neighbor(Address a, int weight, final Router r) {
        this.r = r;
        this.a = a;
        this.w = weight;
        dv = new DistanceVector();
        timer = new Timer();
        restartTimer();
    }

    /**
     * Restart the timer if it's already started. The task is a drop
     * neighbor task
     */
    final void restartTimer() {
        timer.cancel();
        timer = new Timer();
        //this task drop the neighbor from the current router that it's contained in
        TimerTask dropNeighborTask = new TimerTask() {
            @Override
            public void run() {
                r.dropNeighbor(a);
            }
        };
        timer.schedule(dropNeighborTask, n * t * 1000);
    }

    /**
     * Stop the timer
     */
    void stopTimer() {
        timer.cancel();
    }

    DistanceVector getDistVector() {
        return dv;
    }

    void setDistVector(DistanceVector dv) {
        this.dv = dv;
    }

    int getLinkWeight() {
        return w;
    }

    Address getAddress() {
        return a;
    }

    void setLinkWeight(int weight) {
        this.w = weight;
    }
    
}

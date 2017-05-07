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
    private static final int n = 3;
    private final long t = AutoUpdater.T;
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
     * Restart the timer if it's already started. The task is a drop neighbor
     * task
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

    /**
     * Get the distance vector of this neighbor
     *
     * @return The distance vector of this neighbor
     */
    DistanceVector getDistVector() {
        return dv;
    }

    /**
     * Set the distance vector to a new one
     *
     * @param dv The update distance vector
     */
    void setDistVector(DistanceVector dv) {
        this.dv = dv;
    }

    /**
     * Get the link weight between the router and this neighbor
     *
     * @return the link weight between the router and this neighbor
     */
    int getLinkWeight() {
        return w;
    }

    /**
     * Set the link weight to another value
     *
     * @param weight the update link weight
     */
    void setLinkWeight(int weight) {
        this.w = weight;
    }

    /**
     * Retrieve the address of this neighbor router
     *
     * @return the address of this neighbor router
     */
    Address getAddress() {
        return a;
    }

}


package dvroutingsimulator;

/**
 * Implement the neighbor router
 * @author hongha912
 */
public class Neighbor {
    private Address a;
    private DistanceVector dv;
    private int w;
    private Timer timer;
    
    Neighbor(Address a, int weight) {
        this.a = a;
        this.w = weight;
        dv = new DistanceVector();
//        timer = new Timer();
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

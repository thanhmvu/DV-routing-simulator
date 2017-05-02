
package dvroutingsimulator;

/**
 * Implement the neighbor router
 * @author hongha912
 */
public class Neighbor {
    private Address a;
    private DistanceVector dv;
    private int w;
    private boolean updated;
    
    Neighbor(Address a, int weight) {
        this.a = a;
        this.w = weight;
        dv = new DistanceVector();
        updated = false;
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
    
    void setUpdatedStatus(boolean s){
        updated = s;
    }
    
    boolean isUpdated(){
        return updated;
    }
}

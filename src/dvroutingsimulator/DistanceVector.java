
package dvroutingsimulator;

import java.util.HashMap;

/**
 * Create a distance vector for the DV Routing Simulator
 * @author thanhvu
 */
public class DistanceVector {
    
    private HashMap<Address, Integer> dv;
    
    public DistanceVector(){
    
    }
    
    public DistanceVector(String text){
        // This method should parse a text representation of a DV 
        // and create a DistanceVector object based on a specific format
    }
    
    @Override
    public String toString(){
        // This method should output a text representation of a DV 
        // using a specific format
        return null;
    }
}

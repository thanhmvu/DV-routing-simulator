package dvroutingsimulator;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Create a distance vector for the DV Routing Simulator
 *
 * @author hongha912
 */
public class DistanceVector {

    private Map<Address, Integer> dvMap;
    private static final String INTER_DLM = ";"; //inter-vector delimiter
    private static final String INTRA_DLM = ":"; //intra-vector delimiter

    /**
     * Create a new distance vector
     */
    public DistanceVector() {
        dvMap = new ConcurrentHashMap<>();
    }

    /**
     * Create a distance vector based on protocol
     * Protocol format: 
     * 
     * "ip[INTRA_DLM]port[INTRA_DLM]dist[INTER_DLM]
     * ip[INTRA_DLM]port[INTRA_DLM]dist[INTER_DLM]
     * ..."
     *
     * @param text The text protocol that is received through the port
     */
    public DistanceVector(String text) {
        this();
        String[] vTexts = text.split(INTER_DLM);
        for (String vText : vTexts) {
            String[] vFields = vText.split(INTRA_DLM);
            String ip = vFields[0];
            int port = Integer.parseInt(vFields[1]);
            int dist = Integer.parseInt(vFields[2]);
            dvMap.put(new Address(ip, port), dist);
        }

    }

    /**
     * This method returns a text representation of a DV 
     * using the following format:
     * 
     * "ip[INTRA_DLM]port[INTRA_DLM]dist[INTER_DLM]
     * ip[INTRA_DLM]port[INTRA_DLM]dist[INTER_DLM]
     * ..."
     * 
     * @return a String that represents a DV
     */
    @Override
    public String toString() {
        String result = "";
        for (Address a : dvMap.keySet()) {
            result += INTER_DLM + a.ip + INTRA_DLM + a.port + INTRA_DLM + dvMap.get(a);
        }
        if (result.length() > 0) {
            result = result.substring(1);
        }
        return result;
    }
    
    public String debugPrint() {
        String result = "";
        for (Address a : dvMap.keySet()) {
            result += "\n" + a.toString() + " " + dvMap.get(a);
        }
        if (result.length() > 0) {
            result = result.substring(1);
        }
        return result;
    }

    /**
     * Check if distance vector contains the same keys and values to another
     * 
     * @param o Another object
     * @return true if equal, false if not
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof DistanceVector) {
            DistanceVector nDV = (DistanceVector) o;
            return this.dvMap.equals(nDV.dvMap);
        }
        return false;
    }

    /**
     * Override hash code just to enable this to be used in hashmap
     * @return a hash code for the distance vector
     */
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 47 * hash + Objects.hashCode(this.dvMap);
        return hash;
    }

    /**
     * Add/Update the distance for an address
     *
     * @param a The address to be updated
     * @param d The distance to be updated
     */
    public void updateDistance(Address a, Integer d) {
        dvMap.put(a, d);
    }

    /**
     * Get the distance between router and an address
     *
     * @param a The address
     * @return The distance stored in distance vector, null if add not exist
     */
    public Integer getDistance(Address a) {
        return dvMap.get(a);
    }

    /**
     * Get the set of address
     *
     * @return The set of address
     */
    public Set<Address> addressSet() {
        return dvMap.keySet();
    }

    /**
     * Remove a router's address in the dv
     */
    void removeDistance(Address destAdd) {
        dvMap.remove(destAdd);
    }
}

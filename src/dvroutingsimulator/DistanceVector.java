package dvroutingsimulator;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Create a distance vector for the DV Routing Simulator
 *
 * @author thanhvu
 */
public class DistanceVector {

    private HashMap<Address, Integer> dvMap;
    private static final String INTER_DLM = ";"; //inter-vector delimiter
    private static final String INTRA_DLM = ":"; //intra-vector delimiter

    /**
     * Create a new distance vector
     */
    public DistanceVector() {
        dvMap = new LinkedHashMap<>();
    }

    /**
     * Create a distance vector based on protocol
     * @param text The text protocol that is received through the port
     */
    public DistanceVector(String text) {
        this();
        String[] vTexts = text.split(INTER_DLM);
        for (String vText: vTexts) {
            String[] vFields = vText.split(INTRA_DLM);
            String ip = vFields[0];
            int port = Integer.parseInt(vFields[1]);
            int dist = Integer.parseInt(vFields[2]);
            dvMap.put(new Address(ip, port), dist);
        }

    }

    /**
     * This method should output a text representation of a DV using a specific format
     * @return a String that represents a DV
     */
    @Override
    public String toString() {
        String result = "";
        for (Address a: dvMap.keySet()) {
            result += INTER_DLM + a.ip + INTRA_DLM + a.port + INTRA_DLM + dvMap.get(a);
        } 
        if (result.length() > 0) {
            result = result.substring(0);
        }
        return result;
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
}

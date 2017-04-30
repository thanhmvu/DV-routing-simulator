package dvroutingsimulator;

/**
 * A router control message that contains the new weight betwen src and dst routers.
 * This message should be used to advertise weight change to the other neighbor.
 * 
 * @author thanhvu
 */
public class WeightMessage extends Message {

    private int weight;

    /**
     * Constructor for WeightMessage
     *
     * @param srcip IP address of source router
     * @param srcport port number of source router
     * @param dstip IP address of destination router
     * @param dstport port number of destination router
     * @param w the new weight
     */
    public WeightMessage(String srcip, int srcport, String dstip, int dstport, int w) {
        super(MsgType.WEIGHT, srcip, srcport, dstip, dstport);
        this.weight = w;
    }

    /**
     * Constructor that parses a string representation of a WeightMessage
     * Message's format: 
     * 
     * "type[DLM]srcIP[DLM]srcPort[DLM]
     * dstIP[DLM]dstPort[DLM]weight"
     *
     * @param text the text from which
     */
    public WeightMessage(String text) {
        super(text);
        if (this.type != MsgType.WEIGHT) {
            System.out.println("ERROR: Not a WEIGHT Type.");
            return;
        }
        String[] fields = text.split(this.DLM);
        this.weight = Integer.parseInt(fields[5]);
    }

    /**
     * Output a string representation of a WeightMessage using the following
     * format: 
     * 
     * "type[DLM]srcIP[DLM]srcPort[DLM]
     * dstIP[DLM]dstPort[DLM]weight"
     *
     * @return
     */
    @Override
    public String toString() {
        String output = type + DLM + srcAdd.ip + DLM + srcAdd.port
                + DLM + dstAdd.ip + DLM + dstAdd.port
                + DLM + weight;
        return output;
    }

    /**
     * Getter for the weight
     *
     * @return the new weight advertised by this message
     */
    public int getWeight() {
        return weight;
    }

}

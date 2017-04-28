package dvroutingsimulator;

/**
 *
 * @author thanhvu
 */
public class WeightMessage extends Message {

    private int weight;

    public WeightMessage(String srcip, int srcport, String dstip, int dstport, int w) {
        super(MsgType.WEIGHT, srcip, srcport, dstip, dstport);
        this.weight = w;
    }

    /**
     * Constructor that parses a string representation of a WeightMessage
     * Message's format: "type[delimiter]srcIP[delimiter]srcPort[delimiter]
     * dstIP[delimiter]dstPort[delimiter]weight"
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
     * format: "type[delimiter]srcIP[delimiter]srcPort[delimiter]
     * dstIP[delimiter]dstPort[delimiter]weight"
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

    public int getWeight() {
        return weight;
    }

}

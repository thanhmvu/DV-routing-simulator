package dvroutingsimulator;

/**
 *
 * @author thanhvu
 */
public class DVMessage extends Message {

    private DistanceVector dv;

    public DVMessage(Address srcAdd, Address dstAdd, DistanceVector distVect) {
        super(MsgType.DV, srcAdd.ip, srcAdd.port, dstAdd.ip, dstAdd.port);
        this.dv = distVect;
    }

    /**
     * Constructor that parses a string representation of a DVMessage Message's
     * format: "type[delimiter]srcIP[delimiter]srcPort[delimiter]
     * dstIP[delimiter]dstPort[delimiter]distanceVector"
     *
     * @param text
     */
    public DVMessage(String text) {
        super(text);
        if (this.type != MsgType.DV) {
            System.out.println("ERROR: Not a DV Type.");
            return;
        }
        String[] fields = text.split(DLM);
        this.dv = new DistanceVector(fields[5]);
    }

    /**
     * Output a string representation of a DVMessage using the following format:
     * "type[delimiter]srcIP[delimiter]srcPort[delimiter]
     * dstIP[delimiter]dstPort[delimiter]distanceVector"
     *
     * @return a string representation of a DVMessage
     */
    @Override
    public String toString() {
        String output = type + DLM + srcAdd.ip + DLM + srcAdd.port
                + DLM + dstAdd.ip + DLM + dstAdd.port
                + DLM + dv.toString();
        return output;
    }

    public DistanceVector getDistVect() {
        return dv;
    }
}

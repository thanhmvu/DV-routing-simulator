package dvroutingsimulator;

/**
 * A router control message that contains its distance vector.
 * This message should be used to advertise router's dist. vect.
 * 
 * @author thanhvu
 */
public class DVMessage extends Message {

    private DistanceVector dv;

    /**
     * Constructor for DVMessage
     *
     * @param srcAdd IP and port of source router
     * @param dstAdd IP and port of destination router
     * @param distVect the distance vector of src router
     */
    public DVMessage(Address srcAdd, Address dstAdd, DistanceVector distVect) {
        super(MsgType.DV, srcAdd.ip, srcAdd.port, dstAdd.ip, dstAdd.port);
        this.dv = distVect;
    }

    /**
     * Constructor that parses a string representation of a DVMessage Message's
     * format: "type[DLM]srcIP[DLM]srcPort[DLM]
     * dstIP[DLM]dstPort[DLM]distanceVector"
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
     * "type[DLM]srcIP[DLM]srcPort[DLM]
     * dstIP[DLM]dstPort[DLM]distanceVector"
     *
     * @return a string representation of a DVMessage
     */
    @Override
    public String toString() {
        String output = type + DLM + srcAdd.ip + DLM + srcAdd.port
                + DLM + dstAdd.ip + DLM + dstAdd.port
                + DLM + dv.toString() + DLM;
        return output;
    }

    /**
     * Getter for dist. vect
     *
     * @return the dist. vect. advertised by this message
     */
    public DistanceVector getDistVect() {
        return dv;
    }
}

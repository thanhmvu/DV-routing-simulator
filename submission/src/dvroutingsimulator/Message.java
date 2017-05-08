package dvroutingsimulator;

/**
 * A message sent between routers,
 * can one of the three types: distance vector message,
 * weight message, and content message
 * 
 * @author thanhvu
 */
enum MsgType {
    DV, WEIGHT, CONTENT
}

public class Message {

    protected Address srcAdd;
    protected Address dstAdd;
    protected MsgType type;
    protected static final String DLM = "<<>>"; //delimiter

    /**
     * Constructor for Message
     * 
     * @param msgType Type of the message, DV, WEIGHT, or CONTENT
     * @param srcip IP address of source router
     * @param srcport port number of source router
     * @param dstip IP address of destination router
     * @param dstport port number of destination router
     */
    protected Message(MsgType msgType, String srcip, int srcport, String dstip, int dstport) {
        this.type = msgType;
        this.srcAdd = new Address(srcip, srcport);
        this.dstAdd = new Address(dstip, dstport);
    }

    /**
     * Constructor that parse a string representation of a message 
     * and create a Message object
     * 
     * @param text the string to parse
     * Text format: type[DLM]srcIP[DLM]srcPort[DLM]dstIP[DLM]dstPort[DLM]
     * After the last [DLM] are any other fields specific to each type of msg
     */
    protected Message(String text) {
        String[] fields = text.split(DLM);

        if (fields.length < 6) {
            System.out.println("ERROR: Wrong format. Not enough fields.");
            return;
        }

        try {
            this.type = MsgType.valueOf(fields[0]);
        } catch (IllegalArgumentException e) {
            System.out.println("ERROR: Wrong type. Not a Message.");
            return;
        }

        this.srcAdd = new Address(fields[1], Integer.parseInt(fields[2]));
        this.dstAdd = new Address(fields[3], Integer.parseInt(fields[4]));

    }

    /**
     * Getter method for address of source router
     * 
     * @return address of source router
     */
    public Address getSrcAddress() {
        return srcAdd;
    }

    /**
     * Getter method for address of destination router
     * 
     * @return address of destination router
     */
    public Address getDstAddress() {
        return dstAdd;
    }

    /**
     * Getter method for msg type
     * 
     * @return the msg type
     */
    MsgType getType() {
        return type;
    }
    
    /**
     * Static method to detect the type of message
     * 
     * @param text The string representation of the message
     * @return the msg type
     */
    public static MsgType getType(String text) {
        String[] fields = text.split(DLM);
        
        if (fields.length < 1) {
            return null;
        }

        try {
            return MsgType.valueOf(fields[0]);
        } catch (IllegalArgumentException e) {
            return null;
        }

    }
}

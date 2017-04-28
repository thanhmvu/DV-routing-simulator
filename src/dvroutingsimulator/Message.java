package dvroutingsimulator;

/**
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

    protected Message(MsgType msgType, String srcip, int srcport, String dstip, int dstport) {
        this.type = msgType;
        this.srcAdd = new Address(srcip, srcport);
        this.dstAdd = new Address(dstip, dstport);
    }

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

    public Address getSrcAddress() {
        return srcAdd;
    }

    public Address getDstAddress() {
        return dstAdd;
    }

    MsgType getType() {
        return type;
    }
    
    public static MsgType getType(String text) {
        String[] fields = text.split(DLM);
        
        if (fields.length < 6) {
            return null;
        }

        try {
            return MsgType.valueOf(fields[0]);
        } catch (IllegalArgumentException e) {
            return null;
        }

    }
}

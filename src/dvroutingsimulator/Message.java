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
    protected final String delimiter = "<<>>";
    
    protected Message (MsgType msgType, String srcip, int srcport, String dstip, int dstport){
        this.type = msgType;
        this.srcAdd = new Address(srcip, srcport);
        this.dstAdd = new Address(dstip, dstport);
    }
    public Address getSrcAddress() {
        return srcAdd;
    }
    
    public Address getDstAddress() {
        return dstAdd;
    }

    public String getSrcIP() {
        return srcAdd.ip;
    }

    public String getDstIP() {
        return dstAdd.ip;
    }

    public int getSrcPort() {
        return srcAdd.port;
    }

    public int getDstPort() {
        return dstAdd.port;
    }

    public MsgType getType() {
        return type;
    }
}

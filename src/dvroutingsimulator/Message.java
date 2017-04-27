
package dvroutingsimulator;

/**
 *
 * @author thanhvu
 */
enum MsgType { DV, WEIGHT, CONTENT }

public class Message {
    
    protected String srcIP;
    protected String dstIP;
    protected int srcPort;
    protected int dstPort;
    protected MsgType type;
    protected final String delimiter = "<<>>";
   
    public String getSrcIP(){ return srcIP;}
    
    public String getDstIP(){ return dstIP;}
    
    public int getSrcPort(){ return srcPort;}
    
    public int getDstPort(){ return dstPort;}
    
    public MsgType getType(){ return type;}
}

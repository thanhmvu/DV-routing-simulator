
package dvroutingsimulator;

/**
 *
 * @author thanhvu
 */
public class DVMessage extends Message{
    DistanceVector dv;
    
    public DVMessage(String srcip, int srcport, String dstip, int dstport, DistanceVector distVect){
        this.type = MsgType.DV;
        this.srcIP = srcip;
        this.srcPort = srcport;
        this.dstIP = dstip;
        this.dstPort = dstport;
        this.dv = distVect;
    }
    /**
     * Constructor that parses a string representation of a DVMessage
     * Message's format: 
     * "type[delimiter]srcIP[delimiter]srcPort[delimiter]
     * dstIP[delimiter]dstPort[delimiter]distanceVector"
     * @param text
     */
    public DVMessage(String text){
        String[] fields = text.split(this.delimiter);
        
        if(fields.length < 6){
            System.out.println("ERROR: Wrong format. Not enough fields.");
            return;
        }
        
        if(MsgType.DV != MsgType.valueOf(fields[0])){
            System.out.println("ERROR: Wrong type. Not a DVMessage.");
            return;
        }
        this.type = MsgType.DV;
        this.srcIP = fields[1];
        this.srcPort = Integer.parseInt(fields[2]);
        this.dstIP = fields[3];
        this.dstPort = Integer.parseInt(fields[4]);
        this.dv = new DistanceVector(fields[5]);
    }
    
    /**
     * Output a string representation of a DVMessage 
     * using the following format: 
     * "type[delimiter]srcIP[delimiter]srcPort[delimiter]
     * dstIP[delimiter]dstPort[delimiter]distanceVector"
     * @return a string representation of a DVMessage 
     */
    @Override
    public String toString(){
        String output = type +delimiter+ srcIP +delimiter+ srcPort 
                +delimiter+ dstIP +delimiter+ dstPort 
                +delimiter+ dv.toString();
        return output;
    }
    
    public DistanceVector getDistVect(){ return dv;}
}

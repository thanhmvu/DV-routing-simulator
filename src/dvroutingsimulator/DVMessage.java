
package dvroutingsimulator;

/**
 *
 * @author thanhvu
 */
public class DVMessage extends Message{
    DistanceVector dv;
    
    public DVMessage(String srcip, int srcport, String dstip, int dstport, DistanceVector distVect){
        super(MsgType.DV, srcip, srcport, dstip, dstport);
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
        this("", 0, "", 0, null);
        String[] fields = text.split(this.delimiter);
        
        if(fields.length < 6){
            System.out.println("ERROR: Wrong format. Not enough fields.");
            return;
        }
        
        if(MsgType.DV != MsgType.valueOf(fields[0])){
            System.out.println("ERROR: Wrong type. Not a DVMessage.");
            return;
        }
        this.srcAdd = new Address(fields[1], Integer.parseInt(fields[2]));
        this.dstAdd = new Address(fields[3], Integer.parseInt(fields[4]));  
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
        String output = type +delimiter+ srcAdd.ip +delimiter+ srcAdd.port 
                +delimiter+ dstAdd.ip +delimiter+ dstAdd.port 
                +delimiter+ dv.toString();
        return output;
    }
    
    public DistanceVector getDistVect(){ return dv;}
}

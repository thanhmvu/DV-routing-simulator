
package dvroutingsimulator;

/**
 *
 * @author thanhvu
 */
public class WeightMessage extends Message{
    int weight;
    
    public WeightMessage(String srcip, int srcport, String dstip, int dstport, int w){
        this.type = MsgType.WEIGHT;
        this.srcIP = srcip;
        this.srcPort = srcport;
        this.dstIP = dstip;
        this.dstPort = dstport;
        this.weight = w;
    }
    /**
     * Constructor that parses a string representation of a WeightMessage
     * Message's format: 
     * "type[delimiter]srcIP[delimiter]srcPort[delimiter]
     * dstIP[delimiter]dstPort[delimiter]weight"
     * @param text
     */
    public WeightMessage(String text){
        String[] fields = text.split(this.delimiter);
        
        if(fields.length < 6){
            System.out.println("ERROR: Wrong format. Not enough fields.");
            return;
        }
        
        if(MsgType.WEIGHT != MsgType.valueOf(fields[0])){
            System.out.println("ERROR: Wrong type. Not a WeightMessage.");
            return;
        }
        this.type = MsgType.WEIGHT;
        this.srcIP = fields[1];
        this.srcPort = Integer.parseInt(fields[2]);
        this.dstIP = fields[3];
        this.dstPort = Integer.parseInt(fields[4]);
        this.weight = Integer.parseInt(fields[5]);
    }
    
    /**
     * Output a string representation of a WeightMessage 
     * using the following format: 
     * "type[delimiter]srcIP[delimiter]srcPort[delimiter]
     * dstIP[delimiter]dstPort[delimiter]weight"
     * @return 
     */
    @Override
    public String toString(){
        String output = type +delimiter+ srcIP +delimiter+ srcPort 
                +delimiter+ dstIP +delimiter+ dstPort 
                +delimiter+ weight;
        return output;
    }
    
    public int getWeight(){ return weight;}
    
}

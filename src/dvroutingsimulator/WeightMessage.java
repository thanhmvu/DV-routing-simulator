
package dvroutingsimulator;

/**
 *
 * @author thanhvu
 */
public class WeightMessage extends Message{
    int weight;
    
    public WeightMessage(String srcip, int srcport, String dstip, int dstport, int w){
        super(MsgType.WEIGHT, srcip, srcport, dstip, dstport);
        this.weight = w;
    }
    /**
     * Constructor that parses a string representation of a WeightMessage
     * Message's format: 
     * "type[delimiter]srcIP[delimiter]srcPort[delimiter]
     * dstIP[delimiter]dstPort[delimiter]weight"
     * @param text the text from which 
     */
    public WeightMessage(String text){
        this("", 0, "", 0, 0);
        String[] fields = text.split(this.delimiter);
        
        if(fields.length < 6){
            System.out.println("ERROR: Wrong format. Not enough fields.");
            return;
        }
        
        if(MsgType.WEIGHT != MsgType.valueOf(fields[0])){
            System.out.println("ERROR: Wrong type. Not a WeightMessage.");
            return;
        }
        
        this.srcAdd = new Address(fields[1], Integer.parseInt(fields[2]));
        this.dstAdd = new Address(fields[3], Integer.parseInt(fields[4]));        
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
        String output = type +delimiter+ srcAdd.ip +delimiter+ srcAdd.port 
                +delimiter+ dstAdd.ip +delimiter+ dstAdd.port 
                +delimiter+ weight;
        return output;
    }
    
    public int getWeight(){ return weight;}
    
}

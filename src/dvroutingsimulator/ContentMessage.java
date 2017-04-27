
package dvroutingsimulator;

import java.util.ArrayList;

/**
 *
 * @author thanhvu
 */
public class ContentMessage extends Message{
    int timeToLive; // max number of remaining hops, decremented at each router
    ArrayList<Address> path; // addresses of routers in the path
    String msg;
    
    
    public ContentMessage(String srcip, int srcport, String dstip, int dstport, int remainingHops, String mess){
        super(MsgType.CONTENT, srcip, srcport, dstip, dstport);
        this.msg = mess;
        this.timeToLive = remainingHops;
        this.path = new ArrayList<>();
    }
    /**
     * Constructor that parses a string representation of a ContentMessage
     * @param text The message to parse. 
     * Should have at least 7 fields separated by [delimiter] as followed:
     * 
     * "type[delimiter]timeToLive[delimiter]srcIP[delimiter]srcPort[delimiter]
     * dstIP[delimiter]dstPort[delimiter]msg[delimiter]ip-port ip-port ..."
     * 
     */
    public ContentMessage(String text){
        this("", 0, "", 0, 0, "");
        String[] fields = text.split(this.delimiter);
        
        if(fields.length < 7){
            System.out.println("ERROR: Wrong format. Not enough fields.");
            return;
        }
        
        if(MsgType.CONTENT != MsgType.valueOf(fields[0])){
            System.out.println("ERROR: Wrong type. Not a ContentMessage.");
            return;
        }
        
        this.timeToLive = Integer.parseInt(fields[1]);
        this.srcAdd = new Address(fields[2], Integer.parseInt(fields[3]));
        this.dstAdd = new Address(fields[4], Integer.parseInt(fields[5]));   
        this.msg = fields[6];
        this.path = new ArrayList<>();
        if (fields.length > 7) {
            for (String address : fields[7].split(" ")) {
                String[] tmp = address.split("-");
                this.path.add(new Address(tmp[0], Integer.parseInt(tmp[1])));
            }
        }
    }
    
    public void addRouter(String ip, int port){
        path.add(new Address(ip,port));
    }
    
    /**
     * Output a string representation of a ContentMessage 
     * using the following format: 
     * 
     * "type[delimiter]timeToLive[delimiter]srcIP[delimiter]srcPort[delimiter]
     * dstIP[delimiter]dstPort[delimiter]msg[delimiter]ip-port ip-port ..."
     * 
     * @return a string representation of a content message
     */
    @Override
    public String toString(){
        String output = type +delimiter+ timeToLive 
                +delimiter+ srcAdd.ip +delimiter+ srcAdd.port 
                +delimiter+ dstAdd.ip +delimiter+ dstAdd.port 
                +delimiter+ msg +delimiter;
        for(Address ad: path){
            output += ad.ip +"-"+ ad.port +" ";
        }
        return output;
    }
    
    public void reduceTimeTolive(){
        timeToLive--;
    }
    
    public String getMessage(){ return msg;}
    
    public int getTimeToLive(){ return timeToLive;}
    
    public ArrayList<Address> getPath(){ return path;}
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
        this.type = MsgType.CONTENT;
        this.srcIP = srcip;
        this.srcPort = srcport;
        this.dstIP = dstip;
        this.dstPort = dstport;
        this.msg = msg;
        this.timeToLive = remainingHops;
        this.path = new ArrayList<Address>();
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
        String[] fields = text.split(this.delimiter);
        
        if(fields.length < 7){
            System.out.println("ERROR: Wrong format. Not enough fields.");
            return;
        }
        
        if(MsgType.CONTENT != MsgType.valueOf(fields[0])){
            System.out.println("ERROR: Wrong type. Not a ContentMessage.");
            return;
        }
        
        this.type = MsgType.CONTENT;
        this.timeToLive = Integer.parseInt(fields[1]);
        this.srcIP = fields[2];
        this.srcPort = Integer.parseInt(fields[3]);
        this.dstIP = fields[4];
        this.dstPort = Integer.parseInt(fields[5]);
        this.msg = fields[6];
        this.path = new ArrayList<Address>();
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
    
    public String toString(){
        String output = type +delimiter+ timeToLive 
                +delimiter+ srcIP +delimiter+ srcPort 
                +delimiter+ dstIP +delimiter+ dstPort 
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

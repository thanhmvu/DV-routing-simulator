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
public class Message {
    int timeToLive; // max number of remaining hops, decremented at each router
    String srcIP;
    String dstIP;
    int srcPort;
    int dstPort;
    String msg;
    ArrayList<RouterAddress> routers;
    
    public Message(String srcip, int srcport, String dstip, int dstport, int remainingHops, String mess){
        srcIP = srcip;
        srcPort = srcport;
        dstIP = dstip;
        dstPort = dstport;
        timeToLive = remainingHops;
        msg = mess;
        routers = new ArrayList<RouterAddress>();
    }
    
    public Message(String text){
        // text format: "timeToLive srcIP dstIP srcPort dstPort msg ip-port ip-port ip-port"
        String[] fields = text.split(" ");
        timeToLive = Integer.parseInt(fields[0]);
        srcIP = fields[1];
        dstIP = fields[2];
        srcPort = Integer.parseInt(fields[3]);
        dstPort = Integer.parseInt(fields[4]);
        msg = fields[5];
        routers = new ArrayList<RouterAddress>();
        for(int i=6; i<fields.length; i++){
            String[] tmp = fields[i].split("-");
            routers.add(new RouterAddress(tmp[0],Integer.parseInt(tmp[1])));
        }
    }
    
    public void addRouter(String ip, int port){
        routers.add(new RouterAddress(ip,port));
    }
    
    public String toString(){
        String output = timeToLive +" "+ srcIP +" "+ dstIP 
                +" "+ srcPort +" "+ dstPort +" "+ msg;
        for(RouterAddress ra: routers){
            output += " " + ra.ip + "-" + ra.port;
        }
        return output;
    }
    
    public void reduceTimeTolive(){
        timeToLive--;
    }
    
    public String getSrcIP(){ return srcIP;}
    
    public String getDstIP(){ return dstIP;}
    
    public int getSrcPort(){ return srcPort;}
    
    public int getDstPort(){ return dstPort;}
    
    public String getMessage(){ return msg;}
    
    public int getTimeToLive(){ return timeToLive;}
    
    public ArrayList<RouterAddress> getRouters(){ return routers;}
    
    public class RouterAddress{
        public String ip;
        public int port;
        
        RouterAddress(String i, int p){
            ip = i;
            port = p;
        }
    }
}

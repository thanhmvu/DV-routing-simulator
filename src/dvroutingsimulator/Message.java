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

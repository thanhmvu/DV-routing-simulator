/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dvroutingsimulator;

/**
 * A wrap-around class for the ip-port pair
 * @author thanhvu
 */
public class Address {
    public String ip;
    public int port;
        
    Address(String i, int p){
        ip = i;
        port = p;
    }
}

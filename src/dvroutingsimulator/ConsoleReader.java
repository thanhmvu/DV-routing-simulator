/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dvroutingsimulator;

import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author thanhvu
 */
public class ConsoleReader implements Runnable {

    Scanner sc;
    boolean running;
    Router r;
    private static final String DLM = " ";

    public ConsoleReader(Router r) {
        this.r = r;
        sc = new Scanner(System.in);
        running = false;
    }

    public void run() {
        running = true;
        while (running) {
            String[] tmp = sc.nextLine().split(DLM);
            if (tmp.length < 1) {
                System.out.println("No command found!");
            } else if (tmp[0].equalsIgnoreCase("PRINT")) {
                print();
            } else if (tmp[0].equalsIgnoreCase("MSG")) {
                msg(tmp);
            } else if (tmp[0].equalsIgnoreCase("CHANGE")) {
                change(tmp);
            } else {
                System.out.println("Wrong command format!");
            }
        }
    }

    /**
     * Print the current node's distance vector, and the distance vectors
     * received from the neighbors.
     */
    public void print() {
        r.printDistVect();
        r.printNeighborsDV();
    }

    /**
     * Send message msg to a destination with the specified address
     *
     * @param fields array of fields: MSG dst-ip dst-port msg
     */
    public void msg(String[] fields) {
        if (fields.length < 4) {
            System.out.println("Missing fields. Required: MSG"+DLM+"<dst-ip>"+DLM+"<dst-port>"+DLM+"<msg>");
        } else {
            String dstIP = fields[1];
            int dstPort = Integer.parseInt(fields[2]);
            Address dstAdd = new Address(dstIP, dstPort);

            StringBuilder msg = new StringBuilder();
            for (int i = 3; i < fields.length; i++) {
                msg.append(fields[i]+" ");
            }

            try {
                r.sendContentMsg(dstAdd, msg.toString().trim());
            } catch (IOException ex) {
                Logger.getLogger(ConsoleReader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Change the weight between the current node and the specified node to new
     * weight and update the specified node about the change.
     *
     * @param fields array of fields: CHANGE dst-ip dst-port new-weight
     */
    public void change(String[] fields) {
        if (fields.length < 4) {
            System.out.println("Missing fields. Required: CHANGE"+DLM+"<dst-ip>"+DLM+"<dst-port>"+DLM+"<new-weight>");
        } else {
            String dstIP = fields[1];
            int dstPort = Integer.parseInt(fields[2]);
            int newW = Integer.parseInt(fields[3]);
            Address dstAdd = new Address(dstIP, dstPort);
            
            if(dstAdd.equals(r.getAddress())){
                System.out.println("Invalid destination. Router's weight to itself should be 0");
            } else {
                System.out.println("new weight to neighbor "+dstAdd.toString()+" of "+newW);
                try {
                    // Send weight change to the other neighbor
                    r.sendWeightMsg(dstAdd, newW);

                    // Update its own weight
                    if (r.updateWeight(new Address(dstIP, dstPort), newW)) {
                        // if it's a different weight, run DV algorithm and advertise if necessary
                        if (r.runDVAlgorithm()) {
                            r.advertiseDV();
                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(ConsoleReader.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    /**
     * Stop this thread
     */
    public void stop() {
        running = false;
    }
}

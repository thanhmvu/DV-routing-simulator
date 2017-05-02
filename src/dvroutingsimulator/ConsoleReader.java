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

    public ConsoleReader(Router r) {
        this.r = r;
        sc = new Scanner(System.in);
        running = false;
    }

    public void run() {
        running = true;
        while (running) {
            String[] tmp = sc.nextLine().split(" ");
            if (tmp.length < 1) {
                System.out.println("No command found!");
            } else if (tmp[0].equals("PRINT")) {
                print();
            } else if (tmp[0].equals("MSG")) {
                msg(tmp);
            } else if (tmp[0].equals("CHANGE")) {
                change(tmp);
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
            System.out.println("Missing fields. Required: MSG <dst-ip> <dst-port> <msg>");
        } else {
            String dstIP = fields[1];
            int dstPort = Integer.parseInt(fields[2]);

            StringBuilder msg = new StringBuilder();
            for (int i = 3; i < fields.length; i++) {
                msg.append(fields[i]);
            }

            try {
                r.sendContentMsg(dstIP, dstPort, msg.toString());
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
            System.out.println("Missing fields. Required: CHANGE <dst-ip> <dst-port> <new-weight>");
        } else {
            String dstIP = fields[1];
            int dstPort = Integer.parseInt(fields[2]);
            int newW = Integer.parseInt(fields[3]);

            try {
                // Send weight change to the other neighbor
                r.sendWeightMsg(dstIP, dstPort, newW);

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

    /**
     * Stop this thread
     */
    public void stop() {
        running = false;
    }
}

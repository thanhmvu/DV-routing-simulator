package dvroutingsimulator;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;

/**
 * Contains all methods to simulate a router, include all algorithms and
 * containers
 *
 * @author thanhvu
 */
public class Router {

    private HashMap<Address, Address> forwardTable;
    private HashMap<Address, DistanceVector> neighborsDV;
    private HashMap<Address, Integer> neighborsLinkWeight;
    private DistanceVector dv;
    private Address address;

    /**
     * Create a router
     */
    public Router() {
        forwardTable = new HashMap<>();
        neighborsDV = new HashMap<>();
        neighborsLinkWeight = new HashMap<>();
        dv = new DistanceVector();
    }

    /**
     * Send a message directly to a specific router
     *
     * @param m Message
     * @param nextHop The next destination that the message is sent to
     * @throws java.net.SocketException
     * @throws java.net.UnknownHostException
     */
    public void sendMessage(Message m, Address nextHop) throws IOException {
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress dstIP = InetAddress.getByName(nextHop.ip);
            int dstPort = nextHop.port;
            //int maxDataSize = 1024; -- REDUNDANT

            // Put message in a packet
            byte[] sendData = m.toString().getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, dstIP, dstPort);

            // Write to output stream
            socket.send(sendPacket);
        }
    }

    /**
     * Forward a content message to the right destination
     *
     * @param m The message to be forwarded
     * @throws Exception
     */
    public void forwardMessage(ContentMessage m) throws Exception {

        // Look up the dest IP in the forwarding table
        Address nextHop = forwardTable.get(m.getDstAddress());
        //WHAT IF NEXTHOP == NULL? (NO DESTINATION)
        
        // Foward message using writeToOuputStream
        sendMessage(m, nextHop);
    }
    
    public void runDVAlgorithm() {
        //update forward table
    }
    
    /**
     * Advertise distance vectors to neighbors
     * @throws IOException 
     */
    public void advertiseDV() throws IOException {
        for (Address neighborAdd: neighborsLinkWeight.keySet()) {
            DVMessage dvMess = new DVMessage(address, neighborAdd, dv);
            sendMessage(dvMess, neighborAdd);
        }
    }

}

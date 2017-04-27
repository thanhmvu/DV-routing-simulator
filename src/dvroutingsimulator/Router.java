package dvroutingsimulator;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;

/**
 *
 * @author thanhvu
 */
public class Router {

    private HashMap<Address, Address> forwardTable;
    private HashMap<Address, DistanceVector> neighborsDV;
    private HashMap<Address, Integer> neighborsLinkWeight;
    private DistanceVector dv;
    

    public void sendMessage(Message m, Address nextHop) throws Exception {
        // Set up
        DatagramSocket socket = new DatagramSocket();
        InetAddress dstIP = InetAddress.getByName(nextHop.ip);
        int dstPort = nextHop.port;
        int maxDataSize = 1024;

        // Put message in a packet
        String msg = m.toString();
        byte[] sendData = new byte[maxDataSize];
        sendData = msg.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, dstIP, dstPort);

        // Write to output stream
        socket.send(sendPacket);
        socket.close();
    }

    public void forwardMessage(Message m) throws Exception {
        // Look up the dest IP in the forwarding table
        Address nextHop = forwardTable.get(m.getDstAddress());

        // Foward message using writeToOuputStream
        sendMessage(m, nextHop);
    }

}

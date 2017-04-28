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
    private HashMap<Address, Integer> linkWeights;
    private DistanceVector dv;
    private Address address;

    /**
     * Create a router
     *
     * @param ip
     * @param port
     */
    public Router(String ip, int port) {
        address = new Address(ip, port);
        forwardTable = new HashMap<>();
        neighborsDV = new HashMap<>();
        linkWeights = new HashMap<>();
        dv = new DistanceVector();
    }

    /**
     * Add a new neighbor into the router. Link weights map is updated, distance
     * vector is updated. DV Algorithm is run to determine forwarding table
     *
     * @param ip IP of the neighbor
     * @param port Port number of the neighbor
     * @param weight The weight of the neighbor
     */
    public void addNeighbor(String ip, int port, int weight) throws IOException {
        linkWeights.put(new Address(ip, port), weight);
        dv.updateDistance(address, weight);
        if (runDVAlgorithm()) {
            advertiseDV();
        }
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

    /**
     * Update weight between 2 links
     *
     * @param wm A weight message
     */
    public void updateWeight(WeightMessage wm) {
        linkWeights.put(wm.getSrcAddress(), wm.getWeight());
    }

    /**
     * Run the DV Algorithm
     *
     * @return true if algorithm results in change in distance vector, otherwise
     * false
     */
    public boolean runDVAlgorithm() {
        DistanceVector tempDv = new DistanceVector();
        forwardTable = new HashMap<>(); //empty forward table

        //run dv algorithm
        // iterate through all neighbors' distance vector
        for (Address nAdd : neighborsDV.keySet()) {
            DistanceVector nDV = neighborsDV.get(nAdd);

            //iterate though all the destination addresses in a neighbor vector
            for (Address destAdd : nDV.addressSet()) {
                int newDist = linkWeights.get(nAdd) + nDV.getDistance(destAdd);
                Integer currDist = tempDv.getDistance(destAdd);

                //if neighbor's distance 
                if (currDist == null || currDist > newDist) {
                    tempDv.updateDistance(destAdd, newDist);
                    forwardTable.put(destAdd, nAdd);
                }
            }

        }

        // check if there is a change in distance vector, if yes, return true and update things
        if (!tempDv.equals(dv)) {
            dv = tempDv;
            return true;
        }
        return false;

    }

    /**
     * Advertise distance vectors to neighbors
     *
     * @throws IOException
     */
    public void advertiseDV() throws IOException {
        for (Address neighborAdd : linkWeights.keySet()) {
            DVMessage dvMess = new DVMessage(address, neighborAdd, dv);
            sendMessage(dvMess, neighborAdd);
        }
    }

}

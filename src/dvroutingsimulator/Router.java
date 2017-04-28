package dvroutingsimulator;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;

/**
 * Contains all methods to simulate a router, include all algorithms and
 * containers
 *
 * @author thanhvu
 */
public class Router {

    private final Address address;
    private HashMap<Address, Address> forwardTable;
    private HashMap<Address, DistanceVector> neighborsDV;
    private HashMap<Address, Integer> linkWeights;
    private DistanceVector dv;

    private RouterListener rl;
    private boolean running;

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

        // start all thread
        this.startAllThreads();

    }

    /**
     * Get the address of the router
     *
     * @return The address of the router
     */
    public Address getAddress() {
        return address;
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
     */
    public void forwardMessage(ContentMessage m) throws IOException {

        // Look up the dest IP in the forwarding table
        Address nextHop = forwardTable.get(m.getDstAddress());
        //WHAT IF NEXTHOP == NULL? (NO DESTINATION)

        // Foward message using writeToOuputStream
        sendMessage(m, nextHop);
        System.out.println("Message " + m.getMessage()
                + " from " + m.getSrcAddress().toString()
                + " to " + m.getDstAddress().toString()
                + " forwarded to " + nextHop.toString());
    }

    /**
     * Action taken when router receive a content message destined to itself
     *
     * @param m The message received
     */
    public void receiveMessage(ContentMessage m) {
        System.out.println("Message " + m.getMessage() + " received from " + m.getSrcAddress().toString());
    }

    /**
     * Update weight between 2 links
     *
     * @param nAdd Neighbor address
     * @param weight The weight of the new link
     * @return true if is updated, false if no change
     */
    public boolean updateWeight(Address nAdd, int weight) {
        int currWeight = linkWeights.get(nAdd);
        if (currWeight != weight) {
            linkWeights.put(nAdd, weight);
            return true;
        }
        return false;
    }

    /**
     * Update a distance vector of neighbor router. If the update is different,
     * run distance vector algorithm
     *
     * @param nAdd Neighbor address
     * @param nDV Neighbor distance vector
     * @return true if DV is changed and updated
     * @throws java.io.IOException
     */
    public boolean updateDV(Address nAdd, DistanceVector nDV) throws IOException {
        DistanceVector currDV = neighborsDV.get(nAdd);
        if (currDV == null || !currDV.equals(nDV)) {
            neighborsDV.put(nAdd, nDV);
            return true;
        }
        return false;
    }

    /**
     * Run the DV Algorithm
     *
     * @return true if algorithm results in change in distance vector, otherwise
     * false
     */
    public boolean runDVAlgorithm() {
        boolean isChanged = false;
        forwardTable = new HashMap<>(); //empty forward table

        // iterate through all neighbors' distance vector
        for (Address nAdd : neighborsDV.keySet()) {
            DistanceVector nDV = neighborsDV.get(nAdd);

            //iterate though all the destination addresses in a neighbor vector
            for (Address destAdd : nDV.addressSet()) {
                int newDist = linkWeights.get(nAdd) + nDV.getDistance(destAdd);
                Integer currDist = dv.getDistance(destAdd);

                //if link weight to neighbor + neighbor's distance to dest < current distance, update
                if (currDist == null || currDist > newDist) {
                    isChanged = true;
                    dv.updateDistance(destAdd, newDist);
                    forwardTable.put(destAdd, nAdd);
                }
            }
        }
        return isChanged;

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

//======================THREAD CONTROL=====================================
    /**
     * Check if router is still running all its thread
     *
     * @return true if router is still running, false if not
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Start all the threads in the router
     */
    public final void startAllThreads() {
        running = true;
        rl = new RouterListener(this);
        rl.start();
    }

    /**
     * Stop all threads from running
     */
    public void stop() {
        running = false;
    }

}

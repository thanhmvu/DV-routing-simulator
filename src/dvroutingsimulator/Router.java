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
    private HashMap<Address, Neighbor> neighbors;
    private HashMap<Address, Neighbor> forwardTable;
    private final DistanceVector dv;

    private RouterListener rl;
    private AutoUpdater au;
    private ConsoleReader cr;
    private boolean running;
    private final boolean reverse;

    /**
     * Create a router
     *
     * @param ip The IP of the router
     * @param port The port of the router
     * @param reverse true if poison reverse is activated, false if not
     */
    public Router(String ip, int port, boolean reverse) {
        address = new Address(ip, port);
        this.reverse = reverse;
        forwardTable = new HashMap<>();
        neighbors = new HashMap<>();
        dv = new DistanceVector();
        rl = new RouterListener(this);
        au = new AutoUpdater(this);
        cr = new ConsoleReader(this);
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
     * @throws java.io.IOException Happens when DV is advertised
     */
    public void addNeighbor(String ip, int port, int weight) throws IOException {
        Address neighborAddress = new Address(ip, port);
        neighbors.put(neighborAddress, new Neighbor(neighborAddress, weight));
        dv.updateDistance(address, weight);
        if (runDVAlgorithm()) {
            advertiseDV();
        }
    }

    /**
     * Drop a neighbor from the router
     *
     * @param a The address of the neighbor
     */
    public void dropNeighbor(Address a) {
        neighbors.remove(a);

    }

//======================COMMUNICATION METHODS=====================================
    /**
     * Send a message directly to a specific router
     *
     * @param m Message
     * @param neighbor The next hop neighbor
     * @throws java.net.SocketException
     * @throws java.net.UnknownHostException
     */
    private void sendMessage(Message m, Neighbor neighbor) throws IOException {
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress dstIP = InetAddress.getByName(neighbor.getAddress().ip);
            int dstPort = neighbor.getAddress().port;

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
        //check time to live
        if (m.getTimeToLive() > 0) {

            // Look up the dest IP in the forwarding table
            Neighbor nextHopNeighbor = forwardTable.get(m.getDstAddress());
            //WHAT IF NEXTHOP == NULL? (NO DESTINATION)

            // Foward message using writeToOuputStream
            m.reduceTimeTolive();
            sendMessage(m, nextHopNeighbor);
            System.out.println("Message msg"
                    + " from " + m.getSrcAddress().toString()
                    + " to " + m.getDstAddress().toString()
                    + " forwarded to " + nextHopNeighbor.toString()
                    + "\nmsg(" + m.getMessage() + ")");
        } else {
            System.out.println("Message msg"
                    + " from " + m.getSrcAddress().toString()
                    + " to " + m.getDstAddress().toString()
                    + " died (timeToLive <= 0)\nmsg(" + m.getMessage() + ")");
        }
    }

    /**
     * Action taken when router receive a content message destined to itself
     *
     * @param m The message received
     */
    public void receiveMessage(ContentMessage m) {
        System.out.println("Message msg received from " + m.getSrcAddress().toString()
                + "\nmsg(" + m.getMessage() + ")");
    }

//======================DISTANCE VECTOR METHODS=====================================
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
        for (Address nAdd : neighbors.keySet()) {
            Neighbor n = neighbors.get(nAdd);
            DistanceVector nDV = n.getDistVector();

            //iterate though all the destination addresses in a neighbor vector
            for (Address destAdd : nDV.addressSet()) {
                int newDist = n.getLinkWeight() + nDV.getDistance(destAdd);
                Integer currDist = dv.getDistance(destAdd);

                //if link weight to neighbor + neighbor's distance to dest < current distance, update
                if (currDist == null || currDist > newDist) {
                    isChanged = true;
                    dv.updateDistance(destAdd, newDist);
                    forwardTable.put(destAdd, n);
                }
            }
        }

        //if activated poison reverse
        if (reverse) {
            for (Address destAdd : forwardTable.keySet()) {
                Neighbor fwdNeighbor = forwardTable.get(destAdd);
                if (!destAdd.equals(fwdNeighbor.getAddress())) {
                    dv.removeDistance(destAdd);
                }
            }
        }

        return isChanged;

    }

    /**
     * Update weight between 2 links
     *
     * @param nAdd Neighbor address
     * @param weight The weight of the new link
     * @return true if is updated, false if no change
     */
    public boolean updateWeight(Address nAdd, int weight) {
        Neighbor n = neighbors.get(nAdd);
        // WHAT IF nAdd IS NOT A NEIGHBOR, n == null?
        int currWeight = n.getLinkWeight();
        if (currWeight != weight) {
            n.setLinkWeight(weight);
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
        Neighbor n = neighbors.get(nAdd);
        DistanceVector currDV = neighbors.get(nAdd).getDistVector();
        if (!currDV.equals(nDV)) {
            n.setDistVector(nDV);
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
        for (Address nAdd : neighbors.keySet()) {
            DVMessage dvMess = new DVMessage(address, nAdd, dv);
            sendMessage(dvMess, neighbors.get(nAdd));
        }
    }

//======================THREAD CONTROL=====================================
    /**
     * Print
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
        rl.start();
        
        //Starting AutoUpdater thread
        Thread auThread = new Thread(au);
        auThread.start();
        
        //Starting ConsoleReader thread
        Thread crThread = new Thread(cr);
        crThread.start();
    }

    /**
     * Stop all threads from running
     */
    public void stop() {
        // IS THIS THE CORRECT WAY TO STOP THE THREATS?
        running = false;
        au.stop();
        cr.stop();
    }
    
//======================OTHER METHODS=====================================
    /**
     * Print the distance vector
     */
    public void printDistVect() {
        System.out.println("This router: "+dv.toString());
    }
    
    /**
     * Print the neighbor's distance vectors
     */
    public void printNeighborDV() {
        for(Neighbor nei: neighbors.values()){
            System.out.println(nei.getAddress().toString() 
                    +": "+ nei.getDistVector().toString());
        }
    }
}

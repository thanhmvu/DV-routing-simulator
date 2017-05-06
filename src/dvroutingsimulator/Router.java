package dvroutingsimulator;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Contains all methods to simulate a router, include all algorithms and
 * containers
 *
 * @author thanhvu
 */
public class Router {

    private final Address address;

    //containers to store the neighbors
    private final Set<Address> liveNeighborAdds;
    private final Map<Address, Neighbor> neighborsCache;

    //the forward table
    private final Map<Address, Neighbor> forwardTable;

    //current distance vector
    private final DistanceVector dv;

    //threads
    private RouterListener rl;
    private AutoUpdater au;
    private ConsoleReader cr;

    private final boolean reverse;
    private static final int MAX_TIME_TO_LIVE = 15;

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

        //make sure that the containers here are threadsafe
        forwardTable = new ConcurrentHashMap<>();
        liveNeighborAdds = ConcurrentHashMap.newKeySet();
        neighborsCache = new ConcurrentHashMap<>();
        dv = new DistanceVector();

//        try {
//            addNeighbor(address, 0);
//
//        } catch (IOException ex) {
//            Logger.getLogger(Router.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

    /**
     * Get the address of the router
     *
     * @return The address of the router
     */
    public Address getAddress() {
        return address;
    }

    
    
//======================NEIGHBOR METHODS=====================================
    /**
     * Check if an address is a neighbor of a router
     *
     * @param nAdd Neighbor address
     * @return true if it is contained as a neighbor, false if it's not
     */
    public boolean containsNeighbor(Address nAdd) {
        return liveNeighborAdds.contains(nAdd);
    }

    /**
     * Add a neighbor when only address is present -- only add when the neighbor
     * is cached
     *
     * @param a Address of the neighbor
     */
    public void addNeighbor(Address a) {
        if (neighborsCache.containsKey(a)) {
            liveNeighborAdds.add(a);
        }
    }

    /**
     * Add a new neighbor into the router. Link weights map is updated, distance
     * vector is updated. DV Algorithm is run to determine forwarding table
     *
     * @param a Address of the neighbor
     * @param weight The weight of the neighbor
     * @throws java.io.IOException Happens when DV is advertised
     */
    public final void addNeighbor(Address a, int weight) throws IOException {
        Neighbor newNeighbor = new Neighbor(a, weight, this);
        liveNeighborAdds.add(a);
        neighborsCache.put(a, newNeighbor);

        //run the DV algorithm, and advertise if updated
        if (runDVAlgorithm()) {
            advertiseDV();
        }
    }

    /**
     * Remove a neighbor
     *
     * @param a A neighbor address
     */
    public void dropNeighbor(Address a) {
        liveNeighborAdds.remove(a);
        dv.removeDistance(a);
        Neighbor n = neighborsCache.get(a);
        n.stopTimer();
        System.out.println("neighbor " + a.toString() + " dropped");

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
            if (nextHopNeighbor == null) {
                System.out.println(m.getDstAddress().toString() + "is not reachable");
                return;
            }

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
     * Send a content message to a specific router
     *
     * @param dstIP destination IP
     * @param dstPort destination port
     * @param msg the message to send
     * @throws java.io.IOException
     */
    public void sendContentMsg(String dstIP, int dstPort, String msg) throws IOException {
        ContentMessage cm = new ContentMessage(this.address.ip, this.address.port,
                dstIP, dstPort, MAX_TIME_TO_LIVE, msg);
        this.forwardMessage(cm);
    }

    /**
     * Send a weight message directly to a neighbor
     *
     * @param dstIP destination IP of the neighbor
     * @param dstPort destination port of the neighbor
     * @param newW the new weight
     * @throws java.io.IOException
     */
    public void sendWeightMsg(String dstIP, int dstPort, int newW) throws IOException {
        // Create the weight message
        WeightMessage wm = new WeightMessage(
                this.address.ip, this.address.port, dstIP, dstPort, newW);

        // Send the message directly to the neighbor
        this.sendMessage(wm, neighborsCache.get(new Address(dstIP, dstPort)));
    }

    /**
     * Print action taken when router receive a content message destined to
     * itself
     *
     * @param m The message received
     */
    public void debugPrintReceiveMsg(Message m) {
        switch (m.getType()) {
            case CONTENT:
                System.out.println("Message msg received from " + m.getSrcAddress().toString()
                        + "\nmsg(" + ((ContentMessage) m).getMessage() + ")");
                return;
            case DV:
                System.out.println("new dv received from " + m.getSrcAddress().toString());
                DVMessage dMsg = (DVMessage) m;
                System.out.println(dMsg.getDistVect().debugPrint());

                return;
            case WEIGHT:
                System.out.println("new weight to neighbor " + m.getSrcAddress().toString() + " of " + ((WeightMessage) m).getWeight());
                return;
        }

    }

    
    
//======================DISTANCE VECTOR METHODS=====================================
    /**
     * Run the DV Algorithm
     *
     * @return true if algorithm results in change in distance vector, otherwise
     * false
     */
    public boolean runDVAlgorithm() {
        DistanceVector oldDV = (DistanceVector) dv.deepCopy();
        dv.clear();

        //first add all neighbors into dv
        for (Address nAdd : liveNeighborAdds) {
            Neighbor n = neighborsCache.get(nAdd);
            dv.updateDistance(nAdd, n.getLinkWeight());
            forwardTable.put(nAdd, n);
        }

        // then iterate through all neighbors' distance vector
        for (Address nAdd : liveNeighborAdds) {
            Neighbor n = neighborsCache.get(nAdd);

            //iterate though all the destination addresses in a neighbor vector
            DistanceVector nDV = n.getDistVector();
            for (Address destAdd : nDV.addressSet()) {
                if (!destAdd.equals(address)) {
                    Integer newDist = n.getLinkWeight() + nDV.getDistance(destAdd);
                    Integer currDist = dv.getDistance(destAdd);

                    //if link weight to neighbor + neighbor's distance to dest < current distance, update
                    if (currDist == null || currDist > newDist) {
                        dv.updateDistance(destAdd, newDist);
                        forwardTable.put(destAdd, n);
                    }
                }

            }
        }

        //if activated poison reverse, remove distance from addresses not present in forward table
        if (reverse) {
            for (Address destAdd : forwardTable.keySet()) {
                Neighbor fwdNeighbor = forwardTable.get(destAdd);
                if (!destAdd.equals(fwdNeighbor.getAddress())) {
                    dv.removeDistance(destAdd);
                }
            }
        }

        boolean isChanged = !oldDV.equals(dv);

        //debug print to System.out
        if (isChanged) {
            System.out.println("new dv calculated:");
            for (Address a : dv.addressSet()) {
                System.out.println(a.toString() + " "
                        + dv.getDistance(a) + " "
                        + forwardTable.get(a).getAddress().toString());
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
    public boolean updateWeight(Address nAdd, int weight) throws IOException {
        Neighbor n = neighborsCache.get(nAdd);

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
        Neighbor n = neighborsCache.get(nAdd);
        DistanceVector currDV = neighborsCache.get(nAdd).getDistVector();
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
        for (Address nAdd : liveNeighborAdds) {
            DVMessage dvMess = new DVMessage(address, nAdd, dv);
            sendMessage(dvMess, neighborsCache.get(nAdd));
        }
    }

    
    
//======================THREAD CONTROL=====================================
    /**
     * Start all the threads in the router
     */
    public final void startAllThreads() {
        //start RouterListener thread
        rl = new RouterListener(this);
        Thread rlThread = new Thread(rl);
        rlThread.start();

        //Starting AutoUpdater thread
        au = new AutoUpdater(this);
        Thread auThread = new Thread(au);
        auThread.start();

        //Starting ConsoleReader thread
        cr = new ConsoleReader(this);
        Thread crThread = new Thread(cr);
        crThread.start();
    }

    /**
     * Stop all threads from running
     */
    public void stop() {
        rl.stop();
        au.stop();
        cr.stop();
    }

    
    
//======================OTHER METHODS=====================================
    /**
     * Print the distance vector
     */
    public void printDistVect() {
        System.out.println("This router: " + dv.toString());
    }

    /**
     * Print the neighbor's distance vectors
     */
    public void printNeighborsDV() {
        for (Address nAdd : liveNeighborAdds) {
            if (!nAdd.equals(address)) {
                Neighbor nei = neighborsCache.get(nAdd);
                System.out.println(nei.getAddress().toString()
                        + ": " + nei.getDistVector().toString());
            }
        }
    }

    /**
     * Restart the timer every time a DV update is received
     *
     * @param neiAdd
     */
    public void restartNeighborTimer(Address neiAdd) {
        Neighbor n = neighborsCache.get(neiAdd);
        if (n != null) {
            n.restartTimer();
        }
    }

    /**
     * Get the distance vector of this router
     *
     * @return The distance vector of this router
     */
    public DistanceVector getDistVect() {
        return dv;
    }

}

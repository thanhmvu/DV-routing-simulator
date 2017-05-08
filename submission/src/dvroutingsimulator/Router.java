package dvroutingsimulator;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
            advertiseDV(false);
        }
    }

    /**
     * Remove a neighbor
     *
     * @param a A neighbor address
     */
    public void dropNeighbor(Address a) {
        Neighbor n = neighborsCache.get(a);

        liveNeighborAdds.remove(a); // remove neighbor from list of live neighbor addresses
        dv.removeDistance(a); // remove neighbor from distance vector
        n.stopTimer(); // stop timer for this neighbor

        // remove neighbor as a potential nexthop neighbor in forwarding table
        forwardTable.remove(a);
        for (Address destAdd : forwardTable.keySet()) {
            if (forwardTable.get(destAdd).equals(n)) {
                forwardTable.remove(destAdd);
            }
        }

        // debug print to System.out
        System.out.println("neighbor " + a.toString() + " dropped");

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

//======================COMMUNICATION METHODS=====================================
    /**
     * Send a message directly to a specific router
     *
     * @param m Message
     * @param neighbor The next hop neighbor
     * @throws java.net.SocketException
     * @throws java.net.UnknownHostException
     */
    private void sendMessage(String m, Neighbor neighbor) throws IOException {
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress dstIP = InetAddress.getByName(neighbor.getAddress().ip);
            int dstPort = neighbor.getAddress().port;

            // Put message in a packet
            byte[] sendData = m.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, dstIP, dstPort);

            // Write to output stream
            socket.send(sendPacket);
        }
    }

    /**
     * Forward a content message to the right destination
     *
     * @param m The message to be forwarded
     * @throws java.io.IOException
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

            // Process
            m.reduceTimeTolive();
            m.addRouter(this.address);

            // Foward message using writeToOuputStream
            sendMessage(m.toString(), nextHopNeighbor);
            System.out.println("Message msg"
                    + " from " + m.getSrcAddress().toString()
                    + " to " + m.getDstAddress().toString()
                    + " forwarded to " + nextHopNeighbor.getAddress().toString()
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
     * @param dstAdd The destination address
     * @param msg the message to send
     * @throws java.io.IOException
     */
    public void sendContentMsg(Address dstAdd, String msg) throws IOException {
        ContentMessage cm = new ContentMessage(this.address.ip, this.address.port,
                dstAdd.ip, dstAdd.port, MAX_TIME_TO_LIVE, msg);
        this.forwardMessage(cm);
    }

    /**
     * Send a weight message directly to a neighbor
     *
     * @param dstAdd The destination address
     * @param newW the new weight
     * @throws java.io.IOException
     */
    public void sendWeightMsg(Address dstAdd, int newW) throws IOException {
        // Create the weight message
        WeightMessage wm = new WeightMessage(
                this.address.ip, this.address.port, dstAdd.ip, dstAdd.port, newW);

        // Send the message directly to the neighbor
        this.sendMessage(wm.toString(), neighborsCache.get(dstAdd));
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
    public boolean updateWeight(Address nAdd, int weight) {
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
     * @param isAutoUpdate If this function is called from AutoUpdater or not
     * @throws IOException
     */
    public void advertiseDV(boolean isAutoUpdate) throws IOException {

        //if activated poison reverse, remove distance from addresses not present in forward table
        if (reverse) {

            for (Address neiAdd : liveNeighborAdds) {
                DistanceVector dvToSend = dv.deepCopy();

                for (Address destAdd : forwardTable.keySet()) {
                    Address nextHopAdd = forwardTable.get(destAdd).getAddress();
                    if (nextHopAdd.equals(neiAdd)
                            && !destAdd.equals(nextHopAdd)) {
                        dvToSend.removeDistance(destAdd);
                    }
                }

                DVMessage dvMess = new DVMessage(address, neiAdd, dvToSend);
                sendMessage(dvMess.toString(), neighborsCache.get(neiAdd));

                String printout = "Update sent to neighbor " + neiAdd.toString();
                if (isAutoUpdate && au != null) {
                    printout += " at time " + au.getCurrentTime();
                }
                System.out.println(printout);
                System.out.println(dvToSend.debugPrint());

            }
        } else {
            for (Address neiAdd : liveNeighborAdds) {
                DistanceVector dvToSend = dv.deepCopy();
                DVMessage dvMess = new DVMessage(address, neiAdd, dvToSend);
                sendMessage(dvMess.toString(), neighborsCache.get(neiAdd));
            }
            if (!liveNeighborAdds.isEmpty()) {
                String printout = "Update sent to all neighbors";
                if (isAutoUpdate && au != null) {
                    printout += " at time " + au.getCurrentTime();
                }
                System.out.println(printout);
                System.out.println(dv.debugPrint());
            }
        }
    }

    /**
     * Print the distance vector
     */
    public void printDistVect() {
        System.out.println("This router: " + dv.toString());
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
        for (Neighbor n: neighborsCache.values()) {
            n.stopTimer();
        }
        System.exit(0);
    }

//======================MAIN METHODS=====================================
    /**
     * Run the program
     *
     * @param args Command line argument, format "[-reverse] filePath.txt"
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Need at least 1 argument. Format: [-reverse] [filepath]");
            return;
        }

        boolean reverse = false;
        String path = null;
        for (String arg : args) {
            if (arg.equals("-reverse")) {
                reverse = true;
            } else {
                path = arg;
            }
        }

        if (path == null) {
            System.out.println("Need to insert a filePath. Format: [-reverse] [filepath]");
            return;
        }

        Router r = null;
        Path neighborsFilePath = Paths.get(path);
        try {
            // Read file and create a router
            List<String> allLines = Files.readAllLines(neighborsFilePath);
            String myAddress = allLines.get(0);
            String[] myFields = myAddress.split(" ");
            String myIp = myFields[0];
            int myPort = Integer.parseInt(myFields[1]);
            r = new Router(myIp, myPort, reverse);

            // Iterate through and add its neighbors
            for (int i = 1; i < allLines.size(); i++) {
                String[] fields = allLines.get(i).split(" ");
                String ip = fields[0];
                int port = Integer.parseInt(fields[1]);
                int weight = Integer.parseInt(fields[2]);
                r.addNeighbor(new Address(ip, port), weight);
            }
        } catch (IOException ex) {
            Logger.getLogger(Router.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (r != null) {
            r.startAllThreads();
        }
    }
}

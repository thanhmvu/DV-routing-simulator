package dvroutingsimulator;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Listen to update messages from other routers
 *
 * @author hongha912
 */
public class RouterListener implements Runnable {

    private final Router r;
    private volatile boolean running;

    /**
     * Create a new router listener
     *
     * @param router The router on which the thread runs
     */
    RouterListener(Router router) {
        super();
        this.r = router;
        running = true;
    }

    /**
     * Run the thread
     */
    @Override
    public void run() {
        try {
            DatagramSocket listeningSocket = new DatagramSocket(r.getAddress().port);
            int maxSize = 1024;
            while (running) {
                byte[] receiveData = new byte[maxSize];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                listeningSocket.receive(receivePacket);
                String protocol = new String(receivePacket.getData());
                this.handleProtocol(protocol);
            }
        } catch (SocketException ex) {
            Logger.getLogger(RouterListener.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(RouterListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Stop the thread
     */
    public void stop() {
        running = false;
    }

    /**
     * Handle the protocol once it's received, convert to a message, and perform
     * appropriate action
     *
     * @param protocol The protocol received
     */
    private void handleProtocol(String protocol) throws IOException {
        MsgType msgType = Message.getType(protocol);
        switch (msgType) {
            case CONTENT:
                ContentMessage cMsg = new ContentMessage(protocol);

                //if router is the recipient
                if (cMsg.getDstAddress().equals(r.getAddress())) {
                    r.debugPrintReceiveMsg(cMsg);
                } else { //else if message should be forwarded
                    r.forwardMessage(cMsg);
                }
                return;
            case DV:
                DVMessage dMsg = new DVMessage(protocol);
                r.debugPrintReceiveMsg(dMsg);
                Address nAdd = dMsg.getSrcAddress();

                //check if neighbor is added, add if yes
                if (!r.containsNeighbor(nAdd)) {
                    r.addNeighbor(nAdd);
                }

                // set the sender neighbor's status to be updated
                r.restartNeighborTimer(nAdd);

                if (r.updateDV(dMsg.getSrcAddress(), dMsg.getDistVect())) {
                    if (r.runDVAlgorithm()) {
                        r.advertiseDV();
                    }
                }
                return;

            case WEIGHT:
                WeightMessage wMsg = new WeightMessage(protocol);
                r.debugPrintReceiveMsg(wMsg);
                //check if the message comes from a neighbor
                Address neiAdd = wMsg.getSrcAddress();

                //if address is not from the neighbor, it means that a neighbor just joins the network
                if (!r.containsNeighbor(neiAdd)) {
                    r.addNeighbor(neiAdd, wMsg.getWeight());
                } // else, update weight of the neighbor
                else {
                    if (r.updateWeight(wMsg.getSrcAddress(), wMsg.getWeight())) {
                        if (r.runDVAlgorithm()) {
                            r.advertiseDV();
                        }
                    }
                }
                return;
        }
    }

}

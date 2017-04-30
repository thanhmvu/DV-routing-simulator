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

    /**
     * Create a new router listener
     *
     * @param router The router on which the thread runs
     */
    RouterListener(Router router) {
        super();
        this.r = router;
    }

    /**
     * Run the thread
     */
    @Override
    public void run() {
        try {
            DatagramSocket listeningSocket = new DatagramSocket(r.getAddress().port);
            int maxSize = 1024;
            byte[] receiveData = new byte[maxSize];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            while (r.isRunning()) {
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
                if (cMsg.getDstAddress() == r.getAddress()) {
                    r.receiveMessage(cMsg);
                } else { //else if message should be forwarded
                    r.forwardMessage(cMsg);
                }
                return;
            case DV:
                DVMessage dMsg = new DVMessage(protocol);
                if (r.updateDV(dMsg.getSrcAddress(), dMsg.getDistVect())) {
                    if (r.runDVAlgorithm()) {
                        r.advertiseDV();
                    }
                }
                return;

            case WEIGHT:
                WeightMessage wMsg = new WeightMessage(protocol);
                if (r.updateWeight(wMsg.getSrcAddress(), wMsg.getWeight())) {
                    if (r.runDVAlgorithm()) {
                        r.advertiseDV();
                    }
                }
                break;
        }
    }

}

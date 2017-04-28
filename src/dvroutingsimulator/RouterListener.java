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
public class RouterListener extends Thread {

    private Router r;

    /**
     * Create a new router listener
     *
     * @param router
     */
    RouterListener(Router router) {
        super();
        this.r = router;
    }

    @Override
    public void run() {
        try {
            DatagramSocket ls = new DatagramSocket(r.getAddress().port);
            int maxSize = 1024;
            byte[] receiveData = new byte[maxSize];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            while (r.isRunning()) {
                ls.receive(receivePacket);
                String protocol = new String(receivePacket.getData());
                MsgType msgType = Message.getType(protocol);
                
                //ADD MORE 
                
            }
        } catch (SocketException ex) {
            Logger.getLogger(RouterListener.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(RouterListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}

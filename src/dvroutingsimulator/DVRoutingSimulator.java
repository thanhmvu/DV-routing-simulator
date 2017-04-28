
package dvroutingsimulator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simulate a router
 * @author thanhvu
 */
public class DVRoutingSimulator {
    
    private Router r;
    
    /**
     * Create a new simulator
     * @param filePath The path 
     */
    public DVRoutingSimulator(String filePath) {
        Path neighborsFilePath = Paths.get(filePath);
        try {
            List<String> allLines = Files.readAllLines(neighborsFilePath);
            
            // create this router
            String myAddress = allLines.get(0);
            String[] myFields = myAddress.split(" ");
            String myIp = myFields[0];
            int myPort = Integer.parseInt(myFields[1]);
            r = new Router(myIp, myPort);
            
            //iterate through neighbors
            for (int i = 1; i < allLines.size(); i++) {
                String[] fields = allLines.get(i).split(" ");
                String ip = fields[0];
                int port = Integer.parseInt(fields[1]);
                int weight = Integer.parseInt(fields[2]);
                r.addNeighbor(ip, port, weight);
            }
        } catch (IOException ex) {
            Logger.getLogger(DVRoutingSimulator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
    }
    
}

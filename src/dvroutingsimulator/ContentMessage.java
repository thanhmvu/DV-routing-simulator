package dvroutingsimulator;

import java.util.ArrayList;

/**
 * A router message that contains actual content (inside msg)
 * 
 * @author thanhvu
 */
public class ContentMessage extends Message {

    private int timeToLive; // max number of remaining hops, decremented at each router
    private ArrayList<Address> path; // addresses of routers in the path
    private String msg;
    private static final String pDLM = " "; //path delimiter
    private static final String iDLM = "-"; //ip-port delimiter

    /**
     * Constructor for ContentMessage
     *
     * @param srcip IP address of source router
     * @param srcport port number of source router
     * @param dstip IP address of destination router
     * @param dstport port number of destination router
     * @param remainingHops the message's remaining time-to-live
     * @param mess the message content
     */
    public ContentMessage(String srcip, int srcport, String dstip, int dstport, int remainingHops, String mess) {
        super(MsgType.CONTENT, srcip, srcport, dstip, dstport);
        this.msg = mess;
        this.timeToLive = remainingHops;
        this.path = new ArrayList<>();
    }

    /**
     * Constructor that parses a string representation of a ContentMessage
     *
     * @param text The message to parse. Should have at least 7 fields,
     * with the following format:
     *
     * "type[DLM]srcIP[DLM]srcPort[DLM]
     * dstIP[DLM]dstPort[DLM]timeToLive[DLM]msg[DLM]
     * ip[iDLM]port[pDLM]ip[iDLM]port ..."
     *
     */
    public ContentMessage(String text) {
        super(text);
        if (this.type != MsgType.CONTENT) {
            System.out.println("ERROR: Not a CONTENT Type.");
            return;
        }
        String[] fields = text.split(this.DLM);

        if (fields.length < 7) {
            System.out.println("ERROR: Wrong format. Not enough fields.");
            return;
        }

        this.timeToLive = Integer.parseInt(fields[5]);
        this.msg = fields[6];
        this.path = new ArrayList<>();
        if (fields.length > 7) {
            for (String address : fields[7].split(pDLM)) {
                String[] tmp = address.split(iDLM);
                this.path.add(new Address(tmp[0], Integer.parseInt(tmp[1])));
            }
        }
    }

    /**
     * Add a router to the path
     * 
     * @param ip the router's IP address
     * @param port the router's port number
     */
    public void addRouter(String ip, int port) {
        path.add(new Address(ip, port));
    }

    /**
     * Output a string representation of a ContentMessage 
     * using the following format:
     *
     * "type[DLM]srcIP[DLM]srcPort[DLM]
     * dstIP[DLM]dstPort[DLM]timeToLive[DLM]msg[DLM]
     * ip[iDLM]port[pDLM]ip[iDLM]port ..."
     *
     * @return a string representation of a content message
     */
    @Override
    public String toString() {
        String output = type + DLM + srcAdd.ip + DLM + srcAdd.port
                + DLM + dstAdd.ip + DLM + dstAdd.port
                + DLM + timeToLive + DLM + msg + DLM;
        for (Address ad : path) {
            output += ad.ip + iDLM + ad.port + pDLM;
        }
        return output;
    }

    /**
     * Decrease timeToLive by 1 hop
     */
    public void reduceTimeTolive() {
        timeToLive--;
    }

    /**
     * Getter for msg content
     *
     * @return the content of this message
     */
    public String getMessage() {
        return msg;
    }

    /**
     * Getter for timeToLive
     *
     * @return remaining timeToLive
     */
    public int getTimeToLive() {
        return timeToLive;
    }

    /**
     * Getter path - the routers which 
     * this message has went through up to that hop
     *
     * @return current path
     */
    public ArrayList<Address> getPath() {
        return path;
    }
}

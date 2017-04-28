package dvroutingsimulator;

import java.util.ArrayList;

/**
 *
 * @author thanhvu
 */
public class ContentMessage extends Message {

    int timeToLive; // max number of remaining hops, decremented at each router
    ArrayList<Address> path; // addresses of routers in the path
    String msg;

    public ContentMessage(String srcip, int srcport, String dstip, int dstport, int remainingHops, String mess) {
        super(MsgType.CONTENT, srcip, srcport, dstip, dstport);
        this.msg = mess;
        this.timeToLive = remainingHops;
        this.path = new ArrayList<>();
    }

    /**
     * Constructor that parses a string representation of a ContentMessage
     *
     * @param text The message to parse. Should have at least 7 fields separated
     * by [delimiter] as followed:
     *
     * "type[delimiter]srcIP[delimiter]srcPort[delimiter]
     * dstIP[delimiter]dstPort[delimiter]timeToLive[delimiter]msg[delimiter]ip-port
     * ip-port ..."
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
            for (String address : fields[7].split(" ")) {
                String[] tmp = address.split("-");
                this.path.add(new Address(tmp[0], Integer.parseInt(tmp[1])));
            }
        }
    }

    public void addRouter(String ip, int port) {
        path.add(new Address(ip, port));
    }

    /**
     * Output a string representation of a ContentMessage using the following
     * format:
     *
     * "type[delimiter]timeToLive[delimiter]srcIP[delimiter]srcPort[delimiter]
     * dstIP[delimiter]dstPort[delimiter]msg[delimiter]ip-port ip-port ..."
     *
     * @return a string representation of a content message
     */
    @Override
    public String toString() {
        String output = type + DLM + srcAdd.ip + DLM + srcAdd.port
                + DLM + dstAdd.ip + DLM + dstAdd.port
                + DLM + timeToLive + DLM + msg + DLM;
        for (Address ad : path) {
            output += ad.ip + "-" + ad.port + " ";
        }
        return output;
    }

    public void reduceTimeTolive() {
        timeToLive--;
    }

    public String getMessage() {
        return msg;
    }

    public int getTimeToLive() {
        return timeToLive;
    }

    public ArrayList<Address> getPath() {
        return path;
    }
}


package dvroutingsimulator;

/**
 * A wrap-around class for the ip-port pair
 * @author hongha912
 */
public class Address {
    public String ip;
    public int port;
    private static final String DLM = ":";
        
    Address(String i, int p){
        ip = i;
        port = p;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof Address) {
            Address a = (Address) o;
            return this.ip.equals(a.ip) && this.port == a.port;
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + ip.hashCode();
        result = 31 * result + port;
        return result;
    } 
    
    @Override
    public String toString() {
        return ip + DLM + port;
    }
    
    public Address deepClone(){
        return new Address(ip, port);
    }
}

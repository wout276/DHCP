import java.io.IOException;
import java.net.*;

public class DHCPsocket extends DatagramSocket  {
	private static int PACKET_SIZE = 1500; // default MTU for ethernet
	//private static String serverHostname = new String("10.33.14.246");
	private static String serverHostname = new String("localhost");
	private static InetAddress IPAddress = null;
	static {
			try {
				IPAddress = InetAddress.getByName(serverHostname);
				System.out.println("Ip address set");
			} catch (UnknownHostException ex) {
				System.err.println(ex);
			}
	}
    private int defaultSOTIME_OUT = 10000; // 10 second socket timeout
    
    
    //constructor
    public DHCPsocket () throws SocketException, IOException {
    	//containers for data to transmit   
    	this.setSoTimeout(defaultSOTIME_OUT); // set default time out
    }
    
    /**
     * Sets the Maximum Transfer Unit for the UDP DHCP Packets to be set.
     * Default is 1500, MTU for Ethernet
     * @param inSize integer representing desired MTU
     */    
    //This is a function as found online.
    public void setMTU(int inSize) {
	PACKET_SIZE = inSize;
    }

    /**
     * Returns the set MTU for this socket
     * @return the Maximum Transfer Unit set for this socket
     */
    //This is a function as found online.    
    public int getMTU() {
	return PACKET_SIZE;
    }
    
    /**
     * Sends a DHCPMessage object to a predifined host.
     * @param inMessage well-formed DHCPMessage to be sent to a server
     * @throws IOException 
     */
       
    public synchronized void send(DHCPpacket packet) throws IOException {
    	byte data[] = new byte[PACKET_SIZE];
    	data = packet.toByteArray();
    	DatagramPacket result = new DatagramPacket(data, data.length, IPAddress, 1234);
    	this.send(result);
    }
    
    public synchronized boolean receive(DHCPpacket packet)  {
	try {
	    DatagramPacket receivePacket = new DatagramPacket(new byte[PACKET_SIZE],PACKET_SIZE);
	    //System.out.println("Beginnen ontvangen");
	    System.out.println("Start ontvangen in socket.");
	    this.receive(receivePacket);
	    System.out.println("Ontvangen in socket");
	    byte[] data = receivePacket.getData();
	    //System.out.println("getdata");
	    packet.readByteArray(data);
	    //System.out.println("kleir");
	} catch (java.io.IOException ex) {
		// System.err.println(ex);
	    return false;
        }  // end catch    
	return true;
    }
    
}

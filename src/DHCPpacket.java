/**
 * @author Gilles
 *
 */
import java.io.*;
import java.net.*;
import com.google.common.collect.*;




public class DHCPpacket {
    private byte op;									// Opcode
    private byte htype;									// HW address Type
    private byte hlen;									// Hardware address length
    private byte hops;									// HW options
    private byte xid[] = new byte[4];					// transaction id
    private byte secs[] = new byte[2];		 							// elapsed time from trying to boot
    private byte flags[] = new byte[2];						// flags
    private byte ciaddr[] = new byte[4];				// client IP
    private byte yiaddr[] = new byte[4];				// your client IP
    private byte siaddr[] = new byte[4];				// Server IP
    private byte giaddr[] = new byte[4];				// relay agent IP
    private byte chaddr[] = new byte[16];   			// Client HW address
    private byte sname[] = new byte[64];				// Optional server host name
    private byte file [] = new byte[128];   			// Boot file name 
    private DHCPoptions optionsList = new DHCPoptions();// DHCP options

    private int port;									// global port
    private InetAddress ip;								// global IP

    /**
     * Default DHCP client port
     */
    public static final int CLIENT_PORT = 68; // client port (by default)

    /**
     * Default DHCP server port
     */
    public static final int SERVER_PORT = 1234; // server port (by default)
    
    /**
     * Broadcast address declaration
     */
    public static InetAddress BROADCAST_ADDRESS = null;
    
    /**
     * Setting the broadcast address
     */
    static {
    	if (BROADCAST_ADDRESS == null) {
    	    try {
    		//BROADCAST_ADDRESS = InetAddress.getByName("10.33.14.246");
    	    BROADCAST_ADDRESS = InetAddress.getByName("localhost");
    	    } catch (UnknownHostException e) {} 
    	}
        }
    
    /**
     * Codes for different DHCP packet types
     */
    private static final BiMap<String, Integer> packetTypes = HashBiMap.create();
    static {
    	packetTypes.put("DISCOVER", 1);
    	packetTypes.put("OFFER", 2);
    	packetTypes.put("REQUEST", 3);
    	packetTypes.put("ACK", 5);
    	packetTypes.put("NAK", 6);
    	packetTypes.put("RELEASE", 7);
    }
    
    /**
	 * @return the packettype code
	 */
	public static int packetCode(String query) {
		return packetTypes.get(query);
	}
	
	/**
	 * @return the packettype
	 */
	public static String packetCode(int query) {
		return packetTypes.inverse().get(query);
	}

	/**
     * Constructors for empty DHCPpacket objects
     * @param address the host address
     * @param port port number
     */
    public DHCPpacket() {
    	ip = BROADCAST_ADDRESS;
    	port = SERVER_PORT;
    }
    
    public DHCPpacket(InetAddress address, int in_port) {
    	ip = address;
    	port = in_port;
    }
    
    public DHCPpacket(InetAddress address) {
    	ip = address;
    	port = SERVER_PORT;
    }    
    
    public DHCPpacket(int in_port) {
    	ip = BROADCAST_ADDRESS;
    	port = in_port;
    }
    
    /**
     * Convert DHCPpacket object to a byte array
     * @return byte array containing the data from this DHCPpacket object
     */
    public byte[] toByteArray() {
    	ByteArrayOutputStream data = new ByteArrayOutputStream ();
    	try {
    		data.write(op);
        	data.write(htype);
        	data.write(hlen);
        	data.write(hops);
        	data.write(xid);
        	data.write(secs);
        	data.write(flags);
        	data.write(ciaddr);
        	data.write(yiaddr);
        	data.write(giaddr);
        	data.write(chaddr);
        	data.write(sname);
        	data.write(file);
        	data.write(optionsList.toByteArray());
    	} catch (IOException e) {}
    	byte[] message = data.toByteArray();
    	return message;
    }
    
    /**
     * Read data from a byte array and store into this DHCPpacket object.
     * @param message byte array containing data
     * @return a DHCPpacket object containing the data from the byte array
     */
    public DHCPpacket readByteArray(byte[] message) {
    	ByteArrayInputStream data = new ByteArrayInputStream(message);
    	try {
    		byte[] opa = new byte[1];
    		byte[] htypea = new byte[1];
    		byte[] hlena = new byte[1];
    		byte[] hopsa = new byte[1];
    		byte[] options = new byte[312];
    		//System.out.println("initialisatie ok");
    		data.read(opa);
    		this.setOp(opa[0]);
    		data.read(htypea);
    		this.setHtype(htypea[0]);
    		data.read(hlena);
    		this.setHlen(hlena[0]);
    		data.read(hopsa);
    		this.setHops(hopsa[0]);
                //System.out.println("bytes ok");
    		data.read(xid);
    		data.read(secs);
    		data.read(flags);
    		data.read(ciaddr);
    		data.read(yiaddr);
    		data.read(siaddr);
    		data.read(giaddr);
    		data.read(chaddr);
    		data.read(sname);
    		data.read(file);
    		data.read(options);
                //System.out.println("bytearray ok");
    		optionsList.fromByteArray(options);
                //System.out.println("options ok");
    	} catch (IOException ex) {
		System.err.println(ex);
	}
    	return this;
    }
    
    // Getters and setters:
    
	/**
	 * @return the op
	 */
	public byte getOp() {
		return op;
	}

	/**
	 * @param op the op to set
	 */
	public void setOp(byte op) {
		this.op = op;
	}

	/**
	 * @return the htype
	 */
	public byte getHtype() {
		return htype;
	}

	/**
	 * @param htype the htype to set
	 */
	public void setHtype(byte htype) {
		this.htype = htype;
	}

	/**
	 * @return the hlen
	 */
	public byte getHlen() {
		return hlen;
	}

	/**
	 * @param hlen the hlen to set
	 */
	public void setHlen(byte hlen) {
		this.hlen = hlen;
	}

	/**
	 * @return the hops
	 */
	public byte getHops() {
		return hops;
	}

	/**
	 * @param hops the hops to set
	 */
	public void setHops(byte hops) {
		this.hops = hops;
	}

	/**
	 * @return the xid
	 */
	public byte[] getXid() {
		return xid;
	}

	/**
	 * @param xid the xid to set
	 */
	public void setXid(byte[] xid) {
		this.xid = xid;
	}

	/**
	 * @return the secs
	 */
	public byte[] getSecs() {
		return secs;
	}

	/**
	 * @param secs the secs to set
	 */
	public void setSecs(byte[] secs) {
		this.secs = secs;
	}

	/**
	 * @return the broadcast_flag
	 */
	public byte[] getFlags() {
		return flags;
	}

	/**
	 * @param broadcast_flag the broadcast_flag to set
	 */
	public void setFlags(byte[] flag) {
		this.flags = flag;
	}

	/**
	 * @return the ciaddr
	 */
	public byte[] getCiaddr() {
		return ciaddr;
	}

	/**
	 * @param ciaddr the ciaddr to set
	 */
	public void setCiaddr(byte[] ciaddr) {
		this.ciaddr = ciaddr;
	}

	/**
	 * @return the yiaddr
	 */
	public byte[] getYiaddr() {
		return yiaddr;
	}

	/**
	 * @param yiaddr the yiaddr to set
	 */
	public void setYiaddr(byte[] yiaddr) {
		this.yiaddr = yiaddr;
	}

	/**
	 * @return the siaddr
	 */
	public byte[] getSiaddr() {
		return siaddr;
	}

	/**
	 * @param siaddr the siaddr to set
	 */
	public void setSiaddr(byte[] siaddr) {
		this.siaddr = siaddr;
	}

	/**
	 * @return the giaddr
	 */
	public byte[] getGiaddr() {
		return giaddr;
	}

	/**
	 * @param giaddr the giaddr to set
	 */
	public void setGiaddr(byte[] giaddr) {
		this.giaddr = giaddr;
	}

	/**
	 * @return the chaddr
	 */
	public byte[] getChaddr() {
		return chaddr;
	}

	/**
	 * @param chaddr the chaddr to set
	 */
	public void setChaddr(byte[] chaddr) {
		this.chaddr = chaddr;
	}

	/**
	 * @return the sname
	 */
	public byte[] getSname() {
		return sname;
	}

	/**
	 * @param sname the sname to set
	 */
	public void setSname(byte[] sname) {
		this.sname = sname;
	}

	/**
	 * @return the file
	 */
	public byte[] getFile() {
		return file;
	}

	/**
	 * @param file the file to set
	 */
	public void setFile(byte[] file) {
		this.file = file;
	}

	/**
	 * @return the optionsList
	 */
	public DHCPoptions getOptionsList() {
		return optionsList;
	}

	/**
	 * @param optionsList the optionsList to set
	 */
	public void setOptionsList(DHCPoptions optionsList) {
		this.optionsList = optionsList;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * @return the ip
	 */
	public InetAddress getIp() {
		return ip;
	}

	/**
	 * @param ip the ip to set
	 */
	public void setIp(InetAddress ip) {
		this.ip = ip;
	}    
    
    
    
}


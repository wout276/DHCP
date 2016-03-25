import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * @author r0449276
 *
 */
public class DHCPclienthandler implements Runnable {

	static DHCPserver server = null;
	static DHCPpacket packet = null;
	static DHCPsocket socket = null;
    static final int REQUESTED_IP = 50;
    static final int LEASETIME = 51;
    static final int MESSAGE_TYPE = 53;
    static Integer leasetime = 25;
	//server.setConnection("ip", "MAC");
	//server.getConnectionChaddr("ip");
	//server.getConnectionIp("MAC");
	//server.assignIpNumber();
	//server.isIpAvailable("ip");
    //server.removeConnection("MAC")
	
	public DHCPclienthandler(DHCPserver inServer, DHCPpacket serverPacket, DHCPsocket inSocket) {
		server = inServer;
		packet = serverPacket;
		socket = inSocket;
	}
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		// Dit is de method die wordt aangeroepen bij het starten van de thread, dus de zogezegde main method
		// de handler moet een field hebben waarin hij zijn controlerende server bijhoud zodat die gegevens aan de server
		// kan doorgeven (zoals wanneer een client disconnect of hernieuwt.
		//Verification if ACK, NAK, or DECLINE package was returned.
		try{				
	        int answerCode = (int)(packet.getOptionsList().getOption(MESSAGE_TYPE))[0];
	        String packetType = DHCPpacket.packetCode(answerCode);
	        System.out.println("Received a " + packetType);
	        //Determining to messageType and making an answer.
	        if (answerCode == (int) DHCPpacket.packetCode("DISCOVER")){
	        	packet = prepareOffer(packet);
	        	send(packet);
	        } else if (answerCode == (int) DHCPpacket.packetCode("REQUEST")){
	        	packet = chooseAckNak(packet);
	        	send(packet);
	        } else if (answerCode == (int) DHCPpacket.packetCode("RELEASE")){
	        	handleRelease(packet);
	        }
		} catch(IOException ex) {
			System.err.println(ex);
		}
	}
	
	private static DHCPpacket prepareOffer(DHCPpacket packet){
		String ip = server.assignIpNumber();
		//DHCPrequest opcode to bootReply
    	packet.setOp((byte) 2);
    	//Set yiaddr to the offered IP
    	packet.setYiaddr(bytesFromIp(ip));
        //set messageType in options to 'offer'
    	byte[] option = new byte[1];
	    option[0] = (byte) DHCPpacket.packetCode("OFFER");
	    packet.getOptionsList().updateOption(MESSAGE_TYPE, option);
    	return packet;
	}
	
	private static DHCPpacket prepareAck(DHCPpacket packet){
		String MAC = MACFromBytes(packet.getChaddr());
		server.setConnection(stringFromByte(packet.getYiaddr()),MAC);
		//DHCPrequest opcode to bootReply
    	packet.setOp((byte) 2);
        //set messageType in options to 'offer'
    	byte[] option = new byte[1];
	    option[0] = (byte) DHCPpacket.packetCode("ACK");
	    packet.getOptionsList().updateOption(MESSAGE_TYPE, option);
	    byte[] lt = ByteBuffer.allocate(4).putInt(leasetime).array();
	    packet.getOptionsList().updateOption(LEASETIME, lt);
    	return packet;
	}
	
	private static DHCPpacket prepareNak(DHCPpacket packet){
		//DHCPrequest opcode to bootReply
    	packet.setOp((byte) 2);
    	byte[] empty = new byte[4];
    	packet.setYiaddr(empty);
    	packet.setCiaddr(empty);
    	packet.setSiaddr(empty);
        //set messageType in options to 'offer'
    	byte[] option = new byte[1];
	    option[0] = (byte) DHCPpacket.packetCode("NAK");
	    packet.getOptionsList().updateOption(MESSAGE_TYPE, option);
		return packet;
	}
	
	private static void handleRelease(DHCPpacket packet){
		String MAC = MACFromBytes(packet.getChaddr());
		server.removeConnection(MAC);
	}
	
	private static DHCPpacket chooseAckNak(DHCPpacket packet){
		//Check renewal
		byte[] yiaddr = packet.getYiaddr();
		byte[] reqIp = packet.getOptionsList().getOption(REQUESTED_IP);
		String MAC = MACFromBytes(packet.getChaddr());
		if (Arrays.equals(packet.getCiaddr(), yiaddr) && server.getConnectionChaddr(MAC) == stringFromByte(yiaddr)){
			packet = prepareAck(packet); 
		} else if (Arrays.equals(yiaddr,reqIp) || (!Arrays.equals(yiaddr, new byte[4]) && Arrays.equals(reqIp, new byte[4]))){
			if (server.isIpAvailable(stringFromByte(yiaddr))){
				packet = prepareAck(packet);
			} else {
				packet = prepareNak(packet);
			}			
		} else {
			packet = prepareNak(packet);
		}		
		return packet;
	}
	
	private static void send(DHCPpacket packet) throws IOException{
		socket.send(packet);
	}
	
	//TODO
	// Convert a byte[] to a string
	//voor ip adressen
    private static String stringFromByte(byte[] bytes){
    	// Makkelijke methode voor 127.0.0.100:
    	// String str = Arrays.toString(bytes);
	    // String result = str.substring(0, 3) + "." + str.charAt(3) + "." + str.charAt(4) + "." + str.substring(5);
	    String result = new String();
	    for(byte b : bytes){
	    	String part = Byte.toString(b);
	    	result.concat(part + ".");
	    }
	    result = result.substring(0, result.length() - 1);
        return result;
    }

    
    private static String MACFromBytes(byte[] MACadr_byte){
    	// 18:19:D2:66:52:47
        String str = bytesToHex(MACadr_byte);
        String result = str.substring(0, 2) + ":" + str.substring(2, 4) + ":" + str.substring(4, 6) + ":" + str.substring(6, 8) + ":" + str.substring(8, 10) + ":" + str.substring(10);
        return result;
    }
    
    // This code (hexArray) was taken from http://stackoverflow.com/questions/9655181/how-to-convert-a-byte-array-to-a-hex-string-in-java
    final private static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
    
    private static byte[] bytesFromIp(String ip){
        byte[] MACadr = new byte[4];
        // 127.0.0.100
        for ( int k = 0; k < 3; k++ ) {
        	int pos = ip.indexOf(".");
        	byte part = Byte.parseByte(ip.substring(0, pos));
        	MACadr[k] = part;
        	ip = ip.substring(pos + 1);
        }
        MACadr[4] = Byte.parseByte(ip);
        return MACadr;
    }
}

import java.io.IOException;
import java.util.Random;

/**
 * @author r0449276
 *
 */
public class DHCPclienthandler implements Runnable {

	static DHCPserver server = null;
	static DHCPpacket packet = null;
	static DHCPsocket socket = null;
	static Integer serverPort = new Integer(1234);
    static final long TIMEOUT = new Integer(10000);
    static final int REQUESTED_IP = 50;
    static final int LEASETIME = 51;
    static final int MESSAGE_TYPE = 53;
    static final int T1 = 58;
    static final int T2 = 59;
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
	        	packet = prepareRelease(packet);
	        	send(packet);
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
		String ip = server.setConnection(stringFromByte(packet.getYiaddr()),MAC);
		//DHCPrequest opcode to bootReply
    	packet.setOp((byte) 2);
        //set messageType in options to 'offer'
    	byte[] option = new byte[1];
	    option[0] = (byte) DHCPpacket.packetCode("ACK");
	    packet.getOptionsList().updateOption(MESSAGE_TYPE, option);
    	return packet;
	}
	
	private static DHCPpacket prepareNak(DHCPpacket packet){
		return packet;
	}
	
	private static void handleRelease(DHCPpacket packet){
		String MAC_bytes = MACFromBytes(packet.getChaddr());
		server.removeConnection(MAC_bytes);
	}
	
	private static DHCPpacket chooseAckNak(DHCPpacket packet){
		packet = prepareAck(packet);
		packet = prepareNak(packet);
		return packet;
	}
	
	private static void send(DHCPpacket packet) throws IOException{
		socket.send(packet);
	}
	
	//TODO
	// Convert a byte[] to a string
	//voor ip adressen
    private static String stringFromByte(byte[] bytes){
    	//System.out.println(Arrays.toString(bytes));
	    String str = new String();
        for (int x = 0; x < bytes.length; x++) {
            if (x < 3) {
                str += ".";
            } else {
                str += (int)((char) bytes[x]%256);
            }
        }
        return str;
    }

    //convert a byte[] to a long
    private static long longFromByte(byte[] bytes){
        long result = 0;
        for (int x = 0; x < bytes.length; x++){
           result = (result << 8) + (bytes[x] & 0xff);
        }
        return result;
    }

    //Function for converting the MAC-address to bytes
    private static byte[] bytesFromMAC(String MACadr){
        String[] MACadr_split = MACadr.split(":");
        byte[] MACadr_bytes = new byte[16];
        for (int x=0; x<6; x++){
            MACadr_bytes[x] = (byte) (Integer.parseInt(MACadr_split[x], 16));
        }
        return MACadr_bytes;
    }
    
    private static String MACFromBytes(byte[] MACadr_byte){
        String MACadr = null;
        return MACadr;
    }
    
    private static byte[] bytesFromIp(String ip){
        byte[] MACadr = new byte[4];
        return MACadr;
    }
}

import java.io.IOException;

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
	
	public DHCPclienthandler(DHCPserver inServer, DHCPpacket serverPacket, DHCPsocket inSocket) throws Exception {
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
		server.setConnection((int) 0, "MAC");
		server.getConnectionChaddr((int) 0);
		server.getConnectionIp("MAC");
		server.assignIpNumber("MAC");
		server.checkConnection("ip");
		return packet;
	}
	
	private static DHCPpacket prepareAck(DHCPpacket packet){
		return packet;
	}
	
	private static DHCPpacket prepareNak(DHCPpacket packet){
		return packet;
	}
	
	private static DHCPpacket prepareRelease(DHCPpacket packet){
		return packet;
	}
	
	private static DHCPpacket chooseAckNak(DHCPpacket packet){
		packet = prepareAck(packet);
		packet = prepareNak(packet);
		return packet;
	}
	
	private static void send(DHCPpacket packet) throws IOException{
		socket.send(packet);
	}
}

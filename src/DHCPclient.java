import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.lang.NullPointerException;
/**
 * @author Wouter
 *
 */
public class DHCPclient {
	
	//Servername or IP-adress with formatting: "127.0.0.1"
	static String serverHostname = new String("10.33.14.246");
	static InetAddress IPAddress = null;
	static {
			try {
				IPAddress = InetAddress.getByName(serverHostname);
			} catch (UnknownHostException e) {}
	}
	static Integer serverPort = new Integer(1234);
    static final long TIMEOUT = new Integer(10000);
    static final int REQUESTED_IP = 50;
    static final int LEASETIME = 51;
    static final int MESSAGE_TYPE = 53;
    static final int T1 = 58;
    static final int T2 = 59;
	static String MACadr = new String("18:19:D2:66:52:47");
	static byte [] MACadr_bytes = bytesFromMAC(MACadr);
	
	@SuppressWarnings("unused")
	public static void main(String[] args) throws Exception {
		System.out.println("Ulle moe is ulle va");
		//Creating the socket
		DHCPsocket clientSocket = new DHCPsocket();
		System.out.println("Connecting to " + IPAddress);
		try {           
            //Initialize the DHCPpacket that will be used.
            DHCPpacket processingPacket = new DHCPpacket(IPAddress, serverPort);
            //Start main loop
            mainloop:
            while (true) {
            	Thread.sleep((long) 5000); 
            	//Preparing discover message.
            	processingPacket = prepareDiscover(processingPacket);
            	//Send message and store answer.
            	processingPacket = sendReceive(processingPacket, clientSocket, TIMEOUT);
            	System.out.println("mainloop");
            	int answerCode = (int)(processingPacket.getOptionsList().getOption(MESSAGE_TYPE))[0];                //Verification if OFFER-packet was returned.
            	String packetType = DHCPpacket.packetCode(answerCode);
            	System.out.println("Received a " + packetType);
            	if (answerCode != (int) DHCPpacket.packetCode("OFFER")){
            		System.out.println("Got wrong type of answer, restarting.");
            		processingPacket = new DHCPpacket(IPAddress, serverPort);
            		continue mainloop;
	            }
            	//Prints zijn kopie!!
            	System.out.print("Received a DHCPOFFER for ");
            	System.out.println(stringFromByte(processingPacket.getYiaddr()));
            	//Prepare request package.
            	processingPacket = prepareRequest(processingPacket);
            	//Send message and store answer.
            	processingPacket = sendReceive(processingPacket, clientSocket, TIMEOUT);
            	subloop:
            	while (true){
            		//Verification if ACK, NAK, or DECLINE package was returned.
	                answerCode = (int)(processingPacket.getOptionsList().getOption(MESSAGE_TYPE))[0];
	                packetType = DHCPpacket.packetCode(answerCode);
	                System.out.println("Received a " + packetType);
	                if (answerCode == (int) DHCPpacket.packetCode("NAK")){
	                    System.out.println("Got 'NAK' answer, sending new 'DISCOVER' message.");
	                    processingPacket = new DHCPpacket(IPAddress, serverPort);
	                    continue mainloop;
	                } else if (answerCode == (int) DHCPpacket.packetCode("ACK")){
	                	//Start timing for leasetime.
	                    System.out.println("Got 'ACK' answer.");
	                    System.out.println("IP: " + stringFromByte(processingPacket.getYiaddr()));
	                    //Getting leasetime and renewal time T1:
	                    byte[] t1 = new byte[4];
	                    byte[] leasetime = new byte[4];
	                    t1 = processingPacket.getOptionsList().getOption(T1);
	                    leasetime = processingPacket.getOptionsList().getOption(LEASETIME);
	                    Long leasetime_long =  null;
	                    Long t1_long =  null;		   
	                    //Verification if leasetime is valid.
	                    try {
	                    	leasetime_long = longFromByte(leasetime);
	                    } catch (NullPointerException ex) {
	                    	System.out.println("No leasetime given. Shutting down.");
	                        break mainloop;
	                    }
	                    //Verification if t1 is valid.
	                    try {
	                    	t1_long = longFromByte(t1);
	                    } catch (NullPointerException ex) {
	                    	t1_long = leasetime_long/2;
	                    }
	                    if (t1_long>leasetime_long){
	                    	t1_long = leasetime_long/2;
	                    }
	                    System.out.println("Sleeping for renewal time: " + t1_long.toString());
	                    Thread.sleep(t1_long*1000);
	                    //Thread.sleep((long) 11000);
	                    System.out.println("Renewal time T1 is over.");
                    	processingPacket = prepareRenew(processingPacket);
                    	try {
                    	processingPacket = sendReceive(processingPacket, clientSocket, 1000*leasetime_long-1000*t1_long);
                        } catch (TimeoutException ex) {
                        	// Leave group and close client
                    	    processingPacket = prepareRelease(processingPacket);
                    	    processingPacket = sendReceive(processingPacket, clientSocket, leasetime_long);
                    	    clientSocket.close();
                    	    System.err.println("Leasetime expired, connection not renewed.");
                    	    break mainloop;
                        }
	                }   
            	}         			
            }
		} catch (IOException ex) {
			System.err.println(ex);
		} catch (TimeoutException ex) {
			System.err.println(ex);
		}
	}
    
    private static DHCPpacket prepareDiscover(DHCPpacket packet){
        //DHCPrequest opcode
    	packet.setOp((byte) 1);
	    packet.setHtype((byte) 1);
	    packet.setHlen((byte) 6);
	    packet.setHops((byte) 0);
        //Getting a random int for the Xid
	    byte Xid[] = new byte[4];
	    new Random().nextBytes(Xid);
	    packet.setXid(Xid);
	    byte[] secs = {(byte) 0, (byte) 0};
	    byte[] flags = {(byte) 0, (byte) 0};
	    packet.setSecs(secs);
	    packet.setFlags(flags);
	    packet.setChaddr(MACadr_bytes);
	    byte[] option = new byte[1];
        //set messagetype in options to 'discover'
	    option[0] = (byte) DHCPpacket.packetCode("DISCOVER");
	    packet.getOptionsList().updateOption(MESSAGE_TYPE, option);
    	return packet;
    }
	
    private static DHCPpacket prepareRequest(DHCPpacket packet){
        //DHCPrequest opcode
        packet.setOp((byte) 1);
        //set messagetype in options to 'request'
	    byte [] option = new byte[1];
	    option[0] = (byte) DHCPpacket.packetCode("REQUEST");
	    packet.getOptionsList().updateOption(MESSAGE_TYPE, option);
        //get the ip-address from the packet and request it
	    packet.getOptionsList().updateOption(REQUESTED_IP, packet.getYiaddr());
	    return packet;
    }
    
    private static DHCPpacket prepareRenew(DHCPpacket packet){
    	packet.setOp((byte) 1);  // setup message to send a DCHPREQUEST
	    byte [] opt = new byte[1];
	    opt[0] = (byte) DHCPpacket.packetCode("REQUEST");
	    packet.getOptionsList().updateOption(MESSAGE_TYPE, opt); // change message type
	    // must set ciaddr
	    packet.setCiaddr(packet.getYiaddr()); 
    	return packet;
    }

    private static DHCPpacket prepareRelease(DHCPpacket packet){
        //DHCPrequest opcode
        packet.setOp((byte) 1);
        //set messagetype in options to 'release'
        byte [] option = new byte[1];
        option[0] = (byte) DHCPpacket.packetCode("RELEASE");
        packet.getOptionsList().updateOption(MESSAGE_TYPE, option);
        return packet;
    }

    private static DHCPpacket sendReceive(DHCPpacket packet, DHCPsocket clientSocket, long time) throws TimeoutException, IOException {
        //Send the DHCPpacket and print the messagetype you're sending.
        clientSocket.send(packet);
        //create a DHCPpacket container for the answer
        DHCPpacket resultPacket = new DHCPpacket(IPAddress, serverPort);
        //get and print the messagetype
        int code = (int) (packet.getOptionsList().getOption(MESSAGE_TYPE))[0];
        String packetType = DHCPpacket.packetCode(code);
        System.out.println("Sending a " + packetType + " packet and waiting for an answer.");
        //Listen for an answer.
        boolean answer = false;
        Instant previous, current;
        previous = Instant.now();
        while (answer == false){
            current = Instant.now();
            if (previous == null || (ChronoUnit.MILLIS.between(previous,current)>time)) {
               throw new TimeoutException();
            }
            if (clientSocket.receive(resultPacket)){
                System.out.println("Received packet");
		answer = true;
                if  (Arrays.equals(MACadr_bytes,resultPacket.getChaddr())){
                	answer = true;
                }
            } else {
                clientSocket.send(packet);
            }
        }
        return resultPacket;
    }

    // Convert a byte[] to a string
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
}

/**
 * 
 */
import java.net.*;
import java.util.Arrays;
import com.google.common.collect.*;

/**
 * @author Wouter, Gilles
 *
 */
public class DHCPserver {
	private static String ipBase = "224.0.0.";
	private static int ipOffset = 100;
	private BiMap<Integer, String> connections = HashBiMap.create();
	private static int MAX_CONNECTIONS = 20;
	
	
	public static void main(String args[]) throws Exception {
		try {
			
			//Creating UDP socket
			MulticastSocket serverSocket = new MulticastSocket(9876);
			
			//starting never ending loop listening for packages
			while (true) {
				//Containers for data
				byte[] receiveData = new byte[1024];
				byte[] sendData = new byte[1024];
				
				//creating datagram for received data
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

				//Listening for any client to send a datagram
				System.out.println("Ready to receive datagram packet");
				serverSocket.receive(receivePacket);
				
				//getting data from the datagram
				String sentence = new String(receivePacket.getData());
				
				//getting information on client who sent the datagram
				InetAddress IPAddress = receivePacket.getAddress();
				int port = receivePacket.getPort();

				System.out.println("From: " + IPAddress + ":" + port);
				System.out.println("Message: ");
				System.out.println("--------");
				System.out.println(sentence);
				System.out.println("--------");
				
				//Close server if sentence was 'Exit' and warn client of its termination.
				byte[] exitRef = Arrays.copyOfRange(receivePacket.getData(), 0, 4);
				String reference = new String(exitRef, "UTF-8");
				if (reference.equals("Exit")){
					System.out.println("Closing the server.");
					serverSocket.close();
					String answer = "Server will now terminate.";
					sendData = answer.getBytes();
					//Creating datagram to send
					DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
					serverSocket.send(sendPacket);
					//Making sure client has time to receive the answer.
					serverSocket.setSoTimeout(10000);
					//Closing server
					serverSocket.close();
					break;
				// Else send back a capitalized version of the string.
				} else {
						//Sending back a capitalized version of the string
						String capitalizedSentence = sentence.toUpperCase();
						sendData = capitalizedSentence.getBytes();
						//Creating datagram to send
						DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
						serverSocket.send(sendPacket);
				}
			}

		} catch (SocketException ex) {
			System.out.println("UDP Port 9876 is occupied.");
			System.exit(1);
		}
	}

	private void setConnection(int nr, String mac){
		connections.put(nr, mac);
	}

	private String getConnectionChaddr(int nr){
		return connections.get(nr);
	}
	
	private int getConnectionIp(String mac){
		return connections.inverse().get(mac);
	}

	private int assignIpNumber(String mac){
		int ip = ipOffset;
		boolean available = false;
		while (available == false){
			if (connections.containsKey(ip)){
				ip++;
				if (ip > ipOffset + MAX_CONNECTIONS - 1){
					return -1;
				}
			} else {
				available = true;
			}
		}
		return ip;
	}
	
	private String parseIp(int ipNumber){
		String ip = ipBase + Integer.toString(ipNumber);
		return ip;
	}








}



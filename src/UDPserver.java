/**
 * 
 */
import java.net.*;
import java.util.Arrays;

/**
 * @author Wouter
 *
 */
public class UDPserver {
	public static void main(String args[]) throws Exception {
		try {
			
			//Creating UDP socket
			DatagramSocket serverSocket = new DatagramSocket(5555);
			
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
			System.out.println("UDP Port 6789 is occupied.");
			System.exit(1);
		}

	}
}

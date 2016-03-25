/**
 * 
 */
import java.io.*;
import java.net.*;

/**
 * @author Wouter
 *
 */
public class UDPclient {
	public static void main(String args[]) throws Exception {
		try {
			//Servername or IP-adress with formatting: "127.0.0.1"
			String serverHostname = new String("localhost");

			if (args.length > 0){
				serverHostname = args[0];
			}
			
			//Stream for input
			BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
			
			//Creating the socket
			DatagramSocket clientSocket = new DatagramSocket();

			//DNS to get ip from server
			InetAddress IPAddress = InetAddress.getByName(serverHostname);
			System.out.println("Connecting to " + IPAddress);
			
			//containers for data to transmit
			byte[] sendData = new byte[1024];
			byte[] receiveData = new byte[1024];

			//asking for data to send
			System.out.print("Enter message: ");
			String sentence = inFromUser.readLine();
			sendData = sentence.getBytes();
			
			//sending data using a datagram
			System.out.println("Sending data to the server.");
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 5555);

			clientSocket.send(sendPacket);
			
			//creating datagram for receiving data
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			
			//Waiting for server trying to send
			clientSocket.setSoTimeout(5000);
			
			//Receive server response if any has been sent
			try {
				clientSocket.receive(receivePacket);
				String answer = new String(receivePacket.getData());

				InetAddress returnIPAddress = receivePacket.getAddress();
				int port = receivePacket.getPort();

				System.out.println("From server at: " + returnIPAddress + ":" + port);
				System.out.println("Message: " + answer);

			} catch (SocketTimeoutException ste) {
				System.out.println("No package found");
			}

			clientSocket.close();
		} catch (UnknownHostException ex) {
			System.err.println(ex);
		} catch (IOException ex) {
			System.err.println(ex);
		}
	}
}

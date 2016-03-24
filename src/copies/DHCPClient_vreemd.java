package copies;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
 
public class DHCPClient {
	private static final int MAX_BUFFER_SIZE = 1024; // 1024 bytes
	private int listenPort =  68;//1338;
	private String serverIP = "127.0.0.1";
	private int serverPort =  67;//1337;

	/*
	 * public DHCPClient(int servePort) { listenPort = servePort; new
	 * DHCPServer(); }
	 */
 
	public DHCPClient() {
		System.out.println("Connecting to DHCPServer at " + serverIP + " on port " + serverPort + "...");
 
		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket(listenPort);  // ipaddress? throws socket exception

			byte[] payload = new byte[MAX_BUFFER_SIZE];
			int length = 6;
			payload[0] = 'h';
			payload[1] = '3';
			payload[2] = 'l';
			payload[3] = 'l';
			payload[4] = 'o';
			payload[5] = '!';
			DatagramPacket p = new DatagramPacket(payload, length, InetAddress.getByName(serverIP), serverPort);
			socket.send(p); //throws i/o exception
			socket.send(p);
			System.out.println("Connection Established Successfully!");
			System.out.println("Sending data: " + Arrays.toString(p.getData()));
			
 
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 
	}
 
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DHCPClient client;
		/*
		 * if (args.length >= 1) { server = new
		 * DHCPClient(Integer.parseInt(args[0])); } else {
		 */
		client = new DHCPClient();
		//DHCPMessage msgTest = new DHCPMessage();
		printMacAddress();
		// }

	}
	
	public static byte[] getMacAddress() {
		byte[] mac = null;
		try {
			InetAddress address = InetAddress.getLocalHost();
 
			/*
			 * Get NetworkInterface for the current host and then read the
			 * hardware address.
			 */
			NetworkInterface ni = NetworkInterface.getByInetAddress(address);
			mac = ni.getHardwareAddress();
 
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		assert(mac != null);
		return mac;
	}
	
	public static void printMacAddress() {
		try {
			InetAddress address = InetAddress.getLocalHost();
 
			/*
			 * Get NetworkInterface for the current host and then read the
			 * hardware address.
			 */
			NetworkInterface ni = NetworkInterface.getByInetAddress(address);
			byte[] mac = ni.getHardwareAddress();
 
			/*
			 * Extract each array of mac address and convert it to hexa with the
			 * . * following format 08-00-27-DC-4A-9E.
			 */
			for (int i = 0; i < mac.length; i++) {
				System.out.format("%02X%s", mac[i], (i < mac.length - 1) ? "-"
						: "");
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
 
}
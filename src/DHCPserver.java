/**
 * 
 */
import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

import com.google.common.collect.*;

/**
 * @author Wouter, Gilles
 *
 */
public class DHCPserver {
	private static String ipBase = "127.0.0.";
	private static int ipOffset = 101;
	private BiMap<Integer, String> connections = HashBiMap.create();
	private static int MAX_CONNECTIONS = 20;
	
	public static void main(String[] args) throws Exception {
		//Creating the socket
		DHCPsocket serverSocket = new DHCPsocket();
		serverSocket.bind(new InetSocketAddress("127.0.0.1",1234));
		
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



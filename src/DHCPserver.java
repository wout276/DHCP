/**
 * 
 */
import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
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
	private ThreadPoolExecutor threadPool;
	
	public static void main(String[] args) throws Exception {
		DHCPserver server = new DHCPserver();
		server.run();
	}
	
	private void run(){
		//Creating the socket
		DHCPsocket serverSocket = null;
		try {
			serverSocket = new DHCPsocket();
		} catch (IOException e) {}
		try {
			serverSocket.bind(new InetSocketAddress("127.0.0.1",1234));
		} catch (SocketException e) {}
				
		//Starting a thread pool
		this.threadPool = new ThreadPoolExecutor(2, 5, 10000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(5), new serverThread());
		this.threadPool.prestartAllCoreThreads();
		
		while(true){
			DHCPpacket receiver = new DHCPpacket();
			serverSocket.receive(receiver);
			DHCPclienthandler handler = new DHCPclienthandler(this, receiver, serverSocket);
			this.threadPool.execute(handler);
		}
	}
	
	private class serverThread implements ThreadFactory {

		/* (non-Javadoc)
		 * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
		 */
		@Override
		public Thread newThread(Runnable r) {
			return new Thread(r);
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



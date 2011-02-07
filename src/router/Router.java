package router;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.*;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;



import common.Constants;

public class Router {
	private static Logger logger = null;
	
	/*
	 * link status table
	 * key: destination node id
	 * 			0-4 - client nodes
	 * 			  5 - file server 
	 * value: link status
	 * 			0 - good
	 * 			1 - hold
	 * 			2 - drop  
	*/
	static Hashtable<Integer, Integer> link_status = new Hashtable<Integer, Integer>();
	
	// mapping between node id and it's IP address
    static Hashtable<Integer, String> routing_table = new Hashtable<Integer, String>();	
    
    static Hashtable<Integer, LinkedList<String>> hold_queue = new Hashtable<Integer, LinkedList<String>>();
    
    static DatagramSocket serverSocket;
    private static int destination_port;
	
    public Router(String initial_ip_addresses) throws IOException {
    	logger = Logger.getLogger(Router.class);
		PropertyConfigurator.configure(Constants.LOG_CONFIG);
		
		int router_port = 9999;
		int client_port= 9998;
		int fs_port = 9997;
		
		boolean cont = true;
		
		link_status.put(0, 0);
		link_status.put(1, 0);
		link_status.put(2, 0);
		link_status.put(3, 0);
		link_status.put(4, 0);
		link_status.put(5, 0);
		
		String[] ipAddresses = initial_ip_addresses.split(Constants.FIELD_SEPARATOR+"");
				
//	    routing_table.put(0, "127.0.0.1");
//	    routing_table.put(1, "127.0.0.1");
//	    routing_table.put(2, "127.0.0.1");
//	    routing_table.put(3, "127.0.0.1");
//	    routing_table.put(4, "127.0.0.1");
//	    routing_table.put(5, "127.0.0.1");
	    
	    routing_table.put(0, ipAddresses[0]);
	    routing_table.put(1, ipAddresses[1]);
	    routing_table.put(2, ipAddresses[2]);
	    routing_table.put(3, ipAddresses[3]);
	    routing_table.put(4, ipAddresses[4]);
	    routing_table.put(5, ipAddresses[5]);
		
	    hold_queue.put(0, new LinkedList<String>());
	    hold_queue.put(1, new LinkedList<String>());
	    hold_queue.put(2, new LinkedList<String>());
	    hold_queue.put(3, new LinkedList<String>());
	    hold_queue.put(4, new LinkedList<String>());
	    hold_queue.put(5, new LinkedList<String>());
	    
		logger.info("Starting up the router");
		
		RouterUI routerUI = new RouterUI(ipAddresses);
		routerUI.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		routerUI.setSize(500, 350);
		routerUI.setVisible(true);
		
		serverSocket = new DatagramSocket(router_port);
		
		while(cont) {
	    	//create an array of bytes
	    	byte[] receiveData = new byte[1024];
	    	String payload;
	    	
	    	//prepare a place for incoming packet
	    	DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
	    	
	    	//receive a packet and put it in receivePacket
	    	serverSocket.receive(receivePacket);
	    	
	    	//payload is a string in the form of two integers followed by the data (we can assume that the integer is between 0 and 9) separated by colon	    	
	    	payload = new String(receivePacket.getData(), 0, receivePacket.getLength());
	    	String[] payloadParts = payload.split(Constants.FIELD_SEPARATOR+"");	    	
	    	
	    	int destination_id = Integer.valueOf(payloadParts[1]);
	    	
	    	String data = payloadParts[2];
	    	
	    	int destination_status = link_status.get(destination_id);	    	    	
	    	
	    	if (destination_id == 5) {
	    		destination_port = fs_port;
	    	} else {
	    		destination_port = client_port;
	    	}

	    	logger.info("Received packet to: " + destination_id + " with data: " + data);
	    	
	    	if (destination_status == common.Constants.LINK_GOOD) {
	    		
	    		forwardPacket(destination_id, payload);
	    		
	    	} else if (destination_status == common.Constants.LINK_HOLD) {
	    		
	    		enqueuePacket(destination_id, payload);
	    		
	    	} else if (destination_status == common.Constants.LINK_DROP) {
	    		
	    		logger.info("Dropping packet to: " + destination_id);
	    		
	    	}
	    		    	
 		}
    }
    
	public static void forwardPacket(int destination_id, String payload) throws IOException {
	    String target=destination_id +" -> " + routing_table.get(destination_id);
	    logger.info("Forwarding packet to: " + target);
    	byte[] sendData = new byte[1024];
    	sendData = payload.getBytes();		    		        
        
        //get destination IP address
        InetAddress destination_ip = InetAddress.getByName((String)routing_table.get(destination_id));
        
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, destination_ip, destination_port);
        
        serverSocket.send(sendPacket);
	}
	
	public void enqueuePacket(int destination_id, String payload) {
		String target=destination_id +" -> " + routing_table.get(destination_id);
		logger.info("Holding packet to: " + target);
		
		hold_queue.get(destination_id).add(payload);
		debugQueue();
	}
	
	public static void releaseHold(int destination_id) throws IOException {
		link_status.put(destination_id, 0);
		
		ListIterator<String> iter = hold_queue.get(destination_id).listIterator(0);
		
		while(iter.hasNext()) {
			String payload = iter.next();
			forwardPacket(destination_id, payload);			
		}
		
		hold_queue.get(destination_id).clear();
	}
	
	public static void changeLinkStatus(int destination_id, int new_link_status) throws IOException {
		String target = destination_id +" -> " + routing_table.get(destination_id);
		logger.info("Changing link " + target + " to " + new_link_status);
		link_status.put(destination_id, new_link_status);
		
		if (new_link_status == common.Constants.LINK_GOOD) {
			releaseHold(destination_id);
		}
	}
	
	public void debugQueue() {
		Enumeration<Integer> enu = hold_queue.keys();
		
		while(enu.hasMoreElements()) {
			int key = enu.nextElement();
			
			System.out.print("Destination: " + key + ";  ");
			
			ListIterator<String> iter = hold_queue.get(key).listIterator(0);
			
			while(iter.hasNext()) {
				String payload = iter.next();
				System.out.print(payload + ", ");
			}
			
			System.out.println("");
		}
	}
    
	public static void main(String args[]) throws IOException {
		new Router(args[0]);
	}
	
}

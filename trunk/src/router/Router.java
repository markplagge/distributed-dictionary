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
	 * value: link status
	 * 			0 - good
	 * 			1 - hold
	 * 			2 - drop  
	*/
	static Hashtable<Integer, Integer> link_status = new Hashtable<Integer, Integer>();
	
	// mapping between node id and it's IP address
    static Hashtable<Integer, String> routing_table = new Hashtable<Integer, String>();	
    
    static Hashtable<Integer, LinkedList<byte[]>> hold_queue = new Hashtable<Integer, LinkedList<byte[]>>();
    
    static DatagramSocket serverSocket;
    private static int destination_port;
	
    public Router(String initial_ip_addresses) throws IOException {
    	logger = Logger.getLogger(Router.class);
		PropertyConfigurator.configure(Constants.LOG_CONFIG);
		
		boolean cont = true;
		
		link_status.put(0, 0);
		link_status.put(1, 0);
		link_status.put(2, 0);
		link_status.put(3, 0);
		link_status.put(4, 0);
		link_status.put(5, 0);
		
		String[] ipAddresses = initial_ip_addresses.split(Constants.FIELD_SEPARATOR+"");
	    
		for(int nodeId=0;nodeId<Constants.NUM_OF_NODES;nodeId++){
			routing_table.put(nodeId, ipAddresses[nodeId]);
			hold_queue.put(nodeId, new LinkedList<byte[]>());
		}
	    
		logger.info("Starting up the router");
		
		RouterUI routerUI = new RouterUI(ipAddresses);
		routerUI.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		routerUI.setSize(500, 350);
		routerUI.setVisible(true);
		
		serverSocket = new DatagramSocket(Constants.DEFAULT_ROUTER_PORT);
		
		while(cont) {
	    	//create an array of bytes
	    	byte[] receiveData = new byte[Constants.MAX_MSG_SIZE];
	    	String payload;
	    	
	    	//prepare a place for incoming packet
	    	DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
	    	
	    	//receive a packet and put it in receivePacket
	    	serverSocket.receive(receivePacket);
	    	
	    	
	    	int destination_id=receiveData[1];
	    	
	    	
	    	int destination_status = link_status.get(destination_id);	    	    	
	    	
	    	destination_port = Constants.DEFAULT_CLIENT_PORT;
	    	

	    	logger.info("Received packet to: " + destination_id);
	    	
	    	if (destination_status == common.Constants.LINK_GOOD) {
	    		
	    		forwardPacket(destination_id, receiveData);
	    		
	    	} else if (destination_status == common.Constants.LINK_HOLD) {
	    		
	    		enqueuePacket(destination_id, receiveData);
	    		
	    	} else if (destination_status == common.Constants.LINK_DROP) {
	    		
	    		logger.info("Dropping packet to: " + destination_id);
	    		
	    	}
	    		    	
 		}
    }
    
	public static void forwardPacket(int destination_id, byte[] sendData) throws IOException {
	    String target=destination_id +" -> " + routing_table.get(destination_id);
	    logger.info("Forwarding packet to: " + target);
	    
	    //get destination IP address
        InetAddress destination_ip = InetAddress.getByName((String)routing_table.get(destination_id));
        
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, destination_ip, destination_port);
        
        serverSocket.send(sendPacket);
	}
	
	public void enqueuePacket(int destination_id, byte[] sendData) {
		String target=destination_id +" -> " + routing_table.get(destination_id);
		logger.info("Holding packet to: " + target);
		
		hold_queue.get(destination_id).add(sendData);
		debugQueue();
	}
	
	public static void releaseHold(int destination_id) throws IOException {
		link_status.put(destination_id, 0);
		
		ListIterator<byte[]> iter = hold_queue.get(destination_id).listIterator(0);
		
		while(iter.hasNext()) {
			byte[] sendData = iter.next();
			forwardPacket(destination_id, sendData);			
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
			
			ListIterator<byte[]> iter = hold_queue.get(key).listIterator(0);
			
			while(iter.hasNext()) {
				byte[] sendData = iter.next();				
			}
			
			System.out.println("");
		}
	}
    
	public static void main(String args[]) throws IOException {
		new Router(args[0]);
	}
	
}

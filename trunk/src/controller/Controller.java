package controller;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import common.Constants;

public class Controller {

	//Event log for this client
	private EventLog anEventLog;
	
	//Time table for this client
	private TimeTable aTimeTable;
	
	//Clock for this client
	private Clock aClock;
	
	//Local dictionary
	private Map< String, String > aDictionary;
	
	// ID of client connected to this Controller
	private int clientId;

	// Socket object to be used for communication
	private DatagramSocket clientSocket;

	//IP address of the router to which messages should be sent
	private String routerIp;

	//Port at which the router will listen for messages
	private int routerPort;

	
	// Sends messages to the router
	private Sender aSender;

	// Receives messages asynchronously from the router
	private Receiver aReceiver;

	
	// log4j Logger
	private static Logger logger = null;

	public Controller(int clientId){
		this.aClock=new Clock();
		this.anEventLog=new EventLog();
		this.aTimeTable=new TimeTable(clientId);
		this.aDictionary=new HashMap<String, String>();
	}
	
	public Controller(String routerIp, int routerPort, int clientId,
			DatagramSocket clientSocket) throws SocketException, UnknownHostException {
		
		this(clientId);
		
		
		this.setClientId(clientId);
		this.setClientSocket(clientSocket);
		this.setRouterIp(routerIp);
		this.setRouterPort(routerPort);
		
		PropertyConfigurator.configure(Constants.LOG_CONFIG);
		logger = Logger.getLogger(Controller.class);

		// Starting thread to send data to router
		this.aSender = new Sender(clientSocket, common.Constants.MAX_MSG_SIZE,
				this.getRouterIp(), this.getRouterPort());

		// Starting thread to receive data from other clients / router
		this.aReceiver = new Receiver(clientSocket,
				common.Constants.MAX_MSG_SIZE);
	}

	private int getRouterPort() {
		return this.routerPort;
	}
	
	private void setRouterPort(int routerPort) {
		this.routerPort = routerPort;
	}

	private String getRouterIp() {		
		return this.routerIp;
	}
	
	private void setRouterIp(String routerIp) {
		this.routerIp = routerIp;
	}
	

	private int getClientId() {
		return clientId;
	}

	private void setClientId(int clientId) {
		this.clientId = clientId;
	}

	private DatagramSocket getClientSocket() {
		return clientSocket;
	}
	
	private void setClientSocket(DatagramSocket clientSocket) {
		this.clientSocket = clientSocket;
	}
	
	//TODO: Implement this
	private synchronized Message generateMessage(int destinationId){
		return null;
	}
	
	public synchronized boolean sendMessage(int destinationId){
		Message generatedMsg=this.generateMessage(destinationId);
		return this.aSender.queueMessage(generatedMsg);
	}
	
	private synchronized void insert(String key, String value){
		int newClockValue = aClock.getClock();
		this.aTimeTable.updateLocalEntry(newClockValue);
		EventRecord record = new EventRecord(key, value, Constants.EventType.INSERT, newClockValue, this.clientId);
		this.anEventLog.addRecord(record);
		this.aDictionary.put(key, value);
	}
	
	public synchronized void insert(String keyValueStr){
		String[] keyValue=keyValueStr.split(""+Constants.Commands.KEY_VALUE_SEPARATOR);
		this.insert(keyValue[0], keyValue[1]);
	}
	
	public synchronized void delete(String key){
		int newClockValue = aClock.getClock();
		this.aTimeTable.updateLocalEntry(newClockValue);
		EventRecord record = new EventRecord(key, this.aDictionary.get(key), Constants.EventType.DELETE, newClockValue, this.clientId);
		this.anEventLog.addRecord(record);
		this.aDictionary.remove(key);
	}
	
	public synchronized String getDictionary() {
		return this.aDictionary.toString();
	}

}

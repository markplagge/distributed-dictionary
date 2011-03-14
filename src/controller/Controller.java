package controller;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import common.Constants;
import common.Constants.EventType;

public class Controller {

	// Event log for this client
	private volatile EventLog anEventLog;

	// Time table for this client
	private TimeTable aTimeTable;

	// Clock for this client
	private Clock aClock;

	// Local dictionary
	private Map<String, String> aDictionary;

	// ID of client connected to this Controller
	private int clientId;

	// Socket object to be used for communication
	private DatagramSocket clientSocket;

	// IP address of the router to which messages should be sent
	private String routerIp;

	// Port at which the router will listen for messages
	private int routerPort;

	// Sends messages to the router
	private Sender aSender;

	// Receives messages asynchronously from the router
	private Receiver aReceiver;

	// log4j Logger
	private static Logger logger = null;

	public Controller(int clientId) {
		this.aClock = new Clock();
		this.anEventLog = new EventLog();
		this.aTimeTable = new TimeTable(clientId);
		this.aDictionary = new HashMap<String, String>();
	}

	public Controller(String routerIp, int routerPort, int clientId,
			DatagramSocket clientSocket) throws SocketException,
			UnknownHostException {

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
				common.Constants.MAX_MSG_SIZE, this);
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

	private synchronized Message generateMessage(int destinationId) {

		EventLog newPartialLog = new EventLog();

		List<EventRecord> newList = this.anEventLog.getAllRecords();

		for (EventRecord eR : newList) {
			if (!this.aTimeTable.hasrec(eR, destinationId)) {
				newPartialLog.addRecord(eR);
			}
		}

		Message aMsg = new Message(newPartialLog, this.aTimeTable,
				this.getClientId(), destinationId);
		return aMsg;
	}

	public synchronized void insert(String key, String value) {
		this.aDictionary.put(key, value);
	}

	private synchronized void insertAndLog(String key, String value) {
		if (!this.aDictionary.containsKey(key)) {
			int newClockValue = aClock.getClock();
			this.aTimeTable.updateLocalEntry(newClockValue);
			EventRecord record = new EventRecord(key, value,
					Constants.EventType.INSERT, newClockValue, this.clientId);
			this.anEventLog.addRecord(record);
			this.insert(key, value);
			this.logger.info("Inserted : {" + key
					+ Constants.Commands.KEY_VALUE_SEPARATOR + value
					+ "} at time : " + newClockValue);
		} else {
			this.logger.info("Cannot insert : {" + key
					+ Constants.Commands.KEY_VALUE_SEPARATOR + value
					+ "} as key already exists");

		}

	}

	public synchronized void insertAndLog(String keyValueStr) {
		String[] keyValue = keyValueStr.split(""
				+ Constants.Commands.KEY_VALUE_SEPARATOR);

		this.insertAndLog(keyValue[0], keyValue[1]);

	}

	public synchronized String delete(String key) {
		return this.aDictionary.remove(key);
	}

	public synchronized void deleteAndLog(String key) {

		if (aDictionary.containsKey(key)) {
			int newClockValue = aClock.getClock();
			this.aTimeTable.updateLocalEntry(newClockValue);
			EventRecord record = new EventRecord(key,
					this.aDictionary.get(key), Constants.EventType.DELETE,
					newClockValue, this.clientId);
			this.anEventLog.addRecord(record);
			String deletedValue = this.delete(key);

			this.logger.info("Deleted : {" + key
					+ Constants.Commands.KEY_VALUE_SEPARATOR + deletedValue
					+ "} at time : " + newClockValue);
		} else {
			this.logger.info("Cannot delete key : {" + key
					+ Constants.Commands.KEY_VALUE_SEPARATOR
					+ "} as it doesnt exist");
		}
	}

	public synchronized String getDictionary() {
		return this.aDictionary.toString();
	}

	private synchronized void filterDictionary(EventLog filteredLog) {
		List<EventRecord> deleteRecords = filteredLog
				.getRecords(Constants.EventType.DELETE);
		for (EventRecord eR : filteredLog.getAllRecords()) {
			String key = eR.getKey();

			if (this.aDictionary.containsKey(key) && deleteRecords.contains(eR)) {
				this.delete(key);
			}

			else if (eR.getEventType() == EventType.INSERT
					&& !(deleteRecords.contains(eR))) {
				this.insert(key, eR.getValue());
			}
		}
	}

	private EventLog filterReceivedLog(EventLog receivedLog) {
		EventLog filteredLog = new EventLog();
		for (EventRecord eR : receivedLog.getAllRecords()) {
			if (!this.aTimeTable.hasrec(eR, this.getClientId())) {
				filteredLog.addRecord(eR);
			}
		}

		return filteredLog;
	}

	public synchronized void receiveMessage(Message newMessage) {
		EventLog receivedLog = newMessage.getEvenLog();
		TimeTable receivedTT = newMessage.getTimeTable();
		int receivedFrom = newMessage.getSourceId();

		EventLog filteredLog = this.filterReceivedLog(receivedLog);
		this.filterDictionary(filteredLog);

		this.aTimeTable.update(receivedTT, receivedFrom);
		this.updateCurrentLog(filteredLog);

		logger.info("Update successful");
		logger.info("New partial log : \n\n" + this.anEventLog.toString());
		logger.info("New time table : \n\n" + this.aTimeTable.toString());
		logger.info("New dictionary : \n\n" + this.aDictionary.toString());
	}

	private void updateCurrentLog(EventLog filteredLog) {

		EventLog newLog = new EventLog();

		for (EventRecord eR : this.anEventLog.getAllRecords()) {
			for (int nodeId = 0; nodeId < Constants.NUM_OF_NODES; nodeId++) {
				if (!newLog.contains(eR) && !this.aTimeTable.hasrec(eR, nodeId)) {
					newLog.addRecord(eR);
				}
			}
		}

		for (EventRecord eR : filteredLog.getAllRecords()) {
			for (int nodeId = 0; nodeId < Constants.NUM_OF_NODES; nodeId++) {
				if (!newLog.contains(eR) && !this.aTimeTable.hasrec(eR, nodeId)) {
					newLog.addRecord(eR);
				}
			}
		}

		this.anEventLog = newLog;

	}

	public synchronized boolean sendMessage(int destinationId) {
		Message generatedMsg = this.generateMessage(destinationId);
		return this.aSender.queueMessage(generatedMsg);
	}
	
	
	public synchronized String getTimeTableString(){
		return this.aTimeTable.toString();
	}
	
	
	public synchronized String getDictionaryString(){
		return this.aDictionary.toString();
	}
	
	public synchronized String getValue(String key){
		return this.aDictionary.get(key);
	}

	public String getLogString() {
		return this.anEventLog.toString();
	}
}

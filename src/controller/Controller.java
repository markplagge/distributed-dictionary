package controller;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import common.Constants;

public class Controller {

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

	public Controller(String routerIp, int routerPort, int clientId,
			DatagramSocket clientSocket) throws SocketException, UnknownHostException {
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
	
	public boolean sendMessage(Message aMessage){
		return this.aSender.queueMessage(aMessage);
	}

}

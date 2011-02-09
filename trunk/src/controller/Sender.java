package controller;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import common.Constants;

/*
 * Thread to send messages to the router
 */
class Sender implements Runnable {

	// Sender thread
	private Thread aThread;

	// router's port to which the client sends messages
	private int routerPort;

	// queue from which messages are dispatched to the router
	private Queue<Message> messages;

	// socket to send messages to the router
	private DatagramSocket socket;

	// max message size permissible for every message sent
	private int messageMaxSize;

	// ip address of router
	private InetAddress routerIp;

	private static Logger logger = null;

	public Sender(DatagramSocket socket, int messageMaxSize, String routerIp,
			int routerPort) throws SocketException, UnknownHostException {
		Sender.logger = Logger.getLogger(Controller.class);
		PropertyConfigurator.configure(Constants.LOG_CONFIG);
		this.messageMaxSize = messageMaxSize;
		this.socket = socket;
		this.routerIp = InetAddress.getByName(routerIp);
		this.routerPort = routerPort;
		this.messages = new LinkedList<Message>();
		this.aThread = new Thread(this);
		this.aThread.setName("SenderThread");
		this.aThread.start();

	}

	// queues a message into the message queue
	public boolean queueMessage(Message msg) {

		// sychronized block to prevent concurrent read/write to the queue
		synchronized (messages) {
			logger.info("Queued message to \"" + msg.getDestinationId() + "\"");
			this.messages.add(msg);
		}
		return true;
	}

	@Override
	public void run() {
		logger.info("Sender thread started");
		while (true) {
			Message aMsg = null;

			// sychronized block to prevent concurrent read/write to the queue
			synchronized (messages) {
				if (!messages.isEmpty()) {
					aMsg = messages.remove();
				}
			}

			// is there a message to be sent ?
			if (aMsg != null) {
				try {
					sendMessage(aMsg);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}

	}

	private void sendMessage(Message aMessage) throws IOException {
		// convert message to array of bytes
		byte[] dataToBeSent = aMessage.serialize();

		// prepare a place for the packet to be sent
		DatagramPacket aPacket = new DatagramPacket(dataToBeSent,
				dataToBeSent.length, this.routerIp, this.routerPort);

		// sends packet to the router
		this.socket.send(aPacket);

		logger.info("Sent message to router -> \""
				+ aMessage + "\"");
	}

}

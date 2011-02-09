package controller;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import client.Client;

import common.Constants;


class Receiver implements Runnable {

	// max message size
	private int messageMaxSize;	

	// socket to receive messages
	private DatagramSocket socket;

	// Receiver thread
	private Thread aThread;
	
	private static Logger logger = null;	
	
	public Receiver(DatagramSocket socket, int messageMaxSize)
			throws SocketException {
		Receiver.logger = Logger.getLogger(Controller.class);
		PropertyConfigurator.configure(Constants.LOG_CONFIG);
		
		
		this.messageMaxSize = messageMaxSize;
		this.socket = socket;
		this.aThread = new Thread(this);
		this.aThread.setName("ReceiverThread");
		this.aThread.start();

	}

	@Override
	public void run() {
		logger.info("Receiver thread started");
		
		// create an array of bytes to receive message
		byte[] dataToBeReceived = new byte[this.messageMaxSize];

		while (true) {
			
			
			// prepare a place for incoming packet
			DatagramPacket aPacket = new DatagramPacket(dataToBeReceived,
					dataToBeReceived.length);

			// receive a packet
			try {
				socket.receive(aPacket);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			Message receivedMessage = new Message(aPacket.getData());
			logger.info("Received from the router : "+receivedMessage);
		}

	}
	
}
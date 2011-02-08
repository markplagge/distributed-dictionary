
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import common.Constants;


class Receiver implements Runnable {

	// max message size
	private int messageMaxSize;	

	// socket to receive messages
	private DatagramSocket socket;

	// Receiver thread
	private Thread t;
	
	private static Logger logger = null;	
	
	public Receiver(DatagramSocket socket, int messageMaxSize)
			throws SocketException {
		logger = Logger.getLogger(Client.class);
		PropertyConfigurator.configure(Constants.LOG_CONFIG);
		this.messageMaxSize = messageMaxSize;
		this.socket = socket;
		this.t = new Thread(this);
		this.t.setName("ReceiverThread");
		this.t.start();

	}

	@Override
	public void run() {
		logger.info("Receiver thread started");
		while (true) {
			// create an array of bytes to receive message
			byte[] dataToBeReceived = new byte[this.messageMaxSize];

			// prepare a place for incoming packet
			DatagramPacket aPacket = new DatagramPacket(dataToBeReceived,
					dataToBeReceived.length);

			// receive a packet
			try {
				socket.receive(aPacket);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			String payload=new String(dataToBeReceived);
			String[] payloadParts=payload.split(Constants.FIELD_SEPARATOR+"");
			logger.info("Received from client "+payloadParts[0]+" -> \"" + payloadParts[2]+"\"");
		}

	}
}
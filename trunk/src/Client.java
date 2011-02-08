

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramSocket;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import common.Constants;

import exceptions.InvalidMessageException;

public class Client {
	private static final String APPEND_CMD = "5";
	private static final Object QUIT_CMD = "quit";
	private static final String LOG_CONFIG = "log_configuration.dat";
	private String routerIp;
	private int routerPort;
	private static int CLIENT_PORT = 9998;

	private DatagramSocket clientSocket;
	private Sender aSender;
	private Receiver aReceiver;
	private String id;
	private static int node_id;

	private static Logger logger = null;

	public Client(String routerIp, int routerPort, String id)
			throws IOException, InvalidMessageException {
		this.setRouterIp(routerIp);
		this.setRouterPort(routerPort);
		this.setId(id);
		this.setNodeId(id);
		PropertyConfigurator.configure(LOG_CONFIG);
		logger = Logger.getLogger(Client.class);

		// Creating a client socket
		this.clientSocket = new DatagramSocket(CLIENT_PORT);

		// Starting thread to send data to router
		this.aSender = new Sender(clientSocket, common.Constants.MAX_MSG_SIZE,
				this.getRouterIp(), this.getRouterPort());

		// Starting thread to receive data from other clients / router
		this.aReceiver = new Receiver(clientSocket,
				common.Constants.MAX_MSG_SIZE);

	}

	private int getRouterPort() {
		return routerPort;
	}

	private void setRouterPort(int routerPort) {
		this.routerPort = routerPort;
	}

	private String getRouterIp() {
		return routerIp;
	}

	private void setRouterIp(String routerIp) {
		this.routerIp = routerIp;
	}

	private String getId() {
		return id;
	}
	
	public int getNodeId() {
		return node_id;
	}

	private void setId(String id) {
		this.id = id;
	}
	
	private void setNodeId(String id) {
		this.node_id = Integer.parseInt(id);
	}

	public void start() throws IOException {
		logger.info("Client started");
		String msg = null;
		BufferedReader buffRdr = new BufferedReader(new InputStreamReader(
				System.in));

		// Prompt
		System.out.print("\n> ");
		
		// Read messages from stdin
		while ((msg = buffRdr.readLine()) != null) {

			// does the user send a quit command ?
			if (msg.toLowerCase().equals(QUIT_CMD)) {
				System.out.println("\n\nQuitting...\n\nqu");
				return;
			}			
			
			try {
				// is message valid ?
				validateMessage(msg);
				String newMsg=addClientInfo(msg);
				aSender.queueMessage(newMsg);
			} catch (InvalidMessageException msgx) {
				msgx.printStackTrace();				
			}

			// queue the message, after appending client's id
			
			System.out.print("> ");
		}
	}

	private String addClientInfo(String msg) {
		return this.getId()+Constants.FIELD_SEPARATOR+msg;
	}

	// Checks if the message structure is valid
	private void validateMessage(String line) throws InvalidMessageException {
		// There should be atleast 3 characters in the message
		if (line.length() < 3) {
			throw new InvalidMessageException(
					"The message should be atleast of length 3 characters. Message : \""
							+ line + "\" is wrong.");
		}

		// The message's first character should either be an integer
		// representing the destination or it should be a character representing
		// text append in the file server

		if (!((line.charAt(0) >= '0' && line.charAt(0) < '0' + common.Constants.NUM_OF_NODES) || ("" + line
				.charAt(0)).toLowerCase().equals(APPEND_CMD))) {
			throw new InvalidMessageException(
					"The first character of the message should be between to '0' to '"
							+ (common.Constants.NUM_OF_NODES - 1) + "', or '" + APPEND_CMD
							+ "'. Message : \"" + line + "\" is wrong.");
		}

		// The second character in the message should be a single whitespace
		if (line.charAt(1) != Constants.FIELD_SEPARATOR) {
			throw new InvalidMessageException(
					"The second character of the message should be a \""+Constants.FIELD_SEPARATOR+"\". Message : \""
							+ line + "\" is wrong.");
		}

		// The message can't exceed the max buffer size
		if (line.length() > common.Constants.MAX_MSG_SIZE) {
			throw new InvalidMessageException("The message should not exceed "
					+ common.Constants.MAX_MSG_SIZE
					+ " characters. Message : \"" + line + "\" is wrong");
		}

	}

	public static void main(String[] args) throws IOException,
			InvalidMessageException {

		// validate command-line args
		if (args.length != 3) {
			System.err.println("\n\nERROR ! Incorrect number of arguments\n");
			usage();
			return;
		}
		String routerIp = args[0];
		int routerPort = Integer.parseInt(args[1]);
		String clientId = args[2];

		// Just printing the command line args
		System.out.println("Router IP : " + routerIp);
		System.out.println("Router port : " + routerPort);
		System.out.println("Client ID : " + clientId + "\n\n");

		// Creating a client and starting it
		Client aClient = new Client(routerIp, routerPort, clientId);
		aClient.start();
		System.out.println("here");
	}

	// prints the program usage
	private static void usage() {
		System.err.println("Usage : java " + Client.class.getName()
				+ " <Router IP> <Router port> <Client ID>");
		System.err.println("Example : java " + Client.class.getName()
				+ " 127.0.0.1 9999 3");

	}

}

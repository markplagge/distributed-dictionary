package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramSocket;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import common.Constants;
import controller.Controller;
import controller.EventLog;
import controller.Message;
import controller.TimeTable;

public class Client {

	private static final Object QUIT_CMD = "quit";
	private static final String LOG_CONFIG = "log_configuration.dat";
	private String routerIp;
	private int routerPort;
	private int id;

	private DatagramSocket clientSocket;
	private Controller controller;

	private static Logger logger = null;

	public Client(String routerIp, int routerPort, int id) throws IOException {
		this.setRouterIp(routerIp);
		this.setRouterPort(routerPort);
		this.setId(id);
		PropertyConfigurator.configure(LOG_CONFIG);
		logger = Logger.getLogger(Client.class);

		// Creating a client socket
		this.setClientSocket(new DatagramSocket(Constants.DEFAULT_CLIENT_PORT));

		// Setting up the controller
		this.controller = new Controller(this.getRouterIp(),
				this.getRouterPort(), this.getId(), this.getClientSocket());

	}

	private DatagramSocket getClientSocket() {
		return this.clientSocket;
	}

	private void setClientSocket(DatagramSocket clientSocket) {
		this.clientSocket = clientSocket;

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

	private int getId() {
		return id;
	}

	private void setId(int id) {
		this.id = id;
	}

	public void start() throws IOException {
		logger.info("Client started");
		String command = null;
		BufferedReader buffRdr = new BufferedReader(new InputStreamReader(
				System.in));

		// Prompt
		System.out.print("\n> ");

		// Read messages from stdin
		while ((command = buffRdr.readLine()) != null) {

			this.executeCommand(command);
			System.out.print("> ");
		}
	}

	private void executeCommand(String command) {
		String[] cmdsData = command.split(""
				+ Constants.Commands.COMMAND_DATA_SEPARATOR);
		String justTheCommand = cmdsData[0].toLowerCase();
		String data=null;
		if (cmdsData.length > 1) {
			data = cmdsData[1];
		}

		if (contains(Constants.Commands.INSERT_COMMANDS, justTheCommand)) {
			this.controller.insertAndLog(data);
		} else if (contains(Constants.Commands.DELETE_COMMANDS, justTheCommand)) {
			this.controller.deleteAndLog(data);

		} else if (contains(Constants.Commands.SEND_COMMANDS, justTheCommand)) {
			int destinationId = Integer.parseInt(cmdsData[1]);
			this.controller.sendMessage(destinationId);
		} else if (contains(Constants.Commands.SHOW_DICT_COMMANDS,
				justTheCommand)) {
			logger.info("\n\nDictionary contents : \n\n"
					+ this.controller.getDictionaryString() + "\n");
		} else if (contains(Constants.Commands.SHOW_TIMETABLE_COMMANDS,
				justTheCommand)) {
			logger.info("\n\nDictionary contents : \n\n"
					+ this.controller.getTimeTableString() + "\n");
		} else if (contains(Constants.Commands.SHOW_VALUE_COMMANDS,
				justTheCommand)) {
			String key = cmdsData[1];
			logger.info("\n\nValue for key : "+key+"\n\n"
					+ this.controller.getValue(key) + "\n");
		}

	}

	private static boolean contains(String[] array, String item) {
		for (String str : array) {
			if (str.equals(item)) {
				return true;
			}
		}

		return false;
	}

	public static void main(String[] args) throws IOException {

		// validate command-line args
		if (args.length != 3) {
			System.err.println("\n\nERROR ! Incorrect number of arguments\n");
			usage();
			return;
		}
		String routerIp = args[0];
		int routerPort = Integer.parseInt(args[1]);
		int clientId = Integer.parseInt(args[2]);

		// Just printing the command line args
		System.out.println("Router IP : " + routerIp);
		System.out.println("Router port : " + routerPort);
		System.out.println("Client ID : " + clientId + "\n\n");

		// Creating a client and starting it
		Client aClient = new Client(routerIp, routerPort, clientId);
		aClient.start();
	}

	// prints the program usage
	private static void usage() {
		System.err.println("Usage : java " + Client.class.getName()
				+ " <Router IP> <Router port> <Client ID>");
		System.err.println("Example : java " + Client.class.getName()
				+ " 127.0.0.1 9999 3");

	}

}

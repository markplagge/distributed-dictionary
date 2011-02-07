package router;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.*;

public class RouterUI extends JFrame implements ActionListener {

	private int number_of_nodes = 5;
	
	private JPanel fullContainer;
	private JPanel mainPanel;
	private JPanel labelsPanel;
	
	private JPanel nodesPanel;
	private JPanel statusPanel;
	private JPanel ipPanel;
	private JPanel portPanel;
	
	private ConfigRow configRows[];
	
	private JLabel labels[];
	private JLabel nodes[];
	private StatusRadioButtons statusButtons[];
	private JTextField ip_addresses[];
	private JTextField ports[];
	
	private final String label_names[] = {"Node", "good", "hold", "drop", "IP address", "port number"};
	private final String node_names[] = {"node 0", "node 1", "node 2", "node 3", "node 4", "FS" };
	
	private JButton apply_button;
	
	public RouterUI(String[] ipAddresses_args) {
		
		super("Router");
		
		fullContainer = new JPanel();
		fullContainer.setLayout(new BoxLayout(fullContainer, BoxLayout.Y_AXIS));
		
		mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout(node_names.length, 1));
		
		labelsPanel = new JPanel();
		
		FlowLayout labelsLayout = new FlowLayout();
		labelsLayout.setAlignment(FlowLayout.LEFT);
		
		labelsPanel.setLayout(labelsLayout);
		labelsPanel.add(new JLabel("  Node  "));
		labelsPanel.add(new JLabel("Good/Hold/Drop"));
//		labelsPanel.add(new JLabel("Hold"));
//		labelsPanel.add(new JLabel("Drop "));
		labelsPanel.add(new JLabel("      IP Address   "));
		labelsPanel.add(new JLabel("       Port "));
		
		apply_button = new JButton("Apply Changes");
		apply_button.addActionListener(this);
		
		nodesPanel = new JPanel();
		nodesPanel.setLayout(new GridLayout(node_names.length, 1, 1, 8));
		
		statusPanel = new JPanel();
		statusPanel.setLayout(new GridLayout(number_of_nodes, 1));
		
		ipPanel = new JPanel();
		ipPanel.setLayout(new GridLayout(number_of_nodes, 1));
		
		portPanel = new JPanel();
		portPanel.setLayout(new GridLayout(number_of_nodes, 1));
		
		setLayout(new FlowLayout());
		
		configRows = new ConfigRow[number_of_nodes+1];
		
		labels = new JLabel[label_names.length];
		nodes = new JLabel[node_names.length];
		statusButtons = new StatusRadioButtons[number_of_nodes];
		ip_addresses = new JTextField[number_of_nodes];
		ports = new JTextField[number_of_nodes];
		
		for (int i = 0; i < node_names.length; i++) {
			nodes[i] = new JLabel(node_names[i]);
			nodesPanel.add(nodes[i]);
		}

//		add(nodesPanel);
		
		for (int i = 0; i < number_of_nodes; i++) {
			statusButtons[i] = new StatusRadioButtons();
			statusPanel.add(statusButtons[i].getStatusPanel());
			
			ip_addresses[i] = new JTextField(10);
			ipPanel.add(ip_addresses[i]);
			
			ports[i] = new JTextField(3);
			portPanel.add(ports[i]);
		}
		
//		add(statusPanel);
//		add(ipPanel);
//		add(portPanel);
		
		//fill mainPanel using ConfigRows 
		for (int i = 0; i < number_of_nodes; i++) {
			configRows[i] = new ConfigRow(node_names[i], ipAddresses_args[i]);
			mainPanel.add(configRows[i].getConfigRow());
		}
		
		mainPanel.add((new ConfigRow("    FS   ", ipAddresses_args[ipAddresses_args.length-1])).getConfigRow());

		fullContainer.add(labelsPanel);
		fullContainer.add(mainPanel);
		fullContainer.add(apply_button);
		add(fullContainer);
		
//		add(labelsPanel);
//		add(mainPanel);
//		add(apply_button);
	}

	public class StatusRadioButtons {
		
		public JPanel statusPanel;
		private JRadioButton goodButton;
		private JRadioButton holdButton;
		private JRadioButton dropButton;
		
		public StatusRadioButtons() {
			
			statusPanel = new JPanel(new GridLayout(1, 0));
			goodButton = new JRadioButton();
			holdButton = new JRadioButton();
			dropButton = new JRadioButton();
			
			ButtonGroup group = new ButtonGroup();
			group.add(goodButton);
			group.add(holdButton);
			group.add(dropButton);
			
			goodButton.setSelected(true);
			
			statusPanel.add(goodButton);
			statusPanel.add(holdButton);
			statusPanel.add(dropButton);
					
		}
		
		public JPanel getStatusPanel() {
			return statusPanel;
		}
		
		public int getSelectedStatus() {
			int status = -1;
			
			if (goodButton.isSelected()) {
				status = common.Constants.LINK_GOOD;
			} else if (holdButton.isSelected()) {
				status = common.Constants.LINK_HOLD;
			} else {
				status = common.Constants.LINK_DROP;
			}
			
			return status;
		}
		
	}
	
	public class ConfigRow {
		
		public JPanel configRow;
		private JLabel label;
		private JPanel status;
		private JTextField ip_address;
		private JTextField port;
		private StatusRadioButtons statusPanel;
		
		public ConfigRow(String l, String ip_addr) {
			label = new JLabel(l);
			statusPanel = new StatusRadioButtons(); 
			status = statusPanel.getStatusPanel();
			ip_address = new JTextField(ip_addr, 10);
			ip_address.setEditable(false);
			if (l.compareTo("    FS   ") == 0) {
				port = new JTextField(common.Constants.DEFAULT_FS_PORT, 3);
			} else {
				port = new JTextField(common.Constants.DEFAULT_CLIENT_PORT, 3);	
			}
			
			port.setEditable(false);
			
			configRow = new JPanel();
			FlowLayout configLayout = new FlowLayout();
			configLayout.setHgap(10);
			configRow.setLayout(configLayout);
			
			
			configRow.add(label);
			configRow.add(status);
			configRow.add(ip_address);
			configRow.add(port);
		}
		
		public JPanel getConfigRow() {
			return configRow;
		}
		
		public String getIPaddress() {
			return ip_address.getText();
		}
		
		public String getPort() {
			return port.getText();
		}
		
		public int getLinkStatus() {
			return statusPanel.getSelectedStatus();
		}
		
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		System.out.println("Applying changes...");
		
		for (int i = 0; i < number_of_nodes; i++) {
			int new_status = configRows[i].getLinkStatus();
			try {
				Router.changeLinkStatus(i, new_status);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
}

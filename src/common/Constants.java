package common;

import java.util.List;
import java.util.Vector;

public interface Constants {
	String LOG_CONFIG = "log_configuration.dat";
    
    String DEFAULT_IP = "127.0.0.1";
    String DEFAULT_ROUTER_IP="128.111.43.36"; //snoopy.cs.ucsb.edu
    
	int DEFAULT_CLIENT_PORT = 9998;
	int DEFAULT_FS_PORT = 9997;
	int DEFAULT_ROUTER_PORT=9999;
	
	
	int LINK_GOOD = 0;
	int LINK_HOLD = 1;
	int LINK_DROP = 2;
	char FIELD_SEPARATOR = ':';
	int MAX_MSG_SIZE = 30000;
	int NUM_OF_NODES = 5;
	
	interface Commands {
		String[] INSERT_COMMANDS={"insert","i"};
		String[] DELETE_COMMANDS={"delete","d"};
		String[] SEND_COMMANDS={"send","s"};
		String[] SHOW_DICT_COMMANDS = {"showdict","sd"};
		String[] SHOW_TIMETABLE_COMMANDS = {"showtt","stt"};
		String[] SHOW_VALUE_COMMANDS = {"showval","sv"};
		String[] SHOW_LOG_COMMANDS = {"showlog","sl"};
		char COMMAND_DATA_SEPARATOR=' ';
		char KEY_VALUE_SEPARATOR=FIELD_SEPARATOR;
		
	}
	enum EventType { INSERT, DELETE};
}

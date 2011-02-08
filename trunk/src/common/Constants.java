package common;

public interface Constants {
	String LOG_CONFIG = "log_configuration.dat";
        String APPEND_TARGET = "/cs/student/kowshik/cs271/gfs-file-sharing/append_target.dat";
	String DEFAULT_IP = "127.0.0.1";
	String DEFAULT_CLIENT_PORT = "9998";
	String DEFAULT_FS_PORT = "9997";
	int LINK_GOOD = 0;
	int LINK_HOLD = 1;
	int LINK_DROP = 2;
	char FIELD_SEPARATOR = ':';
	int MAX_MSG_SIZE = 1024;
	
	
	
	enum EventType { INSERT, DELETE};
}

import common.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Message implements Serializable {
	
	private EventLog eventLog;
	private TimeTable timeTable;
	private transient int source_id;
	private transient int destination_id;
	
	public Message(EventLog eL, TimeTable tt) {
		eventLog = eL;
		timeTable = tt;
	}
	
	public Message(byte [] byteStream) {		
		ByteArrayInputStream bis = new ByteArrayInputStream(byteStream);
		try {
			ObjectInputStream in = new ObjectInputStream(bis);
			try {
				Message nm = (Message) in.readObject();
				in.close();
				this.eventLog = nm.getEvenLog();
				this.timeTable = nm.getTimeTable();				
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public EventLog getEvenLog() {
		return eventLog;
	}
	
	public TimeTable getTimeTable() {
		return timeTable;
	}
	
	public byte[] serialize() {		
		byte[] serializedMessage = null;		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		
		try {		
			ObjectOutput out = new ObjectOutputStream(bos);
			out.writeObject(this);
		    out.close();
		    
		    serializedMessage = bos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
	
		this.s
		return serializedMessage;
	}
	
	public static void main(String args[]){
		EventLog eL = new EventLog();
		TimeTable tt = new TimeTable(0);
		
		tt.test_updateEntry(0, 10);
		tt.test_updateEntry(3, 4);
		tt.test_updateEntry(2, 14);
		
		Message m0 = new Message(eL, tt);
		System.out.println("m0:");
		System.out.println(m0.getTimeTable().toString());
		
		byte[] ser_m0 = m0.serialize();
		System.out.println("ser_m0 length = " + ser_m0.length);
		
		Message m0_from_bytes = new Message(ser_m0);
		System.out.println("m0_from_bytes:");
		System.out.println(m0_from_bytes.getTimeTable().toString());		
	}
}

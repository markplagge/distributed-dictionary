package controller;
import common.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;

public class Message implements Serializable {
	
	private EventLog eventLog;
	private TimeTable timeTable;
	private transient int source_id;
	private transient int destination_id;
	
	public Message(EventLog eL, TimeTable tt, int sourceId, int destinationId) {
		eventLog = eL;
		timeTable = tt;
		this.source_id = sourceId;
		this.destination_id = destinationId;
	}
	
	public Message(byte [] byteStream) {
		
		this.source_id = byteStream[0];
		this.destination_id=byteStream[1];
		
		byte[] objectBytes=Arrays.copyOfRange(byteStream,2,byteStream.length);
		
		ByteArrayInputStream bis = new ByteArrayInputStream(objectBytes);
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
	
	public int getSourceId() {
		return source_id;
	}

	public int getDestinationId() {
		return destination_id;
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
	
		byte sourceIdByte=new Integer(this.source_id).byteValue();
		byte destnIdByte=new Integer(this.destination_id).byteValue();
		
		byte[] msg=new byte[serializedMessage.length+2];
		msg[0]=sourceIdByte;
		msg[1]=destnIdByte;
		for(int index=0;index<serializedMessage.length;index++){
			msg[index+2]=serializedMessage[index];
		}
		
		
		return msg;
	}
	
	
	public String toString(){
		return "{ "+this.getSourceId()+" -> "+this.getDestinationId()+" }";
	}
	
	public String getVerboseDescription(){
		StringBuffer strBuffer=new StringBuffer();
		strBuffer.append("{");
		strBuffer.append("\nSource ID : "+this.getSourceId());
		strBuffer.append("\nDestn  ID : "+this.getDestinationId());
		strBuffer.append("\nTime Table : \n"+this.getTimeTable().toString());
		strBuffer.append("\nEvent Log : \n"+this.getEvenLog().toString());
		strBuffer.append("}\n");
		
		return strBuffer.toString();
	}
	
	public static void main(String args[]){
		EventLog eL = new EventLog();
		TimeTable tt = new TimeTable(0);
		
		tt.test_updateEntry(0, 10);
		tt.test_updateEntry(3, 4);
		tt.test_updateEntry(2, 14);
		
		Message m0 = new Message(eL, tt, 0, 4);
		System.out.println("m0:");
		System.out.println(m0.getTimeTable().toString());
		
		byte[] ser_m0 = m0.serialize();
		System.out.println("ser_m0 length = " + ser_m0.length);
		
		Message m0_from_bytes = new Message(ser_m0);
		System.out.println("m0_from_bytes:");
		System.out.println(m0_from_bytes.getTimeTable().toString());
		System.out.println(m0_from_bytes.getSourceId() + " -> " + m0_from_bytes.getDestinationId());
	}
}

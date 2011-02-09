package controller;
import java.io.Serializable;

import common.Constants;
import common.Constants.EventType;

/*
 * Represents an event record in the partial log that has the following properties
 */
public class EventRecord implements Cloneable, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1784107304055097219L;
	
	//Key in the key-value pair
	private String key;
	public void setKey(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}

	//Value in the key-value pair
	private String value;	
	public void setValue(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
	
	//Type of operation : insert or delete
	private Constants.EventType eventType;
	
	public Constants.EventType getEventType() {
		return eventType;
	}

	public void setEventType(Constants.EventType eventType) {
		this.eventType = eventType;
	}

	//Node that generates this event
	private int nodeId;
	
	public int getNodeId() {
		return nodeId;
	}

	public void setNodeId(int nodeId) {
		this.nodeId = nodeId;
	}

	//Lamport clock time of this event
	private int timestamp;
	
	
	public int getTimeStamp() {
		return timestamp;
	}

	public void setTimeStamp(int timestamp) {
		this.timestamp = timestamp;
	}

	
	public EventRecord(String key, String value, Constants.EventType eventType, int timestamp, int nodeId){
		this.setKey(key);
		this.setValue(value);
		this.setEventType(eventType);
		this.setTimeStamp(timestamp);
		this.setNodeId(nodeId);		
	}
	
	//Attempting to determine the causal ordering between events A and B.
	//Not sure if this method will ever be useful, but just in case :)
	//Returns true if this event occurs before anEventRecord
	//Returns false otherwise.
	//Ties are broken by nodeId
	public boolean hasHappenedBefore(EventRecord anEventRecord){
		if(this.getTimeStamp() < anEventRecord.getTimeStamp()){
			return true;
		}else if(this.getNodeId() < anEventRecord.getNodeId()){
			return true;
		}
		return false;
	}
	
	//Deep copy
	public Object clone(){
		EventRecord clone=null;
		try {
			clone=(EventRecord)super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		
		clone.setKey(this.getKey());
		clone.setValue(this.getValue());
		clone.setEventType(this.getEventType());
		clone.setNodeId(this.getNodeId());
		clone.setTimeStamp(this.getTimeStamp());
		
		
		return clone;
	}
	
	//A string representation of the record
	//Format        : <Key,Value,EventType,TimeStamp,NodeID>
	//Example : <43,53,INSERT,3,4>
	public String toString(){
		StringBuffer recordStr = new StringBuffer();
		
		recordStr.append("<");
		recordStr.append(this.getEventType());
		recordStr.append("<");
		recordStr.append(this.getKey()+",");
		recordStr.append(this.getValue());
		recordStr.append(">,");				
		recordStr.append(this.getTimeStamp()+",");
		recordStr.append(this.getNodeId());
		recordStr.append(">");
		
		return recordStr.toString();
	}
	
	public static void main(String args[]){
		EventRecord rec=new EventRecord("a","b",EventType.DELETE,3,4);
		System.out.println(rec);
	}

	
}

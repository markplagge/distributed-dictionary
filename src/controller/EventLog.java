package controller;
import java.io.Serializable;
import java.util.List;
import java.util.Vector;

import common.Constants;
import common.Constants.EventType;

/*
 * Stores a log of all events occuring in the distributed system
 */
public class EventLog implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6965204047590764864L;
	
	// List of all events
	private List<EventRecord> listOfRecords;

	private List<EventRecord> getListOfRecords() {
		return this.listOfRecords;
	}

	public EventLog() {
		this.listOfRecords=new Vector<EventRecord>();
	}

	public void addRecord(EventRecord record) {
		this.getListOfRecords().add(record);
	}
	
	//Returns records of a particular type
	public List<EventRecord> getRecords(Constants.EventType theEventType)
	{

		List<EventRecord> listOfRecords = new Vector<EventRecord>();
		for (EventRecord rec : this.getListOfRecords()) {
			if (rec.getEventType().equals(theEventType)) {
				listOfRecords.add(rec);
			}
		}
		return listOfRecords;
	
	 }


	
	//Returns all records
	public List<EventRecord> getAllRecords() {
		return this.getListOfRecords();
	}
	
	
	// Lists all records sequentially
	public String toString() {
		StringBuffer recListString = new StringBuffer();

		recListString.append("Number of records : "
				+ this.getListOfRecords().size());
		recListString.append("\n\n");
		int counter = 1;
		for (EventRecord rec : this.getListOfRecords()) {
			recListString.append("(" + (counter++) + ") " + rec.toString()
					+ "\n");
		}

		return recListString.toString();
	}
	
	public static void main(String args[]){
		EventLog anEventLog = new EventLog();
		anEventLog.addRecord(new EventRecord("3","4",EventType.DELETE,3,4));
		anEventLog.addRecord(new EventRecord("5","6",EventType.INSERT,3,4));
		
		System.out.println(anEventLog);
	}

	public boolean contains(EventRecord eR) {
		if(this.getAllRecords().contains(eR)){
			return true;
		}
		
		
		return false;
	}

}

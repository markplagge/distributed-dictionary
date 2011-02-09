package controller;
import common.*;

import java.io.Serializable;
import java.lang.Math;

public class TimeTable implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8063075524218780397L;
	
	private int[][] timeTable;
	private int node_id;
	
	public TimeTable(int id) {
		node_id = id;
		timeTable = new int[Constants.NUM_OF_NODES][Constants.NUM_OF_NODES];
		
		for (int i = 0; i < Constants.NUM_OF_NODES; i++) {
			for (int j = 0; j < Constants.NUM_OF_NODES; j++) {
				timeTable[i][j] = 0;
			}
		}
	}
	
	public int[][] getTimeTable() {
		return timeTable;
	}
	
	public void update(TimeTable newTimeTable, int node_id, int received_from) {
		int[][] new_timeTable = newTimeTable.getTimeTable();
		
		for (int I = 0; I < Constants.NUM_OF_NODES; I++) {
			timeTable[node_id][I] = Math.max(timeTable[node_id][I], new_timeTable[received_from][I]);
		}
		
		for (int i = 0; i < Constants.NUM_OF_NODES; i++) {
			for (int j = 0; j < Constants.NUM_OF_NODES; j++) {
				timeTable[i][j] = Math.max(timeTable[i][j], new_timeTable[i][j]);
			}
		}
		
		//update partial log
	}
	
	public void updateLocalEntry(int newClockValue) {
		timeTable[node_id][node_id] = newClockValue;
	}
	
	public boolean hasrec(EventRecord eR, int destinationId) {
		if (this.timeTable[destinationId][eR.getNodeId()] >= eR.getTimeStamp()) {
			return true;
		}
		
		return false;
	}
	
	public void test_updateEntry(int node_id, int value) {
		timeTable[node_id][node_id] = value;
	}
	
	public String toString() {
		String timetable_string = "";
		
		for (int i = 0; i < Constants.NUM_OF_NODES; i++) {
			for (int j = 0; j < Constants.NUM_OF_NODES; j++) {
				timetable_string += Integer.toString(timeTable[i][j]) + "\t";
			}
			timetable_string += "\n";
		}
		
		timetable_string += "\n";
		
		return timetable_string;
	}
	
	public static void main(String args[]){
		TimeTable tt0 = new TimeTable(0);
		TimeTable tt1 = new TimeTable(3);
		
//		System.out.println(tt0.toString());
//		System.out.println(tt1.toString());
		
		tt0.updateLocalEntry(0);
		tt0.updateLocalEntry(1);
		tt0.updateLocalEntry(2);
		
		tt1.updateLocalEntry(0);
		tt1.updateLocalEntry(1);
		tt1.updateLocalEntry(2);
		tt1.updateLocalEntry(3);

//		System.out.println(tt0.toString());
//		System.out.println(tt1.toString());
		
		tt0.update(tt1, 0, 3);
		System.out.println(tt0.toString());
	}
	
}

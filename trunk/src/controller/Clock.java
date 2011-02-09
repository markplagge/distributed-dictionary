package controller;

public class Clock {

	static int clock;
	
	public Clock() {
		clock = 0;
	}
	
	public int getClock() {
		increment();
		return clock;
	}
	
	private void increment() {
		clock += 1; 
	}
	
}

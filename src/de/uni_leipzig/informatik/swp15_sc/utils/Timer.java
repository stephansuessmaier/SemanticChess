package de.uni_leipzig.informatik.swp15_sc.utils;

public class Timer  {
	public Timer () {
		time = 0;
	}
	
	public void start() {
		time = System.currentTimeMillis();
	}
	
	public void stop() {
		time = System.currentTimeMillis() - time;
	}
	
	public void reset () {
		time = 0;
	}
	
	public long getTime () {
		return time;
	}
	
	private long time;
}

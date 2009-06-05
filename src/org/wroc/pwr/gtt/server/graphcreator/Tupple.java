package org.wroc.pwr.gtt.server.graphcreator;

import java.util.ArrayList;

public class Tupple implements Comparable {
	int stopid;
	double distance;

	public int compareTo(Object o) {
		if (distance < ((Tupple) o).distance)
			return -1;
		else if (distance > ((Tupple) o).distance)
			return 1;
		else
			return 0;
	}

	public Tupple(int stop_id, double distance) {
		super();
		this.stopid = stop_id;
		this.distance = distance;
	}
	
	public int getStopId(){
		return stopid;
	}

}

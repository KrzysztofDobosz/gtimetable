package org.wroc.pwr.gtt.server.graphcreator;

import java.sql.Time;

public class Leg {
	int start_stop;
	int end_stop;
	int line_id;
	int stopsDistance;
	public Leg(int przystStart, int przystEnd, int linia_id,int stopsDistance) {
		super();
		this.start_stop = przystStart;
		this.end_stop = przystEnd;
		this.line_id = linia_id;
		this.stopsDistance = stopsDistance;
	}

	
	public String toString() {
		return "(" + line_id + ")[" + start_stop + " - " + end_stop + "]-" + stopsDistance;
	}


	public int getStopsDistance() {
		return stopsDistance;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + end_stop;
		result = prime * result + line_id;
		result = prime * result + start_stop;
		result = prime * result + stopsDistance;
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Leg other = (Leg) obj;
		if (end_stop != other.end_stop)
			return false;
		if (line_id != other.line_id)
			return false;
		if (start_stop != other.start_stop)
			return false;
		if (stopsDistance != other.stopsDistance)
			return false;
		return true;
	}


}
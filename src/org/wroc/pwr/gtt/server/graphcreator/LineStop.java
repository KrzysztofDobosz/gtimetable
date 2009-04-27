package org.wroc.pwr.gtt.server.graphcreator;

import java.sql.Time;

public class LineStop {
	int przystStart;
	int przystEnd;

	int linia_id;
	Time time;

	public LineStop(int przystStart, int przystEnd, int linia_id) {
		super();
		this.przystStart = przystStart;
		this.przystEnd = przystEnd;
		this.linia_id = linia_id;
		this.time = null;
	}

	public int getPrzystStart() {
		return przystStart;
	}

	public void setPrzystStart(int przystStart) {
		this.przystStart = przystStart;
	}

	public int getPrzystEnd() {
		return przystEnd;
	}

	public void setPrzystEnd(int przystEnd) {
		this.przystEnd = przystEnd;
	}

	public int getLinia_id() {
		return linia_id;
	}

	public void setLinia_id(int linia_id) {
		this.linia_id = linia_id;
	}

	public String toString() {
		return "(" + linia_id + ")[" + przystStart + " - " + przystEnd + "]";
	}

	public Time getTime() {
		return time;
	}

	public void setTime(Time time) {
		this.time = time;
	}

}
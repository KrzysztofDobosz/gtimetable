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
	/**
	 * @return the przystStart
	 */
	public int getPrzystStart() {
		return przystStart;
	}
	/**
	 * @param przystStart the przystStart to set
	 */
	public void setPrzystStart(int przystStart) {
		this.przystStart = przystStart;
	}
	/**
	 * @return the przystEnd
	 */
	public int getPrzystEnd() {
		return przystEnd;
	}
	/**
	 * @param przystEnd the przystEnd to set
	 */
	public void setPrzystEnd(int przystEnd) {
		this.przystEnd = przystEnd;
	}
	/**
	 * @return the linia_id
	 */
	public int getLinia_id() {
		return linia_id;
	}
	/**
	 * @param linia_id the linia_id to set
	 */
	public void setLinia_id(int linia_id) {
		this.linia_id = linia_id;
	}
	public String toString(){
		return "(" + linia_id + ")["+przystStart+ " - " + przystEnd + "]";
	}
	/**
	 * @return the time
	 */
	public Time getTime() {
		return time;
	}
	/**
	 * @param time the time to set
	 */
	public void setTime(Time time) {
		this.time = time;
	}
	
}

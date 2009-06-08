package org.wroc.pwr.gtt.server.graphcreator;

import java.sql.Time;

/**
 * Klasa reprezentujaca pojedynczy oddzinek polaczenia (bez przesiadek)
 * 
 * @author Michal Brzezinski-Spiczak
 * 
 */
public class Leg {
	private int p1;
	private int p2;
	private int line_id;
	private int stopsDistance;

	/**
	 * Konsruktor odcinka na bazie przystanku poczatkowego p1, koncowego p2,
	 * etykiety line_id oraz odleglosci przystankowej stopsDistance
	 * 
	 * @param p1
	 *            -- przystanek poczatkowy
	 * @param p2
	 *            -- przystanek koncowy
	 * @param linia_id
	 *            -- etykieta id linii
	 * @param stopsDistance
	 *            -- odleglosc przystankowa
	 */
	public Leg(int przystStart, int przystEnd, int linia_id, int stopsDistance) {
		super();
		this.p1 = przystStart;
		this.p2 = przystEnd;
		this.line_id = linia_id;
		this.stopsDistance = stopsDistance;
	}

	public String toString() {
		return "(" + line_id + ")[" + p1 + " - " + p2 + "]-" + stopsDistance;
	}

	public int getStart_stop() {
		return p1;
	}

	public void setStart_stop(int start_stop) {
		this.p1 = start_stop;
	}

	public int getEnd_stop() {
		return p2;
	}

	public void setEnd_stop(int end_stop) {
		this.p2 = end_stop;
	}

	public int getLine_id() {
		return line_id;
	}

	public void setLine_id(int line_id) {
		this.line_id = line_id;
	}

	public void setStopsDistance(int stopsDistance) {
		this.stopsDistance = stopsDistance;
	}

	public int getStopsDistance() {
		return stopsDistance;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + p2;
		result = prime * result + line_id;
		result = prime * result + p1;
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
		if (p2 != other.p2)
			return false;
		if (line_id != other.line_id)
			return false;
		if (p1 != other.p1)
			return false;
		if (stopsDistance != other.stopsDistance)
			return false;
		return true;
	}

}
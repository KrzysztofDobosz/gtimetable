package org.wroc.pwr.gtt.server.graphcreator;


/**
 * Klasa reprezentujaca krotke - przystanek, odleglosc sluzaca do wybierania
 * najblizszych przystankow od zadanego punkstu
 * 
 * @author Michal Brzezinski-Spiczak
 * 
 */
public class StopDist implements Comparable {
	int stopid;
	double distance;

	public int compareTo(Object o) {
		if (distance < ((StopDist) o).distance)
			return -1;
		else if (distance > ((StopDist) o).distance)
			return 1;
		else
			return 0;
	}

	public StopDist(int stop_id, double distance) {
		super();
		this.stopid = stop_id;
		this.distance = distance;
	}

	public int getStopId() {
		return stopid;
	}

}

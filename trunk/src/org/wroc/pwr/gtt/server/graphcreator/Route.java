package org.wroc.pwr.gtt.server.graphcreator;

import java.util.ArrayList;

/**
 * Klasa reprezentujaca trase miedzy wybranymi przystankami (lista odcinkow)
 * 
 * @author Michal Brzezinski-Spiczak
 * 
 */
public class Route implements Comparable {
	ArrayList<Leg> route;

	/**
	 * Bezargumentowy konstruktor trasy
	 */
	public Route() {
		route = new ArrayList<Leg>();
	}

	/**
	 * Metoda dodajaca odcinek p1-p2(line_id) do trasy
	 * 
	 * @param p1
	 *            -- poczatkowy przystanek dodawanego odcinka
	 * @param p2
	 *            -- koncowy przystanek dodawanego odcinka
	 * @param line_id
	 *            -- etykieta odcinka
	 * @param stopsDistance
	 *            -- odleglosc przytankowa odcinka
	 */
	public void add(int p1, int p2, int line_id, int stopsDistance) {
		route.add(new Leg(p1, p2, line_id, stopsDistance));
	}

	public ArrayList<Leg> getTrasa() {
		return route;
	}

	public String toString() {
		String print = "";
		for (Leg l : route)
			print += "(" + l + ")";
		return print;
	}

	/**
	 * Metoda zwracajaca sumaryczna odleglosc przystankowa dla trasy
	 * 
	 * @return
	 */
	public int getStopsDistance() {
		int distance = 0;
		for (Leg el : route)
			distance += el.getStopsDistance();
		return distance;
	}

	/**
	 * Metoda zwracajaca ilosc przesiadek na trasie
	 * 
	 * @return
	 */
	public int getChanges() {
		return route.size();
	}

	public int compareTo(Object o) {
		Route second = (Route) o;
		if (getChanges() < second.getChanges())
			return -1;
		else if (getChanges() > second.getChanges())
			return 1;
		else if (getStopsDistance() < second.getStopsDistance())
			return -1;
		else if (getStopsDistance() > second.getStopsDistance())
			return 1;
		else
			return 0;
	}

	public boolean contains(Route route1) {
		boolean contain = true;
		for (int i = 0; i < route1.route.size(); i++) {
			if (!route.contains(route1.route.get(i)))
				contain = false;
			break;
		}

		return contain;
	}

}

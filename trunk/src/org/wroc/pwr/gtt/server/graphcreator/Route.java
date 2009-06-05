package org.wroc.pwr.gtt.server.graphcreator;

import java.util.ArrayList;

public class Route implements Comparable {
	ArrayList<Leg> trasa;

	public Route() {
		trasa = new ArrayList<Leg>();
	}

	public void add(int p1, int p2, int linia_id, int stopsDistance) {
		trasa.add(new Leg(p1, p2, linia_id, stopsDistance));
	}

	public ArrayList<Leg> getTrasa() {
		return trasa;
	}

	public String toString() {
		String print = "";
		for (Leg l : trasa)
			print += "(" + l + ")";
		return print;
	}

	public int getStopsDistance() {
		int distance = 0;
		for (Leg el : trasa)
			distance += el.getStopsDistance();
		return distance;
	}

	public int getChanges() {
		return trasa.size();
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

	public boolean contains(Route route) {
		boolean contain = true;
		for (int i = 0; i < route.trasa.size(); i++) {
			if (!trasa.contains(route.trasa.get(i)))
				contain = false;
			break;
		}

		return contain;
	}

}

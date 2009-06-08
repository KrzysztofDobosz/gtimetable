package org.wroc.pwr.gtt.server.graphcreator;

import java.sql.Connection;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.wroc.pwr.gtt.server.DBconnector;

/**
 * Klasa rozszerzajaca wazona krawedz dodajaca etykiety - id lini i typ id oraz
 * druga wage -- odleglosc przystankowa. Metody zwracaja this ze wzgledu na
 * konstrukcje metod z biblioteki jgrapht - dodawanie krawedzie do grafu)
 * 
 * @author Michal Brzezinski-Spiczak
 * 
 */
public class WEdge extends DefaultWeightedEdge {
	int line_id;
	int stop_distance;
	DBconnector dbconnector;

	public WEdge setLabel(int linia) {
		line_id = linia;
		return this;
	}

	public String toString() {
		if (line_id != 1)
			return dbconnector.getLineName(line_id) + "("
					+ dbconnector.getStopName((Integer) getSource()) + ","
					+ dbconnector.getStopName((Integer) getTarget()) + ")";
		else
			return "";
	}

	public WEdge setWeight(int weight) {
		stop_distance = weight;
		return this;
	}

	public WEdge setDB(DBconnector bconnector) {
		this.dbconnector = bconnector;
		return this;
	}

	public Object getSourceVertex() {
		return getSource();
	}

	public Object getTargetVertex() {
		return getTarget();
	}

	public int getStop_distance() {
		return stop_distance;
	}

}

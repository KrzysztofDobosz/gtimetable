package org.wroc.pwr.gtt.server.graphcreator;

import java.sql.Connection;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.wroc.pwr.gtt.server.DBconnector;

/**
 * Klasa rozszerzajaca wazona krawedz dodajaca etykiety - id lini i typ id oraz
 * druga wage. Metody zwracaja this ze wzgledu na konstrukcje metod z biblioteki
 * jgrapht - dodawanie krawedzie do grafu)
 *
 * @author Michal Brzezinski-Spiczak
 *
 */
public class WEdge extends DefaultWeightedEdge {
	int label;
	int waga;
	DBconnector conn;

	public WEdge setLabel(int linia) {
		label = linia;
		return this;
	}



	public String toString() {
		if (label != 1)
			return conn.getLiniaNazwa(label) + "(" + conn.getPrzystNazwa((Integer) getSource()) + "," + conn.getPrzystNazwa((Integer) getTarget()) + ")";
		else
			return "";
	}

	public WEdge setWeight(int waga2) {
		waga = waga2;
		return this;
	}

	public WEdge setDB(DBconnector bconnector) {
		this.conn = bconnector;
		return this;
	}
	public Object getSourceVertex(){
		return getSource();
	}
	public Object getTargetVertex(){
		return getTarget();
	}

}

package org.wroc.pwr.gtt.server.graphcreator;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.KShortestPaths;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.jgrapht.graph.Subgraph;
import org.wroc.pwr.gtt.server.DBconnector;

/**
 * Klasa odpowiedzialna za reprezentacje grafow¹, stanowi rozszerzenie aktualnie
 * do wazonego, skierowanego multigrafu biblioteki jgrapht, póxniej raczej
 * w³asnej biblioteki grafowej
 * 
 * @author Micha³ Brzeziñski-Spiczak
 * 
 */
public class GttGraph extends DirectedWeightedMultigraph<Integer, WEdge> {

	public GttGraph() {
		super(WEdge.class);
	}

	public void addWEdge(DBconnector bconnector, int p1, int p2, int linia, int waga, int waga2, int typ) {
		setEdgeWeight((WEdge) addEdge(p1, p2).setLabel(linia).setWeight(waga2).setTyp(typ).setDB(bconnector), waga);
	}

	public void findCourse(int typ, int p1, int p2, int amount) {
		GttGraph g = (GttGraph) this.clone();
		for (WEdge w : edgeSet()) {
			if (!(w.typ <= typ))
				g.removeEdge(w);
		}
		if (g.containsVertex(p1) && g.containsVertex(p2)) {
			KShortestPaths<Integer, WEdge> ks = new KShortestPaths<Integer, WEdge>(g, p1, amount);
			ArrayList<GraphPath<Integer, WEdge>> a = new ArrayList<GraphPath<Integer, WEdge>>();

			if ((a = (ArrayList<GraphPath<Integer, WEdge>>) ks.getPaths(p2)) != null) {

				Collections.sort(a, new CComparator<GraphPath<Integer, WEdge>>());
				for (int i = 0; i < a.size(); i++)
					System.out.println(a.get(i).getEdgeList() + " " + a.get(i).getWeight() + " " + CComparator.edgeWeight(a.get(i).getEdgeList()));
			} else
				System.out.println("brak po³¹czenia");
		} else
			System.out.println("brak przystanku w bazie");
	}
}

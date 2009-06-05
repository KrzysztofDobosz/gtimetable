package org.wroc.pwr.gtt.server.graphcreator;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.KShortestPaths;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.jgrapht.graph.Subgraph;
import org.wroc.pwr.gtt.server.DBconnector;

/**
 * Klasa odpowiedzialna za reprezentacje grafow, stanowi rozszerzenie aktualnie
 * do wazonego, skierowanego multigrafu biblioteki jgrapht, pozniej raczej
 * wlasnej biblioteki grafowej
 *
 * @author Michal Brzezinski-Spiczak
 *
 */
public class GttGraph extends DirectedWeightedMultigraph<Integer, WEdge> {

	public GttGraph() {
		super(WEdge.class);
	}

	public void addWEdge(DBconnector bconnector, int p1, int p2, int linia, double local_weight, int waga2) {
		setEdgeWeight((WEdge) addEdge(p1, p2).setLabel(linia).setWeight(waga2).setDB(bconnector), local_weight);
	}

	public ArrayList<Route> findCourse(int p1, int p2, int amount) {
		ArrayList<Route> result = new ArrayList<Route>();

		if (containsVertex(p1) && containsVertex(p2) && p1!=p2) {
			KShortestPaths<Integer, WEdge> ks = new KShortestPaths<Integer, WEdge>(this, p1, amount);
			ArrayList<GraphPath<Integer, WEdge>> a = new ArrayList<GraphPath<Integer, WEdge>>();

			if ((a = (ArrayList<GraphPath<Integer, WEdge>>) ks.getPaths(p2)) != null) {

			//	Collections.sort(a, new RouteComparator<GraphPath<Integer, WEdge>>());
				for (int i = 0; i < a.size(); i++) {
					//System.out.println(a.get(i).getEdgeList() + " " + a.get(i).getWeight() + " " + CComparator.edgeWeight(a.get(i).getEdgeList()));
					Route resulte = new Route();
					List<WEdge> e = a.get(i).getEdgeList();
					for (int k = 0; k < e.size(); k++) {
						resulte.add((Integer) e.get(k).getSourceVertex(), (Integer) e.get(k).getTargetVertex(), e.get(k).line_id, e.get(k).getStop_distance());
					}
					result.add(resulte);
				}
			} else
				System.out.println("brak po��czenia");
		} else
			System.out.println("brak przystanku w bazie");
		Collections.sort(result);
		return result;
	}

}

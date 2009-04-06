package org.wroc.pwr.gtt.server.graphcreator;

import java.util.Comparator;
import java.util.List;

import org.jgrapht.GraphPath;

public class CComparator<T> implements Comparator<T> {
	public int compare(Object obj1, Object obj2) {
		GraphPath<Integer, WEdge> emp1 = (GraphPath<Integer, WEdge>) obj1;
		GraphPath<Integer, WEdge> emp2 = (GraphPath<Integer, WEdge>) obj2;
		double c = (emp1.getWeight() - emp2.getWeight());
		if (c == 0)

			return (int) edgeWeight(emp1.getEdgeList()) - edgeWeight(emp2.getEdgeList());
		else
			return (int) c;
	}

	public static int changes(List<WEdge> list) {
		int am = 0;
		for (int i = 1; i < list.size(); i++) {
			if (list.get(i).label != list.get(i - 1).label)
				am++;

		}
		return am;

	}

	public static int edgeWeight(List<WEdge> edgeList) {
		int sumweight = 0;
		for (int i = 0; i < edgeList.size(); i++)
			sumweight += edgeList.get(i).waga;
		return sumweight;

	}
}
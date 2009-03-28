package org.wroc.pwr.gtt.server.graphcreator;

import org.jgrapht.graph.DefaultWeightedEdge;

public class WEdge extends DefaultWeightedEdge{
	int label;
	int waga;
	int typ;
	public WEdge setLabel(int linia) {
		label = linia;
		return this;
	}
	public WEdge setTyp(int typ) {
		this.typ = typ;
		return this;
	}
	
	public String toString(){
		return label+"(" +getSource() +","+getTarget()+")";
	}
	public WEdge setWeight(int waga2) {
		waga=waga2;
		return this;
	}

}

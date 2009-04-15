package org.wroc.pwr.gtt.server;

public class Coordinates {
	double dlugosc;
	double szerokosc;
	public Coordinates(double dlugosc, double szerokosc) {
		
		this.dlugosc = dlugosc;
		this.szerokosc = szerokosc;
	}
	/**
	 * @return the dlugosc
	 */
	public double getDlugosc() {
		return dlugosc;
	}
	/**
	 * @param dlugosc the dlugosc to set
	 */
	public void setDlugosc(double dlugosc) {
		this.dlugosc = dlugosc;
	}
	/**
	 * @return the szerokosc
	 */
	public double getSzerokosc() {
		return szerokosc;
	}
	/**
	 * @param szerokosc the szerokosc to set
	 */
	public void setSzerokosc(double szerokosc) {
		this.szerokosc = szerokosc;
	}
	
	

}

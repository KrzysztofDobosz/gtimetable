package org.wroc.pwr.gtt.server;

/**
 * Klasa reprezentuj¹ca wspo³rzêdne geograficzne, przechowuje d³ugoœc i
 * szereokoœæ geograficza; konieczne do wyœwietalnia przyst. na mapie.
 * 
 * @author Micha³ Brzeziñski-Spiczak
 * 
 */
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
	 * @param dlugosc
	 *            the dlugosc to set
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
	 * @param szerokosc
	 *            the szerokosc to set
	 */
	public void setSzerokosc(double szerokosc) {
		this.szerokosc = szerokosc;
	}

}

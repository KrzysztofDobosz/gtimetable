package org.wroc.pwr.gtt.server;

/**
 * Klasa reprezentujaca wspolrzedne geograficzne, przechowuje dlugosc i
 * szereokosc geograficza; konieczne do wyswietalnia przyst. na mapie.
 *
 * @author Michal Brzezinski-Spiczak
 *
 */
public class Coordinates {
	private double lat;
	private double lng;

	public Coordinates(double dlugosc, double szerokosc) {

		this.lat = dlugosc;
		this.lng = szerokosc;
	}

   public double getLat()
   {
      return lat;
   }

   public void setLat(double lat)
   {
      this.lat = lat;
   }

   public double getLng()
   {
      return lng;
   }

   public void setLng(double lng)
   {
      this.lng = lng;
   }


}

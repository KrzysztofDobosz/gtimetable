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

	public Coordinates(double lat, double lng) {

		this.lat = lat;
		this.lng = lng;
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

@Override
public int hashCode() {
	final int prime = 31;
	int result = 1;
	long temp;
	temp = Double.doubleToLongBits(lat);
	result = prime * result + (int) (temp ^ (temp >>> 32));
	temp = Double.doubleToLongBits(lng);
	result = prime * result + (int) (temp ^ (temp >>> 32));
	return result;
}

@Override
public boolean equals(Object obj) {
	if (this == obj)
		return true;
	if (obj == null)
		return false;
	if (getClass() != obj.getClass())
		return false;
	Coordinates other = (Coordinates) obj;
	if (Double.doubleToLongBits(lat) != Double.doubleToLongBits(other.lat))
		return false;
	if (Double.doubleToLongBits(lng) != Double.doubleToLongBits(other.lng))
		return false;
	return true;
}


}

package org.wroc.pwr.gtt.client;

import com.google.gwt.maps.client.InfoWindowContent;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.MarkerOptions;

/**
 * Klasa dziedziczaca po klasie Marker z gwt. Ma dodatkowe
 * pole - InfoWindowContent.
 *
 * @author Krzysztof Dobosz
 *
 */
public class GttMarker extends Marker
{
   private InfoWindowContent content;

   public GttMarker(LatLng point, MarkerOptions options)
   {
      super(point, options);
   }

   public GttMarker(LatLng point)
   {
      super(point);
   }

   public InfoWindowContent getContent()
   {
      return content;
   }

   public void setContent(InfoWindowContent content)
   {
      this.content = content;
   }
}

package org.wroc.pwr.gtt.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.maps.client.InfoWindow;
import com.google.gwt.maps.client.InfoWindowContent;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.control.LargeMapControl;
import com.google.gwt.maps.client.control.MapTypeControl;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.event.MarkerClickHandler;

public class Client implements EntryPoint
{
   private GttServiceAsync service;
   private MapWidget map;

   private Marker createMarker(LatLng point, final String text)
   {
      final Marker marker = new Marker(point);
      marker.addMarkerClickHandler(new MarkerClickHandler()
      {
         public void onClick(MarkerClickEvent event)
         {
            InfoWindow info = map.getInfoWindow();
            info.open(marker, new InfoWindowContent(text));
         }
      });
      return marker;
   }

   public void onModuleLoad()
   {
      service = GttService.Util.getInstance();
      // service.update(null);

      Panel panel = new FlowPanel();

      map = new MapWidget(LatLng.newInstance(51.1078852, 17.0385376), 13);
      map.setSize("800px", "500px");
      map.addControl(new LargeMapControl());
      map.addControl(new MapTypeControl());
      map.clearOverlays();
      map.addOverlay(createMarker(LatLng.newInstance(51.1100157, 17.0317697),
            "Rynek"));
      panel.add(map);

      final Label l = new Label();
      l.setText("Wrocław");
      panel.add(l);
      RootPanel.get().add(panel);
   }

}

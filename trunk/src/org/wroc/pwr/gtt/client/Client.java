package org.wroc.pwr.gtt.client;

import java.util.ArrayList;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.maps.client.InfoWindow;
import com.google.gwt.maps.client.InfoWindowContent;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.control.LargeMapControl;
import com.google.gwt.maps.client.control.MapTypeControl;
import com.google.gwt.maps.client.geocode.Geocoder;
import com.google.gwt.maps.client.geocode.LatLngCallback;
import com.google.gwt.maps.client.geocode.LocationCallback;
import com.google.gwt.maps.client.geocode.Placemark;
import com.google.gwt.maps.client.geocode.StatusCodes;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.event.MapDoubleClickHandler;
import com.google.gwt.maps.client.event.MapMouseMoveHandler;
import com.google.gwt.maps.client.event.MarkerClickHandler;

public class Client implements EntryPoint
{
   private GttServiceAsync service;
   private MapWidget map;
   private Button submit;
   private TextBox address;
   private Geocoder geocoder;
   private InfoWindow info;
   private TextArea l;
   private LatLng startSpot;
   private LatLng stopSpot;

   private void find()
   {
      geocoder.getLatLng("wrocław" + address.getText(), new LatLngCallback()
      {
         public void onFailure()
         {
            Window.alert("Miejsce nie znalezione.");
         }

         public void onSuccess(LatLng point)
         {
            addSpotMarker(point);
         }
      });
   }

   private void addSpotMarker(LatLng point)
   {
      map.setCenter(point);
      final Marker marker = new Marker(point);

      final VerticalPanel panel = new VerticalPanel();

      final Label loc = new Label("");
      panel.add(loc);
      geocoder.getLocations(point, new LocationCallback()
      {
         public void onFailure(int statusCode)
         {
            Window.alert("Failed to geocode" + ". Status: " + statusCode + " "
                  + StatusCodes.getName(statusCode));
         }

         public void onSuccess(JsArray<Placemark> locations)
         {
            Placemark location = locations.get(0);
            if (location.getStreet() != null)
            {
               loc.setText(location.getStreet());
            }
         }
      });
      panel.add(new Label("\n"));
      final Label poczatek = new Label("- początek trasy");
      poczatek.addClickListener(new ClickListener()
      {

         public void onClick(Widget sender)
         {
            // TODO Auto-generated method stub

         }
      });
      final Label koniec = new Label("- koniec trasy");
      koniec.addClickListener(new ClickListener()
      {

         public void onClick(Widget sender)
         {
            // TODO Auto-generated method stub

         }
      });
      panel.add(new Label("Oznacz jako:"));
      panel.add(poczatek);
      panel.add(koniec);
      panel.add(new Label("\n"));
      final Label usun = new Label("Usuń");
      usun.addClickListener(new ClickListener()
      {
         public void onClick(Widget sender)
         {
            map.removeOverlay(marker);
         }
      });
      panel.add(usun);

      final InfoWindowContent content = new InfoWindowContent(panel);
      marker.addMarkerClickHandler(new MarkerClickHandler()
      {
         public void onClick(MarkerClickEvent event)
         {
            info = map.getInfoWindow();
            info.open(marker, content);
         }
      });
      map.addOverlay(marker);
      info = map.getInfoWindow();
      info.open(marker, content);
   }

   public Client()
   {
      service = GttService.Util.getInstance();

      geocoder = new Geocoder();

      address = new TextBox();
      address.setVisibleLength(60);
      address.addKeyboardListener(new KeyboardListenerAdapter()
      {
         public void onKeyPress(Widget sender, char keyCode, int modifiers)
         {
            if (keyCode == (char) KEY_ENTER)
            {
               find();
            }
         }
      });

      submit = new Button("Szukaj");
      submit.addClickListener(new ClickListener()
      {
         public void onClick(Widget sender)
         {
            find();
         }
      });

      map = new MapWidget(LatLng.newInstance(51.1078852, 17.0385376), 14);
      map.setSize("640px", "500px");
      map.addControl(new LargeMapControl());
      map.addControl(new MapTypeControl());
      map.setContinuousZoom(true);
      map.setDoubleClickZoom(false);
      map.setScrollWheelZoomEnabled(true);
      map.addMapDoubleClickHandler(new MapDoubleClickHandler()
      {
         public void onDoubleClick(MapDoubleClickEvent event)
         {
            map.panTo(event.getLatLng());
            addSpotMarker(event.getLatLng());
         }
      });
      map.clearOverlays();
   }

   public void onModuleLoad()
   {
      DockPanel mainPanel = new DockPanel();
      mainPanel.setSpacing(4);
      mainPanel.setSize("1000px", "700px");

      Panel upperPanel = new HorizontalPanel();
      upperPanel.add(address);
      upperPanel.add(submit);

      l = new TextArea();
      l.setText("W tym miejcsu będzie TabPanel");
      l.setVisibleLines(50);
      l.setSize("150px", "100%");

      mainPanel.add(l, DockPanel.WEST);
      mainPanel.add(upperPanel, DockPanel.NORTH);
      mainPanel.add(map, DockPanel.CENTER);

      RootPanel.get().add(mainPanel);
   }
}

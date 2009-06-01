package org.wroc.pwr.gtt.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.wroc.pwr.gtt.server.Coordinates;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
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
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.geom.Size;
import com.google.gwt.maps.client.overlay.Icon;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.MarkerOptions;
import com.google.gwt.maps.client.overlay.Overlay;
import com.google.gwt.maps.client.event.MapClickHandler;
import com.google.gwt.maps.client.event.MapDoubleClickHandler;
import com.google.gwt.maps.client.event.MapMouseMoveHandler;
import com.google.gwt.maps.client.event.MarkerClickHandler;
import com.google.gwt.maps.client.event.MarkerDragEndHandler;
import com.google.gwt.maps.client.event.MarkerDragStartHandler;

public class Client implements EntryPoint
{
   private GttServiceAsync service;
   private MapWidget map;
   private Image addressSubmit;
   private TextBox addressSearch;
   private Button stationSubmit;
   private TextBox stationSearch;
   private Button lineSubmit;
   private SuggestBox lineSearch;
   private ListBox variantList;
   private Geocoder geocoder;
   private InfoWindow info;
   private ArrayList<Integer> stationsIds;
   private ArrayList<LatLng> stationsCoords;
   private ArrayList<Marker> currentStations;
   private VerticalPanel route;
   private Marker startSpot;
   private Marker endSpot;

   private void findPlace()
   {
      geocoder.getLatLng("wrocław" + addressSearch.getText(),
            new LatLngCallback()
            {
               public void onFailure()
               {
                  Window.alert("Miejsce nie znalezione.");
               }

               public void onSuccess(LatLng point)
               {
                  map.setCenter(point);
                  addSpotMarker(point);
               }
            });
   }

   private void findStation()
   {
      for (Marker m : currentStations)
      {
         map.removeOverlay(m);
      }
      route.clear();
      if (stationSearch.getText().length() < 3)
      {
         Window.alert("Nazwa przystanku powinna mieć przynajmniej 3 znaki.");
         return;
      }
      service.getPrzystIds(stationSearch.getText(),
            new AsyncCallback<ArrayList<Integer>>()
            {
               public void onFailure(Throwable caught)
               {
                  Window
                        .alert("Failed to fetch station ids for given station name. "
                              + caught.getMessage());
               }

               public void onSuccess(ArrayList<Integer> result)
               {
                  for (int i = 0; i < result.size(); i++)
                  {
                     Integer id = result.get(i);
                     if (stationsIds.contains(id))
                     {
                        addStationMarker(id, stationsCoords.get(stationsIds
                              .indexOf(id)));
                     }
                  }
               }
            });
   }

   private void showLine()
   {
      for (Marker m : currentStations)
      {
         map.removeOverlay(m);
      }
      route.clear();
      service.getLiniaId(lineSearch.getText(), variantList.getValue(variantList
            .getSelectedIndex()), new AsyncCallback<Integer>()
      {
         public void onFailure(Throwable caught)
         {
            Window
                  .alert("Failed to fetch lines id for given name and variant. "
                        + caught.getMessage());
         }

         public void onSuccess(Integer result)
         {
            service.getTrasa(result, new AsyncCallback<ArrayList<Integer>>()
            {
               public void onFailure(Throwable caught)
               {
                  Window
                        .alert("Failed to fetch stations' ids for given line id. "
                              + caught.getMessage());
               }

               public void onSuccess(ArrayList<Integer> result)
               {
                  for (int i = 0; i < result.size(); i++)
                  {
                     Integer id = result.get(i);
                     if (stationsIds.contains(id))
                     {
                        addStationMarker(id, stationsCoords.get(stationsIds
                              .indexOf(id)));
                     }
                  }
               }
            });
         }
      });
   }

   private void addSpotMarker(LatLng point)
   {
      map.panTo(point);
      MarkerOptions options = MarkerOptions.newInstance();
      options.setDraggable(true);
      Icon icon = Icon
            .newInstance("http://labs.google.com/ridefinder/images/mm_20_red.png");
      icon
            .setShadowURL("http://labs.google.com/ridefinder/images/mm_20_shadow.png");
      icon.setIconSize(Size.newInstance(12, 20));
      icon.setShadowSize(Size.newInstance(22, 20));
      icon.setIconAnchor(Point.newInstance(6, 20));
      icon.setInfoWindowAnchor(Point.newInstance(5, 1));
      options.setIcon(icon);
      final Marker marker = new Marker(point, options);

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
            if (startSpot != null)
            {
               LatLng point = startSpot.getLatLng();
               map.removeOverlay(startSpot);
               if (stationsCoords.contains(point) == false)
               {
                  addSpotMarker(point);
               }
               if (stationsCoords.contains(point) == false)
               {
                  addSpotMarker(point);
               }
               else
               {
                  boolean isCurrent = false;
                  for (int i = 0; i < currentStations.size(); i++)
                  {
                     if (currentStations.get(i).getLatLng() == point)
                     {
                        isCurrent = true;
                        break;
                     }
                  }
                  if (isCurrent)
                  {
                     addStationMarker(stationsIds.get(stationsCoords
                           .indexOf(point)), point);
                  }
               }
            }
            LatLng point = marker.getLatLng();
            map.removeOverlay(marker);
            addStartMarker(point);
         }
      });
      final Label koniec = new Label("- koniec trasy");
      koniec.addClickListener(new ClickListener()
      {
         public void onClick(Widget sender)
         {
            if (endSpot != null)
            {
               LatLng point = endSpot.getLatLng();
               map.removeOverlay(endSpot);
               if (stationsCoords.contains(point) == false)
               {
                  addSpotMarker(point);
               }
               if (stationsCoords.contains(point) == false)
               {
                  addSpotMarker(point);
               }
               else
               {
                  boolean isCurrent = false;
                  for (int i = 0; i < currentStations.size(); i++)
                  {
                     if (currentStations.get(i).getLatLng() == point)
                     {
                        isCurrent = true;
                        break;
                     }
                  }
                  if (isCurrent)
                  {
                     addStationMarker(stationsIds.get(stationsCoords
                           .indexOf(point)), point);
                  }
               }
            }
            LatLng point = marker.getLatLng();
            map.removeOverlay(marker);
            addEndMarker(point);
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
      marker.addMarkerDragEndHandler(new MarkerDragEndHandler()
      {
         public void onDragEnd(MarkerDragEndEvent event)
         {
            addSpotMarker(marker.getLatLng());
            map.removeOverlay(marker);
         }
      });

      marker.addMarkerDragStartHandler(new MarkerDragStartHandler()
      {
         public void onDragStart(MarkerDragStartEvent event)
         {
            info.setVisible(false);
         }
      });

      map.addOverlay(marker);
      info = map.getInfoWindow();
      info.open(marker, content);
   }

   private void addStationMarker(Integer stationId, LatLng point)
   {
      if (stationsIds.size() == 0)
      {
         return;
      }
      final Integer przyst_id = stationId;
      map.panTo(point);
      MarkerOptions options = MarkerOptions.newInstance();
      Icon icon = Icon
            .newInstance("http://labs.google.com/ridefinder/images/mm_20_blue.png");
      icon
            .setShadowURL("http://labs.google.com/ridefinder/images/mm_20_shadow.png");
      icon.setIconSize(Size.newInstance(12, 20));
      icon.setShadowSize(Size.newInstance(22, 20));
      icon.setIconAnchor(Point.newInstance(6, 20));
      icon.setInfoWindowAnchor(Point.newInstance(5, 1));
      options.setIcon(icon);
      final Marker marker = new Marker(point, options);

      final VerticalPanel panel = new VerticalPanel();

      final Label name = new Label("");
      final Label station = new Label("");
      route.add(station);
      panel.add(name);
      service.getPrzystNazwa(stationId, new AsyncCallback<String>()
      {
         public void onFailure(Throwable caught)
         {
            Window
                  .alert("Failed to fetch station name. " + caught.getMessage());
         }

         public void onSuccess(String result)
         {
            name.setText(result);
            station.setText(result);
         }
      });

      final HorizontalPanel lines = new HorizontalPanel();
      service.getLinie(stationId, new AsyncCallback<ArrayList<String>>()
      {
         public void onFailure(Throwable caught)
         {
            Window.alert("Failed to fetch lines. " + caught.getMessage());
         }

         public void onSuccess(ArrayList<String> result)
         {
            for (String lineName : result)
            {
               Label lineN = new Label(lineName);
               lines.add(lineN);
               lineN.addClickListener(new ClickListener()
               {
                  public void onClick(Widget sender)
                  {
                     // ((Label) sender).getText();
                  }
               });
               lines.add(new Label(", "));
            }
            lines.remove(2 * result.size() - 1);
         }
      });
      panel.add(lines);

      panel.add(new Label("\n"));
      final Label poczatek = new Label("- początek trasy");
      poczatek.addClickListener(new ClickListener()
      {
         public void onClick(Widget sender)
         {
            if (startSpot != null)
            {
               LatLng point = startSpot.getLatLng();
               map.removeOverlay(startSpot);
               if (stationsCoords.contains(point) == false)
               {
                  addSpotMarker(point);
               }
               else
               {
                  boolean isCurrent = false;
                  for (int i = 0; i < currentStations.size(); i++)
                  {
                     if (currentStations.get(i).getLatLng() == point)
                     {
                        isCurrent = true;
                        break;
                     }
                  }
                  if (isCurrent)
                  {
                     addStationMarker(stationsIds.get(stationsCoords
                           .indexOf(point)), point);
                  }
               }
            }
            LatLng point = marker.getLatLng();
            map.removeOverlay(marker);
            addStartMarker(point);
         }
      });
      final Label koniec = new Label("- koniec trasy");
      koniec.addClickListener(new ClickListener()
      {
         public void onClick(Widget sender)
         {
            if (endSpot != null)
            {
               LatLng point = endSpot.getLatLng();
               map.removeOverlay(endSpot);
               if (stationsCoords.contains(point) == false)
               {
                  addSpotMarker(point);
               }
               else
               {
                  boolean isCurrent = false;
                  for (int i = 0; i < currentStations.size(); i++)
                  {
                     if (currentStations.get(i).getLatLng() == point)
                     {
                        isCurrent = true;
                        break;
                     }
                  }
                  if (isCurrent)
                  {
                     addStationMarker(stationsIds.get(stationsCoords
                           .indexOf(point)), point);
                  }
               }
            }
            LatLng point = marker.getLatLng();
            map.removeOverlay(marker);
            addEndMarker(point);
         }
      });
      panel.add(new Label("Oznacz jako:"));
      panel.add(poczatek);
      panel.add(koniec);

      final InfoWindowContent content = new InfoWindowContent(panel);
      marker.addMarkerClickHandler(new MarkerClickHandler()
      {
         public void onClick(MarkerClickEvent event)
         {
            info = map.getInfoWindow();
            info.open(marker, content);
         }
      });
      station.addClickListener(new ClickListener()
      {
         public void onClick(Widget sender)
         {
            map.panTo(marker.getLatLng());
            info = map.getInfoWindow();
            info.open(marker, content);
         }
      });

      map.addOverlay(marker);
      currentStations.add(marker);
   }

   private void addStartMarker(LatLng point)
   {
      map.panTo(point);
      MarkerOptions options = MarkerOptions.newInstance();
      options.setDraggable(true);
      Icon icon = Icon
            .newInstance("http://www.google.com/intl/en_ALL/mapfiles/dd-start.png");
      icon
            .setShadowURL("http://www.google.com/intl/en_ALL/mapfiles/shadow50.png");
      icon.setIconSize(Size.newInstance(20, 34));
      icon.setShadowSize(Size.newInstance(37, 34));
      icon.setIconAnchor(Point.newInstance(9, 34));
      icon.setInfoWindowAnchor(Point.newInstance(9, 2));
      options.setIcon(icon);
      final Marker marker = new Marker(point, options);

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
      final Label anuluj = new Label("Anuluj");
      anuluj.addClickListener(new ClickListener()
      {
         public void onClick(Widget sender)
         {
            LatLng point = marker.getLatLng();
            map.removeOverlay(marker);
            startSpot = null;
            if (stationsCoords.contains(point) == false)
            {
               addSpotMarker(point);
            }
            else
            {
               boolean isCurrent = false;
               for (int i = 0; i < currentStations.size(); i++)
               {
                  if (currentStations.get(i).getLatLng() == point)
                  {
                     isCurrent = true;
                     break;
                  }
               }
               if (isCurrent)
               {
                  addStationMarker(stationsIds.get(stationsCoords
                        .indexOf(point)), point);
               }
            }
         }
      });
      panel.add(anuluj);
      panel.add(new Label("\n"));
      if (stationsCoords.contains(point) == false)
      {
         final Label usun = new Label("Usuń");
         usun.addClickListener(new ClickListener()
         {
            public void onClick(Widget sender)
            {
               map.removeOverlay(marker);
            }
         });
         panel.add(usun);
      }

      final InfoWindowContent content = new InfoWindowContent(panel);
      marker.addMarkerClickHandler(new MarkerClickHandler()
      {
         public void onClick(MarkerClickEvent event)
         {
            info = map.getInfoWindow();
            info.open(marker, content);
         }
      });
      marker.addMarkerDragEndHandler(new MarkerDragEndHandler()
      {
         public void onDragEnd(MarkerDragEndEvent event)
         {
            addStartMarker(marker.getLatLng());
            map.removeOverlay(marker);
         }
      });

      marker.addMarkerDragStartHandler(new MarkerDragStartHandler()
      {
         public void onDragStart(MarkerDragStartEvent event)
         {
            info.setVisible(false);
         }
      });

      map.addOverlay(marker);
      info = map.getInfoWindow();
      info.open(marker, content);
      startSpot = marker;
   }

   private void addEndMarker(LatLng point)
   {
      map.panTo(point);
      MarkerOptions options = MarkerOptions.newInstance();
      options.setDraggable(true);
      Icon icon = Icon
            .newInstance("http://www.google.com/intl/en_ALL/mapfiles/dd-end.png");
      icon
            .setShadowURL("http://www.google.com/intl/en_ALL/mapfiles/shadow50.png");
      icon.setIconSize(Size.newInstance(20, 34));
      icon.setShadowSize(Size.newInstance(37, 34));
      icon.setIconAnchor(Point.newInstance(9, 34));
      icon.setInfoWindowAnchor(Point.newInstance(9, 2));
      options.setIcon(icon);
      final Marker marker = new Marker(point, options);

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
      final Label anuluj = new Label("Anuluj");
      anuluj.addClickListener(new ClickListener()
      {
         public void onClick(Widget sender)
         {
            LatLng point = marker.getLatLng();
            map.removeOverlay(marker);
            endSpot = null;
            if (stationsCoords.contains(point) == false)
            {
               addSpotMarker(point);
            }
            else
            {
               boolean isCurrent = false;
               for (int i = 0; i < currentStations.size(); i++)
               {
                  if (currentStations.get(i).getLatLng() == point)
                  {
                     isCurrent = true;
                     break;
                  }
               }
               if (isCurrent)
               {
                  addStationMarker(stationsIds.get(stationsCoords
                        .indexOf(point)), point);
               }
            }
         }
      });
      panel.add(anuluj);
      panel.add(new Label("\n"));
      if (stationsCoords.contains(point) == false)
      {
         final Label usun = new Label("Usuń");
         usun.addClickListener(new ClickListener()
         {
            public void onClick(Widget sender)
            {
               map.removeOverlay(marker);
            }
         });
         panel.add(usun);
      }

      final InfoWindowContent content = new InfoWindowContent(panel);
      marker.addMarkerClickHandler(new MarkerClickHandler()
      {
         public void onClick(MarkerClickEvent event)
         {
            info = map.getInfoWindow();
            info.open(marker, content);
         }
      });
      marker.addMarkerDragEndHandler(new MarkerDragEndHandler()
      {
         public void onDragEnd(MarkerDragEndEvent event)
         {
            addEndMarker(marker.getLatLng());
            map.removeOverlay(marker);
         }
      });

      marker.addMarkerDragStartHandler(new MarkerDragStartHandler()
      {
         public void onDragStart(MarkerDragStartEvent event)
         {
            info.setVisible(false);
         }
      });

      map.addOverlay(marker);
      info = map.getInfoWindow();
      info.open(marker, content);
      endSpot = marker;
   }

   public Client()
   {
      geocoder = new Geocoder();

      stationsIds = new ArrayList<Integer>();
      stationsCoords = new ArrayList<LatLng>();
      currentStations = new ArrayList<Marker>();
      route = new VerticalPanel();

      stationSearch = new TextBox();
      stationSearch.addKeyboardListener(new KeyboardListenerAdapter()
      {
         public void onKeyPress(Widget sender, char keyCode, int modifiers)
         {
            if (keyCode == (char) KEY_ENTER)
            {
               findStation();
            }
         }
      });
      stationSubmit = new Button("Szukaj");
      stationSubmit.addClickListener(new ClickListener()
      {
         public void onClick(Widget sender)
         {
            findStation();
         }
      });

      final MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();
      lineSearch = new SuggestBox(oracle);
      lineSearch.addKeyboardListener(new KeyboardListenerAdapter()
      {
         public void onKeyPress(Widget sender, char keyCode, int modifiers)
         {
            if (keyCode == (char) KEY_ENTER)
            {
               variantList.clear();
               service.getWarianty(lineSearch.getText(),
                     new AsyncCallback<ArrayList<String>>()
                     {
                        public void onFailure(Throwable caught)
                        {
                           Window.alert("Failed to fetch variants. "
                                 + caught.getMessage());
                        }

                        public void onSuccess(ArrayList<String> result)
                        {
                           for (int i = 0; i < result.size(); i++)
                           {
                              variantList.addItem(result.get(i));
                           }
                        }
                     });
            }
         }
      });
      lineSubmit = new Button("Pokaż");
      lineSubmit.addClickListener(new ClickListener()
      {
         public void onClick(Widget sender)
         {
            showLine();
         }
      });
      variantList = new ListBox(false);

      addressSearch = new TextBox();
      addressSearch.addKeyboardListener(new KeyboardListenerAdapter()
      {
         public void onKeyPress(Widget sender, char keyCode, int modifiers)
         {
            if (keyCode == (char) KEY_ENTER)
            {
               findPlace();
            }
         }
      });
      addressSubmit = new Image("gfx/loup.jpg");
      addressSubmit.setPixelSize(22, 19);
      addressSubmit.addClickListener(new ClickListener()
      {
         public void onClick(Widget sender)
         {
            findPlace();
         }
      });

      map = new MapWidget(LatLng.newInstance(51.1078852, 17.0385376), 13);
      map.addControl(new LargeMapControl());
      map.addControl(new MapTypeControl());
      map.setContinuousZoom(true);
      map.setDoubleClickZoom(false);
      map.setScrollWheelZoomEnabled(true);
      map.addMapClickHandler(new MapClickHandler()
      {
         public void onClick(MapClickEvent event)
         {
            if (event.getOverlay() == null)
            {
               // System.out.println(event.getLatLng().getLatitude());
            }
         }
      });
      map.addMapDoubleClickHandler(new MapDoubleClickHandler()
      {
         public void onDoubleClick(MapDoubleClickEvent event)
         {
            map.panTo(event.getLatLng());
            addSpotMarker(event.getLatLng());
         }
      });
      map.clearOverlays();

      service = GttService.Util.getInstance();
      service
            .getAllCoordinates(new AsyncCallback<HashMap<Integer, ArrayList<Double>>>()
            {
               public void onFailure(Throwable caught)
               {
                  Window.alert("Failed to fetch stations info. "
                        + caught.getMessage());
               }

               public void onSuccess(HashMap<Integer, ArrayList<Double>> result)
               {
                  Set<Integer> set = result.keySet();
                  for (Integer key : set)
                  {
                     stationsIds.add(key);
                     stationsCoords.add(LatLng.newInstance(result.get(key).get(
                           0), result.get(key).get(1)));
                  }
               }
            });
      service.getLinie(new AsyncCallback<HashMap<Integer, ArrayList<String>>>()
      {
         public void onFailure(Throwable caught)
         {
            Window.alert("Failed to fetch line names. " + caught.getMessage());
         }

         public void onSuccess(HashMap<Integer, ArrayList<String>> result)
         {
            Set<Integer> ids = result.keySet();
            for (Integer id : ids)
            {
               for (String name : result.get(id))
               {
                  oracle.add(name);
               }
            }
         }
      });
   }

   public void onModuleLoad()
   {
      VerticalPanel mainPanel = new VerticalPanel();

      final AbsolutePanel center = new AbsolutePanel();

      Image background = new Image();
      background.setUrl("gfx/main.jpg");
      background.setPixelSize(800, 629);
      center.add(background);

      addressSearch.setVisibleLength(25);
      center.add(addressSearch, 36, 162);
      center.add(addressSubmit, 226, 162);

      map.setSize("498px", "347px");
      center.add(map, 264, 148);

      ScrollPanel scroll = new ScrollPanel();
      scroll.setSize("190px", "300px");
      scroll.setWidget(route);
      center.add(scroll, 41, 295);

      center.add(new HTML("<small>wyszukaj przystanek:</small>"), 294, 528);
      stationSearch.setVisibleLength(20);
      center.add(stationSearch, 420, 524);
      stationSubmit.setPixelSize(57, 19);
      stationSubmit.addStyleDependentName("station");
      center.add(stationSubmit, 590, 524);

      center.add(new HTML("<small>dostępne linie:</small>"), 294, 558);
      lineSearch.setLimit(4);
      center.add(lineSearch, 420, 554);
      lineSubmit.setPixelSize(57, 19);
      lineSubmit.addStyleDependentName("station");
      center.add(lineSubmit, 590, 554);
      variantList.setWidth("140px");
      center.add(variantList, 420, 584);

      mainPanel.add(center);

      Image footer = new Image();
      footer.setUrl("gfx/foot.jpg");
      footer.setPixelSize(755, 70);
      mainPanel.add(footer);

      RootPanel.get().add(mainPanel);
   }
}

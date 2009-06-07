package org.wroc.pwr.gtt.client;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
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
import com.google.gwt.maps.client.event.MapClickHandler;
import com.google.gwt.maps.client.event.MapDoubleClickHandler;
import com.google.gwt.maps.client.event.MarkerClickHandler;
import com.google.gwt.maps.client.event.MarkerDragEndHandler;
import com.google.gwt.maps.client.event.MarkerDragStartHandler;

public class Client implements EntryPoint
{
   private GttServiceAsync service;
   private MapWidget map;
   private MapClickHandler addSpotHandler;
   private ToggleButton addSpotButton;
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
   private Image startIcon;
   private Image endIcon;
   private HTML start;
   private HTML end;
   private CheckBox normalne;
   private CheckBox pospieszne;
   private CheckBox nocne;
   private Button findCourseButton;
   private ListBox courseList;

   private void findCourse()
   {
      ;
   }

   private void showCourse()
   {
      ;
   }

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
      service.getStopIds(stationSearch.getText(),
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
      service.getLineId(lineSearch.getText(), variantList.getValue(variantList
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
            service.getLineRoute(result,
                  new AsyncCallback<ArrayList<Integer>>()
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
                              addStationMarker(id, stationsCoords
                                    .get(stationsIds.indexOf(id)));
                           }
                        }
                     }
                  });
         }
      });
   }

   private void showTimetable(Integer stationId, String stationName,
         String lineName)
   {
      final TimetableData data = new TimetableData();
      data.setStopName(stationName);
      data.setLineName(lineName);
      service.getStopLineTable(stationId, lineName,
            new AsyncCallback<HashMap<Integer, ArrayList<Time>>>()
            {
               public void onFailure(Throwable caught)
               {
                  Window.alert("Fetching Timetable failed. "
                        + caught.getMessage());
               }

               public void onSuccess(HashMap<Integer, ArrayList<Time>> result)
               {
                  HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> timetable = new HashMap<Integer, HashMap<Integer, ArrayList<Integer>>>();
                  for (Integer day_id : result.keySet())
                  {
                     HashMap<Integer, ArrayList<Integer>> times = new HashMap<Integer, ArrayList<Integer>>();
                     for (Time time : result.get(day_id))
                     {
                        Integer hh = time.getHours();
                        if (!times.containsKey(hh))
                        {
                           times.put(hh, new ArrayList<Integer>());
                        }
                        times.get(hh).add(time.getMinutes());
                     }
                     timetable.put(day_id, times);
                  }
                  data.setTimetable(timetable);
                  service.getDayNames(new ArrayList<Integer>(result.keySet()),
                        new AsyncCallback<HashMap<Integer, String>>()
                        {
                           public void onFailure(Throwable caught)
                           {
                              Window
                                    .alert("Failed to fetch day names hashmap. "
                                          + caught.getMessage());
                           }

                           public void onSuccess(HashMap<Integer, String> result)
                           {
                              data.setDayNames(result);
                              if (data.getStopList() != null
                                    && data.getTypeName() != null)
                              {
                                 createTimetableWindow(data);
                              }
                           }
                        });
               }
            });
      service.getStopNames(lineName, stationId,
            new AsyncCallback<ArrayList<String>>()
            {
               public void onFailure(Throwable caught)
               {
                  Window.alert("Failed to fetch stop names via line name. "
                        + caught.getMessage());
               }

               public void onSuccess(ArrayList<String> result)
               {
                  data.setStopList(result);
                  if (data.getDayNames() != null && data.getTypeName() != null)
                  {
                     createTimetableWindow(data);
                  }
               }
            });
      service.getTypeNameViaLine(lineName, new AsyncCallback<String>()
      {
         public void onFailure(Throwable caught)
         {
            Window.alert("Failed to fetch line's type name. "
                  + caught.getMessage());
         }

         public void onSuccess(String result)
         {
            data.setTypeName(result);
            if (data.getDayNames() != null && data.getStopList() != null)
            {
               createTimetableWindow(data);
            }
         }
      });
   }

   private void createTimetableWindow(TimetableData data)
   {
      final FlexTable timetable = new FlexTable();
      timetable.addStyleName("gwt-FlexTable");
      FlexCellFormatter cellFormatter = timetable.getFlexCellFormatter();

      timetable.setHTML(0, 0, "<small>" + data.getTypeName()
            + "</small><br><font size=\"5\"><b>" + data.getLineName()
            + "</b></font size=\"5\">");
      cellFormatter.setAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER,
            HasVerticalAlignment.ALIGN_MIDDLE);
      cellFormatter.setRowSpan(0, 0, 2);

      timetable.setHTML(0, 1, "Przystanek: <big>" + data.getStopName()
            + "</big>");
      ArrayList<Integer> day_ids = new ArrayList<Integer>(data.getDayNames()
            .keySet());
      Collections.sort(day_ids);
      cellFormatter.setColSpan(0, 1, day_ids.size() * 2);

      Set<Integer> hoursSet = new HashSet<Integer>();
      for (Integer day_id : day_ids)
      {
         hoursSet.addAll(new HashSet<Integer>(data.getTimetable().get(day_id)
               .keySet()));
         timetable.setHTML(1, day_ids.indexOf(day_id), data.getDayNames().get(
               day_id));
         cellFormatter.setColSpan(1, day_ids.indexOf(day_id), 2);
      }
      ArrayList<Integer> hours = new ArrayList<Integer>(hoursSet);
      Collections.sort(hours);
      while ((hours.get(hours.size() - 1) + 1) % 24 == hours.get(0))
      {
         hours.add(hours.get(0));
         hours.remove(0);
      }

      Integer rowsNr = (25 + hours.get(hours.size() - 1) - hours.get(0)) % 24;
      String stopNames = "";
      for (int i = 0; i < data.getStopList().size(); i++)
      {
         if (data.getStopList().get(i).equals(data.getStopName()))
         {
            stopNames += "<big>" + data.getStopList().get(i) + "</big><br>";
         }
         else
         {
            stopNames += data.getStopList().get(i) + "<br>";
         }
      }
      timetable.setHTML(2, 0, stopNames);
      cellFormatter.setAlignment(2, 0, HasHorizontalAlignment.ALIGN_CENTER,
            HasVerticalAlignment.ALIGN_TOP);
      cellFormatter.setRowSpan(2, 0, rowsNr + 1);

      for (int d = 0; d < day_ids.size(); d++)
      {
         for (int i = 0; i < rowsNr; i++)
         {
            Integer j = (hours.get(0) + i) % 24;
            int k;
            if (i == 0)
               k = d * 2 + 1;
            else
               k = d * 2;
            timetable.setHTML(2 + i, k, "<small><b>" + j.toString()
                  + "</b></small>");
            cellFormatter.setHeight(2 + i, k, "20px");
            cellFormatter.setWidth(2 + i, k, "15px");
            cellFormatter.setAlignment(2 + i, k,
                  HasHorizontalAlignment.ALIGN_CENTER,
                  HasVerticalAlignment.ALIGN_MIDDLE);
            if (data.getTimetable().get(day_ids.get(0)).containsKey(j))
            {
               String mins = "<small>";
               ArrayList<Integer> minsList = new ArrayList<Integer>(data
                     .getTimetable().get(day_ids.get(0)).get(j));
               Collections.sort(minsList);
               for (Integer min : minsList)
               {
                  mins += min + " ";
               }
               mins = mins.substring(0, mins.length() - 1) + "</small>";
               timetable.setHTML(2 + i, k + 1, mins);
            }
            else
               timetable.setHTML(2 + i, k + 1, " ");
         }
      }
      timetable.setHTML(2 + rowsNr, 1, "");
      cellFormatter.setColSpan(2 + rowsNr, 1, day_ids.size() * 2);

      final PopupPanel timetableWindow = new PopupPanel(true);
      timetableWindow.setAnimationEnabled(true);
      timetableWindow.setWidget(timetable);
      timetableWindow.center();
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
            addStartMarker(loc.getText(), point);
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
            addEndMarker(loc.getText(), point);
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
      service.getStopName(stationId, new AsyncCallback<String>()
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
      service.getLinesViaStop(stationId, new AsyncCallback<ArrayList<String>>()
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
                     showTimetable(przyst_id, station.getText(),
                           ((Label) sender).getText());
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
            addStartMarker(name.getText(), point);
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
            addEndMarker(name.getText(), point);
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

   private void addStartMarker(String place, LatLng point)
   {
      map.panTo(point);
      MarkerOptions options = MarkerOptions.newInstance();
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

      final Label loc = new Label(place);
      panel.add(loc);
      start.setHTML(place);
      panel.add(new Label("\n"));
      final Label anuluj = new Label("Anuluj");
      anuluj.addClickListener(new ClickListener()
      {
         public void onClick(Widget sender)
         {
            LatLng point = marker.getLatLng();
            map.removeOverlay(marker);
            startSpot = null;
            start.setHTML("");
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
               startSpot = null;
               start.setHTML("");
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

      map.addOverlay(marker);
      info = map.getInfoWindow();
      info.open(marker, content);
      startSpot = marker;
   }

   private void addEndMarker(String place, LatLng point)
   {
      map.panTo(point);
      MarkerOptions options = MarkerOptions.newInstance();
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

      final Label loc = new Label(place);
      panel.add(loc);
      end.setHTML(place);
      panel.add(new Label("\n"));
      final Label anuluj = new Label("Anuluj");
      anuluj.addClickListener(new ClickListener()
      {
         public void onClick(Widget sender)
         {
            LatLng point = marker.getLatLng();
            map.removeOverlay(marker);
            endSpot = null;
            end.setHTML("");
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
               endSpot = null;
               end.setHTML("");
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
               service.getVersions(lineSearch.getText(),
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

      addSpotHandler = new MapClickHandler()
      {
         public void onClick(MapClickEvent event)
         {
            addSpotMarker(event.getLatLng());
            addSpotButton.setDown(false);
            map.removeMapClickHandler(addSpotHandler);
         }
      };
      addSpotButton = new ToggleButton(new Image(
            "http://labs.google.com/ridefinder/images/mm_20_red.png"));
      addSpotButton.addClickListener(new ClickListener()
      {
         public void onClick(Widget sender)
         {
            if (addSpotButton.isDown())
               map.addMapClickHandler(addSpotHandler);
            else
               map.removeMapClickHandler(addSpotHandler);
         }
      });

      normalne = new CheckBox("normalne", true);
      normalne.setChecked(true);
      pospieszne = new CheckBox("pospieszne", true);
      pospieszne.setChecked(false);
      nocne = new CheckBox("nocne", true);
      nocne.setChecked(false);

      startIcon = new Image(
            "http://www.google.com/intl/en_ALL/mapfiles/dd-start.png");
      startIcon.addClickListener(new ClickListener()
      {
         public void onClick(Widget sender)
         {
            if (startSpot != null)
            {
               map.panTo(startSpot.getLatLng());
            }
         }
      });
      endIcon = new Image(
            "http://www.google.com/intl/en_ALL/mapfiles/dd-end.png");
      endIcon.addClickListener(new ClickListener()
      {
         public void onClick(Widget sender)
         {
            if (endSpot != null)
            {
               map.panTo(endSpot.getLatLng());
            }
         }
      });
      start = new HTML();
      end = new HTML();
      findCourseButton = new Button("Znajdź trasę");
      findCourseButton.addClickListener(new ClickListener()
      {
         public void onClick(Widget sender)
         {
            findCourse();
         }
      });
      courseList = new ListBox(false);
      courseList.addChangeListener(new ChangeListener()
      {
         public void onChange(Widget sender)
         {
            showCourse();
         }
      });

      map = new MapWidget(LatLng.newInstance(51.1078852, 17.0385376), 13);
      map.addControl(new LargeMapControl());
      map.addControl(new MapTypeControl());
      map.setContinuousZoom(true);
      map.setDoubleClickZoom(true);
      map.setScrollWheelZoomEnabled(true);
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
      service.getLines(new AsyncCallback<HashMap<Integer, ArrayList<String>>>()
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
      background.setUrl("gfx/main_1024.jpg");
      center.add(background);

      map.setSize("638px", "437px");
      center.add(map, 337, 190);

      center.add(new HTML("znajdź adres:"), 48, 204);
      addressSearch.setVisibleLength(20);
      center.add(addressSearch, 138, 202);
      center.add(addressSubmit, 297, 202);

      center.add(new HTML("lub wskaż miejsce na mapie:"), 48, 234);
      center.add(addSpotButton, 296, 225);

      center.add(normalne, 52, 268);
      center.add(pospieszne, 52, 283);
      center.add(nocne, 52, 298);

      center.add(startIcon, 152, 257);
      center.add(start, 180, 265);
      center.add(endIcon, 152, 297);
      center.add(end, 180, 305);

      courseList.setWidth("155px");
      center.add(courseList, 56, 343);
      center.add(findCourseButton, 224, 340);

      ScrollPanel scroll = new ScrollPanel();
      scroll.setSize("267px", "380px");
      scroll.setWidget(route);
      for (int i = 0; i < 50; i++)
      {
         route.add(new Label("A"));
      }
      center.add(scroll, 56, 383);

      center.add(new HTML("wyszukaj przystanek:"), 383, 670);
      stationSearch.setVisibleLength(20);
      center.add(stationSearch, 529, 668);
      stationSubmit.setPixelSize(57, 19);
      stationSubmit.addStyleDependentName("station");
      center.add(stationSubmit, 699, 668);

      center.add(new HTML("dostępne linie:"), 383, 700);
      lineSearch.setLimit(4);
      center.add(lineSearch, 529, 698);
      lineSubmit.setPixelSize(57, 19);
      lineSubmit.addStyleDependentName("station");
      center.add(lineSubmit, 699, 698);
      variantList.setWidth("140px");
      center.add(variantList, 529, 728);

      mainPanel.add(center);

      RootPanel.get().add(mainPanel);
   }
}

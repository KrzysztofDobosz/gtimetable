package org.wroc.pwr.gtt.client;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
   private Button stopSubmit;
   private TextBox stopSearch;
   private Button lineSubmit;
   private SuggestBox lineSearch;
   private ListBox variantList;
   private Geocoder geocoder;
   private InfoWindow info;
   private ArrayList<Integer> stopsIds;
   private ArrayList<LatLng> stopsCoords;
   private ArrayList<GttMarker> currentStops;
   private VerticalPanel sidePanel;
   private GttMarker startSpot;
   private GttMarker endSpot;
   private Image startIcon;
   private Image endIcon;
   private HTML start;
   private HTML end;
   private CheckBox normalne;
   private CheckBox pospieszne;
   private CheckBox nocne;
   private Button findCourseButton;
   private ArrayList<ArrayList<ArrayList<Integer>>> courses;

   private void findCourse()
   {
      sidePanel.clear();
      if (startSpot == null || endSpot == null)
      {
         Window.alert("Nie określono punktu początkowego lub końcowego trasy");
         return;
      }
      int startStops = 3;
      int endStops = 3;
      if (stopsCoords.contains(startSpot.getLatLng()))
         startStops = 1;
      if (stopsCoords.contains(endSpot.getLatLng()))
         endStops = 1;
      service.findCourse(normalne.isChecked(), pospieszne.isChecked(), nocne
            .isChecked(), startSpot.getLatLng().getLatitude(), startSpot
            .getLatLng().getLongitude(), endSpot.getLatLng().getLatitude(),
            endSpot.getLatLng().getLongitude(), 3, startStops, endStops,
            new AsyncCallback<ArrayList<ArrayList<ArrayList<Integer>>>>()
            {
               public void onFailure(Throwable caught)
               {
                  Window.alert("Failed to find course. " + caught.getMessage());
               }

               public void onSuccess(
                     ArrayList<ArrayList<ArrayList<Integer>>> result)
               {
                  courses = result;
                  for (int i = 0; i < courses.size(); i++)
                  {
                     System.out.println(courses.get(i));
                     HorizontalPanel text = new HorizontalPanel();
                     text.add(new HTML("Trasa " + i + ": "));
                     for (int j = 0; j < courses.get(i).size(); j++)
                     {
                        for (int k = 1; k < courses.get(i).get(j).size(); k++)
                        {
                           ;
                        }
                        service.getLineName(courses.get(i).get(j).get(0),
                              new AsyncCallback<String>()
                              {
                                 public void onFailure(Throwable caught)
                                 {
                                    Window
                                          .alert("Failed to fetch line name in findCourse(). "
                                                + caught.getMessage());
                                 }

                                 public void onSuccess(String result)
                                 {
                                    ;
                                 }
                              });
                     }
                  }
                  showCourse(0);
               }
            });
   }

   private void showCourse(int courseIndex)
   {
      ;
   }

   private VerticalPanel getRoutePanel(String title, ArrayList<GttMarker> stops)
   {
      VerticalPanel routePanel = new VerticalPanel();
      HTML bigTitle = new HTML("<big>-- " + title + "</big>");
      bigTitle.addClickListener(new ClickListener()
      {
         public void onClick(Widget sender)
         {
            double lat = 0;
            double lng = 0;
            for (GttMarker stop : currentStops)
            {
               lat += stop.getLatLng().getLatitude();
               lng += stop.getLatLng().getLongitude();
            }
            map.setCenter(LatLng.newInstance(lat / currentStops.size(), lng
                  / currentStops.size()), 12);
         }
      });
      routePanel.add(bigTitle);
      for (int i = 0; i < stops.size(); i++)
      {
         final GttMarker stop = stops.get(i);
         HorizontalPanel hor = new HorizontalPanel();
         Image zoom = new Image("gfx/lupa_small.png");
         hor.add(zoom);
         final HTML stopName = new HTML("");
         ClickListener click = new ClickListener()
         {
            public void onClick(Widget sender)
            {
               map.setCenter(stop.getLatLng(), 14);
               info = map.getInfoWindow();
               info.open(stop, stop.getContent());
            }
         };
         zoom.addClickListener(click);
         stopName.addClickListener(click);
         service.getStopName(stopsIds
               .get(stopsCoords.indexOf(stop.getLatLng())),
               new AsyncCallback<String>()
               {
                  public void onFailure(Throwable caught)
                  {
                     Window
                           .alert("Failed to fetch stop's name in getRoutePanel(). "
                                 + caught.getMessage());
                  }

                  public void onSuccess(String result)
                  {
                     stopName.setHTML(result);
                  }
               });
         hor.add(stopName);
         routePanel.add(hor);
      }
      return routePanel;
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
                  GttMarker m = getSpotMarker(point);
                  map.addOverlay(m);
                  info = map.getInfoWindow();
                  info.open(m, m.getContent());
               }
            });
   }

   private void findStop()
   {
      for (GttMarker m : currentStops)
      {
         map.removeOverlay(m);
      }
      if (stopSearch.getText().length() < 3)
      {
         Window.alert("Nazwa przystanku powinna mieć przynajmniej 3 znaki.");
         return;
      }
      service.getStopIds(stopSearch.getText(),
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
                  ArrayList<GttMarker> markers = new ArrayList<GttMarker>();
                  double lat = 0;
                  double lng = 0;
                  for (int i = 0; i < result.size(); i++)
                  {
                     int id = result.get(i);
                     if (stopsIds.contains(id))
                     {
                        GttMarker m = getStopMarker(id, stopsCoords
                              .get(stopsIds.indexOf(id)));
                        lat += m.getLatLng().getLatitude();
                        lng += m.getLatLng().getLongitude();
                        markers.add(m);
                        map.addOverlay(m);
                     }
                  }
                  map.setCenter(LatLng.newInstance(lat / result.size(), lng
                        / result.size()), 12);
                  VerticalPanel vert = getRoutePanel(stopSearch.getText(),
                        markers);
                  sidePanel.clear();
                  int max = vert.getWidgetCount();
                  for (int i = 0; i < max; i++)
                  {
                     sidePanel.add(vert.getWidget(0));
                  }
               }
            });
   }

   private void showLine()
   {
      for (GttMarker m : currentStops)
      {
         map.removeOverlay(m);
      }
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
                        ArrayList<GttMarker> markers = new ArrayList<GttMarker>();
                        double lat = 0;
                        double lng = 0;
                        for (int i = 0; i < result.size(); i++)
                        {
                           int id = result.get(i);
                           if (stopsIds.contains(id))
                           {
                              GttMarker m = getStopMarker(id, stopsCoords
                                    .get(stopsIds.indexOf(id)));
                              lat += m.getLatLng().getLatitude();
                              lng += m.getLatLng().getLongitude();
                              markers.add(m);
                              map.addOverlay(m);
                           }
                        }
                        map.setCenter(LatLng.newInstance(lat / result.size(),
                              lng / result.size()), 12);
                        VerticalPanel vert = getRoutePanel(lineSearch.getText()
                              + ": "
                              + variantList.getValue(variantList
                                    .getSelectedIndex()), markers);
                        sidePanel.clear();
                        int max = vert.getWidgetCount();
                        for (int i = 0; i < max; i++)
                        {
                           sidePanel.add(vert.getWidget(0));
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
      Button close = new Button("Zamknij");
      close.addClickListener(new ClickListener()
      {
         public void onClick(Widget sender)
         {
            timetableWindow.hide();
         }
      });
      VerticalPanel panel = new VerticalPanel();
      panel.add(timetable);
      panel.add(close);
      panel.setCellHorizontalAlignment(close,
            HasHorizontalAlignment.ALIGN_RIGHT);
      timetableWindow.setWidget(panel);
      timetableWindow.center();
   }

   private GttMarker getSpotMarker(LatLng point)
   {
      map.setCenter(point);
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
      final GttMarker marker = new GttMarker(point, options);

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
               if (stopsCoords.contains(point) == false)
               {
                  GttMarker m = getSpotMarker(point);
                  map.addOverlay(m);
                  info = map.getInfoWindow();
                  info.open(m, m.getContent());
               }
               if (stopsCoords.contains(point) == false)
               {
                  GttMarker m = getSpotMarker(point);
                  map.addOverlay(m);
                  info = map.getInfoWindow();
                  info.open(m, m.getContent());
               }
               else
               {
                  boolean isCurrent = false;
                  for (int i = 0; i < currentStops.size(); i++)
                  {
                     if (currentStops.get(i).getLatLng() == point)
                     {
                        isCurrent = true;
                        break;
                     }
                  }
                  if (isCurrent)
                  {
                     map.addOverlay(getStopMarker(stopsIds.get(stopsCoords
                           .indexOf(point)), point));
                  }
               }
            }
            LatLng point = marker.getLatLng();
            map.removeOverlay(marker);
            map.addOverlay(getStartMarker(loc.getText(), point));
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
               if (stopsCoords.contains(point) == false)
               {
                  GttMarker m = getSpotMarker(point);
                  map.addOverlay(m);
                  info = map.getInfoWindow();
                  info.open(m, m.getContent());
               }
               if (stopsCoords.contains(point) == false)
               {
                  GttMarker m = getSpotMarker(point);
                  map.addOverlay(m);
                  info = map.getInfoWindow();
                  info.open(m, m.getContent());
               }
               else
               {
                  boolean isCurrent = false;
                  for (int i = 0; i < currentStops.size(); i++)
                  {
                     if (currentStops.get(i).getLatLng() == point)
                     {
                        isCurrent = true;
                        break;
                     }
                  }
                  if (isCurrent)
                  {
                     map.addOverlay(getStopMarker(stopsIds.get(stopsCoords
                           .indexOf(point)), point));
                  }
               }
            }
            LatLng point = marker.getLatLng();
            map.removeOverlay(marker);
            map.addOverlay(getEndMarker(loc.getText(), point));
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
            GttMarker m = getSpotMarker(marker.getLatLng());
            map.addOverlay(m);
            info = map.getInfoWindow();
            info.open(m, m.getContent());
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
      marker.setContent(content);

      return marker;
   }

   private GttMarker getStopMarker(Integer stopId, LatLng point)
   {
      if (stopsIds.size() == 0)
      {
         return null;
      }
      final Integer przyst_id = stopId;
      map.setCenter(point);
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
      final GttMarker marker = new GttMarker(point, options);

      final VerticalPanel panel = new VerticalPanel();

      final Label name = new Label("");
      panel.add(name);
      service.getStopName(stopId, new AsyncCallback<String>()
      {
         public void onFailure(Throwable caught)
         {
            Window.alert("Failed to fetch stop name in getStopMarker(). "
                  + caught.getMessage());
         }

         public void onSuccess(String result)
         {
            name.setText(result);
         }
      });

      final HorizontalPanel lines = new HorizontalPanel();
      service.getLinesViaStop(stopId, new AsyncCallback<ArrayList<String>>()
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
                     showTimetable(przyst_id, name.getText(), ((Label) sender)
                           .getText());
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
               if (stopsCoords.contains(point) == false)
               {
                  GttMarker m = getSpotMarker(point);
                  map.addOverlay(m);
                  info = map.getInfoWindow();
                  info.open(m, m.getContent());
               }
               else
               {
                  boolean isCurrent = false;
                  for (int i = 0; i < currentStops.size(); i++)
                  {
                     if (currentStops.get(i).getLatLng() == point)
                     {
                        isCurrent = true;
                        break;
                     }
                  }
                  if (isCurrent)
                  {
                     map.addOverlay(getStopMarker(stopsIds.get(stopsCoords
                           .indexOf(point)), point));
                  }
               }
            }
            LatLng point = marker.getLatLng();
            map.removeOverlay(marker);
            map.addOverlay(getStartMarker(name.getText(), point));
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
               if (stopsCoords.contains(point) == false)
               {
                  GttMarker m = getSpotMarker(point);
                  map.addOverlay(m);
                  info = map.getInfoWindow();
                  info.open(m, m.getContent());
               }
               else
               {
                  boolean isCurrent = false;
                  for (int i = 0; i < currentStops.size(); i++)
                  {
                     if (currentStops.get(i).getLatLng() == point)
                     {
                        isCurrent = true;
                        break;
                     }
                  }
                  if (isCurrent)
                  {
                     map.addOverlay(getStopMarker(stopsIds.get(stopsCoords
                           .indexOf(point)), point));
                  }
               }
            }
            LatLng point = marker.getLatLng();
            map.removeOverlay(marker);
            map.addOverlay(getEndMarker(name.getText(), point));
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
      marker.setContent(content);

      currentStops.add(marker);
      return marker;
   }

   private GttMarker getStartMarker(String place, LatLng point)
   {
      map.setCenter(point);
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
      final GttMarker marker = new GttMarker(point, options);

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
            if (stopsCoords.contains(point) == false)
            {
               GttMarker m = getSpotMarker(point);
               map.addOverlay(m);
               info = map.getInfoWindow();
               info.open(m, m.getContent());
            }
            else
            {
               boolean isCurrent = false;
               for (int i = 0; i < currentStops.size(); i++)
               {
                  if (currentStops.get(i).getLatLng() == point)
                  {
                     isCurrent = true;
                     break;
                  }
               }
               if (isCurrent)
               {
                  map.addOverlay(getStopMarker(stopsIds.get(stopsCoords
                        .indexOf(point)), point));
               }
            }
         }
      });
      panel.add(anuluj);
      panel.add(new Label("\n"));
      if (stopsCoords.contains(point) == false)
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
      marker.setContent(content);

      info = map.getInfoWindow();
      info.open(marker, content);
      startSpot = marker;
      return marker;
   }

   private GttMarker getEndMarker(String place, LatLng point)
   {
      map.setCenter(point);
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
      final GttMarker marker = new GttMarker(point, options);

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
            if (stopsCoords.contains(point) == false)
            {
               GttMarker m = getSpotMarker(point);
               map.addOverlay(m);
               info = map.getInfoWindow();
               info.open(m, m.getContent());
            }
            else
            {
               boolean isCurrent = false;
               for (int i = 0; i < currentStops.size(); i++)
               {
                  if (currentStops.get(i).getLatLng() == point)
                  {
                     isCurrent = true;
                     break;
                  }
               }
               if (isCurrent)
               {
                  map.addOverlay(getStopMarker(stopsIds.get(stopsCoords
                        .indexOf(point)), point));
               }
            }
         }
      });
      panel.add(anuluj);
      panel.add(new Label("\n"));
      if (stopsCoords.contains(point) == false)
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
      marker.setContent(content);

      info = map.getInfoWindow();
      info.open(marker, content);
      endSpot = marker;
      return marker;
   }

   public Client()
   {
      geocoder = new Geocoder();

      stopsIds = new ArrayList<Integer>();
      stopsCoords = new ArrayList<LatLng>();
      currentStops = new ArrayList<GttMarker>();
      sidePanel = new VerticalPanel();

      stopSearch = new TextBox();
      stopSearch.addKeyboardListener(new KeyboardListenerAdapter()
      {
         public void onKeyPress(Widget sender, char keyCode, int modifiers)
         {
            if (keyCode == (char) KEY_ENTER)
            {
               findStop();
            }
         }
      });
      stopSubmit = new Button("Szukaj");
      stopSubmit.addClickListener(new ClickListener()
      {
         public void onClick(Widget sender)
         {
            findStop();
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
            GttMarker marker = getSpotMarker(event.getLatLng());
            map.addOverlay(marker);
            info = map.getInfoWindow();
            info.open(marker, marker.getContent());
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
               map.setCenter(startSpot.getLatLng());
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
               map.setCenter(endSpot.getLatLng());
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

      map = new MapWidget(LatLng.newInstance(51.1078852, 17.0385376), 13);
      map.addControl(new LargeMapControl());
      map.addControl(new MapTypeControl());
      map.setContinuousZoom(true);
      map.setDoubleClickZoom(true);
      map.setScrollWheelZoomEnabled(false);
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
                     stopsIds.add(key);
                     stopsCoords.add(LatLng.newInstance(result.get(key).get(0),
                           result.get(key).get(1)));
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

      center.add(findCourseButton, 224, 340);

      ScrollPanel scroll = new ScrollPanel();
      scroll.setSize("267px", "380px");
      scroll.setWidget(sidePanel);
      for (int i = 0; i < 50; i++)
      {
         sidePanel.add(new Label("A"));
      }
      center.add(scroll, 56, 383);

      center.add(new HTML("wyszukaj przystanek:"), 383, 670);
      stopSearch.setVisibleLength(20);
      center.add(stopSearch, 529, 668);
      stopSubmit.setPixelSize(57, 19);
      stopSubmit.addStyleDependentName("station");
      center.add(stopSubmit, 699, 668);

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

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
import com.google.gwt.user.client.ui.DisclosurePanel;
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
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;
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
import com.google.gwt.maps.client.overlay.MarkerOptions;
import com.google.gwt.maps.client.event.MapClickHandler;
import com.google.gwt.maps.client.event.MarkerClickHandler;
import com.google.gwt.maps.client.event.MarkerDragEndHandler;
import com.google.gwt.maps.client.event.MarkerDragStartHandler;

/**
 * Glowna klasa aplikacji klienckiej. Definiuje caly wyglad i funkcjonalnosc
 * graficznego interfejsu.
 *
 * @author Krzysztof Dobosz
 *
 */
public class Client implements EntryPoint
{
   private GttServiceAsync service;
   private MapWidget map;
   private MapClickHandler addSpotHandler;
   private Image addSpotButton;
   private Button addressSubmit;
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
   private Image change;
   private CheckBox normalne;
   private CheckBox pospieszne;
   private CheckBox nocne;
   private Button findCourseButton;
   private ArrayList<ArrayList<GttMarker>> courses;
   private Button update;

   /**
    * Procedura do znajdowania tras miedzy dwoma punktami na mapie zadanymi
    * przez wspolrzedne geograficze.
    */
   private void findCourse()
   {
      courses.clear();
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
            endSpot.getLatLng().getLongitude(), 5, startStops, endStops,
            new AsyncCallback<ArrayList<ArrayList<ArrayList<Integer>>>>()
            {
               public void onFailure(Throwable caught)
               {
                  Window.alert("Failed to find course. " + caught.getMessage());
               }

               public void onSuccess(
                     ArrayList<ArrayList<ArrayList<Integer>>> result)
               {
                  sidePanel.clear();
                  for (int i = 0; i < result.size(); i++)
                  {
                     ArrayList<GttMarker> courseMarkers = new ArrayList<GttMarker>();
                     final int iPrim = i;
                     final VerticalPanel list = new VerticalPanel();
                     for (int j = 0; j < result.get(i).size(); j++)
                     {
                        final ArrayList<GttMarker> markers = new ArrayList<GttMarker>();
                        for (int k = 1; k < result.get(i).get(j).size(); k++)
                        {
                           int id = result.get(i).get(j).get(k);
                           if (stopsIds.contains(id))
                           {
                              GttMarker m = getStopMarker(id, stopsCoords
                                    .get(stopsIds.indexOf(id)));
                              markers.add(m);
                              courseMarkers.add(m);
                           }
                        }
                        final int jPrim = j;
                        service.getLineName(result.get(i).get(j).get(0),
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
                                    list.insert(getRoutePanel(result, markers),
                                          jPrim);
                                 }
                              });
                     }
                     HTML head = new HTML("<big>-- Trasa " + (i + 1)
                           + ": </big>");
                     head.addClickListener(new ClickListener()
                     {
                        public void onClick(Widget sender)
                        {
                           showCourse(iPrim);
                        }
                     });
                     courses.add(courseMarkers);
                     DisclosurePanel disc = new DisclosurePanel(head);
                     disc.setAnimationEnabled(true);
                     disc.setContent(list);
                     sidePanel.add(disc);
                  }
                  showCourse(0);
               }
            });
   }

   /**
    * Pokazuje na mapie przystanki z trasy o indeksie courseIndex.
    *
    * @param courseIndex
    */
   private void showCourse(int courseIndex)
   {
      for (GttMarker m : currentStops)
      {
         map.removeOverlay(m);
      }
      ArrayList<GttMarker> markers = courses.get(courseIndex);
      double lat = 0;
      double lng = 0;
      for (int i = 0; i < markers.size(); i++)
      {
         GttMarker m = markers.get(i);
         currentStops.add(m);
         lat += m.getLatLng().getLatitude();
         lng += m.getLatLng().getLongitude();
         map.addOverlay(m);
      }
      map.setCenter(LatLng.newInstance(lat / markers.size(), lng
            / markers.size()), 12);
   }

   /**
    * Tworzy pionowy panel z zadanym tytulem i lista przystankow.
    *
    * @param title
    * @param stops
    * @return
    */
   private VerticalPanel getRoutePanel(String title, ArrayList<GttMarker> stops)
   {
      VerticalPanel routePanel = new VerticalPanel();
      HTML bigTitle = new HTML("<big>-- " + title + "</big>");
      routePanel.add(bigTitle);
      double lat = 0;
      double lng = 0;
      for (int i = 0; i < stops.size(); i++)
      {
         final GttMarker stop = stops.get(i);
         lat += stop.getLatLng().getLatitude();
         lng += stop.getLatLng().getLongitude();
         HorizontalPanel hor = new HorizontalPanel();
         Image zoom = new Image("gfx/lupa_small.png");
         hor.add(zoom);
         final HTML stopName = new HTML("");
         ClickListener click = new ClickListener()
         {
            public void onClick(Widget sender)
            {
               map.setCenter(stop.getLatLng(), 14);
               if (startSpot != null
                     && stop.getLatLng().isEquals(startSpot.getLatLng()))
               {
                  info = map.getInfoWindow();
                  info.open(startSpot, startSpot.getContent());
               }
               else if (endSpot != null
                     && stop.getLatLng().isEquals(endSpot.getLatLng()))
               {
                  info = map.getInfoWindow();
                  info.open(endSpot, endSpot.getContent());
               }
               else
               {
                  info = map.getInfoWindow();
                  info.open(stop, stop.getContent());
               }
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
      final double latPrim = lat / stops.size();
      final double lngPrim = lng / stops.size();
      bigTitle.addClickListener(new ClickListener()
      {
         public void onClick(Widget sender)
         {
            map.setCenter(LatLng.newInstance(latPrim, lngPrim), 12);
         }
      });
      return routePanel;
   }

   /**
    * Korzystajac z geocodera znajduje na mapie miejsce dane w polu tekstowym.
    */
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

   /**
    * Znajduje wszystkie przystanki w ktorych nazwach jest ciag znakow z pola
    * tekstowego.
    */
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
                  final ArrayList<GttMarker> markers = new ArrayList<GttMarker>();
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

   /**
    * Pokazuje na mapie wszystkie przystanki danej linii przy danym wariancie.
    */
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

   /**
    * Zbiera dane potrzebne do wyswietlenia okienka z rozkladem dla danej linii
    * i danego przystanku.
    *
    * @param stationId
    * @param stationName
    * @param lineName
    */
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

   /**
    * Buduje i wyswietla rozklad odjazdow danej linii z danego przystanku.
    *
    * @param data
    */
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

   /**
    * Zwraca zwykly marker opisujacy dany punkt.
    *
    * @param point
    * @return
    */
   private GttMarker getSpotMarker(LatLng point)
   {
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
                  map.addOverlay(getSpotMarker(point));
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
            GttMarker m = getStartMarker(loc.getText(), point);
            map.addOverlay(m);
            info = map.getInfoWindow();
            info.open(m, m.getContent());
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
                  map.addOverlay(getSpotMarker(point));
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
            GttMarker m = getEndMarker(loc.getText(), point);
            map.addOverlay(m);
            info = map.getInfoWindow();
            info.open(m, m.getContent());
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

   /**
    * Zwraca marker opisujacy dany przystanek.
    *
    * @param stopId
    * @param point
    * @return
    */
   private GttMarker getStopMarker(Integer stopId, LatLng point)
   {
      if (stopsIds.size() == 0)
      {
         return null;
      }
      final Integer przyst_id = stopId;
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
                  map.addOverlay(getSpotMarker(point));
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
            GttMarker m = getStartMarker(name.getText(), point);
            map.addOverlay(m);
            info = map.getInfoWindow();
            info.open(m, m.getContent());
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
                  map.addOverlay(getSpotMarker(point));
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
            GttMarker m = getEndMarker(name.getText(), point);
            map.addOverlay(m);
            info = map.getInfoWindow();
            info.open(m, m.getContent());
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

   /**
    * Zwraca marker oznaczajacy punkt poczatkowy trasy.
    *
    * @param place
    * @param point
    * @return
    */
   private GttMarker getStartMarker(String place, LatLng point)
   {
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
            start.setHTML(".....................................");
            if (stopsCoords.contains(point) == false)
            {
               map.addOverlay(getSpotMarker(point));
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
               start.setHTML(".....................................");
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

      startSpot = marker;
      return marker;
   }

   /**
    * Zwraca marker oznaczajacy punkt koncowy trasy.
    *
    * @param place
    * @param point
    * @return
    */
   private GttMarker getEndMarker(String place, LatLng point)
   {
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
            end.setHTML(".....................................");
            if (stopsCoords.contains(point) == false)
            {
               map.addOverlay(getSpotMarker(point));
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
               end.setHTML(".....................................");
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

      endSpot = marker;
      return marker;
   }

   /**
    * Konstruktor klasy. Tworzy wszystkie pola i dodaje do nich funkcjonalnosci.
    */
   public Client()
   {
      geocoder = new Geocoder();

      stopsIds = new ArrayList<Integer>();
      stopsCoords = new ArrayList<LatLng>();
      currentStops = new ArrayList<GttMarker>();
      courses = new ArrayList<ArrayList<GttMarker>>();
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
      lineSearch.addChangeListener(new ChangeListener()
      {
         public void onChange(Widget sender)
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
      addressSubmit = new Button("Szukaj");
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
            map.removeMapClickHandler(addSpotHandler);
         }
      };
      addSpotButton = new Image("gfx/loup.jpg");
      addSpotButton.addClickListener(new ClickListener()
      {
         public void onClick(Widget sender)
         {
            map.addMapClickHandler(addSpotHandler);
         }
      });

      normalne = new CheckBox("normalne", true);
      normalne.setChecked(true);
      pospieszne = new CheckBox("pospieszne", true);
      pospieszne.setChecked(false);
      nocne = new CheckBox("nocne", true);
      nocne.setChecked(false);

      startIcon = new Image("gfx/start.png");
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
      endIcon = new Image("gfx/end.png");
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
      change = new Image("gfx/powrotna.png");
      change.addClickListener(new ClickListener()
      {
         public void onClick(Widget sender)
         {
            if (startSpot != null)
            {
               if (endSpot != null)
               {
                  GttMarker temp = startSpot;
                  String tempName = start.getHTML();
                  map.removeOverlay(startSpot);
                  map.addOverlay(getStartMarker(end.getHTML(), endSpot
                        .getLatLng()));
                  map.removeOverlay(endSpot);
                  map.addOverlay(getEndMarker(tempName, temp.getLatLng()));
               }
               else
               {
                  map.addOverlay(getEndMarker(start.getHTML(), startSpot
                        .getLatLng()));
                  map.removeOverlay(startSpot);
                  startSpot = null;
                  start.setHTML("................................");
               }
            }
            else if (endSpot != null)
            {
               map
                     .addOverlay(getStartMarker(end.getHTML(), endSpot
                           .getLatLng()));
               map.removeOverlay(endSpot);
               endSpot = null;
               end.setHTML("................................");
            }
            else
            {
               ;
            }
         }
      });
      findCourseButton = new Button("Znajdź trasę");
      findCourseButton.addClickListener(new ClickListener()
      {
         public void onClick(Widget sender)
         {
            findCourse();
         }
      });

      update = new Button("update");
      update.addClickListener(new ClickListener()
      {
         public void onClick(Widget sender)
         {
            service.update(null);
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

   /**
    * Funkcja uruchamiana przy starcie strony. Odpowiedzialna za wyglad i
    * rozmieszczenie wszystkick widgetow.
    */
   public void onModuleLoad()
   {
      VerticalPanel mainPanel = new VerticalPanel();

      final AbsolutePanel center = new AbsolutePanel();

      Image background = new Image();
      background.setUrl("gfx/tlo.jpg");
      center.add(background);

      map.setSize("701px", "407px");
      center.add(map, 270, 144);

      addressSearch.setWidth("140px");
      center.add(addressSearch, 35, 172);
      addressSubmit.setPixelSize(57, 19);
      addressSubmit.addStyleDependentName("station");
      center.add(addressSubmit, 180, 172);
      center.add(addSpotButton, 241, 172);

      center.add(normalne, 47, 253);
      center.add(pospieszne, 47, 268);
      center.add(nocne, 47, 283);

      center.add(new HTML("skąd:"), 35, 204);
      center.add(startIcon, 82, 202);
      start.setHTML("................................");
      center.add(start, 108, 204);
      center.add(new HTML("dokąd:"), 35, 227);
      center.add(endIcon, 82, 225);
      end.setHTML("................................");
      center.add(end, 108, 227);
      center.add(change, 241, 214);

      center.add(findCourseButton, 151, 265);

      ScrollPanel scroll = new ScrollPanel();
      scroll.setSize("195px", "325px");
      scroll.setWidget(sidePanel);
      center.add(scroll, 54, 330);

      center.add(new HTML("wyszukaj przystanek:"), 313, 600);
      stopSearch.setWidth("190px");
      center.add(stopSearch, 459, 598);
      stopSubmit.setPixelSize(57, 19);
      stopSubmit.addStyleDependentName("station");
      center.add(stopSubmit, 659, 598);

      center.add(new HTML("dostępne linie:"), 313, 630);
      lineSearch.setLimit(4);
      lineSearch.setWidth("40px");
      center.add(lineSearch, 459, 628);
      variantList.setWidth("140px");
      center.add(variantList, 509, 628);
      lineSubmit.setPixelSize(57, 19);
      lineSubmit.addStyleDependentName("station");
      center.add(lineSubmit, 659, 628);

      update.setPixelSize(57, 19);
      update.addStyleDependentName("station");
      center.add(update, 900, 628);

      mainPanel.add(center);

      RootPanel.get().add(mainPanel);
   }
}

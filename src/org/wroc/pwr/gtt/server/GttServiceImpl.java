package org.wroc.pwr.gtt.server;

import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.wroc.pwr.gtt.client.GttService;
import org.wroc.pwr.gtt.server.dbupdater.TTdownloader;
import org.wroc.pwr.gtt.server.graphcreator.GttGraph;
import org.wroc.pwr.gtt.server.graphcreator.Leg;
import org.wroc.pwr.gtt.server.graphcreator.Route;

import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class GttServiceImpl extends RemoteServiceServlet implements GttService
{
   static String url = "http://www.wroclaw.pl/zdikzip/rozklady_xml.zip";
   static String archName = "MPK.zip";
   static String dir = "zdik";
   static String dbhost = "jdbc:mysql://localhost:3306/";
   static String dbName = "gtt";
   static String driver = "com.mysql.jdbc.Driver";
   static String userName = "root";
   static String pasword = "password";
   static String tramCoFile = "tram.txt";
   static String busCoFile = "bus.txt";
   static DBconnector connector = new DBconnector(driver, dbhost, dbName,
         userName, pasword);

   public ArrayList<Integer> getChanges(int przyst_id)
   {
      return connector.getChanges(przyst_id);
   }

   public ArrayList<String> getLinesViaStop(int przyst_id)
   {
      return connector.getLinesViaStop(przyst_id);
   }

   public int getLineId(String nazwa)
   {
      return connector.getLineId(nazwa);
   }

   public int getLineId(String nazwa, String variant)
   {
      return connector.getLineId(nazwa, variant);
   }

   public String getLineName(int linia_id)
   {
      return connector.getLineName(linia_id);
   }

   public HashMap<Integer, ArrayList<String>> getLines()
   {
      return connector.getLines();
   }

   public HashMap<Integer, String> getDayNames(ArrayList<Integer> day_ids)
   {
      return connector.getDayNames(day_ids);
   }

   public ArrayList<Time> getNearestDeparture(int przyst_id, int linia_id,
         int dzien_id, Time start)
   {
      return connector
            .getNearestDeparture(przyst_id, linia_id, dzien_id, start);
   }

   public int getStopId(String nazwa)
   {
      return connector.getStopId(nazwa);
   }

   public ArrayList<Integer> getStopIds(String nazwa)
   {
      return connector.getStopIds(nazwa);
   }

   public String getStopName(int przyst_id)
   {
      return connector.getStopName(przyst_id);
   }

   public ArrayList<String> getStopNames(String line_name, int stop_id)
   {
      return connector.getStopNames(line_name, stop_id);
   }

   public HashMap<Integer, ArrayList<Time>> getStopLineTable(int przyst_id,
         String linia)
   {
      return connector.getStopLineTable(przyst_id, linia);
   }

   public ArrayList<Integer> getLineRoute(int linia_id)
   {
      return connector.getLineRoute(linia_id);
   }

   public String getTypeName(int typ_id)
   {
      return connector.getTypeName(typ_id);
   }

   public String getTypeNameViaLine(String line_name)
   {
      return connector.getTypeNameViaLine(line_name);
   }

   public String getVersionName(int linia_id)
   {
      return connector.getVersionName(linia_id);
   }

   public ArrayList<String> getVersions(String linia_nazwa)
   {
      return connector.getVersions(linia_nazwa);
   }

   public HashMap<Integer, ArrayList<Double>> getAllCoordinates()
   {
      return connector.getAllCoordinates();
   }

   public void update()
   {
      ArrayList<String> xmlFiles;
      TTdownloader.download(url, archName);
      xmlFiles = TTdownloader.unzip(archName, dir);
      connector.updateDB(xmlFiles, tramCoFile, busCoFile);
   }

   public HashMap<ArrayList<ArrayList<ArrayList<Integer>>>, HashMap<Integer, String>> findCourse(
         boolean normal, boolean fast, boolean night, double xLat, double xLng,
         double yLat, double yLng, int amount, int cx, int cy)
   {
      HashMap<ArrayList<ArrayList<ArrayList<Integer>>>, HashMap<Integer, String>> output = new HashMap<ArrayList<ArrayList<ArrayList<Integer>>>, HashMap<Integer, String>>();
      ArrayList<ArrayList<ArrayList<Integer>>> routeList = new ArrayList<ArrayList<ArrayList<Integer>>>();
      HashMap<Integer, String> stopMap = new HashMap<Integer, String>();
      ArrayList<Route> routes = connector.findCourse(normal, fast, night,
            new Coordinates(xLat, xLng), new Coordinates(yLat, yLng), amount,
            cx, cy);
      for (Route route : routes)
      {
         ArrayList<ArrayList<Integer>> legList = new ArrayList<ArrayList<Integer>>();
         ArrayList<Leg> legs = route.getTrasa();
         for (Leg leg : legs)
         {
            ArrayList<Integer> lineAndStops = new ArrayList<Integer>();
            lineAndStops.add(leg.getLine_id());
            stopMap.put(leg.getLine_id(), getLineName(leg.getLine_id()));
            ArrayList<Integer> allStops = getLineRoute(leg.getLine_id());
            for (int i = allStops.indexOf(leg.getStart_stop()); i < allStops
                  .indexOf(leg.getEnd_stop()) + 1; i++)
            {
               lineAndStops.add(allStops.get(i));
            }
            legList.add(lineAndStops);
         }
         routeList.add(legList);
      }
      output.put(routeList, stopMap);
      return output;
   }
}

package org.wroc.pwr.gtt.server;

import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;

import org.wroc.pwr.gtt.client.GttService;
import org.wroc.pwr.gtt.server.dbupdater.TTdownloader;
import org.wroc.pwr.gtt.server.graphcreator.GttGraph;

import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class GttServiceImpl extends RemoteServiceServlet implements GttService
{
   static String url = "http://www.wroclaw.pl/zdikzip/rozklady_xml.zip";
   static String archName = "MPK.zip";
   static String dir = "zdik";
   static String dbhost = "jdbc:mysql://localhost:3306/";
   static String dbName = "mysql";
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

   public ArrayList<Time> getNearestDeparture(int przyst_id, int linia_id, int dzien_id,
         Time start)
   {
      return connector.getNearestDeparture(przyst_id, linia_id, dzien_id, start);
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

}

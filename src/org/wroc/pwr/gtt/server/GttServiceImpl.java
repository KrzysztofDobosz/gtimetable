package org.wroc.pwr.gtt.server;

import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;

import org.wroc.pwr.gtt.client.GttService;
import org.wroc.pwr.gtt.server.dbupdater.TTdownloader;
import org.wroc.pwr.gtt.server.graphcreator.GttGraph;

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
   static DBconnector connector = new DBconnector(driver, dbhost, dbName, userName,
         pasword);
   public ArrayList<Integer> getChanges(int przyst_id)
   {
      return connector.getChanges(przyst_id);
   }
   public int getLiniaId(String nazwa)
   {
      return connector.getLiniaId(nazwa);
   }
   public String getLiniaNazwa(int linia_id)
   {
      return connector.getLiniaNazwa(linia_id);
   }
   public HashMap<Integer, ArrayList<String>> getLinie()
   {
      return connector.getLinie();
   }
   public ArrayList<Time> getNearest(int przyst_id, int linia_id, int dzien_id,
         Time start)
   {
      return connector.getNearest(przyst_id, linia_id, dzien_id, start);
   }
   public int getPrzystId(String nazwa)
   {
      return connector.getPrzystId(nazwa);
   }
   public String getPrzystNazwa(int przyst_id)
   {
      return connector.getPrzystNazwa(przyst_id);
   }
   public HashMap<Integer, ArrayList<Time>> getRozklad(int przyst_id,
         String linia)
   {
      return connector.getRozklad(przyst_id, linia);
   }
   public ArrayList<Integer> getTrasa(int linia_id)
   {
      return connector.getTrasa(linia_id);
   }
   public String getTypNazwa(int typ_id)
   {
      return connector.getTypNazwa(typ_id);
   }
   public String getWariantNazwa(int linia_id)
   {
      return connector.getWariantNazwa(linia_id);
   }
   public ArrayList<String> getWarianty(String linia_nazwa)
   {
      return connector.getWarianty(linia_nazwa);
   }
   public void update()
   {
      ArrayList<String> xmlFiles;
      TTdownloader.download(url, archName);
      xmlFiles = TTdownloader.unzip(archName, dir);
      connector.updateDB(xmlFiles);
   }
}

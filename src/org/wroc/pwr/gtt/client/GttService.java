package org.wroc.pwr.gtt.client;

import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.wroc.pwr.gtt.server.Coordinates;

import com.google.gwt.core.client.GWT;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

@RemoteServiceRelativePath("GttService")
public interface GttService extends RemoteService
{
   public static class Util
   {
      public static GttServiceAsync getInstance()
      {
         return GWT.create(GttService.class);
      }
   }

   public void update();

   public String getStopName(int przyst_id);

   public ArrayList<String> getStopNames(String line_name, int stop_id);

   public String getLineName(int linia_id);

   public int getStopId(String nazwa);

   public ArrayList<Integer> getStopIds(String nazwa);

   public int getLineId(String nazwa);

   public int getLineId(String nazwa, String variant);

   public ArrayList<Integer> getLineRoute(int linia_id);

   public HashMap<Integer, ArrayList<Time>> getStopLineTable(int przyst_id,
         String linia);

   public HashMap<Integer, ArrayList<String>> getLines();

   public HashMap<Integer, String> getDayNames(ArrayList<Integer> day_ids);

   public String getTypeName(int typ_id);

   public String getTypeNameViaLine(String line_name);

   public String getVersionName(int linia_id);

   public ArrayList<String> getVersions(String linia_nazwa);

   public ArrayList<Time> getNearestDeparture(int przyst_id, int linia_id,
         int dzien_id, Time start);

   public ArrayList<Integer> getChanges(int przyst_id);

   public ArrayList<String> getLinesViaStop(int przyst_id);

   public HashMap<Integer, ArrayList<Double>> getAllCoordinates();

   public HashMap<ArrayList<ArrayList<ArrayList<Integer>>>, HashMap<Integer, String>> findCourse(
         boolean normal, boolean fast, boolean night, double xLat, double xLng,
         double yLat, double yLng, int amount, int cx, int cy);
}

package org.wroc.pwr.gtt.client;

import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Interfejs do komunikacji z serwerem.
 *
 * @author Krzysztof Dobosz
 *
 */
public interface GttServiceAsync
{
   public void update(AsyncCallback<?> callback);

   public void getStopName(int przyst_id, AsyncCallback<String> callback);

   public void getStopNames(String line_name, int stop_id, AsyncCallback<ArrayList<String>> callback);

   public void getLineName(int linia_id, AsyncCallback<String> callback);

   public void getStopId(String nazwa, AsyncCallback<Integer> callback);

   public void getStopIds(String nazwa, AsyncCallback<ArrayList<Integer>> callback);

   public void getLineId(String nazwa, AsyncCallback<Integer> callback);

   public void getLineId(String nazwa, String variant, AsyncCallback<Integer> callback);

   public void getLineRoute(int linia_id, AsyncCallback<ArrayList<Integer>> callback);

   public void getStopLineTable(int przyst_id,
         String linia, AsyncCallback<HashMap<Integer, ArrayList<Time>>> callback);

   public void getLines(AsyncCallback<HashMap<Integer, ArrayList<String>>> callback);

   public void getDayNames(ArrayList<Integer> day_ids, AsyncCallback<HashMap<Integer, String>> callback);

   public void getTypeName(int typ_id, AsyncCallback<String> callback);

   public void getTypeNameViaLine(String line_name, AsyncCallback<String> callback);

   public void getVersionName(int linia_id, AsyncCallback<String> callback);

   public void getVersions(String linia_nazwa, AsyncCallback<ArrayList<String>> callback);

   public void getNearestDeparture(int przyst_id, int linia_id,
         int dzien_id, Time start, AsyncCallback<ArrayList<Time>> callback);

   public void getChanges(int przyst_id, AsyncCallback<ArrayList<Integer>> callback);

   public void getLinesViaStop(int przyst_id, AsyncCallback<ArrayList<String>> callback);

   public void getAllCoordinates(AsyncCallback<HashMap<Integer, ArrayList<Double>>> callback);

   public void findCourse(boolean normal,
         boolean fast, boolean night, double xLat, double xLng, double yLat,
         double yLng, int amount, int cx, int cy, AsyncCallback<ArrayList<ArrayList<ArrayList<Integer>>>> callback);

}

package org.wroc.pwr.gtt.client;

import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;

import org.wroc.pwr.gtt.server.Coordinates;


import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface GttServiceAsync
{
   public void update(AsyncCallback<?> callback);
   public void getStopName(int przyst_id, AsyncCallback<String> callback);
   public void getLineName(int linia_id, AsyncCallback<String> callback);
   public void getStopId(String nazwa, AsyncCallback<Integer> callback);
   public void getStopIds(String nazwa, AsyncCallback<ArrayList<Integer>> callback);
   public void getLineId(String nazwa, AsyncCallback<Integer> callback);
   public void getLineId(String nazwa, String variant, AsyncCallback<Integer> callback);
   public void getLineRoute(int linia_id, AsyncCallback<ArrayList<Integer>> callback);
   public void getStopLineTable(int przyst_id, String linia, AsyncCallback<HashMap<Integer, ArrayList<Time>>> callback);
   public void getLines(AsyncCallback<HashMap<Integer, ArrayList<String>>> callback);
   public void getTypeName(int typ_id, AsyncCallback<String> callback);
   public void getVersionName(int linia_id, AsyncCallback<String> callback);
   public void getVersions(String linia_nazwa, AsyncCallback<ArrayList<String>> callback);
   public void getNearestDeparture(int przyst_id, int linia_id, int dzien_id, Time start, AsyncCallback<ArrayList<Time>> callback);
   public void getChanges(int przyst_id, AsyncCallback<ArrayList<Integer>> callback);
   public void getLinesViaStop(int przyst_id, AsyncCallback<ArrayList<String>> callback);
   public void getAllCoordinates(AsyncCallback<HashMap<Integer, ArrayList<Double>>> callback);
}

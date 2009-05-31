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
   public void getPrzystNazwa(int przyst_id, AsyncCallback<String> callback);
   public void getLiniaNazwa(int linia_id, AsyncCallback<String> callback);
   public void getPrzystId(String nazwa, AsyncCallback<Integer> callback);
   public void getLiniaId(String nazwa, AsyncCallback<Integer> callback);
   public void getTrasa(int linia_id, AsyncCallback<ArrayList<Integer>> callback);
   public void getRozklad(int przyst_id, String linia, AsyncCallback<HashMap<Integer, ArrayList<Time>>> callback);
   public void getLinie(AsyncCallback<HashMap<Integer, ArrayList<String>>> callback);
   public void getTypNazwa(int typ_id, AsyncCallback<String> callback);
   public void getWariantNazwa(int linia_id, AsyncCallback<String> callback);
   public void getWarianty(String linia_nazwa, AsyncCallback<ArrayList<String>> callback);
   public void getNearest(int przyst_id, int linia_id, int dzien_id, Time start, AsyncCallback<ArrayList<Time>> callback);
   public void getChanges(int przyst_id, AsyncCallback<ArrayList<Integer>> callback);
   public void getLinie(int przyst_id, AsyncCallback<ArrayList<Integer>> callback);
   public void getAllCoordinates(AsyncCallback<HashMap<Integer, ArrayList<Double>>> callback);
}

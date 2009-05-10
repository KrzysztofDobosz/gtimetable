package org.wroc.pwr.gtt.client;

import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.gwt.core.client.GWT;
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
   public String getPrzystNazwa(int przyst_id);
   public String getLiniaNazwa(int linia_id);
   public int getPrzystId(String nazwa);
   public int getLiniaId(String nazwa);
   public ArrayList<Integer> getTrasa(int linia_id);
   public HashMap<Integer, ArrayList<Time>> getRozklad(int przyst_id, String linia);
   public HashMap<Integer, ArrayList<String>> getLinie();
   public String getTypNazwa(int typ_id);
   public String getWariantNazwa(int linia_id);
   public ArrayList<String> getWarianty(String linia_nazwa);
   public ArrayList<Time> getNearest(int przyst_id, int linia_id, int dzien_id, Time start);
   public ArrayList<Integer> getChanges(int przyst_id);
}

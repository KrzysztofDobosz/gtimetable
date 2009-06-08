package org.wroc.pwr.gtt.client;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Klasa sluzaca jako kontener dla danych potrzebnych do wyswietlenia rozkladu
 * danej linii dla danego przystanku.
 *
 * @author Krzysztof Dobosz
 *
 */
public class TimetableData
{
   private String lineName;
   private String typeName;
   private String stopName;
   private ArrayList<String> stopList;
   private HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> timetable;
   private HashMap<Integer, String> dayNames;

   public TimetableData()
   {
      lineName = null;
      typeName = null;
      stopName = null;
      stopList = null;
      timetable = null;
      dayNames = null;
   }

   public String getLineName()
   {
      return lineName;
   }

   public void setLineName(String lineName)
   {
      this.lineName = lineName;
   }

   public String getTypeName()
   {
      return typeName;
   }

   public void setTypeName(String typeName)
   {
      this.typeName = typeName;
   }

   public String getStopName()
   {
      return stopName;
   }

   public void setStopName(String stopName)
   {
      this.stopName = stopName;
   }

   public ArrayList<String> getStopList()
   {
      return stopList;
   }

   public void setStopList(ArrayList<String> stopList)
   {
      this.stopList = stopList;
   }

   public HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> getTimetable()
   {
      return timetable;
   }

   public void setTimetable(HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> timetable)
   {
      this.timetable = timetable;
   }

   public HashMap<Integer, String> getDayNames()
   {
      return dayNames;
   }

   public void setDayNames(HashMap<Integer, String> dayNames)
   {
      this.dayNames = dayNames;
   }

}

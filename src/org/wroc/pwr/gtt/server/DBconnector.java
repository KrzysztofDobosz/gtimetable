package org.wroc.pwr.gtt.server;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.jgrapht.GraphPath;
import org.wroc.pwr.gtt.server.dbupdater.XmlParser;
import org.wroc.pwr.gtt.server.graphcreator.CComparator;
import org.wroc.pwr.gtt.server.graphcreator.GttGraph;
import org.wroc.pwr.gtt.server.graphcreator.LineStop;
import org.wroc.pwr.gtt.server.graphcreator.WEdge;

/**
 * Klasa odpowiedzialna za ca�o�� po��czenia z baz� danych - od nawi�zania
 * po��czania, przez wype�nienie bazy dancyh po wszelki dost�p do samych danych
 * za spraw� odpowiednich metod
 *
 * @author Micha� Brzezi�ski
 *
 */
public class DBconnector
{
   Connection conn = null;
   XmlParser parser = new XmlParser();
   Statement stmt;

   String driver;
   String host;
   String dbName;
   String userName;
   String pasword;

   /**
    * Konstruktor DBconnector przyjmuj�cy parametry po��czenia JDBC i
    * nawiazuj�cy po��czenie z baz�
    *
    * @param driver
    * @param host
    * @param dbName
    * @param userName
    * @param pasword
    */
   public DBconnector(String driver, String host, String dbName,
         String userName, String pasword)
   {
      this.driver = driver;
      this.host = host;
      this.dbName = dbName;
      this.userName = userName;
      this.pasword = pasword;
      try
      {
         Class.forName(driver).newInstance();
         conn = DriverManager.getConnection(host + dbName, userName, pasword);
         stmt = conn.createStatement();
         stmt.execute("use gtt");
         ResultSet rs;
      }
      catch (InstantiationException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      catch (IllegalAccessException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      catch (ClassNotFoundException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      catch (SQLException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }

   }

   /**
    * Metoda aktualizuj�ca-wype�niaj�ca baz� danych na podstawie plik�w XML z
    * rozk�adami
    *
    * @param fileList
    *           - lista lokalizacji plik�w xml
    */

   public void updateDB(ArrayList<String> fileList, String tramCo, String busCo)
   {

      try
      {

         String[] createStatement = readFileAsString("create.sql").split("\\n");
         for (int i = 0; i < createStatement.length; i++)
         {
            System.out.println(createStatement[i]);
            stmt.executeUpdate(createStatement[i]);
         }
         ResultSet s;
         stmt.execute("use gtt");
         // stmt.executeUpdate("INSERT INTO Dzien (dzien_id, dzien_nazwa)" +
         // " VALUES('" + 0 + "', 'NONE')");
         stmt.executeUpdate("INSERT INTO Typ (typ_id, typ_nazwa)"
               + " VALUES('0','Przejscie do innego przystanku')");
         stmt
               .executeUpdate("INSERT INTO Linia (linia_nazwa, wariant_id, wariant_nazwa, typ_id)VALUES('X', '0', '0','1')");

         for (int i = 0; i < fileList.size(); i++)
         {
            parser.parse(fileList.get(i), conn);
         }
         s = stmt
               .executeQuery("Select przyst_id, przyst_nazwa from Przystanek");
         ArrayList<String> przyst_nazwa = new ArrayList<String>();
         ArrayList<Integer> przyst_id = new ArrayList<Integer>();
         while (s.next())
         {
            przyst_nazwa.add(s.getString("przyst_nazwa"));
            przyst_id.add(s.getInt("przyst_id"));
         }

         for (int i = 0; i < przyst_nazwa.size(); i++)
            for (int k = 0; k < przyst_nazwa.size(); k++)
               if (k != i && przyst_nazwa.get(i).equals(przyst_nazwa.get(k)))
               {
                  // System.out.println(1 + " " + przyst_id.get(i) + " " +
                  // przyst_id.get(k) + " " + (0));
                  stmt
                        .executeUpdate("INSERT INTO Graf (ps_id, pe_id, linia_id, waga, typ_id) VALUES('"
                              + przyst_id.get(i)
                              + "','"
                              + przyst_id.get(k)
                              + "','" + 1 + "','" + 0 + "', '" + 1 + "')");

               }

         // update przyst coordinates:
         HashMap<String, Coordinates> tramCoord = readCoordinates(tramCo);
         HashMap<String, Coordinates> burCoord = readCoordinates(busCo);
         tramCoord.putAll(burCoord);
         Set<Entry<String, Coordinates>> set = tramCoord.entrySet();

         Iterator<Entry<String, Coordinates>> i = set.iterator();

         while (i.hasNext())
         {
            Map.Entry me = i.next();
            stmt.executeUpdate("UPDATE Przystanek set lat = '"
                  + ((Coordinates) me.getValue()).getLat() + "', lng ='"
                  + ((Coordinates) me.getValue()).getLng()
                  + "' WHERE zdik_id = '" + me.getKey() + "'");

            // System.out.println(me.getKey() + " : " + me.getValue() );
         }

      }
      catch (SQLException se)
      {
         System.out.println("SQL Exception:");

         // Loop through the SQL Exceptions
         while (se != null)
         {
            System.out.println("State  : " + se.getSQLState());
            System.out.println("Message: " + se.getMessage());
            System.out.println("Error  : " + se.getErrorCode());

            se = se.getNextException();
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   public static HashMap<String, Coordinates> readCoordinates(String fileName)
   {
      HashMap<String, Coordinates> map = new HashMap<String, Coordinates>();
      try
      {

         FileInputStream fstream = new FileInputStream(fileName);
         // Get the object of DataInputStream
         DataInputStream in = new DataInputStream(fstream);
         BufferedReader br = new BufferedReader(new InputStreamReader(in));
         String strLine;
         strLine = br.readLine();
         while ((strLine = br.readLine()) != null)
         {

            String[] line = strLine.split(",");
            map.put(line[1], new Coordinates(Double.parseDouble(line[6] + "."
                  + line[7]), Double.parseDouble(line[8] + "." + line[9])));

         }

         in.close();
      }
      catch (Exception e)
      {
         System.err.println("Error: " + e.getMessage());
      }

      return map;

   }

   private static String readFileAsString(String filePath)
         throws java.io.IOException
   {
      StringBuffer fileData = new StringBuffer(1000);
      BufferedReader reader = new BufferedReader(new FileReader(filePath));
      char[] buf = new char[1024];
      int numRead = 0;
      while ((numRead = reader.read(buf)) != -1)
      {
         String readData = String.valueOf(buf, 0, numRead);
         fileData.append(readData);
         buf = new char[1024];
      }
      reader.close();
      return fileData.toString();
   }

   /**
    * Metoda wyszukuj�ca po��czenie mi�dzy dwoma przystankami p1 i p2; linie
    * ograniczone do typu typ (wg id z bazy 2- tylko normalne, 3-normalne i
    * pospieszne... do uzupe�nienia na switchu nocne itp); amount - z�dana ilo��
    * po��cze�,
    *
    * @param typ
    * @param p1
    * @param p2
    * @param amount
    * @return
    */
   public ArrayList<ArrayList<LineStop>> findCourse(boolean norm, boolean posp,
         boolean nocne, Coordinates x, Coordinates y, int amount)
   {
      GttGraph graph = loadGraph(norm, posp, nocne);
      ArrayList<Integer> xNearest = new ArrayList<Integer>();
      ArrayList<Integer> yNearest = new ArrayList<Integer>();
      xNearest = (ArrayList<Integer>) getNearestStops(x).subList(0, 5);
      yNearest = (ArrayList<Integer>) getNearestStops(y).subList(0, 5);
      ArrayList<ArrayList<LineStop>> result = new ArrayList<ArrayList<LineStop>>();
      for (int i = 0; i < xNearest.size(); i++)
         for (int k = 0; k < yNearest.size(); k++)
         {
            result.addAll(graph.findCourse(xNearest.get(i), yNearest.get(k),
                  amount / 5));
         }
      // Collections.sort(result, new CComparator<GraphPath<Integer, WEdge>>());
      return result;
   }

   public ArrayList<ArrayList<LineStop>> findCourse(boolean norm, boolean posp,
         boolean nocne, int p1, int p2, int amount)
   {
      ArrayList<ArrayList<LineStop>> list = loadGraph(norm, posp, nocne)
            .findCourse(p1, p2, amount);
      return list;
   }

   /**
    * Metoda wczytuj�ca struktur� grafu z bazy danych
    *
    * @return
    */
   private GttGraph loadGraph(boolean norm, boolean posp, boolean nocne)
   {
      GttGraph graph = new GttGraph();
      ArrayList<Integer> typs = new ArrayList<Integer>();
      typs.add(1);
      if (norm)
      {
         typs.add(2);
         typs.add(5);
         typs.add(18);
         typs.add(19);
         typs.add(20);
         typs.add(21);
      }
      if (posp)
      {
         typs.add(3);
      }
      if (nocne)
      {
         for (int i = 6; i < 18; i++)
            typs.add(i);
      }
      try
      {

         ResultSet rs = stmt.executeQuery("Select przyst_id from Przystanek");
         while (rs.next())
            graph.addVertex(rs.getInt("przyst_id"));

         rs = stmt
               .executeQuery("Select graf_id, ps_id, pe_id, linia_id, waga,graf.typ_id, wariant_id from Graf join Linia using(linia_id) where wariant_id<3");

         while (rs.next())
         {

            if (typs.contains(rs.getInt(6)))
            {
               if (rs.getInt("waga") == 0)
                  graph.addWEdge(this, rs.getInt(2), rs.getInt(3),
                        rs.getInt(4), 0, rs.getInt(5));
               else

                  graph.addWEdge(this, rs.getInt(2), rs.getInt(3),
                        rs.getInt(4), 1, rs.getInt(5));
            }
         }

      }
      catch (SQLException e)
      {
         e.printStackTrace();
      }

      return graph;
   }

   /**
    * Zwraca nazwe przystanku o zadanym id
    *
    * @param przyst_id
    * @return
    */
   public String getPrzystNazwa(int przyst_id)
   {
      String nazwa = null;
      try
      {
         ResultSet s = stmt
               .executeQuery("Select przyst_nazwa from Przystanek where przyst_id='"
                     + przyst_id + "'");
         while (s.next())
            nazwa = s.getString(1);
      }
      catch (SQLException e)
      {
         e.printStackTrace();
      }
      return nazwa;

   }

   /**
    * Zwraca nazw� lini o zadanym id
    *
    * @param linia_id
    * @return
    */
   public String getLiniaNazwa(int linia_id)
   {
      String nazwa = null;
      try
      {
         ResultSet s = stmt
               .executeQuery("Select linia_nazwa from Linia where linia_id='"
                     + linia_id + "'");
         while (s.next())
            nazwa = s.getString(1);
      }
      catch (SQLException e)
      {
         e.printStackTrace();
      }
      return nazwa;

   }

   /**
    * zwraca jeden, losowy id przystanku o zadanej nazwie
    *
    * @param nazwa
    * @return
    */
   public int getPrzystId(String nazwa)
   {
      int id = -1;
      try
      {
         stmt.execute("use gtt");
         ResultSet s = stmt
               .executeQuery("Select przyst_id from Przystanek where przyst_nazwa like('%"
                     + nazwa + "%') limit 1");
         while (s.next())
            id = s.getInt(1);
      }
      catch (SQLException e)
      {
         e.printStackTrace();
      }
      return id;

   }

   /**
    * zwraca id lini o zadanej nazwie (wariant 1)
    *
    * @param nazwa
    * @return
    */
   public int getLiniaId(String nazwa)
   {
      int id = -1;
      try
      {
         stmt.execute("use gtt");
         ResultSet s = stmt
               .executeQuery("Select linia_id from Linia where linia_nazwa='"
                     + nazwa + "' and wariant_id=1 limit 1");
         while (s.next())
            id = s.getInt(1);
      }
      catch (SQLException e)
      {
         e.printStackTrace();
      }
      return id;

   }

   /**
    * zwraca list� id przystank�w w kolejno�ci pokonywania na zadanej po id lini
    *
    * @param linia_id
    * @return
    */
   public ArrayList<Integer> getTrasa(int linia_id)
   {
      ArrayList<Integer> list = new ArrayList<Integer>();

      try
      {
         stmt.execute("use gtt");
         ResultSet s = stmt
               .executeQuery("select distinct przyst_id from Rozklad where linia_id='"
                     + linia_id + "';");
         while (s.next())
         {
            list.add(s.getInt("przyst_id"));
         }
      }
      catch (SQLException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }

      return list;
   }

   /**
    * zwraca rozk�ad danej lini z zadanego przystanku - struktura tablicy
    * hashuj�cej dzien_id->lista<czas>
    *
    * @param przyst_id
    * @param linia
    * @return
    */
   public HashMap<Integer, ArrayList<Time>> getRozklad(int przyst_id,
         String linia)
   {
      HashMap<Integer, ArrayList<Time>> timeTable = new HashMap<Integer, ArrayList<Time>>();
      try
      {
         stmt.execute("use gtt");
         ResultSet s = stmt
               .executeQuery("select przyst_nazwa,linia_nazwa, linia_id, dzien_nazwa, dzien_id, czas from Rozklad join (select linia_id, linia_nazwa from Linia where linia_nazwa='"
                     + linia
                     + "') as l using(linia_id) join (select przyst_id, przyst_nazwa from Przystanek where przyst_id='"
                     + przyst_id
                     + "') as p using(przyst_id) join dzien using(dzien_id);");
         while (s.next())
         {
            if (!timeTable.containsKey(s.getInt("dzien_id")))
            {
               timeTable.put(s.getInt("dzien_id"), new ArrayList<Time>());
            }

            timeTable.get(s.getInt("dzien_id")).add((Time) s.getObject("czas"));
         }
      }
      catch (SQLException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      return timeTable;

   }

   /**
    * Zwraca nazwy linii z podzia�em na typu hashmapa typ_id-> lista nazw
    *
    * @return
    */
   public HashMap<Integer, ArrayList<String>> getLinie()
   {
      HashMap<Integer, ArrayList<String>> lines = new HashMap<Integer, ArrayList<String>>();
      try
      {
         stmt.execute("use gtt");
         ResultSet s = stmt
               .executeQuery("select distinct linia_nazwa, typ_id from Linia");
         s.absolute(1);
         while (s.next())
         {
            if (!lines.containsKey(s.getInt("typ_id")))
               lines.put(s.getInt("typ_id"), new ArrayList<String>());
            lines.get(s.getInt("typ_id")).add(s.getString("linia_nazwa"));
         }
      }
      catch (SQLException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();

      }
      return lines;
   }

   /**
    * zwraca nazw� typu wzgl�dem id
    *
    * @param typ_id
    * @return
    */
   public String getTypNazwa(int typ_id)
   {
      String typ_nazwa = null;
      try
      {
         stmt.execute("use gtt");
         ResultSet s = stmt
               .executeQuery("select typ_nazwa from Typ where typ_id='"
                     + typ_id + "'");
         while (s.next())
         {
            typ_nazwa = s.getString("typ_nazwa");
         }

      }
      catch (SQLException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();

      }
      return typ_nazwa;
   }

   /**
    * zwraca nazw� wariantu wzgl�dem id lini
    *
    * @param linia_id
    * @return
    */
   public String getWariantNazwa(int linia_id)
   {
      String typ_nazwa = null;
      try
      {
         stmt.execute("use gtt");
         ResultSet s = stmt
               .executeQuery("select wariant_nazwa from Linia where linia_id='"
                     + linia_id + "'");
         while (s.next())
         {
            typ_nazwa = s.getString("wariant_nazwa");
         }

      }
      catch (SQLException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();

      }
      return typ_nazwa;
   }

   /**
    * zwraca list� wariant�w danej linii wzgl�dem nazwy
    *
    * @param linia_nazwa
    * @return
    */
   public ArrayList<String> getWarianty(String linia_nazwa)
   {
      ArrayList<String> warianty = new ArrayList<String>();

      try
      {
         stmt.execute("use gtt");
         ResultSet s = stmt
               .executeQuery("select wariant_nazwa from Linia where linia_nazwa='"
                     + linia_nazwa + "';");
         while (s.next())
         {
            warianty.add(s.getString("wariant_nazwa"));
         }

      }
      catch (SQLException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();

      }
      return warianty;
   }

   /**
    * zwraca wsp�rz�dne przystaneku wzgl�dem id
    *
    * @param przyst_id
    * @return
    */
   public Coordinates getCoordinates(int przyst_id)
   {
      Coordinates wsp = null;
      try
      {
         stmt.execute("use gtt");
         ResultSet s = stmt
               .executeQuery("select lat,lng from Przystanek where przyst_id='"
                     + przyst_id + "'");
         while (s.next())
         {
            wsp = new Coordinates(s.getDouble("lat"), s.getDouble("lng"));
         }

      }
      catch (SQLException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();

      }
      return wsp;
   }

   /**
    * zwraca id i wsp�rz�dne przystanekow
    *
    * @return
    */
   public HashMap<Integer, ArrayList<Double>> getAllCoordinates()
   {
      HashMap<Integer, ArrayList<Double>> stations = new HashMap<Integer, ArrayList<Double>>();
      try
      {
         stmt.execute("use gtt");
         ResultSet s = stmt
               .executeQuery("select przyst_id,lat,lng from Przystanek where lat is not null");
         while (s.next())
         {
            ArrayList<Double> coords = new ArrayList<Double>();
            coords.add(s.getDouble("lat"));
            coords.add(s.getDouble("lng"));
            stations.put(s.getInt("przyst_id"), coords);
         }
      }
      catch (SQLException e)
      {
         e.printStackTrace();
      }
      return stations;
   }

   /**
    * zwraca najbizszy zadanemu czasowi (start) wyjazd danej linii (linia_id) z
    * zadanego przystanku (przyst_id) w zadanyc dzien (dzien_id(
    *
    * @param przyst_id
    * @param linia_id
    * @param dzien_id
    * @param start
    * @return
    */
   public ArrayList<Time> getNearest(int przyst_id, int linia_id, int dzien_id,
         Time start)
   {

      ArrayList<Time> time = new ArrayList<Time>();
      try
      {
         stmt.execute("use gtt");
         ResultSet s = stmt
               .executeQuery("select czas from Rozklad where linia_id='"
                     + linia_id + "' and przyst_id='" + przyst_id
                     + "' and dzien_id='" + dzien_id + "' and czas>'" + start
                     + "' limit 3");
         while (s.next())
         {
            time.add((Time) s.getObject("czas"));
         }

      }
      catch (SQLException e)
      {
         e.printStackTrace();

      }
      return time;
   }

   /**
    * zwraca mozliwe przesiadki (lista id lini) z zadanego przystanku lub
    * przystank�w o tej samej nazwie (tzn. w najbli�szej okolicy)
    *
    * @param przyst_id
    * @return
    */
   public ArrayList<Integer> getChanges(int przyst_id)
   {
      ArrayList<Integer> list = new ArrayList<Integer>();
      try
      {
         stmt.execute("use gtt");
         ResultSet s = stmt
               .executeQuery("Select distinct linia_id from Rozklad join (select przyst_id from Przystanek where przyst_nazwa=(select przyst_nazwa from Przystanek where przyst_id='"
                     + przyst_id + "')) as p using(przyst_id)");
         while (s.next())
         {
            list.add(s.getInt("linia_id"));
         }

      }
      catch (SQLException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();

      }
      Collections.sort(list);
      return list;
   }

   /**
    * zwraca linie jadace przez (lista id lini) zadany przystanek
    *
    * @param przyst_id
    * @return
    */
   public ArrayList<Integer> getLinie(int przyst_id)
   {
      ArrayList<Integer> list = new ArrayList<Integer>();
      try
      {
         stmt.execute("use gtt");
         ResultSet s = stmt
               .executeQuery("Select distinct linia_id from Rozklad where przyst_id='"
                     + przyst_id + "'");
         while (s.next())
         {
            list.add(s.getInt("linia_id"));
         }

      }
      catch (SQLException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();

      }
      Collections.sort(list);
      return list;
   }

   public double distance(Coordinates x, Coordinates y)
   {

      double q = Math.sin(x.getLat()) * Math.sin(y.getLat())
            + Math.cos(x.getLat()) * Math.cos(y.getLat())
            * Math.cos(x.getLng() - y.getLng());
      double d = (2 * Math.PI * 6400 * Math.acos(q)) / 360;
      return d;

   }

   public ArrayList<Integer> getNearestStops(Coordinates x)
   {
      ArrayList<Integer> s = new ArrayList<Integer>();
      ResultSet rs;
      try
      {
         rs = stmt.executeQuery("select przyst_id, lat, lng from Przystanek");
         while (rs.next())
         {
            if (distance(x, new Coordinates(rs.getDouble("lat"), rs
                  .getDouble("lng"))) < 0.2)
               s.add(rs.getInt("przyst_id"));
         }
      }
      catch (SQLException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }

      return s;
   }
}

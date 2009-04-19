package org.wroc.pwr.gtt.server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.wroc.pwr.gtt.server.dbupdater.XmlParser;
import org.wroc.pwr.gtt.server.graphcreator.GttGraph;

/**
 * Klasa odpowiedzialna za ca³oœæ po³¹czenia z baz¹ danych - od nawi¹zania
 * po³¹czania, przez wype³nienie bazy dancyh po wszelki dostêp do samych danych
 * za spraw¹ odpowiednich metod
 * 
 * @author Micha³ Brzeziñski
 * 
 */
public class DBconnector {
	Connection conn = null;
	XmlParser parser = new XmlParser();
	Statement stmt;

	String driver;
	String host;
	String dbName;
	String userName;
	String pasword;

	/**
	 * Konstruktor DBconnector przyjmuj¹cy parametry po³¹czenia JDBC i
	 * nawiazuj¹cy po³¹czenie z baz¹
	 * 
	 * @param driver
	 * @param host
	 * @param dbName
	 * @param userName
	 * @param pasword
	 */
	public DBconnector(String driver, String host, String dbName, String userName, String pasword) {
		this.driver = driver;
		this.host = host;
		this.dbName = dbName;
		this.userName = userName;
		this.pasword = pasword;
		try {
			Class.forName(driver).newInstance();
			conn = DriverManager.getConnection(host + dbName, userName, pasword);
			stmt = conn.createStatement();
			stmt.execute("use gtt");
			ResultSet rs;
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Metoda aktualizuj¹ca-wype³niaj¹ca bazê danych na podstawie plików XML z
	 * rozk³adami
	 * 
	 * @param fileList
	 *            - lista lokalizacji plików xml
	 */

	public void updateDB(ArrayList<String> fileList) {

		try {

			String[] createStatement = readFileAsString("create.sql").split("\\n");
			for (int i = 0; i < createStatement.length; i++) {
				System.out.println(createStatement[i]);
				stmt.executeUpdate(createStatement[i]);
			}
			ResultSet s;
			stmt.execute("use gtt");
			// stmt.executeUpdate("INSERT INTO Dzien (dzien_id, dzien_nazwa)" +
			// " VALUES('" + 0 + "', 'NONE')");
			stmt.executeUpdate("INSERT INTO Typ (typ_id, typ_nazwa)" + " VALUES('0','Przejscie do innego przystanku')");
			stmt.executeUpdate("INSERT INTO Linia (linia_nazwa, wariant_id, wariant_nazwa, typ_id)VALUES('X', '0', '0','1')");

			for (int i = 0; i < fileList.size(); i++) {
				parser.parse(fileList.get(i), conn);
			}
			s = stmt.executeQuery("Select przyst_id, przyst_nazwa from Przystanek");
			ArrayList<String> przyst_nazwa = new ArrayList<String>();
			ArrayList<Integer> przyst_id = new ArrayList<Integer>();
			while (s.next()) {
				przyst_nazwa.add(s.getString("przyst_nazwa"));
				przyst_id.add(s.getInt("przyst_id"));
			}

			for (int i = 0; i < przyst_nazwa.size(); i++)
				for (int k = 0; k < przyst_nazwa.size(); k++)
					if (k != i && przyst_nazwa.get(i).equals(przyst_nazwa.get(k))) {
						// System.out.println(1 + " " + przyst_id.get(i) + " " +
						// przyst_id.get(k) + " " + (0));
						stmt.executeUpdate("INSERT INTO Graf (ps_id, pe_id, linia_id, waga, typ_id) VALUES('" + przyst_id.get(i) + "','" + przyst_id.get(k)
								+ "','" + 1 + "','" + 0 + "', '" + 1 + "')");

					}
		} catch (SQLException se) {
			System.out.println("SQL Exception:");

			// Loop through the SQL Exceptions
			while (se != null) {
				System.out.println("State  : " + se.getSQLState());
				System.out.println("Message: " + se.getMessage());
				System.out.println("Error  : " + se.getErrorCode());

				se = se.getNextException();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String readFileAsString(String filePath) throws java.io.IOException {
		StringBuffer fileData = new StringBuffer(1000);
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
		char[] buf = new char[1024];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			String readData = String.valueOf(buf, 0, numRead);
			fileData.append(readData);
			buf = new char[1024];
		}
		reader.close();
		return fileData.toString();
	}

	/**
	 * Metoda wyszukuj¹ca po³¹czenie miêdzy dwoma przystankami p1 i p2; linie
	 * ograniczone do typu typ (wg id z bazy 2- tylko normalne, 3-normalne i
	 * pospieszne... do uzupe³nienia na switchu nocne itp); amount - z¹dana
	 * iloœæ po³¹czeñ,
	 * 
	 * @param typ
	 * @param p1
	 * @param p2
	 * @param amount
	 */
	public void findCourse(int typ, int p1, int p2, int amount) {
		loadGraph().findCourse(typ, p1, p2, amount);
	}

	/**
	 * Metoda wczytuj¹ca strukturê grafu z bazy danych
	 * 
	 * @return
	 */
	private GttGraph loadGraph() {
		GttGraph graph = new GttGraph();

		try {

			ResultSet rs = stmt.executeQuery("Select przyst_id from Przystanek");
			while (rs.next())
				graph.addVertex(rs.getInt("przyst_id"));

			rs = stmt
					.executeQuery("Select graf_id, ps_id, pe_id, linia_id, waga,graf.typ_id, wariant_id from Graf join Linia using(linia_id) where wariant_id<3");
			System.out.println("loaded sql");

			while (rs.next()) {
				if (rs.getInt("waga") == 0)
					graph.addWEdge(this, rs.getInt(2), rs.getInt(3), rs.getInt(4), 0, rs.getInt(5), rs.getInt(6));
				else

					graph.addWEdge(this, rs.getInt(2), rs.getInt(3), rs.getInt(4), 1, rs.getInt(5), rs.getInt(6));

			}

		} catch (SQLException e) {
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
	public String getPrzystNazwa(int przyst_id) {
		String nazwa = null;
		try {
			ResultSet s = stmt.executeQuery("Select przyst_nazwa from Przystanek where przyst_id='" + przyst_id + "'");
			while (s.next())
				nazwa = s.getString(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return nazwa;

	}

	/**
	 * Zwraca nazwê lini o zadanym id
	 * 
	 * @param linia_id
	 * @return
	 */
	public String getLiniaNazwa(int linia_id) {
		String nazwa = null;
		try {
			ResultSet s = stmt.executeQuery("Select linia_nazwa from Linia where linia_id='" + linia_id + "'");
			while (s.next())
				nazwa = s.getString(1);
		} catch (SQLException e) {
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
	public int getPrzystId(String nazwa) {
		int id = -1;
		try {
			stmt.execute("use gtt");
			ResultSet s = stmt.executeQuery("Select przyst_id from Przystanek where przyst_nazwa like('%" + nazwa + "%') limit 1");
			while (s.next())
				id = s.getInt(1);
		} catch (SQLException e) {
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
	public int getLiniaId(String nazwa) {
		int id = -1;
		try {
			stmt.execute("use gtt");
			ResultSet s = stmt.executeQuery("Select linia_id from Linia where linia_nazwa='" + nazwa + "' and wariant_id=1 limit 1");
			while (s.next())
				id = s.getInt(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return id;

	}

	/**
	 * zwraca listê id przystanków w kolejnoœci pokonywania na zadanej po id
	 * lini
	 * 
	 * @param linia_id
	 * @return
	 */
	public ArrayList<Integer> getTrasa(int linia_id) {
		ArrayList<Integer> list = new ArrayList<Integer>();

		try {
			stmt.execute("use gtt");
			ResultSet s = stmt.executeQuery("select distinct przyst_id from Rozklad where linia_id='" + linia_id + "';");
			while (s.next()) {
				list.add(s.getInt("przyst_id"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return list;
	}

	/**
	 * zwraca rozk³ad danej lini z zadanego przystanku - struktura tablicy
	 * hashuj¹cej dzien_id->lista<czas>
	 * 
	 * @param przyst_id
	 * @param linia
	 * @return
	 */
	public HashMap<Integer, ArrayList<Time>> getRozklad(int przyst_id, String linia) {
		HashMap<Integer, ArrayList<Time>> timeTable = new HashMap<Integer, ArrayList<Time>>();
		try {
			stmt.execute("use gtt");
			ResultSet s = stmt
					.executeQuery("select przyst_nazwa,linia_nazwa, linia_id, dzien_nazwa, dzien_id, czas from Rozklad join (select linia_id, linia_nazwa from Linia where linia_nazwa='"
							+ linia
							+ "') as l using(linia_id) join (select przyst_id, przyst_nazwa from Przystanek where przyst_id='"
							+ przyst_id
							+ "') as p using(przyst_id) join dzien using(dzien_id);");
			while (s.next()) {
				if (!timeTable.containsKey(s.getInt("dzien_id"))) {
					timeTable.put(s.getInt("dzien_id"), new ArrayList<Time>());
				}

				timeTable.get(s.getInt("dzien_id")).add((Time) s.getObject("czas"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return timeTable;

	}

	/**
	 * Zwraca nazwy linii z podzia³em na typu hashmapa typ_id-> lista nazw
	 * 
	 * @return
	 */
	public HashMap<Integer, ArrayList<String>> getLinie() {
		HashMap<Integer, ArrayList<String>> lines = new HashMap<Integer, ArrayList<String>>();
		try {
			stmt.execute("use gtt");
			ResultSet s = stmt.executeQuery("select distinct linia_nazwa, typ_id from Linia");
			s.absolute(1);
			while (s.next()) {
				if (!lines.containsKey(s.getInt("typ_id")))
					lines.put(s.getInt("typ_id"), new ArrayList<String>());
				lines.get(s.getInt("typ_id")).add(s.getString("linia_nazwa"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
		return lines;
	}

	/**
	 * zwraca nazwê typu wzglêdem id
	 * 
	 * @param typ_id
	 * @return
	 */
	public String getTypNazwa(int typ_id) {
		String typ_nazwa = null;
		try {
			stmt.execute("use gtt");
			ResultSet s = stmt.executeQuery("select typ_nazwa from Typ where typ_id='" + typ_id + "'");
			while (s.next()) {
				typ_nazwa = s.getString("typ_nazwa");
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
		return typ_nazwa;
	}

	/**
	 * zwraca nazwê wariantu wzglêdem id lini
	 * 
	 * @param linia_id
	 * @return
	 */
	public String getWariantNazwa(int linia_id) {
		String typ_nazwa = null;
		try {
			stmt.execute("use gtt");
			ResultSet s = stmt.executeQuery("select wariant_nazwa from Linia where linia_id='" + linia_id + "'");
			while (s.next()) {
				typ_nazwa = s.getString("wariant_nazwa");
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
		return typ_nazwa;
	}

	/**
	 * zwraca listê wariantów danej linii wzglêdem nazwy
	 * 
	 * @param linia_nazwa
	 * @return
	 */
	public ArrayList<String> getWarianty(String linia_nazwa) {
		ArrayList<String> warianty = new ArrayList<String>();

		try {
			stmt.execute("use gtt");
			ResultSet s = stmt.executeQuery("select wariant_nazwa from Linia where linia_nazwa='" + linia_nazwa + "';");
			while (s.next()) {
				warianty.add(s.getString("wariant_nazwa"));
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
		return warianty;
	}

	/**
	 * zwraca wspó³rzêdne przystaneku wzglêdem id
	 * 
	 * @param przyst_id
	 * @return
	 */
	public Coordinates getWspolrzedne(int przyst_id) {
		Coordinates wsp = null;
		try {
			stmt.execute("use gtt");
			ResultSet s = stmt.executeQuery("select lat,lng from Przystanek where przyst_id='" + przyst_id + "'");
			while (s.next()) {
				wsp = new Coordinates(s.getDouble("lat"), s.getDouble("lng"));
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
		return wsp;
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
	public ArrayList<Time> getNearest(int przyst_id, int linia_id, int dzien_id, Time start) {

		ArrayList<Time> time = new ArrayList<Time>();
		try {
			stmt.execute("use gtt");
			ResultSet s = stmt.executeQuery("select  czas from Rozklad where linia_id='" + linia_id + "' and przyst_id='" + przyst_id + "' and dzien_id='"
					+ dzien_id + "' and czas>'" + start + "' limit 3");
			while (s.next()) {
				time.add((Time) s.getObject("czas"));
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
		return time;
	}

	/**
	 * zwraca mozliwe przesiadki (lista id lini) z zadanego przystanku lub
	 * przystanków o tej samej nazwie (tzn. w najbli¿szej okolicy)
	 * 
	 * @param przyst_id
	 * @return
	 */
	public ArrayList<Integer> getChanges(int przyst_id) {
		ArrayList<Integer> list = new ArrayList<Integer>();
		try {
			stmt.execute("use gtt");
			ResultSet s = stmt
					.executeQuery("Select distinct linia_id from Rozklad join (select przyst_id from Przystanek where przyst_nazwa=(select przyst_nazwa from Przystanek where przyst_id='"
							+ przyst_id + "')) as p using(przyst_id)");
			while (s.next()) {
				list.add(s.getInt("linia_id"));
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
		Collections.sort(list);
		return list;
	}
}

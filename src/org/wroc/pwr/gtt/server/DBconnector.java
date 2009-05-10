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
import org.wroc.pwr.gtt.server.graphcreator.LineStop;

/**
 * Klasa odpowiedzialna za calosc polaczenia z baza danych - od nawiazania
 * polaczania, przez wypelnienie bazy danych po wszelki dostep do samych danych
 * za sprawa odpowiednich metod
 *
 * @author Michal Brzezinski
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
	 * Konstruktor DBconnector przyjmujacy parametry polaczenia JDBC i
	 * nawiazujacy polaczenie z baza
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
	 * Metoda aktualizujaca-wypelniajaca baze danych na podstawie plikow XML z
	 * rozkladami
	 *
	 * @param fileList
	 *            - lista lokalizacji plikow xml
	 */

	public void updateDB(ArrayList<String> fileList) {

		try {

			String[] createStatement = readFileAsString(System.getenv("TOMCAT_HOME") + "/webapps/gtt/WEB-INF/lib/create.sql").split("\\n");
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
	 * Metoda wyszukujaca polaczenie miedzy dwoma przystankami p1 i p2; linie
	 * ograniczone do typu typ (wg id z bazy 2- tylko normalne, 3-normalne i
	 * pospieszne... do uzupelnienia na switchu nocne itp); amount - zadana
	 * ilosc polaczen,
	 *
	 * @param typ
	 * @param p1
	 * @param p2
	 * @param amount
	 * @return
	 */
	public ArrayList<ArrayList<LineStop>> findCourse(int typ, int p1, int p2, int amount) {
		ArrayList<ArrayList<LineStop>> list = loadGraph(typ).findCourse(p1, p2, amount);
		return list;
	}

	/**
	 * Metoda wczytujaca strukture grafu z bazy danych
	 *
	 * @return
	 */
	private GttGraph loadGraph(int typ) {
		GttGraph graph = new GttGraph();
		ArrayList<Integer> typs = new ArrayList<Integer>();

		switch (typ) {
		case 1: // normalne
			typs.add(1);
			typs.add(2);
			typs.add(5);
			typs.add(18);
			typs.add(20);

			break;
		case 2:// pospieszne
			typs.add(1);
			typs.add(3);
			break;
		case 3: // normalne i pospieszne
			typs.add(1);
			typs.add(2);
			typs.add(3);
			typs.add(5);
			typs.add(18);
			typs.add(20);
			break;
		case 4: // nocne
			typs.add(4);
			for (int i = 6; i < 18; i++)
				typs.add(i);
			break;
		case 5: // wszystkie
			for (int i = 1; i < 22; i++)
				typs.add(i);
		}
		try {

			ResultSet rs = stmt.executeQuery("Select przyst_id from Przystanek");
			while (rs.next())
				graph.addVertex(rs.getInt("przyst_id"));

			rs = stmt
					.executeQuery("Select graf_id, ps_id, pe_id, linia_id, waga,graf.typ_id, wariant_id from Graf join Linia using(linia_id) where wariant_id<3");
			System.out.println("loaded sql");

			while (rs.next()) {
				if (typs.contains(rs.getInt(6))) {
					if (rs.getInt("waga") == 0)
						graph.addWEdge(this, rs.getInt(2), rs.getInt(3), rs.getInt(4), 0, rs.getInt(5));
					else

						graph.addWEdge(this, rs.getInt(2), rs.getInt(3), rs.getInt(4), 1, rs.getInt(5));
				}
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
	 * Zwraca nazwe lini o zadanym id
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
	 * zwraca liste id przystankow w kolejnosci pokonywania na zadanej po id
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
	 * zwraca rozklad danej lini z zadanego przystanku - struktura tablicy
	 * hashujacej dzien_id->lista<czas>
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
	 * Zwraca nazwy linii z podzialem na typu hashmapa typ_id-> lista nazw
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
	 * zwraca nazwe typu wzgledem id
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
	 * zwraca nazwe wariantu wzgledem id lini
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
	 * zwraca liste wariantow danej linii wzgledem nazwy
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
	 * zwraca wspolrzedne przystanku wzgledem id
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
			ResultSet s = stmt.executeQuery("select czas from Rozklad where linia_id='" + linia_id + "' and przyst_id='" + przyst_id + "' and dzien_id='"
					+ dzien_id + "' and czas>'" + start + "' limit 3");
			while (s.next()) {
				time.add((Time) s.getObject("czas"));
			}

		} catch (SQLException e) {
			e.printStackTrace();

		}
		return time;
	}

	/**
	 * zwraca mozliwe przesiadki (lista id lini) z zadanego przystanku lub
	 * przystankow o tej samej nazwie (tzn. w najblizszej okolicy)
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

	public ArrayList<ArrayList<LineStop>> resultTimeUpdate(ArrayList<ArrayList<LineStop>> list, Time time, int dzien_id) {
		for (int i = 0; i < list.size(); i++) {
			Time time1 = (Time) time.clone();
			for (int k = 0; k < list.get(i).size(); k++) {

				if (list.get(i).get(k).getLinia_id() != 1) {
					list.get(i).get(k).setTime(getNearest(list.get(i).get(k).getPrzystStart(), list.get(i).get(k).getLinia_id(), dzien_id, time1).get(0));
					time1 = getNearest(list.get(i).get(k).getPrzystEnd(), list.get(i).get(k).getLinia_id(), dzien_id, time1).get(0);
					// problemy z czasami! jak wyliczy� czas jazdy do petli
					// skoro z p�tli nie odjezd�a ta wersja linii...
					// w og�le problemy z wyliczaniem czas�w przejazd�w... z
					// dupy to wszystko....
					// trzeba zgarn�� do bazdy w ogole od razy czasy
					// przystanek-przystanek...

				} else
					list.get(i).get(k).setTime(time1);
			}
		}
		return list;
	}
}

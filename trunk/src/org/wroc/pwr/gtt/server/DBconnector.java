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
import org.wroc.pwr.gtt.server.graphcreator.Route;
import org.wroc.pwr.gtt.server.graphcreator.RouteComparator;
import org.wroc.pwr.gtt.server.graphcreator.GttGraph;
import org.wroc.pwr.gtt.server.graphcreator.Leg;
import org.wroc.pwr.gtt.server.graphcreator.Tupple;
import org.wroc.pwr.gtt.server.graphcreator.WEdge;

/**
 * Klasa odpowiedzialna za ca�o�� po��czenia z baz� danych - od nawi�zania
 * po��czania, przez wype�nienie bazy dancyh po wszelki dost�p do samych danych
 * za spraw� odpowiednich metod
 * 
 * @author Micha� Brzezi�ski
 * 
 */
public class DBconnector {
	Connection conn = null;
	XmlParser parser = new XmlParser();

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
			String userName, String pasword) {
		this.driver = driver;
		this.host = host;
		this.dbName = dbName;
		this.userName = userName;
		this.pasword = pasword;
		try {
			Class.forName(driver).newInstance();
			conn = DriverManager
					.getConnection(host + dbName, userName, pasword);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Metoda aktualizuj�ca-wype�niaj�ca baz� danych na podstawie plik�w XML z
	 * rozk�adami
	 * 
	 * @param fileList
	 *            - lista lokalizacji plik�w xml
	 */

	public void updateDB(ArrayList<String> fileList, String tramCo, String busCo) {

		try {
			String[] createStatement = readFileAsString("create.sql").split(
					"\\n");
			Statement stmt = conn.createStatement();
			for (int i = 0; i < createStatement.length; i++) {
				System.out.println(createStatement[i]);
				stmt.executeUpdate(createStatement[i]);
			}
			ResultSet s;
			stmt.execute("use gtt");
			// stmt.executeUpdate("INSERT INTO Dzien (dzien_id, dzien_nazwa)" +
			// " VALUES('" + 0 + "', 'NONE')");
			stmt.executeUpdate("INSERT INTO Type (type_id, type_name)"
					+ " VALUES('0','Przejscie do innego przystanku')");
			stmt
					.executeUpdate("INSERT INTO Line (line_name, version_id, version_name, type_id)VALUES('X', '0', '0','1')");

			for (int i = 0; i < fileList.size(); i++) {
				parser.parse(fileList.get(i), conn);
			}

			// update przyst coordinates:
			HashMap<String, Coordinates> tramCoord = readCoordinates(tramCo);
			HashMap<String, Coordinates> burCoord = readCoordinates(busCo);
			tramCoord.putAll(burCoord);
			Set<Entry<String, Coordinates>> set = tramCoord.entrySet();

			Iterator<Entry<String, Coordinates>> it = set.iterator();

			while (it.hasNext()) {
				Map.Entry me = it.next();
				stmt.executeUpdate("UPDATE Stop set lat = '"
						+ ((Coordinates) me.getValue()).getLat() + "', lng ='"
						+ ((Coordinates) me.getValue()).getLng()
						+ "' WHERE zdik_id = '" + me.getKey() + "'");

				// System.out.println(me.getKey() + " : " + me.getValue() );
			}
			s = stmt.executeQuery("Select stop_id, stop_name from Stop");
			ArrayList<String> stop_names = new ArrayList<String>();
			ArrayList<Integer> stop_ids = new ArrayList<Integer>();
			while (s.next()) {
				stop_names.add(s.getString("stop_name"));
				stop_ids.add(s.getInt("stop_id"));
			}
			for (int i = 0; i < stop_names.size(); i++)
				for (int k = 0; k < stop_names.size(); k++)
					if (k != i && stop_names.get(i).equals(stop_names.get(k))) {
						// System.out.println(1 + " " + przyst_id.get(i) + " " +
						// przyst_id.get(k) + " " + (0));
						double distance = distance(getCoordinates(stop_ids
								.get(i)), getCoordinates(stop_ids.get(k)));

						double w = distance * 10;
						if (distance < 0.1)
							stmt
									.executeUpdate("INSERT INTO Graph (ps_id, pe_id, line_id, weight, type_id) VALUES('"
											+ stop_ids.get(i)
											+ "','"
											+ stop_ids.get(k)
											+ "','"
											+ 1
											+ "','" + 0 + "', '" + 1 + "')");

					}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateGraph() {
		try {
			Statement stmt = conn.createStatement();
			ResultSet s;
			stmt.execute("use gtt");
			s = stmt.executeQuery("Select stop_id, stop_name from Stop");
			ArrayList<String> stop_names = new ArrayList<String>();
			ArrayList<Integer> stop_ids = new ArrayList<Integer>();
			while (s.next()) {
				stop_names.add(s.getString("stop_name"));
				stop_ids.add(s.getInt("stop_id"));
			}
			for (int i = 0; i < stop_names.size(); i++)
				for (int k = 0; k < stop_names.size(); k++)
					if (k != i && stop_names.get(i).equals(stop_names.get(k))) {
						// System.out.println(1 + " " + przyst_id.get(i) + " " +
						// przyst_id.get(k) + " " + (0));
						double distance = distance(getCoordinates(stop_ids
								.get(i)), getCoordinates(stop_ids.get(k)));

						double w = distance;
						if (distance < 0.4)
							stmt
									.executeUpdate("UPDATE Graph set weight = '"
											+ w
											+ "' WHERE ps_id='"
											+ stop_ids.get(i)
											+ "' and pe_id = '"
											+ stop_ids.get(k) + "'");
						stmt.executeUpdate("UPDATE Graph set weight = '" + w
								+ "' WHERE ps_id='" + stop_ids.get(k)
								+ "' and pe_id = '" + stop_ids.get(i) + "'");
					}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static HashMap<String, Coordinates> readCoordinates(String fileName) {
		HashMap<String, Coordinates> map = new HashMap<String, Coordinates>();
		try {

			FileInputStream fstream = new FileInputStream(fileName);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			strLine = br.readLine();
			while ((strLine = br.readLine()) != null) {

				String[] line = strLine.split(",");
				map.put(line[1], new Coordinates(Double.parseDouble(line[6]
						+ "." + line[7]), Double.parseDouble(line[8] + "."
						+ line[9])));

			}

			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return map;

	}

	private static String readFileAsString(String filePath)
			throws java.io.IOException {
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
	 * Metoda wyszukuj�ca po��czenie mi�dzy dwoma punktami x i y; normal, fast,
	 * night okreslaja rodzaje polaczen; amount - liczba pol. do wyszukania
	 * miedzy kazdymi dwoma przystnakami sposrod cx najblizszych poczatkowemu i
	 * cy koncowemu
	 * 
	 * @param typ
	 * @param p1
	 * @param p2
	 * @param amount
	 * @return
	 */
	public ArrayList<Route> findCourse(boolean normal, boolean fast,
			boolean night, Coordinates x, Coordinates y, int amount, int cx,
			int cy) {
		GttGraph graph = loadGraph(normal, fast, night);
		ArrayList<Integer> xNearest;
		ArrayList<Integer> yNearest;
		xNearest = (ArrayList<Integer>) getNearestStops(x, cx);
		yNearest = (ArrayList<Integer>) getNearestStops(y, cy);
		ArrayList<Route> result = new ArrayList<Route>();
		for (int i = 0; i < xNearest.size(); i++)
			for (int k = 0; k < yNearest.size(); k++) {
				result.addAll(graph.findCourse(xNearest.get(i),
						yNearest.get(k), amount));
			}
		Collections.sort(result);

		return result;
	}

	public ArrayList<Route> findCourse(boolean normal, boolean fast,
			boolean night, int p1, int p2, int amount) {
		ArrayList<Route> list = loadGraph(normal, fast, night).findCourse(p1,
				p2, amount);
		return list;
	}

	/**
	 * Metoda wczytuj�ca struktur� grafu z bazy danych
	 * 
	 * @return
	 */
	public GttGraph loadGraph(boolean normal, boolean fast, boolean night) {
		GttGraph graph = new GttGraph();
		ArrayList<Integer> types = new ArrayList<Integer>();
		types.add(1);
		if (normal) {
			types.add(2);
			types.add(5);
			types.add(18);
			types.add(19);
			types.add(20);
			types.add(21);
		}
		if (fast) {
			types.add(3);
		}
		if (night) {
			for (int i = 6; i < 18; i++)
				types.add(i);
		}

		try {
			Statement stmt = conn.createStatement();
			stmt.execute("use gtt");
			ResultSet rs = stmt.executeQuery("Select stop_id from Stop");

			while (rs.next())
				graph.addVertex(rs.getInt("stop_id"));

			rs = stmt
					.executeQuery("Select graph_id, ps_id, pe_id, line_id, weight, Graph.type_id, version_id from Graph join Line using(line_id) where version_id<3");

			while (rs.next()) {

				if (types.contains(rs.getInt("Graph.type_id"))) {

					if (rs.getInt("line_id") == 1) {

						graph.addWEdge(this, rs.getInt("ps_id"), rs
								.getInt("pe_id"), rs.getInt("line_id"), rs
								.getInt("weight"), 1);
					} else {
						{

							graph.addWEdge(this, rs.getInt("ps_id"), rs
									.getInt("pe_id"), rs.getInt("line_id"), 1,
									rs.getInt("weight"));
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return graph;
	}

	/**
	 * Zwraca nazwe przystanku o zadanym id
	 * 
	 * @param stop_id
	 * @return
	 */
	public String getStopName(int stop_id) {
		String name = null;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute("use gtt");
			ResultSet s = stmt
					.executeQuery("Select stop_name from Stop where stop_id='"
							+ stop_id + "'");
			while (s.next())
				name = s.getString(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return name;

	}

	/**
	 * Zwraca nazw� lini o zadanym id
	 * 
	 * @param line_id
	 * @return
	 */
	public String getLineName(int line_id) {
		String name = null;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute("use gtt");
			ResultSet s = stmt
					.executeQuery("Select line_name from Line where line_id='"
							+ line_id + "'");
			while (s.next())
				name = s.getString(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return name;

	}

	/**
	 * zwraca jeden, losowy id przystanku o zadanej nazwie
	 * 
	 * @param stop_name
	 * @return
	 */
	public int getStopId(String stop_name) {
		int id = -1;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute("use gtt");
			ResultSet s = stmt
					.executeQuery("Select stop_id from Stop where stop_id like('%"
							+ stop_name + "%') limit 1");
			while (s.next())
				id = s.getInt(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return id;
	}

	/**
	 * zwraca id przystankow o zadanej nazwie
	 * 
	 * @param stop_name
	 * @return
	 */
	public ArrayList<Integer> getStopIds(String stop_name) {
		ArrayList<Integer> ids = new ArrayList<Integer>();
		try {
			Statement stmt = conn.createStatement();
			stmt.execute("use gtt");
			ResultSet s = stmt
					.executeQuery("Select stop_id from Stop where stop_name like('%"
							+ stop_name + "%')");
			while (s.next())
				ids.add(s.getInt("stop_id"));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ids;
	}

	/**
	 * zwraca id lini o zadanej nazwie (wariant 1)
	 * 
	 * @param line_name
	 * @return
	 */
	public int getLineId(String line_name) {
		int id = -1;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute("use gtt");
			ResultSet s = stmt
					.executeQuery("Select line_id from Line where line_name='"
							+ line_name + "' and version_id=1 limit 1");
			while (s.next())
				id = s.getInt("line_id");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return id;

	}

	/**
	 * zwraca id lini o zadanej nazwie i wariancie
	 * 
	 * @param line_name
	 * @return
	 */
	public int getLineId(String line_name, String version_name) {
		int id = -1;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute("use gtt");
			ResultSet s = stmt
					.executeQuery("Select line_id from Line where line_name='"
							+ line_name + "' and version_name='" + version_name
							+ "' limit 1");
			while (s.next())
				id = s.getInt("line_id");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return id;

	}

	/**
	 * zwraca list� id przystank�w w kolejno�ci pokonywania na zadanej po id
	 * lini
	 * 
	 * @param line_id
	 * @return
	 */
	public ArrayList<Integer> getLineRoute(int line_id) {
		ArrayList<Integer> list = new ArrayList<Integer>();

		try {
			Statement stmt = conn.createStatement();
			stmt.execute("use gtt");
			ResultSet s = stmt
					.executeQuery("select distinct stop_id from Timetable where line_id='"
							+ line_id + "';");
			while (s.next()) {
				list.add(s.getInt("stop_id"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return list;
	}

	/**
	 * zwraca rozk�ad danej lini z zadanego przystanku - struktura tablicy
	 * hashuj�cej dzien_id->lista<czas>
	 * 
	 * @param stop_id
	 * @param line_name
	 * @return
	 */
	public HashMap<Integer, ArrayList<Time>> getStopLineTable(int stop_id,
			String line_name) {
		HashMap<Integer, ArrayList<Time>> timeTable = new HashMap<Integer, ArrayList<Time>>();
		try {
			Statement stmt = conn.createStatement();
			stmt.execute("use gtt");
			ResultSet s = stmt
					.executeQuery("select stop_name,line_name, line_id, day_name, day_id, time from Timetable join (select line_id, line_name from Line where line_name='"
							+ line_name
							+ "') as l using(line_id) join (select stop_id, stop_name from Stop where stop_id='"
							+ stop_id
							+ "') as p using(stop_id) join Day using(day_id);");
			while (s.next()) {
				if (!timeTable.containsKey(s.getInt("day_id"))) {
					timeTable.put(s.getInt("day_id"), new ArrayList<Time>());
				}
				timeTable.get(s.getInt("day_id")).add(
						(Time) s.getObject("time"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return timeTable;

	}

	/**
	 * Zwraca nazwy linii z podzia�em na typu hashmapa typ_id-> lista nazw
	 * 
	 * @return
	 */
	public HashMap<Integer, ArrayList<String>> getLines() {
		HashMap<Integer, ArrayList<String>> lines = new HashMap<Integer, ArrayList<String>>();
		try {
			Statement stmt = conn.createStatement();
			stmt.execute("use gtt");
			ResultSet s = stmt
					.executeQuery("select distinct line_name, type_id from Line");
			s.absolute(1);
			while (s.next()) {
				if (!lines.containsKey(s.getInt("type_id")))
					lines.put(s.getInt("type_id"), new ArrayList<String>());
				lines.get(s.getInt("type_id")).add(s.getString("line_name"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
		return lines;
	}

	/**
	 * zwraca nazw� typu wzgl�dem id
	 * 
	 * @param type_id
	 * @return
	 */
	public String getTypeName(int type_id) {
		String type_name = null;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute("use gtt");
			ResultSet s = stmt
					.executeQuery("select type_name from Type where type_id='"
							+ type_id + "'");
			while (s.next()) {
				type_name = s.getString("type_name");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
		return type_name;
	}

	/**
	 * zwraca nazw� wariantu wzgl�dem id lini
	 * 
	 * @param line_name
	 * @return
	 */
	public String getVersionName(int line_name) {
		String version_name = null;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute("use gtt");
			ResultSet s = stmt
					.executeQuery("select version_name from Line where line_id='"
							+ line_name + "'");
			while (s.next()) {
				version_name = s.getString("version_name");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
		return version_name;
	}

	/**
	 * zwraca list� wariant�w danej linii wzgl�dem nazwy
	 * 
	 * @param line_name
	 * @return
	 */
	public ArrayList<String> getVersions(String line_name) {
		ArrayList<String> versions = new ArrayList<String>();

		try {
			Statement stmt = conn.createStatement();
			stmt.execute("use gtt");
			ResultSet s = stmt
					.executeQuery("select version_name from Line where line_name='"
							+ line_name + "';");
			while (s.next()) {
				versions.add(s.getString("version_name"));
			}
		} catch (SQLException e) {
			e.printStackTrace();

		}
		return versions;
	}

	/**
	 * zwraca wsp�rz�dne przystaneku wzgl�dem id
	 * 
	 * @param stop_id
	 * @return
	 */
	public Coordinates getCoordinates(int stop_id) {
		Coordinates coord = null;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute("use gtt");
			ResultSet s = stmt
					.executeQuery("select lat,lng from Stop where stop_id='"
							+ stop_id + "'");
			while (s.next()) {
				coord = new Coordinates(s.getDouble("lat"), s.getDouble("lng"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
		return coord;
	}

	/**
	 * zwraca id i wsp�rz�dne przystanekow
	 * 
	 * @return
	 */
	public HashMap<Integer, ArrayList<Double>> getAllCoordinates() {
		HashMap<Integer, ArrayList<Double>> stations = new HashMap<Integer, ArrayList<Double>>();
		try {
			Statement stmt = conn.createStatement();
			stmt.execute("use gtt");
			ResultSet s = stmt
					.executeQuery("select stop_id,lat,lng from Stop where lat is not null");
			while (s.next()) {
				ArrayList<Double> coords = new ArrayList<Double>();
				coords.add(s.getDouble("lat"));
				coords.add(s.getDouble("lng"));
				stations.put(s.getInt("stop_id"), coords);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return stations;
	}

	/**
	 * zwraca najbizszy zadanemu czasowi (start) wyjazd danej linii (linia_id) z
	 * zadanego przystanku (przyst_id) w zadanyc dzien (dzien_id(
	 * 
	 * @param stop_id
	 * @param line_id
	 * @param dzien_id
	 * @param start
	 * @return
	 */
	public ArrayList<Time> getNearestDeparture(int stop_id, int line_id,
			int dzien_id, Time start) {

		ArrayList<Time> time = new ArrayList<Time>();
		try {
			Statement stmt = conn.createStatement();
			stmt.execute("use gtt");
			ResultSet s = stmt
					.executeQuery("select time from Timetable where line_id='"
							+ line_id + "' and stop_id='" + stop_id
							+ "' and day_id='" + dzien_id + "' and time>'"
							+ start + "' limit 3");
			while (s.next()) {
				time.add((Time) s.getObject("time"));
			}
			System.out.println("znaleziono najblisze");
		} catch (SQLException e) {
			e.printStackTrace();

		}
		return time;
	}

	/**
	 * zwraca mozliwe przesiadki (lista id lini) z zadanego przystanku lub
	 * przystank�w o tej samej nazwie (tzn. w najbli�szej okolicy)
	 * 
	 * @param stop_id
	 * @return
	 */
	public ArrayList<Integer> getChanges(int stop_id) {
		ArrayList<Integer> list = new ArrayList<Integer>();
		try {
			Statement stmt = conn.createStatement();
			stmt.execute("use gtt");
			ResultSet s = stmt
					.executeQuery("Select distinct line_id from Timetable join (select stop_id from Stop where stop_name=(select stop_name from Stop where stop_id='"
							+ stop_id + "')) as p using(stop_id)");
			while (s.next()) {
				list.add(s.getInt("line_id"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
		Collections.sort(list);
		return list;
	}

	/**
	 * zwraca nazwy linii jadacych przez zadany przystanek
	 * 
	 * @param stop_id
	 * @return
	 */
	public ArrayList<String> getLinesViaStop(int stop_id) {
		ArrayList<String> list = new ArrayList<String>();
		try {
			Statement stmt = conn.createStatement();
			stmt.execute("use gtt");
			ResultSet s = stmt
					.executeQuery("SELECT distinct line_name from Line join"
							+ " (select line_id from Timetable where stop_id='"
							+ stop_id + "') S on Line.line_id = S.line_id");
			while (s.next()) {
				list.add(s.getString("line_name"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
		return list;
	}

	public double distance(Coordinates x, Coordinates y) {
		if (x.equals(y))
			return 0;
		else {
			double q = Math.sin(x.getLat()) * Math.sin(y.getLat())
					+ Math.cos(x.getLat()) * Math.cos(y.getLat())
					* Math.cos(x.getLng() - y.getLng());
			double d = (2 * Math.PI * 6400 * Math.acos(q)) / 360;
			return d;
		}

	}

	public ArrayList<Integer> getNearestStops(Coordinates x, int amount) {
		ArrayList<Tupple> s = new ArrayList<Tupple>();
		ArrayList<Integer> stops = new ArrayList<Integer>();
		ResultSet rs;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute("use gtt");
			rs = stmt.executeQuery("select stop_id, lat, lng from Stop");
			while (rs.next()) {
				double distance = distance(x, new Coordinates(rs
						.getDouble("lat"), rs.getDouble("lng")));
				s.add(new Tupple(rs.getInt("stop_id"), distance));
			}
			Collections.sort(s);
			for (int i = 0; i < amount; i++) {
				stops.add(s.get(i).getStopId());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return stops;
	}
}

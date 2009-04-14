package org.wroc.pwr.gtt.server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import org.wroc.pwr.gtt.server.dbupdater.XmlParser;
import org.wroc.pwr.gtt.server.graphcreator.GttGraph;

public class DBconnector {
	Connection conn = null;
	XmlParser parser = new XmlParser();
	Statement stmt;

	String driver;
	String host;
	String dbName;
	String userName;
	String pasword;

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

	public GttGraph loadGraph() {
		GttGraph graph = new GttGraph();

		try {
			Class.forName(driver).newInstance();
			conn = DriverManager.getConnection(host + dbName, userName, pasword);
			stmt = conn.createStatement();
			stmt.execute("use gtt");
			ResultSet rs = stmt.executeQuery("Select przyst_id from Przystanek");
			while (rs.next())
				graph.addVertex(rs.getInt("przyst_id"));

			rs = stmt.executeQuery("Select graf_id, ps_id, pe_id, linia_id, waga, Graf.typ_id, wariant_id from Graf join Linia using(linia_id) where wariant_id<3");
			System.out.println("loaded sql");

			while (rs.next()) {
				if (rs.getInt("waga") == 0)
					graph.addWEdge(this,rs.getInt(2), rs.getInt(3), rs.getInt(4), 0, rs.getInt(5), rs.getInt(6));
				else

					graph.addWEdge(this,rs.getInt(2), rs.getInt(3), rs.getInt(4), 1, rs.getInt(5), rs.getInt(6));

			}

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return graph;
	}

	public void updateDB(ArrayList<String> fileList) {

		try {

			String[] createStatement = readFileAsString("create.sql").split("\\n");
			for (int i = 0; i < createStatement.length; i++) {
				System.out.println(createStatement[i]);
				stmt.executeUpdate(createStatement[i]);
			}
			ResultSet s;
			stmt.execute("use gtt");
			//stmt.executeUpdate("INSERT INTO Dzien (dzien_id, dzien_nazwa)" + " VALUES('" + 0 + "', 'NONE')");
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
						//System.out.println(1 + " " + przyst_id.get(i) + " " + przyst_id.get(k) + " " + (0));
						stmt.executeUpdate("INSERT INTO Graf (ps_id, pe_id, linia_id, waga, typ_id) VALUES('" + przyst_id.get(i) + "','" + przyst_id.get(k) + "','" + 1
								+ "','" + 0 + "', '"+1+"')");

					}
			conn.close();
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

	public String getPrzystNazwa(int przyst_id){
		String nazwa=null;
		try {
			ResultSet s= stmt.executeQuery("Select przyst_nazwa from Przystanek where przyst_id='" + przyst_id +"'");
			while(s.next())
				nazwa = s.getString(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return nazwa;

	}
	public String getLiniaNazwa(int linia_id){
		String nazwa=null;
		try {
			ResultSet s= stmt.executeQuery("Select linia_nazwa from Linia where linia_id='" + linia_id +"'");
			while(s.next())
				nazwa = s.getString(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return nazwa;

	}

	public int getPrzystId(String nazwa){
		int id=-1;
		try {
			stmt.execute("use gtt");
			ResultSet s= stmt.executeQuery("Select przyst_id from Przystanek where przyst_nazwa like('%" + nazwa +"%') limit 1");
			while(s.next())
				id = s.getInt(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return id;

	}
	public int getLiniaId(String nazwa){
		int id=-1;
		try {
			stmt.execute("use gtt");
			ResultSet s= stmt.executeQuery("Select linia_id from Linia where linia_nazwa='" + nazwa +"' limit 1");
			while(s.next())
				id = s.getInt(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return id;

	}
	public ArrayList<String> getRoute(int linia_id){
		ArrayList<String> list = new ArrayList<String>();

			try {
				stmt.execute("use gtt");
				ResultSet s = stmt.executeQuery("select distinct linia_nazwa, linia_id, przyst_nazwa from Rozklad join (select linia_id, linia_nazwa from Linia where linia_id='" + linia_id + "') as l using(linia_id) join Przystanek using(przyst_id);");
				while (s.next()){
					list.add(s.getString(3));
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}



		return list;
	}

	public HashMap<String, ArrayList<String>> getTimeTable(int przyst_id, int linia){
		HashMap<String, ArrayList<String>> timeTable = new HashMap<String, ArrayList<String>>();
		try {
			stmt.execute("use gtt");
			ResultSet s = stmt.executeQuery("select przyst_nazwa,linia_nazwa, linia_id, dzien_nazwa, dzien_id, czas from Rozklad join (select linia_id, linia_nazwa from Linia where linia_id='" + linia + "') as l using(linia_id) join (select przyst_id, przyst_nazwa from Przystanek where przyst_id='" + przyst_id + "') as p using(przyst_id) join Dzien using(dzien_id);");
			while (s.next()){
				if (!timeTable.containsKey(s.getString("dzien_nazwa"))){
					timeTable.put(s.getString("dzien_nazwa"), new ArrayList<String>());
				}
				else{
					timeTable.get(s.getString("dzien_nazwa")).add(s.getString("czas"));
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return timeTable ;

	}
}

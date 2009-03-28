package org.wroc.pwr.gtt.server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

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

			rs = stmt.executeQuery("Select * from Graf");
			System.out.println("loaded sql");

			while (rs.next()) {
				if (rs.getInt("waga") == 0)
					graph.addWEdge(rs.getInt(2), rs.getInt(3), rs.getInt(4), 0, rs.getInt(5), rs.getInt(6));
				else

					graph.addWEdge(rs.getInt(2), rs.getInt(3), rs.getInt(4), 1, rs.getInt(5), rs.getInt(6));

			}
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return graph;
	}

	public void updateDB(ArrayList<String> fileList) {

		try {
			Class.forName(driver).newInstance();
			conn = DriverManager.getConnection(host + dbName, userName, pasword);
			stmt = conn.createStatement();
			String[] createStatement = readFileAsString("create.sql").split("\\n");
			for (int i = 0; i < createStatement.length; i++) {
				System.out.println(createStatement[i]);
				stmt.executeUpdate(createStatement[i]);
			}
			ResultSet s;
			stmt.execute("use gtt");
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
}

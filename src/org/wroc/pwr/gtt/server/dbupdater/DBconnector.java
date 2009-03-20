package org.wroc.pwr.gtt.server.dbupdater;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.sql.Connection;
import java.util.ArrayList;


public class DBconnector {
	Connection conn = null;
	XmlParser parser = new XmlParser();

	public void updateDB(String driver, String host, String dbName,
			String userName, String pasword, ArrayList<String> fileList) {

		try {
			Class.forName(driver).newInstance();
			conn = DriverManager
					.getConnection(host + dbName, userName, pasword);
			System.out.println("Connected to the database");

			Statement stmt = conn.createStatement();

			String[] createStatement = readFileAsString("create.sql").split(
					"\\n");
			for (int i = 0; i < createStatement.length; i++) {
				System.out.println(createStatement[i]);
				stmt.executeUpdate(createStatement[i]);
			}

			// Loop through the result set
			for (int i = 0; i < fileList.size(); i++) {
				
				parser.parse(fileList.get(i), conn);}
				
			

			conn.close();
			System.out.println("Disconnected from database");
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
}

package org.wroc.pwr.gtt;

import java.sql.Time;
import java.util.ArrayList;

import org.wroc.pwr.gtt.server.DBconnector;
import org.wroc.pwr.gtt.server.dbupdater.TTdownloader;
import org.wroc.pwr.gtt.server.graphcreator.GttGraph;

/**
 * Aktualny, testowy, konsolowy main pokazuj¹cy co i jak na razie dzia³a albo i
 * nie dzia³a...
 * 
 * @author Micha³ Brzeziñski-Spiczak
 * 
 */
public class Main {

	static String url = "http://www.wroclaw.pl/zdikzip/rozklady_xml.zip";
	static String archName = "MPK.zip";
	static String dir = "zdik";
	static String dbhost = "jdbc:mysql://localhost:3306/";
	static String dbName = "mysql";
	static String driver = "com.mysql.jdbc.Driver";
	static String userName = "root";
	static String pasword = "password";

	public static void main(String[] args) {

		// ArrayList<String> xmlFiles;
		// TTdownloader.download(url, archName); //pobieranie archiwum z
		// rozk³adami z serwera
		// xmlFiles = TTdownloader.unzip(archName, dir); //rozpakowanie archiwum
		// do odpowiedniego katalogu, zwraca liste nazw rozpakowanych plików
		DBconnector connector = new DBconnector(driver, dbhost, dbName, userName, pasword); // nawi¹zanie
																							// po³¹czenia
																							// z
																							// baz¹
		// connector.updateDB(xmlFiles); //update bazy danych wg plików xml

		connector.findCourse(2, connector.getPrzystId("dworzec"), connector.getPrzystId("krzyki"), 20);
		System.out.println(connector.getLinie());
		System.out.println(connector.getWarianty("2"));
		System.out.println(connector.getLiniaNazwa(2));
		System.out.println(connector.getWariantNazwa(2));
		System.out.println(connector.getTrasa(2));

		System.out.println(connector.getRozklad(1, "4"));

		Time time = new Time(9, 25, 0);
		System.out.println(connector.getNearest(35, 3, 1, time));

		System.out.println(connector.getChanges(14));
	}
}

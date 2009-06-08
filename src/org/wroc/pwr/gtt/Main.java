package org.wroc.pwr.gtt;

import java.util.ArrayList;

import org.wroc.pwr.gtt.server.Coordinates;
import org.wroc.pwr.gtt.server.DBconnector;
import org.wroc.pwr.gtt.server.dbupdater.TTdownloader;
import org.wroc.pwr.gtt.server.graphcreator.Leg;
import org.wroc.pwr.gtt.server.graphcreator.Route;

/**
 * Aktualny, testowy, konsolowy main pokazujacy co i jak na razie dziala albo i
 * nie dziala...
 * 
 * @author Michal Brzezinski-Spiczak
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
	static String tramCoFile = "tram.txt";
	static String busCoFile = "bus.txt";

	public static void main(String[] args) {

		 //update();

		DBconnector connector = new DBconnector(driver, dbhost, dbName,
				userName, pasword); // nawi�zanie po��czenia z baz�
		System.out.println(connector.getStopName(747));
		System.out.println(connector.getStopName(124));
		//System.out.println(connector.getStopIds("Eureka"));
		// System.out.println(connector.getLinesViaStop(15));
		System.out.println(connector.distance(connector.getCoordinates(747),
		 connector.getCoordinates(124)));
		// System.out.println(connector.distance(connector.getCoordinates(91),
		// connector.getCoordinates(257)));
		// System.out.println(connector.distance(connector.getCoordinates(91),
		// connector.getCoordinates(1303)));
		// System.out.println(connector.getNearestStops(connector.getCoordinates(91),2));
		// connector.updateGraph();
		// System.out.println(connector.getLineRoute(7));
	//	System.out.println(connector.getNearestStops(connector.getCoordinates(54),5));
		//System.out.println(connector.getNearestStops(connector.getCoordinates(56),5));
		//ArrayList<Route> result = connector.findCourse(true, false, false,
		//		connector.getCoordinates(63), connector.getCoordinates(360), 10,
		//		1, 1);
	//	for (Route r : result)
	//		System.out.println(r);
		
		
		//System.out.println(connector.findCourse(true, false, false, 55, 58, 10));
	}

	private static void update() {
		ArrayList<String> xmlFiles;
		TTdownloader.download(url, archName); // pobieranie archiwum z
		// rozk�adami z serwera
		xmlFiles = TTdownloader.unzip(archName, dir); // rozpakowanie
		// archiwum do odpowiedniego katalogu, zwraca liste nazw rozpakowanych
		// plik�w
		DBconnector connector = new DBconnector(driver, dbhost, dbName,
				userName, pasword); // nawi�zanie po��czenia z baz�

		connector.updateDB(xmlFiles, tramCoFile, busCoFile); // update bazy
		// danych wg plik�w xml

	}
}

package org.wroc.pwr.gtt;

import java.sql.Time;
import java.util.ArrayList;

import org.wroc.pwr.gtt.server.DBconnector;
import org.wroc.pwr.gtt.server.dbupdater.TTdownloader;
import org.wroc.pwr.gtt.server.graphcreator.GttGraph;
import org.wroc.pwr.gtt.server.graphcreator.LineStop;

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
	static String tramCoFile = "tram.txt";
	static String busCoFile = "bus.txt";

	public static void main(String[] args) {

		ArrayList<String> xmlFiles;
		TTdownloader.download(url, archName); // pobieranie archiwum z
		// rozk³adami z serwera
		xmlFiles = TTdownloader.unzip(archName, dir); // rozpakowanie archiwum
		// do odpowiedniego katalogu, zwraca liste nazw rozpakowanych plików
		DBconnector connector = new DBconnector(driver, dbhost, dbName, userName, pasword); // nawi¹zanie
		// po³¹czenia z baz¹
		connector.updateDB(xmlFiles,tramCoFile,busCoFile); // update bazy danych wg plików xml

		// ////////////////////wyszukiwanie po³¹czeñ z wydrukiem wszystkiego...
		Time time = new Time(3, 25, 0);
		ArrayList<ArrayList<LineStop>> sResult = connector.findCourse(4, connector.getPrzystId("eureka"), connector.getPrzystId("jana"), 10);
	// sResult = connector.resultTimeUpdate(sResult, time, 1);
		for (int i = 0; i < sResult.size(); i++) {
			for (int k = 0; k < sResult.get(i).size(); k++) {
				System.out.print("(" + connector.getLiniaNazwa(sResult.get(i).get(k).getLinia_id()) + ")");
				System.out.print("[" + connector.getPrzystNazwa(sResult.get(i).get(k).getPrzystStart()) + " - ");
				System.out.print(connector.getPrzystNazwa(sResult.get(i).get(k).getPrzystEnd()) + "(" + sResult.get(i).get(k).getTime() + ")]");
			}
//			System.out.println();
		}
		System.out.println(connector.getLinie());
		System.out.println(connector.getWarianty("2"));
		System.out.println(connector.getLiniaNazwa(2));
		System.out.println(connector.getWariantNazwa(2));
		System.out.println(connector.getTrasa(2));
		// System.out.println(connector.getRozklad(1, "4"));
		// System.out.println(connector.getNearest(35, 3, 1, time));
		// System.out.println(connector.getChanges(14));

	
	
	}
}

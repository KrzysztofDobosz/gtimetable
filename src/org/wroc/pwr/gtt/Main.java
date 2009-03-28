package org.wroc.pwr.gtt;

import java.util.ArrayList;

import org.wroc.pwr.gtt.server.DBconnector;
import org.wroc.pwr.gtt.server.dbupdater.TTdownloader;
import org.wroc.pwr.gtt.server.graphcreator.GttGraph;

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
	
		long start=System.currentTimeMillis();
		ArrayList<String> xmlFiles;
		TTdownloader.download(url, archName);
		xmlFiles = TTdownloader.unzip(archName, dir);
		DBconnector connector = new DBconnector(driver, dbhost, dbName, userName, pasword);
		connector.updateDB(xmlFiles);
		GttGraph graph = connector.loadGraph();
		System.out.println("loaded graph");
		System.out.println("wierzcholkow: " + graph.vertexSet().size());
		System.out.println("krawedzi: " + graph.edgeSet().size());
		long t=System.currentTimeMillis();
		System.out.println("rozpoczêto: " + (t -start)/1000);
		graph.findCourse(2,487, 14, 10);
	}
}

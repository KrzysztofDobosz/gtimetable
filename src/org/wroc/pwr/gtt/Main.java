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
		long t=System.currentTimeMillis();
		GttGraph graph = connector.loadGraph();
		System.out.println("loaded graph in " + (t-start)/1000 + ":" + (t-start)%1000);
		System.out.println("wierzcholkow: " + graph.vertexSet().size());
		System.out.println("krawedzi: " + graph.edgeSet().size());
		
		t=System.currentTimeMillis();
		graph.findCourse(20,connector.getPrzystId("eureka"), connector.getPrzystId("pracy"), 10);
		long t1=System.currentTimeMillis();
		System.out.println("wyszukano: " + (t1 -t)/1000 + ":" + (t1-t)%1000);
		System.out.println(connector.getRoute(2));
		System.out.println(connector.getTimeTable(3, 2));
	}
}

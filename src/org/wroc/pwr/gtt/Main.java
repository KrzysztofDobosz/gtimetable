package org.wroc.pwr.gtt;

import java.util.ArrayList;

import org.wroc.pwr.gtt.server.dbupdater.DBconnector;
import org.wroc.pwr.gtt.server.dbupdater.TTdownloader;

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
		//TTdownloader.download(url, archName);
		xmlFiles = TTdownloader.unzip(archName, dir);
		DBconnector connector = new DBconnector();
		
		connector.updateDB(driver, dbhost, dbName, userName, pasword, xmlFiles);
		
		long time = System.currentTimeMillis() - start;
		System.out.println("wykonano w " + time/1000 + "s");
		
		
	}
}

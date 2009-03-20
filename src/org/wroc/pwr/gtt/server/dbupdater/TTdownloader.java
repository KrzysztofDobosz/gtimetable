package org.wroc.pwr.gtt.server.dbupdater;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class TTdownloader {


	/**
	 * @param args
	 */



	final static int size = 1024;

	public static void download(String fAddress, String localFileName) {
		OutputStream outStream = null;
		URLConnection uCon = null;

		InputStream is = null;
		try {
			URL Url;
			byte[] buf;
			int ByteRead, ByteWritten = 0;
			Url = new URL(fAddress);
			

			outStream = new BufferedOutputStream(new FileOutputStream(
					 localFileName));

			uCon = Url.openConnection();
			System.out.println("Pobierany rozklad z dnia: " + new Date(uCon.getLastModified()));
			
			
			is = uCon.getInputStream();
			buf = new byte[size];
			while ((ByteRead = is.read(buf)) != -1) {
				outStream.write(buf, 0, ByteRead);
				ByteWritten += ByteRead;
			}
			System.out.println("Downloaded Successfully.");
			System.out.println("File name:\"" + localFileName
					+ "\"\nNo ofbytes :" + ByteWritten);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
				outStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static ArrayList<String> unzip(String file, String dir) {
		Enumeration entries;
		ZipFile zipFile;
		ArrayList<String> fileNames = new ArrayList<String>();

		try {
			zipFile = new ZipFile(file);

			entries = zipFile.entries();
			new File(dir).mkdir();
			while (entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) entries.nextElement();

				if (entry.isDirectory()) {
					// Assume directories are stored parents first then
					// children.
					System.err.println("Extracting directory: "
							+ entry.getName());
					// This is not robust, just for demonstration purposes.
					(new File(entry.getName())).mkdir();
					continue;
				}

				System.err.println("Extracting file: " + entry.getName());
				if (!entry.getName().contains("Kopia")){
				String[] nameTab = entry
				.getName().split("/");
				String name = nameTab[nameTab.length-1];
				
				copyInputStream(zipFile.getInputStream(entry),
						new BufferedOutputStream(new FileOutputStream(dir + "\\" + name)));
				fileNames.add(dir+"\\" + name);}
			}

			zipFile.close();
//			File del = new File(file);
//			del.delete();
		} catch (IOException ioe) {
			System.err.println("Unhandled exception:");
			ioe.printStackTrace();
			return fileNames;
		}
		return fileNames;
	}

	public static final void copyInputStream(InputStream in, OutputStream out)
			throws IOException {
		byte[] buffer = new byte[1024];
		int len;

		while ((len = in.read(buffer)) >= 0)
			out.write(buffer, 0, len);

		in.close();
		out.close();
	}

}
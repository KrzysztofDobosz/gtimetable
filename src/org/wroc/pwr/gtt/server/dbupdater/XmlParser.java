package org.wroc.pwr.gtt.server.dbupdater;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XmlParser {

	/**
	 * @param args
	 * @throws SQLException
	 */

	public void parse(String file, Connection conn) throws SQLException {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		javax.xml.parsers.DocumentBuilder builder = null;
		Statement stmt = conn.createStatement();

		try {
			builder = factory.newDocumentBuilder();
			Document doc = builder.parse(file);

			Node root = doc.getDocumentElement();

			if (root.getNodeName().equals("linie")) {

				NodeList lineNodes = root.getChildNodes();
				for (int i = 0; i < lineNodes.getLength(); i++) {
					Node line = lineNodes.item(i);
					if (line.getNodeName().equals("linia")) {
						String linia_nazwa = line.getAttributes().getNamedItem("nazwa").getNodeValue();
						System.out.print("LINIA " + linia_nazwa + " ...");
						long start = System.currentTimeMillis();
						String wazny_od = line.getAttributes().getNamedItem("wazny_od").getNodeValue();

						if (wazny_od.length() < 9)
							wazny_od = "NULL";
						else
							wazny_od = toSQLDate(wazny_od);

						String wazny_do = line.getAttributes().getNamedItem("wazny_do").getNodeValue();
						if (wazny_do.length() < 9)
							wazny_do = "NULL";

						else
							wazny_do = toSQLDate(wazny_do);

						String typ = line.getAttributes().getNamedItem("typ").getNodeValue();
						// update DB - typ
						ResultSet s = stmt.executeQuery("SELECT count(*) from Typ where typ = '" + typ + "'");
						s.next();
						if (s.getInt(1) == 0)
							stmt.executeUpdate("INSERT INTO Typ (typ)" + " VALUES('" + typ + "')");

						NodeList versionNodes = line.getChildNodes();
						for (int k = 0; k < versionNodes.getLength(); k++) {
							Node version = versionNodes.item(k);
							if (version.getNodeName().equals("wariant")) {
								String wariant_nazwa = version.getAttributes().getNamedItem("nazwa").getNodeValue();
								String wariant_id = version.getAttributes().getNamedItem("id").getNodeValue();

								// update DB - LINIA
								stmt.executeUpdate("INSERT INTO Linia (linia_nazwa, wariant_id, wariant_nazwa, typ_id, wazny_od, wazny_do)" + " VALUES('"
										+ linia_nazwa + "', '" + wariant_id + "', '" + wariant_nazwa + "', " + "(SELECT typ_id from Typ where typ='" + typ
										+ "'), " + wazny_od + ", " + wazny_do

										+ ")");

								NodeList stopsNodes = version.getChildNodes();
								for (int j = 0; j < stopsNodes.getLength(); j++) {
									Node stop = stopsNodes.item(j);
									if (stop.getNodeName().equals("przystanek")) {
										String zdik_id = stop.getAttributes().getNamedItem("id").getNodeValue();
										String przyst_nazwa = stop.getAttributes().getNamedItem("nazwa").getNodeValue();
										String ulica = stop.getAttributes().getNamedItem("ulica").getNodeValue();
										String cechy = stop.getAttributes().getNamedItem("cechy").getNodeValue();
										// update DB - przystanek
										s = stmt.executeQuery("SELECT count(*) from Przystanek where zdik_id = '" + zdik_id + "'");
										s.next();
										if (s.getInt(1) == 0)
											stmt.executeUpdate("INSERT INTO Przystanek (zdik_id, przyst_nazwa, ulica, cechy)" + " VALUES('" + zdik_id + "', '"
													+ przyst_nazwa + "', '" + ulica + "', '" + cechy + "')");
										// ///////////////
										NodeList tabNodes = stop.getChildNodes();
										for (int m = 0; m < tabNodes.getLength(); m++) {
											Node tab = tabNodes.item(m);
											String tabId = "";
											if (tab.getNodeName().equals("tabliczka"))
												tabId = tab.getAttributes().getNamedItem("id").getNodeValue();
											NodeList dayNodes = tab.getChildNodes();
											for (int n = 0; n < dayNodes.getLength(); n++) {
												Node day = dayNodes.item(n);
												String dzien = "";
												if (day.getNodeName().equals("dzien")) {
													dzien = day.getAttributes().getNamedItem("nazwa").getNodeValue();
													s = stmt.executeQuery("SELECT count(*) from Dzien where dzien_nazwa = '" + dzien + "'");
													s.next();
													if (s.getInt(1) == 0)
														stmt.executeUpdate("INSERT INTO Dzien (dzien_nazwa)" + " VALUES('" + dzien + "')");

												}
												NodeList hourNodes = day.getChildNodes();

												for (int p = 0; p < hourNodes.getLength(); p++) {
													Node hour = hourNodes.item(p);
													if (hour.getNodeName().equals("godz")) {
														NodeList minNodes = hour.getChildNodes();

														for (int r = 0; r < minNodes.getLength(); r++) {
															Node min = minNodes.item(r);
															if (min.getNodeName().equals("min")) {
																String godz = hour.getAttributes().getNamedItem("h").getNodeValue();
																String minuta = min.getAttributes().getNamedItem("m").getNodeValue();
																// update DB -
																// godzina,
																// minuta dla
																// rozklad

																stmt
																		.executeUpdate("INSERT INTO Rozklad (linia_id, przyst_id, nr_przyst, godzina, minuta, dzien_id)"
																				+ " VALUES("
																				+ "(SELECT linia_id from Linia where linia_nazwa = '"
																				+ linia_nazwa
																				+ "' AND wariant_id = '"
																				+ wariant_id
																				+ "'),"
																				+ "(SELECT przyst_id FROM Przystanek where zdik_id = '"
																				+ zdik_id
																				+ "')"
																				+ ", '"
																				+ tabId
																				+ "', "
																				+ godz
																				+ ", "
																				+ minuta
																				+ ", "
																				+ "(SELECT dzien_id from Dzien where dzien_nazwa = '" + dzien + "')" + ")");
																if (min.getAttributes().getLength() > 2) {
																	String ozn = removeBegSpeces(min.getAttributes().getNamedItem("ozn").getNodeValue());
																	String przyp = removeBegSpeces(min.getAttributes().getNamedItem("przyp").getNodeValue());
																	przyp = przyp.replace("'", "");

																	String[] tabprzyp = przyp.split(";");

																	if (!ozn.equals(" ")) {

																		for (int oznl = 0; oznl < ozn.length(); oznl++) {

																			tabprzyp[oznl] = removeBegSpeces(tabprzyp[oznl]);
																			s = stmt.executeQuery("SELECT count(*) from Przypis where przyp = '"
																					+ tabprzyp[oznl] + "'");
																			s.next();
																			if (s.getInt(1) == 0)
																				stmt.executeUpdate("INSERT INTO Przypis (przyp_ozn, przyp)" + " VALUES('"
																						+ ozn.charAt(oznl) + "', '" + tabprzyp[oznl] + "')");
																			s = stmt.executeQuery("SELECT count(*) from Oznaczenie where stop_id = (SELECT stop_id from Rozklad order by stop_id desc limit 1) and przyp_id = "
																					+ "(SELECT przyp_id from Przypis where przyp = '" + tabprzyp[oznl] + "')");

																			s.next();
																			if (s.getInt(1) == 0)
																			stmt.executeUpdate("INSERT INTO Oznaczenie (stop_id, przyp_id)" + " VALUES("
																					+ "(SELECT stop_id from Rozklad order by stop_id desc limit 1), "
																					+ "(SELECT przyp_id from Przypis where przyp = '" + tabprzyp[oznl] + "'))");

																		}

																	}

																}

															}
														}
													}

												}

											}
										}

									}
								}
							}
						}
						System.out.print("...loaded___");
						long time = System.currentTimeMillis() - start;
						System.out.println("wykonano w " + time / 1000 + "s");
					}
				}
			}

		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static String toSQLDate(String date) {

		String[] tab = date.split("\\.");
		String use = "'" + tab[2] + "-" + tab[1] + "-" + tab[0] + "'";
		return use;
	}

	public static String removeBegSpeces(String word) {
		String w = word;
		w = w.replaceAll("  ", " ");

		while (w.length() > 1)
			if (w.charAt(0) == ' ')
				w = w.substring(1);
			else
				break;
		while (w.length() > 1)
			if (w.charAt(w.length() - 1) == ' ')
				w = w.substring(0, w.length() - 1);
			else
				break;
		return w;
	}
}

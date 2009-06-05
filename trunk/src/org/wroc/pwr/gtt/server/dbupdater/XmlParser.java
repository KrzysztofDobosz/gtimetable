package org.wroc.pwr.gtt.server.dbupdater;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
/**
 * Parser xmli z rozkladem
 * @author Michal Brzezinski-Spiczak
 *
 */
public class XmlParser {

	public void parse(String file, Connection conn) throws SQLException {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		javax.xml.parsers.DocumentBuilder builder = null;
		Statement stmt = conn.createStatement();
		ResultSet s;

		try {
			builder = factory.newDocumentBuilder();
			Document doc = builder.parse(file);

			Node root = doc.getDocumentElement();
			String tabId = "NULL";

			if (root.getNodeName().equals("linie")) {

				NodeList lineNodes = root.getChildNodes();
				for (int i = 0; i < lineNodes.getLength(); i++) {
					Node line = lineNodes.item(i);
					if (line.getNodeName().equals("linia")) {
						String line_name = line.getAttributes().getNamedItem("nazwa").getNodeValue();
						line_name= line_name.replaceAll(" ", "");
						System.out.print("LINIA " + line_name + " ...");
						long start = System.currentTimeMillis();
						String valid_from = line.getAttributes().getNamedItem("wazny_od").getNodeValue();

						if (valid_from.length() < 9)
							valid_from = "NULL";
						else
							valid_from = toSQLDate(valid_from);

						String valid_to = line.getAttributes().getNamedItem("wazny_do").getNodeValue();
						if (valid_to.length() < 9)
							valid_to = "NULL";

						else
							valid_to = toSQLDate(valid_to);

						String type = line.getAttributes().getNamedItem("typ").getNodeValue();
						// update DB - typ
						s = stmt.executeQuery("SELECT count(*) from Type where type_name = '" + type + "'");
						s.next();
						if (s.getInt(1) == 0)
							stmt.executeUpdate("INSERT INTO Type (type_name)" + " VALUES('" + type + "')");

						NodeList versionNodes = line.getChildNodes();
						for (int k = 0; k < versionNodes.getLength(); k++) {
							Node version = versionNodes.item(k);
							if (version.getNodeName().equals("wariant")) {
								String version_name = version.getAttributes().getNamedItem("nazwa").getNodeValue();
								String version_id = version.getAttributes().getNamedItem("id").getNodeValue();

								// update DB - LINIA
								stmt.executeUpdate("INSERT INTO Line (line_name, version_id, version_name, type_id, valid_from, valid_to)" + " VALUES('"
										+ line_name + "', '" + version_id + "', '" + version_name + "', " + "(SELECT type_id from Type where type_name='"
										+ type + "'), " + valid_from + ", " + valid_to

										+ ")");
								ArrayList<Integer> stop_ids = new ArrayList<Integer>();
								NodeList stopsNodes = version.getChildNodes();
								for (int j = 0; j < stopsNodes.getLength(); j++) {
									Node stop = stopsNodes.item(j);
									if (stop.getNodeName().equals("przystanek")) {
										String zdik_id = stop.getAttributes().getNamedItem("id").getNodeValue();
										String stop_name = stop.getAttributes().getNamedItem("nazwa").getNodeValue();
										String street = stop.getAttributes().getNamedItem("ulica").getNodeValue();
										String features = stop.getAttributes().getNamedItem("cechy").getNodeValue();
										// update DB - przystanek
										s = stmt.executeQuery("SELECT distinct stop_id from Stop where zdik_id = '" + zdik_id + "'");

										if (!s.next()) {
											stmt.executeUpdate("INSERT INTO Stop (zdik_id, stop_name, street, features)" + " VALUES('" + zdik_id + "', '"
													+ stop_name + "', '" + street + "', '" + features + "')");
											s = stmt.executeQuery("SELECT distinct stop_id from Stop where zdik_id = '" + zdik_id + "'");
											s.next();
											stop_ids.add(s.getInt(1));
										} else {
											stop_ids.add(s.getInt(1));
										}

										// ///////////////
										NodeList tabNodes = stop.getChildNodes();

										boolean added = false;

										for (int m = 0; m < tabNodes.getLength(); m++) {

											Node tab = tabNodes.item(m);

											if (tab.getNodeName().equals("tabliczka")) {
												added = true;
												tabId = tab.getAttributes().getNamedItem("id").getNodeValue();

												NodeList dayNodes = tab.getChildNodes();
												for (int n = 0; n < dayNodes.getLength(); n++) {
													Node day = dayNodes.item(n);
													String day_name = "";
													if (day.getNodeName().equals("dzien")) {
														day_name = day.getAttributes().getNamedItem("nazwa").getNodeValue();
														s = stmt.executeQuery("SELECT count(*) from Day where day_name = '" + day_name + "'");
														s.next();
														if (s.getInt(1) == 0)
															stmt.executeUpdate("INSERT INTO Day (day_name)" + " VALUES('" + day_name + "')");

													}
													NodeList hourNodes = day.getChildNodes();

													for (int p = 0; p < hourNodes.getLength(); p++) {
														Node hourNode = hourNodes.item(p);
														if (hourNode.getNodeName().equals("godz")) {
															NodeList minNodes = hourNode.getChildNodes();

															for (int r = 0; r < minNodes.getLength(); r++) {
																Node minNode = minNodes.item(r);
																if (minNode.getNodeName().equals("min")) {
																	String godz = hourNode.getAttributes().getNamedItem("h").getNodeValue();
																	String minute = minNode.getAttributes().getNamedItem("m").getNodeValue();

																	stmt.executeUpdate("INSERT INTO Timetable (line_id, stop_id, stop_number, departuretime, day_id)"
																			+ " VALUES(" + "(SELECT line_id from Line where line_name = '" + line_name
																			+ "' AND version_id = '" + version_id + "'),"
																			+ "(SELECT stop_id FROM Stop where zdik_id = '" + zdik_id + "')" + ", '"
																			+ tabId + "', '" + Integer.parseInt(godz) % 24 + ":" + minute + "', "
																			+ "(SELECT day_id from Day where day_name = '" + day_name + "')" + ")");
																	if (minNode.getAttributes().getLength() > 2) {
																		String note_code = removeBegSpeces(minNode.getAttributes().getNamedItem("ozn").getNodeValue());
																		String note_mean = removeBegSpeces(minNode.getAttributes().getNamedItem("przyp").getNodeValue());
																		note_mean = note_mean.replace("'", "");

																		String[] tabprzyp = note_mean.split(";");

																		if (!note_mean.equals("")) {

																			for (int oznl = 0; oznl < tabprzyp.length; oznl++) {

																				tabprzyp[oznl] = removeBegSpeces(tabprzyp[oznl]);
																				s = stmt.executeQuery("SELECT count(*) from Note where note_mean = '"
																						+ tabprzyp[oznl] + "'");
																				s.next();
																				if (s.getInt(1) == 0)
																					stmt.executeUpdate("INSERT INTO Note (note_code, note_mean)" + " VALUES('"
																							+ note_mean.charAt(oznl) + "', '" + tabprzyp[oznl] + "')");
																				s = stmt
																						.executeQuery("SELECT count(*) from Mark where event_id = (SELECT event_id from Timetable order by event_id desc limit 1) and note_id = "
																								+ "(SELECT note_id from Note where note_mean ='"
																								+ tabprzyp[oznl] + "')");
																				s.next();
																				if (s.getInt(1) == 0)
																					stmt.executeUpdate("INSERT INTO Mark (event_id, note_id)"
																							+ " VALUES("
																							+ "(SELECT event_id from Timetable order by event_id desc limit 1), "
																							+ "(SELECT note_id from Note where note_mean = '" + tabprzyp[oznl]
																							+ "'))");

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
										// /dodawanie przystank�w koncowych do
										// rozk�adu (w celu p�niejszej
										// u�atwionej generacji trasy...
										// godzina, minuta=NULL, powatarzaj� sie
										// nr_przyst...
										if (!added) {
											stmt.executeUpdate("INSERT INTO Timetable (line_id, stop_id, stop_number) VALUES("
													+ "(SELECT line_id from Line where line_name = '" + line_name + "' AND version_id = '" + version_id
													+ "')," + "(SELECT stop_id FROM Stop where zdik_id = '" + zdik_id + "')" + ", "
													+ (Integer.parseInt(tabId) + 1) + ")");
										}

									}
								}
								
								if (version_id.equals("1") || version_id.equals("2")){
								s = stmt.executeQuery("Select line_id, type_id from Line where line_name = '" + line_name + "' and version_id='"
										+ version_id + "'");
								s.next();
								int line_id = s.getInt(1);
								int type_id = s.getInt(2);
								for (int q = 0; q < stop_ids.size(); q++) {
									int current = stop_ids.get(q);
									for (int w = q + 1; w < stop_ids.size(); w++) {
										int next = stop_ids.get(w);
										// System.out.println(linia_id + " " +
										// current + " " + next + " " + (w-q));
										// insert into graf values
										// current, next, linia_id, w-q
										stmt.executeUpdate("INSERT INTO Graph (ps_id, pe_id, line_id, weight, type_id) VALUES('" + current + "','" + next + "','"
												+ line_id + "','" + (w - q) + "', '" + type_id + "')");
									}
								}
							}}
						}
						System.out.print("...loaded___");
						long time = System.currentTimeMillis() - start;
						System.out.print("wykonano w " + time / 1000 + "s");
					}
				}
			}
			long start = System.currentTimeMillis();

			long time = System.currentTimeMillis() - start;
			System.out.println("   dodanie do grafu w  " + time / 1000 + "s");
		} catch (Exception e) {
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

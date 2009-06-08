#!/bin/bash

source exports.sh
APPDIR=`dirname $0`;

mkdir out

java -Xmx256m -cp "$APPDIR/src:$GWT_HOME/gwt-user.jar:$GWT_HOME/gwt-dev-linux.jar:$GWT_MAPS/gwt-maps.jar" \
	  com.google.gwt.dev.GWTCompiler -out "$APPDIR/out" "$@" org.wroc.pwr.gtt.Client;

mkdir -p out/org.wroc.pwr.gtt.Client/WEB-INF/classes
mkdir out/org.wroc.pwr.gtt.Client/WEB-INF/lib

cp web.xml out/org.wroc.pwr.gtt.Client/WEB-INF/web.xml
cp $GWT_HOME/gwt-servlet.jar out/org.wroc.pwr.gtt.Client/WEB-INF/lib/
cp create.sql out/org.wroc.pwr.gtt.Client/
cp bus.txt out/org.wroc.pwr.gtt.Client/
cp tram.txt out/org.wroc.pwr.gtt.Client/
cp mysql-connector-java-5.1.7-bin.jar out/org.wroc.pwr.gtt.Client/WEB-INF/lib/
cp jgraph.jar out/org.wroc.pwr.gtt.Client/WEB-INF/lib/
cp jgrapht-jdk1.6.jar out/org.wroc.pwr.gtt.Client/WEB-INF/lib/

javac -cp "$APPDIR/src/org/wroc/pwr/gtt/client:$APPDIR/src/org/wroc/pwr/gtt/server:$GWT_HOME/gwt-user.jar:$GWT_HOME/gwt-servlet.jar:$APPDIR/mysql-connector-java-5.1.7-bin.jar:$APPDIR/jgraph.jar:$APPDIR/jgrapht-jdk1.6.jar" \
	-d "$APPDIR/out/org.wroc.pwr.gtt.Client/WEB-INF/classes" \
	src/org/wroc/pwr/gtt/client/GttService.java \
	src/org/wroc/pwr/gtt/client/GttServiceAsync.java \
	src/org/wroc/pwr/gtt/server/dbupdater/TTdownloader.java \
	src/org/wroc/pwr/gtt/server/dbupdater/XmlParser.java \
	src/org/wroc/pwr/gtt/server/graphcreator/GttGraph.java \
	src/org/wroc/pwr/gtt/server/graphcreator/Leg.java \
	src/org/wroc/pwr/gtt/server/graphcreator/Route.java \
	src/org/wroc/pwr/gtt/server/graphcreator/StopDist.java \
	src/org/wroc/pwr/gtt/server/graphcreator/WEdge.java \
	src/org/wroc/pwr/gtt/server/Coordinates.java \
	src/org/wroc/pwr/gtt/server/DBconnector.java \
	src/org/wroc/pwr/gtt/server/GttServiceImpl.java

cd out/org.wroc.pwr.gtt.Client/
zip -qr gtt *
rm -R $TOMCAT_HOME/webapps/gtt
rm $TOMCAT_HOME/webapps/gtt.war
cp gtt.zip $TOMCAT_HOME/webapps/gtt.war
cd ../..
rm -R out

cd $TOMCAT_HOME/bin
./startup.sh
read -p "Press enter to shutdown tomcat server..."
./shutdown.sh

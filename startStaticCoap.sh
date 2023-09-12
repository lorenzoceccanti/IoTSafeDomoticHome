cd statical-coap-discovery
mvn clean install
mvn package
read -p "Compilation done. Press enter to execute"
gnome-terminal -- sh -c "java -jar target/statical-coap-discovery-0.0.1-SNAPSHOT.jar; exec bash"


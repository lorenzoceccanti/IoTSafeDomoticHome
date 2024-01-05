cd SafeDomoticHome
mvn clean install
mvn package
read -p "Compilation done. Press enter to execute"
gnome-terminal -- sh -c "java -jar target/SafeDomoticHome-0.0.1-SNAPSHOT.jar; exec bash"


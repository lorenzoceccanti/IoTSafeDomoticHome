read -p "ATTENTION. Make sure that the BR is already DEPLOYED. If YES press enter"
read -p "Connect the mote as second USB in VBox and press enter"
read -p "You are flashing the MQTT VOC sensor. Press enter to continue"
cd mqtt-network
make TARGET=nrf52840 BOARD=dongle kitchen_sensor.dfu-upload PORT=/dev/ttyACM1

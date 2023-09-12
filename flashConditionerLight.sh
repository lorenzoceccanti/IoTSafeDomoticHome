read -p "ATTENTION. Make sure that the BR is already DEPLOYED. If YES press enter"
read -p "Connect the mote as second USB in VBox and press enter"
read -p "You are flashing the COAP conditioner and light. Press enter to continue"
cd coap-wsn
make TARGET=nrf52840 BOARD=dongle cond-server.dfu-upload PORT=/dev/ttyACM1

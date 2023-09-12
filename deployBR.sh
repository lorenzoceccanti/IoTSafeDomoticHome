read -p "ATTENTION. MAKE SURE THAT THE BR IS ALREADY FLASHED INTO THE SENSOR. If YES press enter"
read -p "YOU HAVE INSERTED THE BR MOTE AS THE FIRST USB IN VIRTUALBOX? If YES press enter"
cd rpl-border-router
make TARGET=nrf52840 BOARD=dongle PORT=/dev/ttyACM0 connect-router

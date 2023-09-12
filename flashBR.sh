read -p "ATTENTION. Make sure that this mote is connected as first. Then press enter"
read -p "You are flashing the Contiki-BR. Press enter to continue"
cd rpl-border-router
make TARGET=nrf52840 BOARD=dongle border-router.dfu-upload PORT=/dev/ttyACM0

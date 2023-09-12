# SafeDomoticHome IoT project
This is the repository for the IoT project that I've developed in september 2023.
I started the project on Sunday 3rd September and I've finished it on Tuesday 12th September.

# To have the application ready on the IoT VM
Just execute the following indications:
- Extract this zip in the folder home/contiki-ng/, then open a terminal and type:
```
docker start -ai fervent_sutherland
```
```
./flashBR.sh
```
```
./deployBR.sh
```
- Open an other terminal
```
docker start -ai jolly_rubin
```
- Flash one by one all the WSN devices. For each of this steps use only one USB port at time.
```
./flashTemperature.sh
```
```
./flashVOC
```
```
./flashConditionerLight.sh
```
- Write down the SN number written on the label of the mote you've just flashed
```
./flashSirenFan.sh
```
- Write down the SN number written on the label of the mote you've just flashed

# To automatically compile and execute the Java APP
- To make the statical CoAP discovery procedure start:
```
./startStatiCoap.sh
```
- To make the Java Main Application start
```
./startApp.sh
```
# Other utility commands:
Please notice that the Contiki code has to be recompiled with the command:
```
make TARGET=nrf52840 BOARD=dongle name-file.dfu-upload PORT=/dev/ttyACM$
```
Where $ could be 0,1,2,...

Also the Border Router has to be recompiled. To compile it:
```
make TARGET=nrf52840 BOARD=dongle border-router.dfu-upload PORT=/dev/ttyACM0
```
To use tunslip6:
```
make TARGET=nrf52840 BOARD=dongle PORT=/dev/ttyACM0 connect-router
```

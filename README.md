# SafeDomoticHome IoT project
This is the repository for the IoT project that I've developed in september 2023.
I started the project on Sunday 3rd September and I've finished it on Tuesday 12th September.

Please notice that the Contiki code has to be recompiled with the command:
make TARGET=nrf52840 BOARD=dongle name-file.dfu-upload PORT=/dev/ttyACM$
Where $ could be 0,1,2,...

Also the Border Router has to be recompiled:
To compile
'''
make TARGET=nrf52840 BOARD=dongle PORT=/dev/ttyACM0 border-router.dfu-upload
'''
To use tunslip6:
'''
make TARGET=nrf52840 BOARD=dongle PORT=/dev/ttyACM0 connect-router
'''

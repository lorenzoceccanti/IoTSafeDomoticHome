CONTIKI_PROJECT = siren-server cond-server
all: $(CONTIKI_PROJECT)

# Include CoAP resources
MODULES_REL += ./resources

CONTIKI = ../..

# Enable CoAP
include $(CONTIKI)/Makefile.dir-variables
MODULES += $(CONTIKI_NG_APP_LAYER_DIR)/coap

include $(CONTIKI)/Makefile.include

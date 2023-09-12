#include <stdlib.h>
#include <string.h>
#include "coap-engine.h"
#include "os/dev/leds.h"
#include <stdio.h>
#include "../fanSpeed.h"

//nrf52840 dongle
#define LEDS_CONF_YELLOW    1

int fanSpeed = 0;

static void res_fan_put_handler(coap_message_t* request, coap_message_t* response, uint8_t *buffer,
uint16_t preferred_size, int32_t *offset);

RESOURCE(fan_res, "Fan", NULL, NULL, res_fan_put_handler, NULL);

static void res_fan_put_handler(coap_message_t* request, coap_message_t* response, uint8_t *buffer,
uint16_t preferred_size, int32_t *offset){
    char outputString[60];
    strcpy(outputString, "");

    unsigned int accept = -1;
    coap_get_header_content_format(request, &accept);
    if(accept == APPLICATION_JSON)
        sscanf((const char*)request->payload,"{\"fanSpeed\":%d}",&fanSpeed);
    else{
        coap_set_status_code(response, NOT_ACCEPTABLE_4_06);
        const char *msg = "Supporting content-type application/json only";
        coap_set_payload(response, msg, strlen(msg));
        return;
    }
    sprintf(outputString, "[FAN]: Speed set to %d", fanSpeed);

    coap_set_header_content_format(response, TEXT_PLAIN);
    memcpy(buffer, outputString, strlen(outputString)+1);
    coap_set_payload(response, buffer, strlen((char*)buffer)+1);
}
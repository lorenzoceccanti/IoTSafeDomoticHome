#include <stdlib.h>
#include <string.h>
#include "coap-engine.h"
#include "os/dev/leds.h"
#include "sys/ctimer.h"
#include <stdio.h>
#include "clima_res.h"

//nrf52840 dongle
#define LEDS_CONF_YELLOW    1

static int isOn = 0;

static void res_light_get_handler(coap_message_t* request, coap_message_t* response, uint8_t *buffer,
uint16_t preferred_size, int32_t *offset);
static void res_light_put_handler(coap_message_t* request, coap_message_t* response, uint8_t *buffer,
uint16_t preferred_size, int32_t *offset);

RESOURCE(light_res, "Light res", res_light_get_handler, NULL, res_light_put_handler, NULL);


static void res_light_get_handler(coap_message_t* request, coap_message_t* response, uint8_t *buffer,
uint16_t preferred_size, int32_t *offset){

    char outputString[40];
    strcpy(outputString, "");

    unsigned int accept = -1;
    coap_get_header_accept(request, &accept);
    if(accept == APPLICATION_JSON)
    {
        sprintf(outputString, "{\"bulbStatus\":%d}", isOn);
        coap_set_header_content_format(response, APPLICATION_JSON);
        memcpy(buffer, outputString, strlen(outputString)+1);
        coap_set_payload(response, buffer, strlen((char*)buffer));
    } else {
        coap_set_status_code(response, NOT_ACCEPTABLE_4_06);
        const char *msg = "Supporting content-type application/json only";
        coap_set_payload(response, msg, strlen(msg));
    }
}

// request->payload contains the payload of the CoAP request
// request->payload_len is the lenght of the CoAP request

static void res_light_put_handler(coap_message_t* request, coap_message_t* response, uint8_t *buffer,
uint16_t preferred_size, int32_t *offset){

    int command = -1;
    char outputString[180];
    strcpy(outputString, "");

    unsigned int accept = -1;
    coap_get_header_content_format(request, &accept);
    if(accept == APPLICATION_JSON)
    {
        coap_set_header_content_format(response, TEXT_PLAIN);
        sscanf((const char*)request->payload,"{\"command\":%d}",&command);
        if(command == 0)
        {
            isOn = 0;
            strcpy(outputString, "[Light]: OFF");
            leds_off(LEDS_CONF_YELLOW);
        }else if(command == 1)
        {
            isOn = 1;
            strcpy(outputString, "[Light]: ON");
            leds_on(LEDS_CONF_YELLOW);
        }
        memcpy(buffer, outputString, strlen(outputString)+1);
        if(command !=0 && command != 1)
            coap_set_status_code(response, BAD_REQUEST_4_00);
        coap_set_payload(response, buffer, strlen((char*)buffer)); // ignoring the undesired \0
    } else {
        coap_set_status_code(response, NOT_ACCEPTABLE_4_06);
        const char *msg = "Supporting content-type application/json only";
        coap_set_payload(response, msg, strlen(msg));
    }
}
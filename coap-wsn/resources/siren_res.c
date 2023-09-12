#include <stdlib.h>
#include <string.h>
#include "coap-engine.h"
#include "os/dev/leds.h"
#include "sys/ctimer.h"
#include <stdio.h>

//nrf52840 dongle
#define LEDS_CONF_YELLOW    1

static int zone0Triggered = 0;
static int zone1Triggered = 0;

static void res_siren_put_handler(coap_message_t* request, coap_message_t* response, uint8_t *buffer,
uint16_t preferred_size, int32_t *offset);

RESOURCE(siren_res, "Siren", NULL, NULL, res_siren_put_handler, NULL);

static void res_siren_put_handler(coap_message_t* request, coap_message_t* response, uint8_t *buffer,
uint16_t preferred_size, int32_t *offset){
    int len;
    const char* text = NULL;
    char outputString[80];
    strcpy(outputString, "");
    
    char zone[15];
    memset(zone, 0, 15);
    char mode[4];
    memset(mode, 0, 4);
    len = coap_get_post_variable(request, "zone", &text);
    if(len > 0 && len < 15)
        memcpy(zone, text, len);
    len = coap_get_post_variable(request, "mode", &text);
    if(len > 0 && len < 4)
        memcpy(mode, text, len);
    

    if(strcmp(zone, "alarm_0") == 0)
    {
            // Acting on the zone #0
            if(strcmp(mode, "ON")==0)
            {
                leds_off(LEDS_CONF_GREEN);
                leds_on(LEDS_CONF_BLUE);
                zone0Triggered = 1;

            }
            else if(strcmp(mode, "OFF")==0)
            {
                leds_off(LEDS_CONF_BLUE);
                zone0Triggered = 0;
            } else{
                coap_set_status_code(response, BAD_REQUEST_4_00);
                return;
            }
    }
    else if(strcmp(zone, "alarm_1") == 0){
        // Acting on the zone #1
        if(strcmp(mode, "ON")==0)
        {
            leds_off(LEDS_CONF_GREEN);
            leds_on(LEDS_CONF_RED);
            zone1Triggered = 1;
        }
        else if(strcmp(mode, "OFF")==0)
        {
            leds_off(LEDS_CONF_RED);
            zone1Triggered = 0;
        } else{
            coap_set_status_code(response, BAD_REQUEST_4_00);
            return;
        }       
    }else{
        coap_set_status_code(response, BAD_REQUEST_4_00);
        return;
    }

    // Checking if it's needed to return on the green led
    if(zone0Triggered == 0 && zone1Triggered == 0) // NO ALARMS
        leds_on(LEDS_CONF_GREEN);

    // Answering
    strcpy(outputString, "Done");

    coap_set_header_content_format(response, TEXT_PLAIN);
    memcpy(buffer, outputString, strlen(outputString)+1);
    coap_set_payload(response, buffer, strlen((char*)buffer)+1);
}
#include <stdlib.h>
#include <string.h>
#include "coap-engine.h"
#include "os/dev/leds.h"
#include "sys/ctimer.h"
#include <stdio.h>
#include "clima_res.h"

//nrf52840 dongle
#define LEDS_CONF_YELLOW    1
#define LEDS_CLIMA_ALL      10

static void res_clima_post_handler(coap_message_t* request, coap_message_t* response, uint8_t *buffer,
uint16_t preferred_size, int32_t *offset);

static void res_clima_put_handler(coap_message_t* request, coap_message_t* response, uint8_t *buffer,
uint16_t preferred_size, int32_t *offset);

RESOURCE(clima_res, "Clima res", NULL, res_clima_post_handler, res_clima_put_handler, NULL);

// request->payload contains the payload of the CoAP request
// request->payload_len is the lenght of the CoAP request
static void res_clima_post_handler(coap_message_t* request, coap_message_t* response, uint8_t *buffer,
uint16_t preferred_size, int32_t *offset){
   
    // NOTE: TO TEST 
    // coap-client -m post coap://[fd00::f6ce:36b3:3f0b:956]/clima -e "{\"warming\":0,\"cooling\":1}"

    int warming; int cooling;
    char outputString[180];
    strcpy(outputString, "");

    unsigned int accept = -1;
    coap_get_header_content_format(request, &accept);
    if(accept == APPLICATION_JSON)
    {
        sscanf((const char*)request->payload,"{\"warming\":%d,\"cooling\":%d}",&warming, &cooling);
    } else {
        coap_set_status_code(response, NOT_ACCEPTABLE_4_06);
        const char *msg = "Supporting content-type application/json only";
        coap_set_payload(response, msg, strlen(msg));
        return;
    }

    if(warming == 1 && cooling == 0)
    {
            leds_off(LEDS_CONF_BLUE);
            leds_on(LEDS_CONF_RED);
    }
    else if(cooling == 1 && warming == 0){
        leds_on(LEDS_CONF_BLUE);
        leds_off(LEDS_CONF_RED);
    } else if(cooling == 0 && warming == 0){
        leds_off(LEDS_CLIMA_ALL);
    } else {
        coap_set_status_code(response, BAD_REQUEST_4_00);
        return;
    }

    // Answering
    strcpy(outputString, "Done");
    coap_set_header_content_format(response, TEXT_PLAIN);
    memcpy(buffer, outputString, strlen(outputString)+1);
    coap_set_payload(response, buffer, strlen((char*)buffer)+1);
}

static void res_clima_put_handler(coap_message_t* request, coap_message_t* response, uint8_t *buffer,
uint16_t preferred_size, int32_t *offset){

    struct remoteController rc;
    char outputString[180];
    strcpy(outputString, "");

    unsigned int accept = -1;
    coap_get_header_content_format(request, &accept);
    if(accept == APPLICATION_JSON)
         sscanf((const char*)request->payload,"{\"power\":\"%2s\",\"selTemp\":%d,\"fanSpeed\":%d}",rc.power, &rc.selTemp, &rc.fanSpeed);
    else{
        coap_set_status_code(response, NOT_ACCEPTABLE_4_06);
        const char *msg = "Supporting content-type application/json only";
        coap_set_payload(response, msg, strlen(msg));
        return;
    }
       
    if(strcmp(rc.power,"OF") == 0)
    {
        strcpy(outputString, "[Conditioner]: OFF");
        leds_off(LEDS_CLIMA_ALL);
    }
    else if(strcmp(rc.power, "ON") == 0 && (rc.selTemp >= 160 && rc.selTemp <= 340) && (rc.fanSpeed > 0 && rc.fanSpeed < 6))
    {
        sprintf(outputString, "[Conditioner]: ON, Temperature: %d, Fan Speed: %d", rc.selTemp, rc.fanSpeed);
    } else {
        coap_set_status_code(response, BAD_REQUEST_4_00);
        const char *msg = "[Conditioner]: temp must be in [16,34] and sp in [0,5]";
        coap_set_payload(response, msg, strlen(msg));
        return; 
    }
    coap_set_header_content_format(response, TEXT_PLAIN);
    memcpy(buffer, outputString, strlen(outputString)+1);
    coap_set_payload(response, buffer, strlen((char*)buffer)+1);
}
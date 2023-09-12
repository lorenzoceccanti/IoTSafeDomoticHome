#include "contiki.h"
#include "coap-engine.h"
#include "sys/etimer.h"
#include "os/dev/button-hal.h"
#include "leds.h"
#include "fanSpeed.h"

#define LEDS_CONF_YELLOW    1

// Define the resource
extern coap_resource_t siren_res;
extern coap_resource_t fan_res;

static struct etimer et;
PROCESS(sirenServer, "CoAP server");
AUTOSTART_PROCESSES(&sirenServer);

int fanSpeedToClock(int speed)
{
    int ret = 0;
    switch(speed)
    {
        case 1:
            ret = 2;
        break;
        case 2:
            ret = 1;
        break;
        case 3:
            ret = 0;
        break;
        default:
            ret = 0;
        break;
    }
    return ret;
}

PROCESS_THREAD(sirenServer, ev, data)
{
    PROCESS_BEGIN();
    // Activation of the resource siren
    coap_activate_resource(&siren_res, "siren");
    // Activation of the resource fan
    coap_activate_resource(&fan_res, "fan");

    // No alarms
    leds_on(LEDS_CONF_GREEN);

    // etimer loading
    fanSpeed = 0;
    etimer_set(&et, fanSpeedToClock(fanSpeed)*CLOCK_SECOND);
    while(1)
    {
        PROCESS_WAIT_EVENT();
        if(ev == PROCESS_EVENT_TIMER)
        {
            if(fanSpeed == 0)
                leds_off(LEDS_CONF_YELLOW);
            else
                leds_toggle(LEDS_CONF_YELLOW);
            etimer_set(&et, fanSpeedToClock(fanSpeed)*CLOCK_SECOND);
        }
    }
    PROCESS_END();
}
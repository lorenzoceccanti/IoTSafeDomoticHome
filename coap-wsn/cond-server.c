#include "contiki.h"
#include "coap-engine.h"

// Define the resource
extern coap_resource_t clima_res;
extern coap_resource_t light_res;

PROCESS(condServer, "CoAP server");
AUTOSTART_PROCESSES(&condServer);

PROCESS_THREAD(condServer, ev, data)
{
    PROCESS_BEGIN();
    // Activation of the resource clima
    coap_activate_resource(&clima_res, "clima");
    coap_activate_resource(&light_res, "light");
    while(1)
    {
        PROCESS_WAIT_EVENT();
    }
    PROCESS_END();
}
// Code to flash into Temperature + Alarm sensor

// Basing on LEDs status and the button presssion, the alarm
// zone will change accordingly and so also the to topic towards publish to
// first color (BLUE): -> zone 0
// second color (RED): -> zone 1

#include "contiki.h"
#include "net/routing/routing.h"
#include "mqtt.h"
#include "net/ipv6/uip.h"
#include "net/ipv6/uip-icmp6.h"
#include "net/ipv6/sicslowpan.h"
#include "sys/etimer.h"
#include "sys/ctimer.h"
#include "lib/sensors.h"
#include "dev/button-hal.h"
#include "dev/leds.h"
#include "os/sys/log.h"
#include "mqtt-client.h"
#include "door_sensor.h"

#include <string.h>
#include <strings.h>
#include <time.h>
#include <stdio.h>
#include <stdlib.h>

#define LOG_MODULE "door_sensor"
#ifdef MQTT_CLIENT_CONF_LOG_LEVEL
#define LOG_LEVEL MQTT_CLIENT_CONF_LOG_LEVEL
#else
#define LOG_LEVEL LOG_LEVEL_DBG
#endif

// Defining the broker address
#define MQTT_CLIENT_BROKER_IP_ADDR "fd00::1"
static const char *broker_ip = MQTT_CLIENT_BROKER_IP_ADDR;

// Broker configuration params
#define DEFAULT_BROKER_PORT         1883
#define DEFAULT_PUBLISH_INTERVAL    (30 * CLOCK_SECOND)
#define PUBLISH_INTERVAL            (3 * CLOCK_SECOND)

/* MQTT client statuses*/
static uint8_t state;

#define STATE_INIT    		        0   // Initial state
#define STATE_NET_OK    	        1   // Network is initialized
#define STATE_CONNECTING            2   // Connecting to MQTT broker
#define STATE_CONNECTED             3   // Connecting successful
#define STATE_SUBSCRIBED            4   // Topics of interest subscribed
#define STATE_DISCONNECTED          5   // Disconnected from MQTT broker

PROCESS_NAME(mqtt_door_sensor);
AUTOSTART_PROCESSES(&mqtt_door_sensor);
// Data structure that stores the connection status
static struct mqtt_connection conn;
/* Maximum TCP segment size and lenght of IPv6 addresses */
#define MAX_TCP_SEGMENT_SIZE        32
#define CONFIG_IP_ADDR_STR_LEN      64

mqtt_status_t status;
static struct mqtt_message *msg_ptr = 0;
char broker_address[CONFIG_IP_ADDR_STR_LEN];

// Buffers for clientId and topics
#define BUFFER_SIZE 64
static char client_id[BUFFER_SIZE];
static char pub_topic1[BUFFER_SIZE];
static char pub_topic2[BUFFER_SIZE];
static char sub_topic[BUFFER_SIZE];

// Periodic timer to check the state of the MQTT client
#define STATE_MACHINE_PERIODIC     (CLOCK_SECOND >> 1)
static struct etimer periodic_timer;

// Application level buffer
#define APP_BUFFER_SIZE             512
static char app_buffer[APP_BUFFER_SIZE];

// Sensor specific

static struct ctimer b_leds;
static int publishingCounter = 0;

// Alarm part
#define NUM_ZONES                   2
// Variable related to the magnet status
// isMagnetOpen is equal to 1 -> the door/window is OPEN
// isMagnetOpen is equal to 0 ->  the door/window is CLOSED
// We assume that by default the magnet is closed
static int isMagnetOpen = 0;
static int zone = -1;
static int probability = 20;

// Thermometer part
static struct conditioner cond;
static int currentTemp;
// By default the emulated behaviour is random
static bool warming = false;
static bool cooling = false;
static char strTemperature[10];
static int variation;

// nrf52840 specific
#define LEDS_CONF_YELLOW            1

PROCESS(mqtt_door_sensor, "MQTT Door Sensor");


/* Before any MQTT operation the network has to be initialized correctly
This function could be used to check that the network is operational*/
static bool have_connectivity()
{
    if(uip_ds6_get_global(ADDR_PREFERRED) == NULL 
    ||uip_ds6_defrt_choose() == NULL) {
        return false;
    }
    return true;
}

void parseArrivingJSON(char* jsonStr)
{
    sscanf(jsonStr,"{\"power\":\"%2s\",\"selTemp\":%d,\"fanSpeed\":%d}",cond.power, &cond.tempset, &cond.speed);
}

// This handler is triggered when a notification on a subscribed topic is
// received.
// This emulates the increasing/decreasing of temperature when the 
// air conditioner is on
static void pub_handler(const char *topic, uint16_t topic_len, const uint8_t *chunk,
uint16_t chunk_len)
{
    if(strcmp((char*)topic, "conditioner")==0)
    {
        // Arriving JSON
        parseArrivingJSON((char*)chunk);
        
        if(strcmp(cond.power, "ON")==0)
        {
            // Compare the actual temperature with the selected one
            if(cond.tempset - currentTemp < 0)
            {
              // The conditioner acts to cool
              warming = false;
              cooling = true;
            } else if(cond.tempset - currentTemp > 0){
              // The conditioner acts to warm
              cooling = false;
              warming = true;
            } else {
              // Otherwise don't change anything in temp values and vars
              cooling = false;
              warming = false;
            }
        }
    } else {
        LOG_ERR("Undesired topic\n");
    }
}

// MQTT Event handler
static void mqtt_event(struct mqtt_connection *m, mqtt_event_t event, void *data)
{
  switch(event) {
  case MQTT_EVENT_CONNECTED: {
    printf("Application has a MQTT connection\n");
    state = STATE_CONNECTED;
    break;
  }
  case MQTT_EVENT_DISCONNECTED: {
    printf("MQTT Disconnect. Reason %u\n", *((mqtt_event_t *)data));
    state = STATE_DISCONNECTED;
    process_poll(&mqtt_door_sensor);
    break;
  }
  case MQTT_EVENT_PUBLISH: {
    msg_ptr = data;
    pub_handler(msg_ptr->topic, strlen(msg_ptr->topic),
                msg_ptr->payload_chunk, msg_ptr->payload_length);
    break;
  }
  case MQTT_EVENT_SUBACK: {
#if MQTT_311
    mqtt_suback_event_t *suback_event = (mqtt_suback_event_t *)data;
    if(suback_event->success) {
      printf("Application is subscribed to topic successfully\n");
    } else {
      printf("Application failed to subscribe to topic (ret code %x)\n", suback_event->return_code);
    }
#else
    printf("Application is subscribed to topic successfully\n");
#endif
    break;
  }
  case MQTT_EVENT_UNSUBACK: {
    printf("Application is unsubscribed to topic successfully\n");
    break;
  }
  case MQTT_EVENT_PUBACK: {
    printf("Publishing complete.\n");
    break;
  }
  default:
    printf("Application got a unhandled MQTT event: %i\n", event);
    break;
  }
}

// Function that colours the leds
void led_init(){
    switch(zone)
    {
        case 0: // COLOUR COOJA GREEN
            leds_set(LEDS_CONF_YELLOW | LEDS_CONF_BLUE);
            break;
        case 1: // COLOUR COOJA RED
            leds_set(LEDS_CONF_YELLOW | LEDS_CONF_RED);
            break;
        default:
            leds_off(LEDS_ALL);
            break;
    }
}

void leds_def(){
  switch(zone)
  {
      case 0: // COLOUR COOJA GREEN
            leds_set(LEDS_CONF_BLUE);
            break;
      case 1: // COLOUR COOJA RED
            leds_set(LEDS_CONF_RED);
            break;
      default:
            leds_off(LEDS_ALL);
            break;
  }
}

void sensor_emulate_opening()
{
    // The probability changes depeding on the button pressing
    // after the zone selection

    // Generating an index between 0 and 99
    int randomizator = rand() % 100;

    isMagnetOpen = (randomizator <= probability - 1) ? 1 : 0;

}

void sensor_emulate_temperature_sensing()
{
    // Checking states
    // Changes here
    if(cooling)
    {
      if(cond.tempset - currentTemp < 0)
      {
          currentTemp -= cond.speed; 
      } else
          cooling = false;
    }
    else if(warming){
      if(cond.tempset - currentTemp > 0)
      {
          currentTemp += cond.speed;
      } else
          warming = false;
    } else {
      // Random increasing/decreasing of temperature
      if((rand()%10) < 6) { // 60% chance that the temperature will change
						variation = (rand()%3)-1; // a value in [-1, 1]
						currentTemp = currentTemp + variation;
					}
    }
}

PROCESS_THREAD(mqtt_door_sensor, ev, data)
{
    PROCESS_BEGIN();
    // To indicate that a zone is not selected yet, YELLOW led is on
    leds_on(LEDS_CONF_YELLOW);

    // Zone selection
    // This portion of code is used to select the proper zone to which the sensor belongs to
    // 1 btn pressure -> zone 0
    // 2 btn pressures -> zone 1
    // 3 btn pressures -> zone 2
    // The zone confirmation has to be confirmed with a holding > 5 sec
    
    
    button_hal_button_t *btn;
    while(1)
    {
        PROCESS_YIELD();
        // If the button has pressed
        if(ev == button_hal_press_event)
        {
            btn = (button_hal_button_t*)data;
            zone = (zone + 1) % NUM_ZONES;
            led_init();
        }
        if(ev == button_hal_periodic_event){
            btn = (button_hal_button_t*)data;
            // If the pression lasts for more than 5 seconds
            if(btn->press_duration_seconds >= 5)
            {
                // First turning on all leds
                leds_on(LEDS_ALL);
                // Confirmation of the zone
                ctimer_set(&b_leds, 2*CLOCK_SECOND, leds_def, NULL);
                break;
            }
        }
    }
    
    // Generate a random temperature
    currentTemp = rand() % 200 + 150;   // in the range 15.0-34.9 degrees

    // At this point the node will have the color corresponding to the belonging zone
    // Initialize the ClientID and MAC address of the node
    snprintf(client_id, BUFFER_SIZE, "%02x%02x%02x%02x%02x%02x",
    linkaddr_node_addr.u8[0], linkaddr_node_addr.u8[1],
    linkaddr_node_addr.u8[2], linkaddr_node_addr.u8[5],
    linkaddr_node_addr.u8[6], linkaddr_node_addr.u8[7]);
    
    // Broker registration
    mqtt_register(&conn, &mqtt_door_sensor, client_id, mqtt_event, 
    MAX_TCP_SEGMENT_SIZE);

    state = STATE_INIT;

    // Initialize periodic timer to check the status 
    etimer_set(&periodic_timer, PUBLISH_INTERVAL);

    // Main loop
    while(1)
    {
        PROCESS_YIELD();

        if((ev == PROCESS_EVENT_TIMER && data == &periodic_timer) || 
        ev == PROCESS_EVENT_POLL){
            // Managing the states
            if(state == STATE_INIT){
                if(have_connectivity() == true)
                    state = STATE_NET_OK;
            }
            if(state == STATE_NET_OK){
                printf("Connecting to MQTT server\n");
                memcpy(broker_address, broker_ip, strlen(broker_ip));
                mqtt_connect(&conn, broker_address, DEFAULT_BROKER_PORT,
                (DEFAULT_PUBLISH_INTERVAL * 3)/ CLOCK_SECOND, MQTT_CLEAN_SESSION_ON);
                state = STATE_CONNECTING;
            }
            if(state == STATE_CONNECTED)
            {
              // Subscring to the conditioner topic
              // This is only needed to make the temperature sensing emulation realistic
              strcpy(sub_topic, "conditioner");
              status = mqtt_subscribe(&conn, NULL, sub_topic, MQTT_QOS_LEVEL_0);
              if(status == MQTT_STATUS_OUT_QUEUE_FULL){
                  LOG_ERR("Tried to subscribe but command queue was full!\n");
                  PROCESS_EXIT();
				      }
              state = STATE_SUBSCRIBED;
            }
            // Simulating a possible behaviour for the alarm sensor
            if(state == STATE_SUBSCRIBED){

                // Distinguishing the behaviour
                if(publishingCounter % 2 == 0)
                {
                    sprintf(pub_topic1, "%s_%d", "zone", zone);
                    sensor_emulate_opening();
                    sprintf(app_buffer, "{\"sensor_id\": %s, \"isOpened\": %d}", client_id, isMagnetOpen);
                    mqtt_publish(&conn, NULL, pub_topic1, (uint8_t*) app_buffer, strlen(app_buffer),
                    MQTT_QOS_LEVEL_0, MQTT_RETAIN_OFF);
                } else {
                    strcpy(pub_topic2, "temp");
                    sensor_emulate_temperature_sensing();
                    sprintf(strTemperature, "%d", currentTemp);
                    sprintf(app_buffer, "{\"sensor_id\": %s, \"currentTemp\": %c%c.%c}", client_id,
                    strTemperature[0],strTemperature[1], strTemperature[2]);
                    mqtt_publish(&conn, NULL, pub_topic2, (uint8_t*) app_buffer, strlen(app_buffer),
                    MQTT_QOS_LEVEL_0, MQTT_RETAIN_OFF);
                }
            } 
            if(state == STATE_DISCONNECTED){
                printf("Disconnected form MQTT broker\n");	
                state = STATE_INIT;
		        }
            publishingCounter++;
        		etimer_set(&periodic_timer, PUBLISH_INTERVAL);
      }

        if(ev == button_hal_press_event)
        {
            btn = (button_hal_button_t*)data;
            probability = (probability + 20) % 100;
            LOG_INFO("Opening probability changed to: %d\n", probability);
        }

    }
    PROCESS_END();    
}

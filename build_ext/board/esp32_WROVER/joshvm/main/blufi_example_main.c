/*
   This example code is in the Public Domain (or CC0 licensed, at your option.)

   Unless required by applicable law or agreed to in writing, this
   software is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
   CONDITIONS OF ANY KIND, either express or implied.
*/


/****************************************************************************
* This is a demo for bluetooth config wifi connection to ap. You can config ESP32 to connect a softap
* or config ESP32 as a softap to be connected by other device. APP can be downloaded from github
* android source code: https://github.com/EspressifApp/EspBlufi
* iOS source code: https://github.com/EspressifApp/EspBlufiForiOS
****************************************************************************/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "freertos/event_groups.h"
#include "esp_system.h"
#include "esp_wifi.h"
#include "esp_event.h"
#include "esp_log.h"
#include "nvs_flash.h"
#include "esp_bt.h"

#include "esp_blufi_api.h"
#include "esp_bt_defs.h"
#include "esp_gap_ble_api.h"
#include "esp_bt_main.h"
#include "esp_bt_device.h"
#include "blufi_example.h"
extern void javacall_printf(const char * format,...);
extern void javanotify_blufi_event(const int val);
static void example_event_callback(esp_blufi_cb_event_t event, esp_blufi_cb_param_t *param);

//Macros of Blufi Events. See BlufiThread.java
#define BLUFI_EVT_CUSTOMDATA 1
#define BLUFI_EVT_SSID 2
#define BLUFI_EVT_PASSWORD 3
#define BLUFI_EVT_ACTIVE_CLOSE 4
#define BLUFI_EVT_CONNECT 5
#define BLUFI_EVT_DISCONNECT 6
#define BLUFI_EVT_GOT_REQ_WIFI_CONNECT 7
#define BLUFI_EVT_GOT_REQ_WIFI_DISCONNECT 8
#define WIFI_EVT_START 100
#define WIFI_EVT_CONNECTED 101
#define WIFI_EVT_DISCONNECTED 102
#define WIFI_EVT_GOT_IP 103

//Wifi disconnect reason
#define JOSHVM_WIFI_DISCONNECT_REASON_DEFAULT 0
#define JOSHVM_WIFI_DISCONNECT_REASON_AUTH_EXPIRE 1
#define JOSHVM_WIFI_DISCONNECT_REASON_4WAY_HANDSHAKE_TIMEOUT 2
#define JOSHVM_WIFI_DISCONNECT_REASON_AUTH_FAIL 3
#define JOSHVM_WIFI_DISCONNECT_REASON_ASSOC_EXPIRE 4
#define JOSHVM_WIFI_DISCONNECT_REASON_HANDSHAKE_TIMEOUT 5
#define JOSHVM_WIFI_DISCONNECT_REASON_NO_AP_FOUND 6

#define CUSTOM_SIZE 64
#define MAX_ADV_DATA_SIZE 25
static char blufi_device_name[MAX_ADV_DATA_SIZE + 1]={'\0'};
static uint8_t gl_service_uuid128[32] = {
    /* LSB <--------------------------------------------------------------------------------> MSB */
    //first uuid, 16bit, [12],[13] is the value
    0xfb, 0x34, 0x9b, 0x5f, 0x80, 0x00, 0x00, 0x80, 0x00, 0x10, 0x00, 0x00, 0xFF, 0xFF, 0x00, 0x00,
};
static const uint8_t const_service_uuid128[32] = {
    /* LSB <--------------------------------------------------------------------------------> MSB */
    //first uuid, 16bit, [12],[13] is the value
    0xfb, 0x34, 0x9b, 0x5f, 0x80, 0x00, 0x00, 0x80, 0x00, 0x10, 0x00, 0x00, 0xFF, 0xFF, 0x00, 0x00,
};
/* manufacture data*/
static uint8_t gl_manufacturer_data[MAX_ADV_DATA_SIZE] = {'\0'};

static int custom_data_len = 0;
//static uint8_t test_manufacturer[TEST_MANUFACTURER_DATA_LEN] =  {0x12, 0x23, 0x45, 0x56};
static esp_ble_adv_data_t ble_adv_data = {
    .set_scan_rsp = false,
    .include_name = false,
    .include_txpower = false,
    .min_interval = 0, //slave connection min interval, Time = min_interval * 1.25 msec
    .max_interval = 0, //slave connection max interval, Time = max_interval * 1.25 msec
    .appearance = 0x00,
    .manufacturer_len = 0,
    .p_manufacturer_data =  gl_manufacturer_data,
    .service_data_len = 0,
    .p_service_data = NULL,
    .service_uuid_len = 16,
    .p_service_uuid = gl_service_uuid128,
    .flag = 0x6,
};

static esp_ble_adv_params_t example_adv_params = {
    .adv_int_min        = 0x100,
    .adv_int_max        = 0x100,
    .adv_type           = ADV_TYPE_IND,
    .own_addr_type      = BLE_ADDR_TYPE_PUBLIC,
    //.peer_addr            =
    //.peer_addr_type       =
    .channel_map        = ADV_CHNL_ALL,
    .adv_filter_policy = ADV_FILTER_ALLOW_SCAN_ANY_CON_ANY,
};

#define WIFI_LIST_NUM   10

static wifi_config_t sta_config;
static wifi_config_t ap_config;

static enum {
    STA_UNINITIALIZED = 0,
    STA_START,
    STA_CONNECTING,
    STA_CONNECTED,
    STA_DISCONNECTED
} wifi_status = STA_UNINITIALIZED;

/*
  custom buffer
*/
static unsigned char custom_buffer[64];

/* FreeRTOS event group to signal when we are connected & ready to make a request */
static EventGroupHandle_t wifi_event_group;

/* The event group allows multiple bits for each event,
   but we only care about one event - are we connected
   to the AP with an IP? */
const int CONNECTED_BIT = BIT0;

/* store the station info for send back to phone */
static bool gl_sta_connected = false;
static bool ble_is_connected = false;
static uint8_t gl_sta_bssid[6];
static uint8_t gl_sta_ssid[32];
static int gl_sta_ssid_len;

/* connect infor*/
static uint8_t server_if;
static uint16_t conn_id;

/* flag auto reconnect setting*/
int auto_reconnect = 0;

//
// JOSHVM supply code
//
int joshvm_esp32_blufi_set_ble_name(char* deviceName) {
    strncpy(blufi_device_name, deviceName, MAX_ADV_DATA_SIZE);
    blufi_device_name[MAX_ADV_DATA_SIZE] = '\0';
    return strlen(blufi_device_name);
}

int joshvm_esp32_blufi_set_manufacturer_data(uint8_t* data, int data_len) {
    if (data_len > MAX_ADV_DATA_SIZE) {
        data_len = MAX_ADV_DATA_SIZE;
    }
    memcpy(gl_manufacturer_data, data, data_len);
    ble_adv_data.manufacturer_len = data_len;
    return data_len;
}

void joshvm_esp32_blufi_set_service_uuid(uint32_t uuid) {
    memcpy(gl_service_uuid128, const_service_uuid128, sizeof(gl_service_uuid128));
    gl_service_uuid128[12] = uuid & 0xFF;
    gl_service_uuid128[13] = (uuid >> 8) & 0xFF;
    gl_service_uuid128[14] = (uuid >> 16) & 0xFF;
    gl_service_uuid128[15] = (uuid >> 24) & 0xFF;
}

void joshvm_esp32_blufi_set_service_uuid128(uint8_t* uuid) {
    memcpy(gl_service_uuid128, uuid, sizeof(gl_service_uuid128));
}

int  joshvm_esp32_blufi_get_data(unsigned char* buffer,int buflen){
	
	if(custom_data_len>0){
	    /*Data available*/
	    if (buflen > custom_data_len) buflen = custom_data_len;
	    memcpy(buffer,custom_buffer,buflen);
	    custom_data_len=0;
	}else{
	    buflen = 0 ;
	}
	return buflen;
}

void joshvm_esp32_blufi_send_data(char* message, int len){
		esp_blufi_send_custom_data((uint8_t*)message,(uint32_t)len);
}

int joshvm_esp32_wifi_get_state(int* state) {
    *state = wifi_status;
    return 0;
}

wifi_config_t* joshvm_esp32_blufi_get_wifi_config() {
    return &sta_config;
}

int joshvm_esp32_get_sys_info(char* info, int size) {   
	return 0;
}

int joshvm_esp32_wifi_set(char* ssid, int ssid_len, char* password, int password_len, int force) {    
    //If ssid == NULL, keep ssid untouched
    if (ssid) {
        if (ssid_len < 0 || ssid_len >= sizeof(sta_config.sta.ssid)) {
            return -1;
        }
        memcpy((char *)sta_config.sta.ssid, ssid, ssid_len);
        sta_config.sta.ssid[ssid_len] = '\0';
    }

    //If password is NULL, keep password untouched
    if (password) {
        if (password_len < 0 || password_len >= sizeof(sta_config.sta.password)) {
            return -1;
        }
        memcpy((char *)sta_config.sta.password, password, password_len);
        sta_config.sta.password[password_len] = '\0';
    }

    //Set wifi config if force is not 0
    if (force) {
        esp_wifi_set_config(WIFI_IF_STA, &sta_config);
    }

    return 0;
}

int joshvm_esp32_blufi_get_bt_addr(char* addr_buffer, int buffer_len) {
    if (buffer_len < 6) {
        return -1;
    }

    memcpy(addr_buffer, esp_bt_dev_get_address(), 6);
    return 6;
}

int joshvm_esp32_wifi_get_connected_ssid(char* ssid_buffer, int buffer_len) {
    if (buffer_len < gl_sta_ssid_len) {
        return -1;
    }
    memcpy(ssid_buffer, gl_sta_ssid, gl_sta_ssid_len);
    return gl_sta_ssid_len;
}

int joshvm_esp32_blufi_is_connected() {
    if (ble_is_connected){
        return 1;
    } else {
        return 0;
    }
}

void joshvm_esp32_wifi_connect() {
    /* there is no wifi callback when the device has already connected to this wifi
        so disconnect wifi before connection.
        */
    esp_wifi_disconnect();
    wifi_status = STA_DISCONNECTED;
    auto_reconnect = 1;
    esp_wifi_connect();
    wifi_status = STA_CONNECTING;   
}

void joshvm_esp32_wifi_disconnect() {
    auto_reconnect = 0;
    esp_wifi_disconnect();
    wifi_status = STA_DISCONNECTED; 
}

void joshvm_esp32_blufi_close() {
    javanotify_blufi_event(BLUFI_EVT_ACTIVE_CLOSE);
}

/** Event handlers*/
static void ip_event_handler(void* arg, esp_event_base_t event_base,
                                int32_t event_id, void* event_data)
{
    wifi_mode_t mode;

    switch (event_id) {
    case IP_EVENT_STA_GOT_IP: {
        esp_blufi_extra_info_t info;

        xEventGroupSetBits(wifi_event_group, CONNECTED_BIT);
        esp_wifi_get_mode(&mode);

        memset(&info, 0, sizeof(esp_blufi_extra_info_t));
        memcpy(info.sta_bssid, gl_sta_bssid, 6);
        info.sta_bssid_set = true;
        info.sta_ssid = gl_sta_ssid;
        info.sta_ssid_len = gl_sta_ssid_len;
        wifi_status = STA_CONNECTED;
        javanotify_blufi_event(WIFI_EVT_GOT_IP);
        if (ble_is_connected == true) {
            esp_blufi_send_wifi_conn_report(mode, ESP_BLUFI_STA_CONN_SUCCESS, 0, &info);            
        }
        break;
    }
    default:
        break;
    }
    return;
}

static void wifi_event_handler(void* arg, esp_event_base_t event_base,
                                int32_t event_id, void* event_data)
{
    wifi_event_sta_connected_t *event;
    wifi_mode_t mode;

    switch (event_id) {
    case WIFI_EVENT_STA_START:
        wifi_status = STA_START;
        //esp_wifi_connect();
        //wifi_status = STA_CONNECTING;
        javanotify_blufi_event(WIFI_EVT_START);
        break;
    case WIFI_EVENT_STA_CONNECTED:
        gl_sta_connected = true;
        //wifi_status = STA_CONNECTED;
        event = (wifi_event_sta_connected_t*) event_data;
        memcpy(gl_sta_bssid, event->bssid, 6);
        memcpy(gl_sta_ssid, event->ssid, event->ssid_len);
        gl_sta_ssid_len = event->ssid_len;
        javanotify_blufi_event(WIFI_EVT_CONNECTED);
        break;
    case WIFI_EVENT_STA_DISCONNECTED:
        {
        unsigned int reason;
        wifi_event_sta_disconnected_t* disconnected = (wifi_event_sta_disconnected_t*) event_data;

        /* This is a workaround as ESP32 WiFi libs don't currently
           auto-reassociate. */
        gl_sta_connected = false;
        wifi_status = STA_DISCONNECTED;
        memset(gl_sta_ssid, 0, 32);
        memset(gl_sta_bssid, 0, 6);
        gl_sta_ssid_len = 0;
        switch (disconnected->reason) {
            case WIFI_REASON_AUTH_EXPIRE:
                reason = JOSHVM_WIFI_DISCONNECT_REASON_AUTH_EXPIRE;
                break;
            case WIFI_REASON_4WAY_HANDSHAKE_TIMEOUT:
                reason = JOSHVM_WIFI_DISCONNECT_REASON_4WAY_HANDSHAKE_TIMEOUT;
                break;
            case WIFI_REASON_AUTH_FAIL:
                reason = JOSHVM_WIFI_DISCONNECT_REASON_AUTH_FAIL;
                break;
            case WIFI_REASON_ASSOC_EXPIRE:
                reason = JOSHVM_WIFI_DISCONNECT_REASON_ASSOC_EXPIRE;
                break;
            case WIFI_REASON_HANDSHAKE_TIMEOUT:                
                reason = JOSHVM_WIFI_DISCONNECT_REASON_HANDSHAKE_TIMEOUT;
                break;
            case WIFI_REASON_NO_AP_FOUND:
                reason = JOSHVM_WIFI_DISCONNECT_REASON_NO_AP_FOUND;
                break;
            default:                
                reason = JOSHVM_WIFI_DISCONNECT_REASON_DEFAULT;
        }

        if (reason == JOSHVM_WIFI_DISCONNECT_REASON_DEFAULT && auto_reconnect) {
            esp_wifi_connect();
            wifi_status = STA_CONNECTING;
        } else {
            javanotify_blufi_event(WIFI_EVT_DISCONNECTED | (reason << 16));
        }

        xEventGroupClearBits(wifi_event_group, CONNECTED_BIT);
        }
        break;
    case WIFI_EVENT_AP_START:
        esp_wifi_get_mode(&mode);

        /* TODO: get config or information of softap, then set to report extra_info */
        if (ble_is_connected == true) {
            if (gl_sta_connected) {
                esp_blufi_send_wifi_conn_report(mode, ESP_BLUFI_STA_CONN_SUCCESS, 0, NULL);
            } else {
                esp_blufi_send_wifi_conn_report(mode, ESP_BLUFI_STA_CONN_FAIL, 0, NULL);
            }
        }
        break;
    case WIFI_EVENT_SCAN_DONE: {
        uint16_t apCount = 0;
        esp_wifi_scan_get_ap_num(&apCount);
        if (apCount == 0) {
            break;
        }
        wifi_ap_record_t *ap_list = (wifi_ap_record_t *)malloc(sizeof(wifi_ap_record_t) * apCount);
        if (!ap_list) {
            break;
        }
        ESP_ERROR_CHECK(esp_wifi_scan_get_ap_records(&apCount, ap_list));
        esp_blufi_ap_record_t * blufi_ap_list = (esp_blufi_ap_record_t *)malloc(apCount * sizeof(esp_blufi_ap_record_t));
        if (!blufi_ap_list) {
            if (ap_list) {
                free(ap_list);
            }
            break;
        }
        for (int i = 0; i < apCount; ++i)
        {
            blufi_ap_list[i].rssi = ap_list[i].rssi;
            memcpy(blufi_ap_list[i].ssid, ap_list[i].ssid, sizeof(ap_list[i].ssid));
        }

        if (ble_is_connected == true) {
            esp_blufi_send_wifi_list(apCount, blufi_ap_list);
        }

        esp_wifi_scan_stop();
        free(ap_list);
        free(blufi_ap_list);
        break;
    }
    default:
        break;
    }
    return;
}

void initialise_wifi(void)
{
    ESP_ERROR_CHECK(esp_netif_init());
    wifi_event_group = xEventGroupCreate();
    ESP_ERROR_CHECK(esp_event_loop_create_default());
    esp_netif_t *sta_netif = esp_netif_create_default_wifi_sta();
    assert(sta_netif);
    ESP_ERROR_CHECK(esp_event_handler_register(WIFI_EVENT, ESP_EVENT_ANY_ID, &wifi_event_handler, NULL));
    ESP_ERROR_CHECK(esp_event_handler_register(IP_EVENT, IP_EVENT_STA_GOT_IP, &ip_event_handler, NULL));

    wifi_init_config_t cfg = WIFI_INIT_CONFIG_DEFAULT();
    ESP_ERROR_CHECK( esp_wifi_init(&cfg) );
    ESP_ERROR_CHECK( esp_wifi_set_mode(WIFI_MODE_STA) );
    ESP_ERROR_CHECK( esp_wifi_start() );
}

static esp_blufi_callbacks_t example_callbacks = {
    .event_cb = example_event_callback,
    .negotiate_data_handler = blufi_dh_negotiate_data_handler,
    .encrypt_func = blufi_aes_encrypt,
    .decrypt_func = blufi_aes_decrypt,
    .checksum_func = blufi_crc_checksum,
};

static void example_event_callback(esp_blufi_cb_event_t event, esp_blufi_cb_param_t *param)
{
    /* actually, should post to blufi_task handle the procedure,
     * now, as a example, we do it more simply */
    esp_err_t rets;
    switch (event) {
    case ESP_BLUFI_EVENT_INIT_FINISH:
        rets= esp_ble_gap_set_device_name(blufi_device_name);
        if (strlen(blufi_device_name) > 0) {
            ble_adv_data.include_name = true;
        } else {
            ble_adv_data.include_name = false;
        }
        javacall_printf("manufacturer_len=%d\n", ble_adv_data.manufacturer_len);
        esp_ble_gap_config_adv_data(&ble_adv_data);
        break;
    case ESP_BLUFI_EVENT_DEINIT_FINISH:
        break;
    case ESP_BLUFI_EVENT_BLE_CONNECT:
        ble_is_connected = true;
        server_if = param->connect.server_if;
        conn_id = param->connect.conn_id;
        esp_ble_gap_stop_advertising();
        blufi_security_init();
        javanotify_blufi_event(BLUFI_EVT_CONNECT);
        break;
    case ESP_BLUFI_EVENT_BLE_DISCONNECT:
        ble_is_connected = false;
        blufi_security_deinit();
        esp_ble_gap_start_advertising(&example_adv_params);
        javanotify_blufi_event(BLUFI_EVT_DISCONNECT);
        break;
    case ESP_BLUFI_EVENT_SET_WIFI_OPMODE:
        ESP_ERROR_CHECK( esp_wifi_set_mode(param->wifi_mode.op_mode) );
        break;
    case ESP_BLUFI_EVENT_REQ_CONNECT_TO_AP:
        joshvm_esp32_wifi_connect();
        javanotify_blufi_event(BLUFI_EVT_GOT_REQ_WIFI_CONNECT);
        break;
    case ESP_BLUFI_EVENT_REQ_DISCONNECT_FROM_AP:
        joshvm_esp32_wifi_disconnect();
        javanotify_blufi_event(BLUFI_EVT_GOT_REQ_WIFI_DISCONNECT);
        break;
    case ESP_BLUFI_EVENT_REPORT_ERROR:
        esp_blufi_send_error_info(param->report_error.state);
        break;
    case ESP_BLUFI_EVENT_GET_WIFI_STATUS: {
        wifi_mode_t mode;
        esp_blufi_extra_info_t info;

        esp_wifi_get_mode(&mode);

        if (gl_sta_connected) {
            memset(&info, 0, sizeof(esp_blufi_extra_info_t));
            memcpy(info.sta_bssid, gl_sta_bssid, 6);
            info.sta_bssid_set = true;
            info.sta_ssid = gl_sta_ssid;
            info.sta_ssid_len = gl_sta_ssid_len;
            esp_blufi_send_wifi_conn_report(mode, ESP_BLUFI_STA_CONN_SUCCESS, 0, &info);
        } else {
            esp_blufi_send_wifi_conn_report(mode, ESP_BLUFI_STA_CONN_FAIL, 0, NULL);
        }

        break;
    }
    case ESP_BLUFI_EVENT_RECV_SLAVE_DISCONNECT_BLE:
        esp_blufi_close(server_if, conn_id);
        break;
    case ESP_BLUFI_EVENT_DEAUTHENTICATE_STA:
        /* TODO */
        break;
	case ESP_BLUFI_EVENT_RECV_STA_BSSID:
        memcpy(sta_config.sta.bssid, param->sta_bssid.bssid, 6);
        sta_config.sta.bssid_set = 1;
        esp_wifi_set_config(WIFI_IF_STA, &sta_config);
        break;
	case ESP_BLUFI_EVENT_RECV_STA_SSID:
        strncpy((char *)sta_config.sta.ssid, (char *)param->sta_ssid.ssid, param->sta_ssid.ssid_len);
        sta_config.sta.ssid[param->sta_ssid.ssid_len] = '\0';
        esp_wifi_set_config(WIFI_IF_STA, &sta_config);
        javanotify_blufi_event(BLUFI_EVT_SSID);
        break;
	case ESP_BLUFI_EVENT_RECV_STA_PASSWD:
        strncpy((char *)sta_config.sta.password, (char *)param->sta_passwd.passwd, param->sta_passwd.passwd_len);
        sta_config.sta.password[param->sta_passwd.passwd_len] = '\0';
        esp_wifi_set_config(WIFI_IF_STA, &sta_config);
        javanotify_blufi_event(BLUFI_EVT_PASSWORD);
        break;
	case ESP_BLUFI_EVENT_RECV_SOFTAP_SSID:
        strncpy((char *)ap_config.ap.ssid, (char *)param->softap_ssid.ssid, param->softap_ssid.ssid_len);
        ap_config.ap.ssid[param->softap_ssid.ssid_len] = '\0';
        ap_config.ap.ssid_len = param->softap_ssid.ssid_len;
        esp_wifi_set_config(WIFI_IF_AP, &ap_config);
        break;
	case ESP_BLUFI_EVENT_RECV_SOFTAP_PASSWD:
        strncpy((char *)ap_config.ap.password, (char *)param->softap_passwd.passwd, param->softap_passwd.passwd_len);
        ap_config.ap.password[param->softap_passwd.passwd_len] = '\0';
        esp_wifi_set_config(WIFI_IF_AP, &ap_config);
        break;
	case ESP_BLUFI_EVENT_RECV_SOFTAP_MAX_CONN_NUM:
        if (param->softap_max_conn_num.max_conn_num > 4) {
            return;
        }
        ap_config.ap.max_connection = param->softap_max_conn_num.max_conn_num;
        esp_wifi_set_config(WIFI_IF_AP, &ap_config);
        break;
	case ESP_BLUFI_EVENT_RECV_SOFTAP_AUTH_MODE:
        if (param->softap_auth_mode.auth_mode >= WIFI_AUTH_MAX) {
            return;
        }
        ap_config.ap.authmode = param->softap_auth_mode.auth_mode;
        esp_wifi_set_config(WIFI_IF_AP, &ap_config);
        break;
	case ESP_BLUFI_EVENT_RECV_SOFTAP_CHANNEL:
        if (param->softap_channel.channel > 13) {
            return;
        }
        ap_config.ap.channel = param->softap_channel.channel;
        esp_wifi_set_config(WIFI_IF_AP, &ap_config);
        break;
    case ESP_BLUFI_EVENT_GET_WIFI_LIST:{
        wifi_scan_config_t scanConf = {
            .ssid = NULL,
            .bssid = NULL,
            .channel = 0,
            .show_hidden = false
        };
        esp_wifi_scan_start(&scanConf, true);
        break;
    }
    case ESP_BLUFI_EVENT_RECV_CUSTOM_DATA:
        esp_log_buffer_hex("Custom Data", param->custom_data.data, param->custom_data.data_len);
        if(custom_data_len==0){
            int actualLen = param->custom_data.data_len;
            if (actualLen > CUSTOM_SIZE) actualLen = CUSTOM_SIZE;
            memcpy(custom_buffer,param->custom_data.data,actualLen);
            custom_data_len = actualLen;
            
        }
        javanotify_blufi_event(BLUFI_EVT_CUSTOMDATA);
        break;
	case ESP_BLUFI_EVENT_RECV_USERNAME:
        /* Not handle currently */
        break;
	case ESP_BLUFI_EVENT_RECV_CA_CERT:
        /* Not handle currently */
        break;
	case ESP_BLUFI_EVENT_RECV_CLIENT_CERT:
        /* Not handle currently */
        break;
	case ESP_BLUFI_EVENT_RECV_SERVER_CERT:
        /* Not handle currently */
        break;
	case ESP_BLUFI_EVENT_RECV_CLIENT_PRIV_KEY:
        /* Not handle currently */
        break;;
	case ESP_BLUFI_EVENT_RECV_SERVER_PRIV_KEY:
        /* Not handle currently */
        break;
    default:
        break;
    }
}

static void example_gap_event_handler(esp_gap_ble_cb_event_t event, esp_ble_gap_cb_param_t *param)
{
    switch (event) {
    case ESP_GAP_BLE_ADV_DATA_SET_COMPLETE_EVT:
        esp_ble_gap_start_advertising(&example_adv_params);
        break;
    default:
        break;
    }
}

/** Blufi initialization code for JOSHVM*/
void blufi_init_joshvm(void)
{
    esp_err_t ret;

    // Initialize NVS

    ret = nvs_flash_init();
   if (ret == ESP_ERR_NVS_NO_FREE_PAGES || ret == ESP_ERR_NVS_NEW_VERSION_FOUND) {
        ESP_ERROR_CHECK(nvs_flash_erase());
       ret = nvs_flash_init();
    }
    javacall_printf("nvs_flash_init return %d\n", ret);
    ESP_ERROR_CHECK( ret );

    initialise_wifi();
    javacall_printf("Initialise_wifi ok\n");


    ret = esp_bt_controller_mem_release(ESP_BT_MODE_CLASSIC_BT);
    ESP_ERROR_CHECK(ret);

    esp_bt_controller_config_t bt_cfg = BT_CONTROLLER_INIT_CONFIG_DEFAULT();
    ret = esp_bt_controller_init(&bt_cfg);
    if (ret) {
        javacall_printf("%s initialize bt controller failed: %s\n", __func__, esp_err_to_name(ret));
    }

    ret = esp_bt_controller_enable(ESP_BT_MODE_BLE);
    if (ret) {
        javacall_printf("%s enable bt controller failed: %s\n", __func__, esp_err_to_name(ret));
        return;
    }

    ret = esp_bluedroid_init();
    if (ret) {
        javacall_printf("%s init bluedroid failed: %s\n", __func__, esp_err_to_name(ret));
        return;
    }

    ret = esp_bluedroid_enable();
    if (ret) {
        javacall_printf("%s init bluedroid failed: %s\n", __func__, esp_err_to_name(ret));
        return;
    }

    javacall_printf("BD ADDR: "ESP_BD_ADDR_STR"\n", ESP_BD_ADDR_HEX(esp_bt_dev_get_address()));

    javacall_printf("BLUFI VERSION %04x\n", esp_blufi_get_version());
}

/** Blufi starting for JOSHVM*/
void blufi_start_joshvm() {

    esp_err_t ret;


    ret = esp_ble_gap_register_callback(example_gap_event_handler);
    if(ret){
        javacall_printf("%s gap register failed, error code = %x\n", __func__, ret);
        return;
    }
    ret = esp_blufi_register_callbacks(&example_callbacks);
    if(ret){
        javacall_printf("%s blufi register failed, error code = %x\n", __func__, ret);
        return;
    }

    esp_blufi_profile_init();
}


#include <javacall_logging.h>
#include "javacall_blufi.h"

extern void blufi_start_joshvm();
void javacall_blufi_start(char *bluetoothName) {
	javacall_printf("finsh blufi %s ",bluetoothName);
	blufi_start_joshvm(bluetoothName);
}

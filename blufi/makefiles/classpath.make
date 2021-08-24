# Copyright (C) Max Mu
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
# 
# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License
# version 2, as published by the Free Software Foundation.
# 
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License version 2 for more details.
# 
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
# 
# Please visit www.joshvm.org if you need additional information or
# have any questions.

MODULE_NAME=esp32-blufi

# Javadoc source path
BLUFI_SOURCEPATH += $(BUILD_ROOT_DIR)/blufi/src/classes

# Java files for the ( blufi ) module
#

BLUFI_JAVA_DIR = $(ESP32_BLUFI_DIR)/src/classes

# Public API classes
#
MODULE_BLUFI_API_JAVA_FILES = \
	${BLUFI_JAVA_DIR}/org/joshvm/esp32/blufi/BlufiServer.java \
	${BLUFI_JAVA_DIR}/org/joshvm/esp32/blufi/BlufiEventListener.java \
	${BLUFI_JAVA_DIR}/org/joshvm/esp32/blufi/BlufiThread.java \
	${BLUFI_JAVA_DIR}/org/joshvm/esp32/blufi/BluetoothUUID.java 

# BLUFI Impl classes
#
MODULE_BLUFI_IMPL_JAVA_FILES += \

# WIFI classes
#
MODULE_WIFI_API_JAVA_FILES = \
	${BLUFI_JAVA_DIR}/org/joshvm/esp32/wifi/WifiManager.java \
	${BLUFI_JAVA_DIR}/org/joshvm/esp32/wifi/WifiStationConfig.java \
	${BLUFI_JAVA_DIR}/org/joshvm/esp32/wifi/WifiEventListener.java

# Determines what option we have made and assigns it to the
# variable that the global makefile recognizes.
#
JSR_JAVA_FILES_DIR += \
    $(MODULE_BLUFI_API_JAVA_FILES) \
    $(MODULE_BLUFI_IMPL_JAVA_FILES) \
	$(MODULE_WIFI_API_JAVA_FILES)

DOC_SOURCE_$(MODULE_NAME)_PATH=$(BLUFI_SOURCEPATH)
DOC_SOURCE_PATH := $(DOC_SOURCE_PATH)$(DOC_SOURCE_$(MODULE_NAME)_PATH)$(DOC_PATH_SEP)
DOC_$(MODULE_NAME)_PACKAGES += org.joshvm.esp32.blufi org.joshvm.esp32.wifi
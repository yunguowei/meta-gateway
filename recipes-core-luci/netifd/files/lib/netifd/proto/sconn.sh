#!/bin/sh

. /lib/functions.sh
. ../netifd-proto.sh
init_proto "$@"

proto_sconn_init_config() {
	no_device=0
	available=1
	proto_config_add_string "device"
	proto_config_add_string "sconnservice"
}

proto_sconn_setup() {
	local config="$1"
	local iface="$2"
	local sconn_conf_file="/etc/swconnman/swconnman.conf"

	json_get_var device device
	json_get_var sconnservice sconnservice

	[ -e "$device" ] || {
		proto_set_available "$interface" 0
		return 1
	}

	[ ! -e /etc/init.d/swconnman ] && { 
		echo "ERROR: Sierra Connection Manager in /etc/init.d/swconnman not found"
		return 1
	}

	sed -i s/Radio_interface=.*$/Radio_interface=$sconnservice/g $sconn_conf_file
	/etc/init.d/swconnman restart >/dev/null 2>&1
	sleep 10

	proto_export "INTERFACE=$config"
	proto_run_command "$config" udhcpc \
		-p /var/run/udhcpc-$iface.pid \
		-s /lib/netifd/dhcp.script \
		-f -t 0 -i "$iface"
}

proto_sconn_teardown() {
	local interface="$1"
	/etc/init.d/swconnman stop >/dev/null 2>&1
	proto_kill_command "$interface"
	#kill -9 `/var/run/udhcpc-usb0.pid`
}

add_protocol sconn

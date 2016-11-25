#!/bin/sh

mask="0"

[ -n "$INCLUDE_ONLY" ] || {
	. /lib/functions.sh
	. ../netifd-proto.sh
	init_proto "$@"
}
#DBG=-v

proto_mbim_init_config() {
	available=1
	no_device=1
	proto_config_add_string "device:device"
	proto_config_add_string apn
	proto_config_add_string pincode
	proto_config_add_string delay
	proto_config_add_string auth
	proto_config_add_string username
	proto_config_add_string password
	proto_config_add_string dhcp
}

# https://stackoverflow.com/questions/20762575
cdr2mask ()
{
   # Number of args to shift, 255..255, first non-255 byte, zeroes
   set -- $(( 5 - ($1 / 8) )) 255 255 255 255 $(( (255 << (8 - ($1 % 8))) & 255 )) 0 0 0
   [ $1 -gt 1 ] && shift $1 || shift
   echo ${1-0}.${2-0}.${3-0}.${4-0}
   mask=${1-0}.${2-0}.${3-0}.${4-0}
}

_proto_mbim_setup() {
	local interface="$1"
	local tid=2
	local ret

	local device apn pincode delay
	json_get_vars device apn pincode delay auth username password dhcp

	[ -n "$ctl_device" ] && device=$ctl_device

	[ -n "$device" ] || {
		echo "mbim[$$]" "No control device specified"
		proto_notify_error "$interface" NO_DEVICE
		proto_set_available "$interface" 0
		return 1
	}
	[ -c "$device" ] || {
		echo "mbim[$$]" "The specified control device does not exist"
		proto_notify_error "$interface" NO_DEVICE
		proto_set_available "$interface" 0
		return 1
	}

	devname="$(basename "$device")"
	devpath="$(readlink -f /sys/class/usbmisc/$devname/device/)"
	ifname="$( ls "$devpath"/net )"

	[ -n "$ifname" ] || {
		echo "mbim[$$]" "Failed to find matching interface"
		proto_notify_error "$interface" NO_IFNAME
		proto_set_available "$interface" 0
		return 1
	}

	[ -n "$apn" ] || {
		echo "mbim[$$]" "No APN specified"
		proto_notify_error "$interface" NO_APN
	}

	[ -n "$delay" ] && sleep "$delay"
	
	echo "mbim[$$]" "Query radio state"
	STATUS=$(umbim $DBG -d $device -n radio| grep off)
	sleep 1
	
	[ "$STATUS" = 0 ] || {
		echo "mbim[$$]" "Setting FCC Auth"
		uqmi $DBG -m -d $device --fcc-auth
		sleep 2
	}
	
	echo "mbim[$$]" "Reading capabilities"
	umbim $DBG -d $device -n -t $tid caps || {
		echo "mbim[$$]" "Failed to read modem caps"
		proto_notify_error "$interface" PIN_FAILED
		return 1
	}
	tid=$((tid + 1))

	echo "mbim[$$]" "Checking subscriber"
 	umbim $DBG -d $device -n -t $tid subscriber || {
		echo "mbim[$$]" "Subscriber init failed"
		proto_notify_error "$interface" NO_SUBSCRIBER
		return 1
	}
	tid=$((tid + 1))

	echo "mbim[$$]" "Register with network"
  	umbim $DBG -d $device -n -t $tid registration || {
		echo "mbim[$$]" "Subscriber registration failed"
		proto_notify_error "$interface" NO_REGISTRATION
		return 1
	}
	tid=$((tid + 1))

	echo "mbim[$$]" "Attach to network"
   	umbim $DBG -d $device -n -t $tid attach || {
		echo "mbim[$$]" "Failed to attach to network"
		proto_notify_error "$interface" ATTACH_FAILED
		return 1
	}
	tid=$((tid + 1))
 
	echo "mbim[$$]" "Connect to network"
	while ! umbim $DBG -d $device -n -t $tid connect "$apn" "$auth" "$username" "$password"; do
		tid=$((tid + 1))
		sleep 1;
	done
	tid=$((tid + 1))
	
	echo "mbim[$$]" "Get IP config"
	CONFIG=$(umbim $DBG -d $device -n -t $tid config) || {
		echo "mbim[$$]" "config failed"
		return 1
	}
	
	echo "$CONFIG" > /tmp/ip
	IP=$(cat /tmp/ip |grep ipv4address |grep -E -o "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)")
	NM=$(cat /tmp/ip |grep ipv4address |grep -o '.\{2\}$')
	GW=$(cat /tmp/ip |grep ipv4gateway |grep -E -o "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)")
	DNS=$(cat /tmp/ip |grep ipv4dnsserver |grep -E -o "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)" |head -1)
	
	cdr2mask $NM

	echo "IP: $IP $mask"
	echo "GW: $GW"
	echo "DNS: $DNS"

	uci_set_state network $interface tid "$tid"
	uci -q set network.${interface}.ifname=$ifname
	uci -q set network.${interface}.ipaddr=$IP
	uci -q set network.${interface}.netmask=$mask
	uci commit

	echo "mbim[$$]" "Connected, setting IP"
	if [ -z "$dhcp" -o "$dhcp" = 0 ]; then
		proto_init_update "$ifname" 1 "" "static"
		proto_set_keep 1
		proto_add_ipv4_address "$IP" "$mask"
		proto_add_dns_server "$DNS"
		proto_add_ipv4_route "0.0.0.0" 0 "$GW"
		proto_send_update "$interface"

		echo "nameserver $DNS" >> /tmp/resolv.conf.auto
		echo "nameserver $DNS" >> /etc/resolv.conf
	else
		json_init
		json_add_string name "${interface}_4"
		json_add_string ifname "@$interface"
		json_add_string proto "dhcp"
		ubus call network add_dynamic "$(json_dump)"
	fi
	
	return 0
}

proto_mbim_setup() {
	local ret

	_proto_mbim_setup $@
	ret=$?

	[ "$ret" = 0 ] || {
		logger "mbim bringup failed, retry in 15s"
		sleep 15
	}

	return $rt
}

proto_mbim_teardown() {
	local interface="$1"

	local device
	json_get_vars device
	local tid=$(uci_get_state network $interface tid)

	[ -n "$ctl_device" ] && device=$ctl_device

	echo "mbim[$$]" "Stopping network"
	[ -n "$tid" ] && {
		umbim $DBG -t$tid -d "$device" disconnect
		uci_revert_state network $interface tid
	}

	proto_init_update "*" 0
	proto_send_update "$interface"
}

[ -n "$INCLUDE_ONLY" ] || add_protocol mbim

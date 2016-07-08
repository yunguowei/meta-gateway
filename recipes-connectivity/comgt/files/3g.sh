#!/bin/sh
INCLUDE_ONLY=1

. ../netifd-proto.sh
. ./ppp.sh
init_proto "$@"

proto_3g_init_config() {
	no_device=1
	available=1
	ppp_generic_init_config
	proto_config_add_string "device"
	proto_config_add_string "apn"
	proto_config_add_string "service"
	proto_config_add_string "pincode"
	proto_config_add_string "dns"
}

proto_3g_setup() {
	local interface="$1"
	local chat modem_name lockfile lockpid pid

	json_get_var device device
	json_get_var apn apn
	json_get_var service service
	json_get_var pincode pincode
	json_get_var dns dns

	# Check modem
	[ -e "$device" -a "$(uci -q get network.modem_cell.present)" = "Yes" ] || {
		proto_set_available "$interface" 0
		return 1
	}

	# Check SIM card in modems which can check SIM card status.
	#modem_name="$(uci -q get network.modem_cell.Product)"
	#case "$modem_name" in
	#	HE910)
	#		[ "$(uci -q get network.sim_card.present)" = "Yes" ] || {
	#			proto_set_available "$interface" 0
	#			return 1
	#		}
	#	;;
	#esac

	case "$service" in
		cdma|evdo)
			chat="/etc/chatscripts/evdo.chat"
		;;
		*)
			chat="/etc/chatscripts/3g.chat"
			cardinfo=$(gcom -d "$device" -s /etc/gcom/getcardinfo.gcom)
			if echo "$cardinfo" | grep -q Novatel; then
				case "$service" in
					umts_only) CODE=2;;
					gprs_only) CODE=1;;
					*) CODE=0;;
				esac
				export MODE="AT\$NWRAT=${CODE},2"
			elif echo "$cardinfo" | grep -q Option; then
				case "$service" in
					umts_only) CODE=1;;
					gprs_only) CODE=0;;
					*) CODE=3;;
				esac
				export MODE="AT_OPSYS=${CODE}"
			elif echo "$cardinfo" | grep -q "Sierra Wireless"; then
				SIERRA=1
			elif echo "$cardinfo" | grep -qi huawei; then
				case "$service" in
					umts_only) CODE="14,2";;
					gprs_only) CODE="13,1";;
					*) CODE="2,2";;
				esac
				export MODE="AT^SYSCFG=${CODE},3FFFFFFF,2,4"
			fi

			if [ -n "$pincode" ]; then
				PINCODE="$pincode" gcom -d "$device" -s /etc/gcom/setpin.gcom || {
					proto_notify_error "$interface" PIN_FAILED
					proto_block_restart "$interface"
					return 1
				}
			fi
			[ -n "$MODE" ] && gcom -d "$device" -s /etc/gcom/setmode.gcom

			# wait for carrier to avoid firmware stability bugs
			[ -n "$SIERRA" ] && {
				gcom -d "$device" -s /etc/gcom/getcarrier.gcom || return 1
			}
		;;
	esac
	
	# Kill already existent 3G pppd call process and lock file.
	lockfile="/var/lock/LCK..$(basename $device)"
	lockpid="$(cat $lockfile 2>/dev/null | sed 's/\ //g')"
	[ -e "$lockfile" ] && rm -rf "$lockfile"
	for pid in $(pidof pppd 2>/dev/null) $lockpid ; do
		ps -ef | grep "pppd.*$device" | grep -q "$pid" && kill -9 "$pid"
	done

	#light up led
	gcom -d "$device" -s /etc/gcom/ledon.gcom

	chat_cmd=$(which chat)
	connect="${apn:+USE_APN=$apn }$chat_cmd -t5 -v -s -S -E -f $chat"
	ppp_generic_setup "$interface" \
		noaccomp \
		nopcomp \
		novj \
		nobsdcomp \
		noauth \
		lock \
		crtscts \
		115200 "$device"

	for d in $dns ; do
		proto_add_dns_server "$d"
	done
	proto_send_update "$interface"
	return 0
}

proto_3g_teardown() {
	json_get_var device device

	proto_kill_command "$interface"

	#wait for pppd to releasing device
	sleep 2
	gcom -d "$device" -s /etc/gcom/ledoff.gcom
}

add_protocol 3g

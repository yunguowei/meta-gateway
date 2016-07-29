#!/bin/sh
#===========================================================================
# Copyright (c) 2015-2016 Wind River Systems, Inc.
#
# MultiWAN is a package from openwrt, which provides agent script that
# makes Multi-WAN configuration simple, easy and manageable.
#
# The original openwrt version is complete with load balancing, failover
# and an easy to manage traffic ruleset. However, here we implement it as
# a stripped version, just test connectivity, select one active upstream
# interface as default, and switch to next one if the connection is lost.
#
# Created by: Junxian.xiao@windirver.com
# Date: Sep 22, 2015
#===========================================================================

. /lib/functions.sh

MW_TMPDIR="/tmp/.mwan"
MW_JOBFILE="${MW_TMPDIR}/jobqueue"
MW_LOGFILE="/var/log/multiwan.log"
MW_debug=""

mnote() {
    local level color prefix

    [ -z "$MW_debug" -o "$MW_debug" == "0" ] && return 0

    level=$1 && shift 1
    case $level in
        error) color=31 ; prefix="ERROR" ;;
         info) color=32 ; prefix=" INFO" ;;
        debug) color=00 ; prefix="DEBUG" ;;
        *) return 0 ;;
    esac

    [ "$MW_debug" == "stdout" ] && {
        echo -e "\033[${color}m$(date '+%F %X') [${prefix}] $@\033[m"
    }
    [ "$MW_debug" == "1" ] && {
        echo -e "\033[${color}m$(date '+%F %X') [${prefix}] $@\033[m" \
            >>$MW_LOGFILE
    }
}

mwan_update_resolv() {
    mnote info "Monitor: Update resolv.conf file ..."
    grep -q "127.0.0.1" /etc/resolv.conf || {
        echo "nameserver 127.0.0.1" > /etc/resolv.conf
        echo "search lan" >> /etc/resolv.conf
    }
}

mwan_update_dns() {
    local tmp_resolv_file="/tmp/resolv.conf.auto"
    local title="# MultiWAN, please add your own DNS before this."
    local dns dns_all

    dns_all="$(echo $1 | sed -e 's/,/ /g')"
    mnote info "Monitor: Update DNS: $dns_all ..."

    sed -i -e '/# MultiWAN/, $d' $tmp_resolv_file
    echo $title >> $tmp_resolv_file
    for dns in $dns_all ; do
        grep -q "$dns" $tmp_resolv_file 2>/dev/null || {
            mnote debug "Add nameserver $dns"
            echo "nameserver $dns" >> $tmp_resolv_file
        }
    done
}

mwan_update_default_route() {
    local iface="$1"
    local ifname gateway=""

    eval ifname="\$MW_${iface}_device"
    [ -z "$ifname" ] && {
        mnote error "$1: Update default route: unknown interface name"
        return 1
    }
    mnote debug "$iface: update default route to $ifname"

    ip route | grep "default" 2>&1 | head -n 1 | grep -q "\<$ifname\>" || {
        # As I don't want to save gateway for interfaces, I don't know any
        # other way to get the gateway if it's also not in the route table.
        which ifstatus >/dev/null 2>&1 && {
            gateway=$(ifstatus $iface | sed '/\"inactive\"/,$d' | \
                sed -n '/\"route\"/,/\]/p' | grep '\"nexthop\": ' | \
                grep -o '[[:digit:].]*')
            [ -n "$gateway" ] && gateway="via $gateway"
        }

        while ip route | grep -q "default" 2>/dev/null ; do
            ip route del default >/dev/null 2>&1
        done

        # Allow to add default route without gateway.
        mnote info "$iface: ip route add default $gateway dev $ifname"
        ip route add default $gateway dev $ifname >/dev/null 2>&1
    }
}

mwan_not_default_route() {
    ip route | grep "default" 2>&1 | head -n 1 | grep -q "default.*$1" && {
        return 1
    }
    return 0
}

mwan_interface_active() {
    local iface="$1"
    local ifname status

    eval ifname=\$MW_${iface}_device
    [ -z "$ifname" ] && return 1

    status="$(ip addr show $ifname 2>/dev/null)"
    echo $status | grep -q "<.*UP.*>" || return 1
    echo $status | grep -q "inet [0-9]" || return 1
    return 0
}

mwan_load_config() {
    find_interfaces() {
        local iface=$1
        local interval failover

        MW_interfaces="$MW_interfaces $iface"
        config_get interval $iface "health_interval" "3"
        config_get failover $iface "failover_to" "none"
        eval MW_${iface}_interval=$interval
        eval MW_${iface}_failover=$failover
        eval proto=$(uci -q get network.${iface}.proto)
        if [ "$proto" == "3g" ]; then
            eval MW_${iface}_device="3g-${iface}"
        else
            eval MW_${iface}_device=$(uci -q get network.${iface}.ifname)
        fi
    }

    config_load "multiwan"
    #config_get MW_dns config "dns" ""
    config_get MW_debug config "debug" "0"
    config_get MW_enabled config "enabled" '1'
    config_get MW_priority config "priority"
    config_foreach find_interfaces "interface"
}

mwan_valid_config() {
    local iface

    mnote info "## Main Initialization ##"

    [ "$MW_enabled" == "0" ] && {
        mnote info "MultiWAN is disabled!"
        return 1
    }

    [ -z "$MW_interfaces" ] && {
        mnote error "Not each MultiWAN interface defined!"
        return 1
    }

    iface="$MW_priority"
    while true ; do
        echo "$MW_interfaces" | grep -q "\<$iface\>" || {
            [ "$iface" == "$MW_priority" ] && {
                mnote error "Priority is not a valid MultiWAN interface!"
            } || {
                mnote error "Invalid failover to interface: $iface!"
            }
            return 1
        }

        MW_failover_chain="$MW_failover_chain $iface"
        eval iface="\$MW_${iface}_failover"

        [ "$iface" == "$MW_priority" -o "$iface" == "disable" ] && break
        [ "$iface" == "None" -o "$iface" == "none" ] && break
        echo $MW_failover_chain | grep -q "\<$iface\>" && {
            mnote error "failover_to loop without priority interface!"
            return 1
        }
    done

    for iface in $MW_failover_chain ; do
        [ "$(uci -q get network.$iface)" != "interface" ] && {
            mnote error "$iface is not a valid network interface!"
            return 1
        }
    done

    mnote info "Failover chain:$MW_failover_chain"
    return 0
}

mwan_monitor_init() {
    # Maybe /etc/resolv.conf will be modified by ppp or udhcpc.
    # Just add a chance here to recovery this file back.
    #mwan_update_resolv

    # Update customer DNS from multiwan configuration file
    # Let netifd to manage the DNS only, otherwise we need to
    # sync the changes between netifd and multiwan.
    # For example, ifdown wan && ifup wan after start multiwan.
    #mwan_update_dns "$MW_dns"

    return 0
}

mwan_monitor_start() {
    local iface="$1"
    local iface_status="init"
    local ifname last_time delta interval

    eval ifname="\$MW_${iface}_device"
    eval interval="\$MW_${iface}_interval"
    mnote info "$iface: Monitor starting ..."
    mnote debug "$iface: healthy interval time: $interval"

    touch $MW_TMPDIR/${iface}.monitor_running
    while [ -e "$MW_TMPDIR/${iface}.monitor_running" ] ; do
        last_time="$(date +%s)"
        if mwan_interface_active "$iface" ; then
            mnote debug "$iface: interface is running!"
            if [ "$iface_status" != "recovery" ] || \
            mwan_not_default_route "$ifname" ; then
                iface_status="recovery"
                mwan_add_task "$iface" "$iface_status"
            fi
        else
            mnote debug "$iface: interface is not running!"
            if [ "$iface_status" != "failover" ] ; then
                iface_status="failover"
                mwan_add_task "$iface" "$iface_status"
            fi
        fi

        # delay for next time, time precision: 1s
        delta=$(($last_time + $interval - $(date +%s)))
        mnote debug "$iface: delta time: $delta"
        [ "$delta" -gt 0 ] && sleep $delta
    done
}

mwan_monitor_stop() {
    local iface="$1"

    mnote info "$iface: Monitor stopping ..."
    rm -rf $MW_TMPDIR/${iface}.monitor_running
}

mwan_monitor_search() {
    local iface="$1"
    local direct="$2"
    local included="$3"
    local start_show i

    if [ "$direct" == "before" ] ; then
        for i in $MW_failover_chain ; do
            if [ "$i" != "$iface" ] ; then
                echo "$i "
            else
                [ "$included" == "1" ] && echo "$i "
                break;
            fi
        done
    else
        start_show=0
        for i in $MW_failover_chain ; do
            if [ "$i" != "$iface" ] ; then
                [ "$start_show" -eq 1 ] && echo "$i "
            else
                [ "$included" == "1" ] && echo "$i "
                start_show=1
            fi
        done
    fi
}

mwan_monitor_recovery() {
    local iface="$1"
    local i others

    # if outside [priority, current]
    mwan_monitor_search "$MW_current" "after" 0 | grep -q "\<$iface\>" && {
        return 1
    }

    # if just current interface
    [ "$MW_current" == "$iface" ] && {
        mnote info "Monitor: Maintain $MW_current"
        mwan_update_default_route "$iface"
        return 0
    }

    # if between [priority, current)
    MW_current="$iface"
    mwan_update_default_route "$iface"
    others=$(mwan_monitor_search "$iface" "after" 0)
    mnote debug "Monitor: stop other monitors: $others"
    for i in $others ; do
        mwan_monitor_stop "$i"
    done
    mnote info "Monitor: Switched to $iface"
}

mwan_monitor_failover() {
    local iface="$1"

    [ "$MW_current" == "$iface" ] && {
        mnote info "Monitor: Switch from $iface"
        eval iface="\$MW_${iface}_failover"
        mnote debug "Monitor: Failover to $iface"
        [ "$iface" == "$MW_priority" -o "$iface" == "disable" ] && return 0
        [ "$iface" == "None" -o "$iface" == "none" ] && return 0
        MW_current="$iface"
        mwan_monitor_start "$iface" &
    }
}

# Add a task to the $jobfile while ensuring
# no duplicate tasks for the specified group
mwan_add_task() {
    local group=$1
    local task=$2
    grep -o "$group.$task" $MW_JOBFILE >&- 2>&- || {
        echo "$group.$task" >> $MW_JOBFILE 2>&-
    }
}

# Process each task in the $jobfile in FIFO order
mwan_do_tasks() {
    local line queued_task

    if [ -f $MW_JOBFILE ] ; then
        mv $MW_JOBFILE ${MW_JOBFILE}.work
        while read line; do
            execute_task() {
                mnote info "Monitor: Task $1 $2"
                case $2 in
                    "failover") mwan_monitor_failover $1;;
                    "recovery") mwan_monitor_recovery $1;;
                    *) mnote error "## Unknown task command: $2 ##";;
                esac
            }
            queued_task=$(echo $line | awk -F "." '{print $1,$2}')
            execute_task $queued_task
        done < ${MW_JOBFILE}.work
        rm -rf ${MW_JOBFILE}.work
    fi
}

mwan_monitor_loop() {
    MW_current=$MW_priority
    MW_monitor_daemon_running=1
    mwan_monitor_start $MW_current &

    while [ "$MW_monitor_daemon_running" == 1 ] ; do
        mwan_do_tasks
        sleep 1
    done
}

mwan_monitor_exit() {
    local iface

    for iface in $MW_failover_chain ; do
        mwan_monitor_stop $iface
    done
    MW_monitor_daemon_running=0

    rm -rf $MW_TMPDIR
    rm -rf /var/run/multiwan.pid
}

start() {
    rm -rf $MW_TMPDIR
    mkdir -p $MW_TMPDIR >/dev/null 2>&1
    echo "$$" >/var/run/multiwan.pid

    mwan_load_config
    mwan_valid_config || return 1

    mwan_monitor_init || return 1
    mwan_monitor_loop
}

stop() {
    local keyword="multiwan start"
    local pid=`cat /var/run/multiwan.pid`
    local pids

    # stop all background monitor processes and wait
    rm -rf $MW_TMPDIR/*.monitor_running

    # stop the MultiWAN main process
    [ -n "$pid" ] && kill -9 $pid >/dev/null 2>&1

    pids=`ps -ef 2>&1 | grep "$keyword" | grep -v "grep" | awk '{print $1}'`
    [ -n "$pids" ] && kill $pids >/dev/null 2>&1

    # clear temporary things
    rm -rf $MW_TMPDIR
    rm -rf /var/run/multiwan.pid
}


#==============================================================================
# main function
#==============================================================================
trap "mwan_monitor_exit" SIGINT

case $1 in
    start) start && exit 0 || exit 1;;
    stop) stop && exit 0 || exit 1;;
    *) exit 1;;
esac


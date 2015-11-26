#!/bin/sh

# Copright (C) 2015 Wind River Systems, All rights reserved.
#
# This file contains an example runonce script.  As a customer, you may use this to 
# perform system initialization once at first boot.
#
# If the file /etc/runonce/customer-runonce.sh exists, that script will be executed.
# If your code returns zero (success), the service will be disabled.  If it returns
# any non-zero value, the service will run again at next boot.  
# 
# In the event that this script doesn't exist on the first boot but somehow appears
# later (e.g., you install one), it will be run at the first boot that it's detected,
# even though it's not necessarily the first boot.
#
# Here are some examples of things that you might like to put into your customer
# runonce script.
#
# This command sets the hostname to something like WR-IDP-08002b4b6a27:
#
#     uci set system.@system[0].hostname=WR-IDP-`cat /sys/class/net/eth0/address \
#            | sed -e "s/://g"`
#
#This example shows how to allow SSH from the WAN:
#
#     uci add firewall rule
#     uci set firewall.@rule[-1].src=wan
#     uci set firewall.@rule[-1].target=ACCEPT
#     uci set firewall.@rule[-1].proto=tcp
#     uci set firewall.@rule[-1].dest_port=22
#     uci commit firewall
#     /etc/init.d/firewall restart
#
# These commands set the target wireless SSID:
#
#     uci set wireless.@wifi-device[0].disabled=0
#     uci set wireless.@wifi-iface[0].ssid=customerNet
#     uci commit wireless
#
#

if [ -x /etc/runonce/customer-runonce.sh ]
then
    /etc/runonce/customer-runonce.sh
    retval=$?
else
    echo "No executable runonce script (/etc/runonce/customer-runonce.sh)"
    exit 0
fi


if [[ $retval == 0 ]]
then
	echo "Runonce was successful, disabling the service"
	systemctl disable runonce
else
	echo "Runonce has failed."
fi



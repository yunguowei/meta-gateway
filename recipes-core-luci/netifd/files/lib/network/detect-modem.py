#!/usr/bin/python
# -*- coding: utf-8 -*-
#
# Created by: Junxian.xiao@windirver.com
# Date: Aug 31, 2015
# Copyright (c) 2015 Wind River System, Inc.

import os
import sys
import time

debug_mode = False
def log(msg):
    if debug_mode:
        print("%s" % msg)

def uci_set(package, section, option, value):
    os.system('uci -q set %s.%s.%s="%s"' % (package, section, option, value))


def uci_commit(package):
    os.system('uci commit %s' % package)


def comgt_get(device, script):
    cmd = 'gcom -d %s -s /etc/gcom/%s 2>/dev/null' % (device, script)
    ret = os.popen(cmd).read().strip()
    log('comgt result:\n%s' % ret)
    return ret


def device_expand(device):
    if device is None:
        return None

    device = device.strip()
    if device.find('/dev/') == 0:
        return device
    elif len(device) != 0:
        return '/dev/' + device
    else:
        return None


def has_usb_modem(usball, vendorID, prodID):
        for line in usball:
            if line.find("ID %s:%s" %(vendorID, prodID)) != -1:
                return True
        return False


def scan_default_3g_modem():
    default_file = '/etc/modem_cell_default'
    list_name = ['Manufacturer', 'Product', 'Vendor', 'ProdID',
                 'protoall', 'pppddev', 'statedev', 'present']

    if os.path.isfile(default_file):
        usbs = os.popen('lsusb 2>/dev/null').readlines()
        for line in open(default_file).readlines():
            if line.lstrip()[0] == '#':
                continue
            
            list_value = line.strip().split('|')
            if has_usb_modem(usbs, list_value[2], list_value[3]):
                list_value.append('Yes')
                return dict(zip(list_name, list_value))

    list_value = [None, None, None, None, None, None, None, 'No']
    return dict(zip(list_name, list_value))


def scan_default_sim_card(imsi):
    default_file = '/etc/sim_card_operator_default'
    if os.path.isfile(default_file) and imsi is not None:
        MNC_2 = imsi[0:5]
        MNC_3 = imsi[0:6]
        for line in open(default_file).readlines():
            line = line.strip()
            if len(line) == 0 or line[0] == '#':
                continue

            info = line.strip().split(':')
            if info[2] == MNC_2 or info[2] == MNC_3:
                return {'country': info[0], 'apn': info[3], 'service': info[6]}
    return {}


def detect_3g_modem():
    uci = {'modem': {}, 'sim': {}, 'wwan': {}}
    uci['modem'] =  scan_default_3g_modem()
    uci['modem']['pppddev'] = device_expand(uci['modem']['pppddev'])
    uci['modem']['statedev'] = device_expand(uci['modem']['statedev'])
    uci['modem']['Rev'] = None
    uci['modem']['SerialNumber'] = None

    device = uci['modem'].get('statedev', None)

    if uci['modem']['present'] != 'Yes':
        uci['wwan']['proto'] = 'none'
    elif device is not None:
        for line in comgt_get(device, 'modem3g.gcom').split('\n'):
            config, option, value = line.split('|')
            uci[config.strip()][option.strip()] = value.strip()

    imsi = uci['sim'].get('IMSI', None)
    if imsi is not None and len(imsi) != 0:
        uci['sim']['present'] = 'Yes'
        uci['wwan'] = scan_default_sim_card(imsi)
    else:
        uci['sim'] = {'IMSI': None, 'operator': None, 'present': 'No'}

    uci['wwan']['device'] = uci['modem']['pppddev']

    for option in uci['modem'].keys():
        value = uci['modem'][option] if uci['modem'][option] is not None else ''
        uci_set('network', 'modem_cell', option, value)
    for option in uci['sim'].keys():
        value = uci['sim'][option] if uci['sim'][option] is not None else ''
        uci_set('network', 'sim_card', option, value)
    for option in uci['wwan'].keys():
        if uci['wwan'][option] is not None:
            uci_set('network', 'wwan', option, uci['wwan'][option])

    log('modem: %s' % uci['modem'])
    log('sim: %s' % uci['sim'])
    log('wwan: %s' % uci['wwan'])
    uci_commit('network')


if __name__ == '__main__':
    argc = len(sys.argv)
    if argc == 3 and sys.argv[2] == '-v':
        debug_mode = True

    if argc == 1 or sys.argv[1] == 'all':
        detect_3g_modem()
    elif sys.argv[1] == '3g':
        detect_3g_modem()

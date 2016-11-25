#!/usr/bin/python
# -*- coding: utf-8 -*-
#
# Created by: Junxian.xiao@windirver.com
# Date: Aug 31, 2015
# Copyright (c) 2015-2016 Wind River System, Inc.

import os
import re
import sys
import xml.etree.ElementTree as ET

debug_mode = False
def log(msg):
    if debug_mode:
        print("%s" % msg)

def uci_set(package, section, option, value):
    os.system('uci -q set %s.%s.%s="%s"' % (package, section, option, value))


def uci_get(package, section, option):
    cmd = 'uci -q get %s.%s.%s' % (package, section, option)
    return os.popen(cmd).readline().strip()


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


def query_service_provider(imsi, pdp_apn, current_apn):
    wwan = {}
    xmlfile = '/usr/share/mobile-broadband-provider-info/serviceproviders.xml'
    if not os.path.isfile(xmlfile):
        log('No serviceproviders xml file for wwan default apn')
        return wwan

    try:
        root = ET.parse(xmlfile).getroot()
    except ET.ParseError:
        log('Fail to parse serviceproviders xml file for wwan default apn')
        return wwan

    # Structure: country -> provider -> network{gsm, cdma} -> {netowrk-id, apn}
    # Only search all gsm type network which include right network_id
    # And the apn usage type must be 'internet'.
    for country in root:
        country_code = country.get('code')
        for provider in country:
            for network in provider.iter('gsm'):
                network_found = False
                apns=[]
                for netid in network.iter('network-id'):
                    mcc_mnc = '%s%s' % (netid.get('mcc'), netid.get('mnc'))
                    if re.match(mcc_mnc, imsi):
                        network_found = True
                if not network_found:
                    continue
                for apn in network.iter('apn'):
                    if apn.find('usage').get('type') == 'internet':
                        apns.append(apn.get('value'))
                log('All valid APNs: %s' % apns)
                if current_apn in apns:
                    apn_value = current_apn
                elif pdp_apn in apns:
                    apn_value = pdp_apn
                elif len(apns) > 0:
                    apn_value = apns[0]
                else:
                    apn_value = ''
                return {'country': country_code, 'apn': apn_value}
    return wwan


def detect_3g_modem():
    # Maybe the cdc_acm kernel driver is include as module
    os.system('modprobe cdc_acm')

    uci = {'modem': {}, 'sim': {}, 'wwan': {}}
    uci['modem'] = scan_default_3g_modem()
    uci['modem']['pppddev'] = device_expand(uci['modem']['pppddev'])
    uci['modem']['statedev'] = device_expand(uci['modem']['statedev'])
    uci['modem']['Rev'] = None
    uci['modem']['SerialNumber'] = None

    device = uci['modem'].get('statedev', None)
    count = 1

    if uci['modem']['present'] != 'Yes':
        uci['wwan']['proto'] = 'none'
    elif device is not None and os.path.exists(device):
        lines = comgt_get(device, 'modem3g.gcom').split('\n')
        while len(lines) < 4 and count < 5:
            lines = comgt_get(device, 'modem3g.gcom').split('\n')
            count += 1
        for line in lines:
            if len(line.strip()):
                config, option, value = line.split('|')
                uci[config.strip()][option.strip()] = value.strip()
        try:
            sim_pdp = comgt_get(device, 'getpdp.gcom')
            if re.match('\+CGDCONT:', sim_pdp):
                pdp_apn = sim_pdp.split(',')[2].replace('"', '')
                log('PDP APN: %s' % pdp_apn)
        except Exception, e:
            log('Fail to get APN from SIM card PDP')
            pdp_apn = ''

    imsi = uci['sim'].get('IMSI', None)
    if imsi is not None and len(imsi) != 0:
        uci['sim']['present'] = 'Yes'
        current_apn = uci_get('network', 'wwan', 'apn')
        log('Current APN: %s' % current_apn)
        uci['wwan'] = query_service_provider(imsi, pdp_apn, current_apn)
	uci['wwan']['proto'] = uci['modem']['protoall']
    else:
        uci['sim'] = {'IMSI': None, 'operator': None, 'present': 'No'}

    uci['wwan']['device'] = uci['modem']['pppddev']

    proto = uci['modem'].get('protoall', None)
    if proto is not None and not cmp(proto, 'mbim'):
        uci['wwan']['proto'] = uci['modem']['protoall']

    for option in uci['modem'].keys():
        value = uci['modem'][option] if uci['modem'][option] is not None else ''
        uci_set('network', 'modem_cell', option, value)
    for option in uci['sim'].keys():
        value = uci['sim'][option] if uci['sim'][option] is not None else ''
        uci_set('network', 'sim_card', option, value)
    for option in uci['wwan'].keys():
        value = uci['wwan'][option] if uci['wwan'][option] is not None else ''
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

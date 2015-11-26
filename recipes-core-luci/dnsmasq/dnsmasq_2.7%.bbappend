#
# Copyright (C) 2015 Wind River Systems, Inc.
#
FILESEXTRAPATHS_prepend := "${THISDIR}/files:"
SRC_URI += "\
            file://0001-dnsmasq-fixes-bug-that-causes-unsynchronized-resolve.patch \
            file://dnsmasq.init \
            file://dhcp.config \
            file://dnsmasq.service \
            "

INITSCRIPT_NAME = "dnsmasq"
SYSTEMD_SERVICE_${PN} = "dnsmasq.service"

do_install () {
        oe_runmake "PREFIX=${D}${prefix}" \
                   "BINDIR=${D}${bindir}" \
                   "MANDIR=${D}${mandir}" \
                   install
        install -d ${D}${sysconfdir}/ ${D}${sysconfdir}/init.d ${D}${sysconfdir}/dnsmasq.d
        install -m 644 ${WORKDIR}/dnsmasq.conf ${D}${sysconfdir}/
        install -m 755 ${WORKDIR}/init ${D}${sysconfdir}/init.d/dnsmasq
        install -d -m 0755 ${D}/${sysconfdir}/config
        install -d -m 0755 ${D}/${sysconfdir}/init.d
        install -m 0755 ${WORKDIR}/dhcp.config ${D}/${sysconfdir}/config/dhcp
        install -m 0755 ${WORKDIR}/dnsmasq.init ${D}/${sysconfdir}/init.d/dnsmasq
        mkdir -p ${D}${base_sbindir}
        ln -s ${bindir}/dnsmasq ${D}${base_sbindir}/dnsmasq
        mkdir -p ${D}${sbindir}
        install -m 0755 ${WORKDIR}/dnsmasq.init ${D}/${sbindir}/dnsmasq-systemd-wrapper

        install -d ${D}${systemd_unitdir}/system
        install -m 0644 ${WORKDIR}/dnsmasq.service ${D}${systemd_unitdir}/system

        if [ "${@base_contains('PACKAGECONFIG', 'dbus', 'dbus', '', d)}" != "" ]; then
            install -d ${D}${sysconfdir}/dbus-1/system.d
            install -m 644 dbus/dnsmasq.conf ${D}${sysconfdir}/dbus-1/system.d/
        fi
}

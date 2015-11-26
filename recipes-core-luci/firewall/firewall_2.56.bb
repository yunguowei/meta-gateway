DESCRIPTION = "UCI based firewall"
LICENSE = "GPLv2"

#SRC_URI = "svn://svn.openwrt.org/openwrt/trunk/package/network/config;module=firewall;protocol=svn"
SRC_URI = "file://bin/fw \
           file://firewall.config \
           file://firewall.hotplug \
           file://firewall.init \
           file://firewall.user \
           file://reflection.hotplug \
           file://lib/config.sh \
           file://lib/core.sh \
           file://lib/core_forwarding.sh \
           file://lib/core_init.sh \
           file://lib/core_interface.sh \
           file://lib/core_redirect.sh \
           file://lib/core_rule.sh \
           file://lib/fw.sh \
           file://lib/uci_firewall.sh \
           file://firewall.service \
           file://etc/config/firewall \
           "

LIC_FILES_CHKSUM = "file://${COREBASE}/LICENSE;md5=4d92cd373abda3937c2bc47fbc49d690"

RDEPENDS_${PN} = "iptables"

S="${WORKDIR}"

do_install() {
    install -m 0755 -d ${D}/${base_sbindir}
    install -m 0755 -d ${D}/${base_libdir}/firewall
    install -m 0755 -d ${D}/lib/firewall
    install -m 0755 -d ${D}/${sysconfdir}/init.d
    install -m 0755 -d ${D}/${sysconfdir}/hotplug.d/firewall
    install -m 0755 -d ${D}/${sysconfdir}/hotplug.d/iface
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/firewall.service ${D}${systemd_unitdir}/system
    install -m 0755 ${S}/lib/* ${D}/${base_libdir}/firewall
    install -m 0755 ${S}/lib/* ${D}/lib/firewall
    install -m 0755 ${S}/bin/fw ${D}/${base_sbindir}
    install -m 0755 ${S}/firewall.init ${D}/${sysconfdir}/init.d/firewall
    install -m 0644 ${S}/firewall.hotplug ${D}/${sysconfdir}/hotplug.d/iface/20-firewall
    install -m 0644 ${S}/reflection.hotplug ${D}/${sysconfdir}/hotplug.d/firewall/10-nat-reflection
    install -m 0644 ${S}/firewall.user ${D}/${sysconfdir}

    install -m 0755 -d ${D}/${sysconfdir}/config
    install -m 0644 ${WORKDIR}/etc/config/firewall ${D}/${sysconfdir}/config
}

inherit update-rc.d systemd
INITSCRIPT_NAME = "firewall"
INITSCRIPT_PARAMS = "start 45 3 5 . stop 88 0 1 6 ."

SYSTEMD_SERVICE_${PN} = "firewall.service"

FILES_${PN} += "${base_libdir}/firewall/*"
FILES_${PN}_append_x86-64 += "/lib/firewall/*"

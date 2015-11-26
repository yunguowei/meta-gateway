DESCRIPTION = "	MultiWan is a package from openwrt, which provides agent script that \
	makes Multi-WAN configuration simple, easy and manageable. Complete with load \
	balancing, failover and an easy to manage traffic ruleset. \
	\
	However, script here is a stripped version, just test connectivity, select one \
	as default, and swith to next one if fail.\
"
HOMEPAGE = "https://openwrt.org/"
SECTION = "console/utils"
PRIORITY = "optional"
LICENSE = "GPLv2"
PV = "1.0.22+svnr${SRCPV}"
PR = "r1"

# Need commands: ubus, iptalbes, ip, route
RDEPENDS_${PN} = "netifd uci iptables busybox"

SRC_URI  = "svn://svn.openwrt.org/openwrt/packages/net;module=multiwan;protocol=svn \
	file://usr/bin/multiwan \
	file://etc/init.d/multiwan \
	file://etc/config/multiwan \
	file://multiwan.service \
"

SRCREV = "33471"
S = "${WORKDIR}/multiwan"

SRC_URI[md5sum] = "329bf7073b8e20ad37ca729814deacf5"
SRC_URI[sha256sum] = "38c312a663707d1818b3094d5837a06553a35fe30ca1b875d9a1597742e6a879"

LIC_FILES_CHKSUM = "\
	file://Makefile;beginline=1;endline=6;md5=0871892b193c3b1814c7db5849cb06f7"

FILES_${PN} = "\
	${bindir}/multiwan \
	${systemd_unitdir}/system/* \
	${sysconfdir}/config/multiwan \
"
inherit update-rc.d systemd

INITSCRIPT_NAME = "multiwan"
INITSCRIPT_PARAMS = "start 99 3 5 . stop 01 0 1 6 ."

SYSTEMD_SERVICE_${PN} = "multiwan.service"
SYSTEMD_AUTO_ENABLE = "disable"

do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_install() {
	install -m 755 -d ${D}/${bindir}

	# Don't install Multi WAN original files, use the strip version
	install -m 755 ${WORKDIR}/usr/bin/multiwan ${D}/${bindir}/

	install -d ${D}${systemd_unitdir}/system
	install -m 0644 ${WORKDIR}/multiwan.service ${D}${systemd_unitdir}/system

	install -d ${D}${sysconfdir}/config
	install -m 0644 ${WORKDIR}/etc/config/multiwan ${D}${sysconfdir}/config
}


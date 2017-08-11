LICENSE = "GPLv2"
SECTION = "console/utils"
DEPENDS = "libubox ubus uci iptables"

PR = "r0"

SRC_URI = "git://git.openwrt.org/project/firewall3.git \
	   file://firewall.config \
	   file://firewall.hotplug \
	   file://firewall.user \
	   file://firewall.service \
	   file://README \
	   file://firewall-kmod.conf \
"

SRCREV = "a4d98aea373e04f3fdc3c492c1688ba52ce490a9"
LIC_FILES_CHKSUM = "file://${COREBASE}/LICENSE;md5=4d92cd373abda3937c2bc47fbc49d690"

S = "${WORKDIR}/git"

inherit cmake systemd

SYSTEMD_SERVICE_${PN} = "firewall.service"

EXTRA_OECMAKE = "${@bb.utils.contains('DISTRO_FEATURES', 'ipv6', '-DDISABLE_IPV6=OFF', '-DDISABLE_IPV6=ON', d)}"
EXTRA_OECMAKE += "-DCMAKE_SKIP_RPATH:BOOL=YES -DBUILD_LUA=OFF -DLIBNL_LIBS=-lnl-tiny -DDEBUG=1"
OECMAKE_C_FLAGS += "-I ${STAGING_INCDIR}/libubus -I ${STAGING_INCDIR} -I ${S}"

do_install() {
	install -m 0755 -d ${D}/${base_sbindir}
	install -m 0755 ${WORKDIR}/build/firewall3 ${D}/${base_sbindir}/fw3

	install -m 0755 -d ${D}/${sysconfdir}/hotplug.d/iface
	install -m 0644 ${WORKDIR}/firewall.hotplug ${D}/${sysconfdir}/hotplug.d/iface/20-firewall

	install -m 0755 -d ${D}/${sysconfdir}/config
	install -m 0644 ${WORKDIR}/firewall.config ${D}/${sysconfdir}/config/firewall

	install -m 0644 ${WORKDIR}/firewall.user ${D}/${sysconfdir}

	install -d ${D}${systemd_unitdir}/system
	install -m 0644 ${WORKDIR}/firewall.service ${D}${systemd_unitdir}/system

	install -d ${D}${sysconfdir}/modules-load.d
	install -m 0644 ${WORKDIR}/firewall-kmod.conf  ${D}${sysconfdir}/modules-load.d/
}

FILES_${PN} += "\ ${base_libdir}/firewall/* \
                  ${sysconfdir}/config/firewall \"
CONFFILES_${PN} += "\ ${sysconfdir}/config/firewall \"


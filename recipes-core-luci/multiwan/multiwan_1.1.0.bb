DESCRIPTION = "	MultiWan is a package name from openwrt, which provides agent script \
    that makes Multi-WAN configuration simple, easy and manageable. Complete with load \
    balancing, failover and an easy to manage traffic ruleset. \
    \
    However, script here is a stripped version, just test connectivity, select one \
    as default, and swith to next one if fail.\
"
SECTION = "console/utils"
PRIORITY = "optional"
LICENSE = "windriver"
LICENSE_FLAGS = "commercial_windriver"
LIC_FILES_CHKSUM = "file://${WR_EXTRA_LIC_DIR}/windriver;md5=eb3421117285c0b7ccbe9fbc5f1f37d7"

PR = "r0"

# Need commands: uci ip
RDEPENDS_${PN} = "uci iproute2 netifd"

SRC_URI  = "\
    file://usr/bin/multiwan.sh \
    file://etc/config/multiwan \
    file://multiwan.service \
"

S = "${WORKDIR}/multiwan"


FILES_${PN} = "\
    ${bindir}/multiwan \
    ${systemd_unitdir}/system/* \
    ${sysconfdir}/config/multiwan \
"
CONFFILES_${PN} += "\ ${sysconfdir}/config/multiwan \"

inherit systemd

SYSTEMD_SERVICE_${PN} = "multiwan.service"
SYSTEMD_AUTO_ENABLE = "disable"

do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_install() {
    install -m 755 -d ${D}/${bindir}
    install -m 755 ${WORKDIR}/usr/bin/multiwan.sh ${D}/${bindir}/multiwan

    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/multiwan.service ${D}${systemd_unitdir}/system

    install -d ${D}${sysconfdir}/config
    install -m 0644 ${WORKDIR}/etc/config/multiwan ${D}${sysconfdir}/config
}

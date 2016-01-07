SUMMARY = "Option/Vodafone 3G/GPRS control tool"
DESCRIPTION = "Comgt is a command line tool for controlling, \
configuring and interacting with Option Wireless 3G and 2G ( HSDPA, \
UMTS, EDGE, GPRS, GSM) data devices within the Linux environment."
LICENSE = "GPLv2"
SECTION = "console/utils"
PR = "r0"

SRC_URI = "${SOURCEFORGE_MIRROR}/${BPN}/${BPN}.${PV}.tgz \
            file://3g.chat \
            file://3g.chat.comgt \
            file://3g.sh \
            file://3g.usb \
            file://evdo.chat \
            file://getcardinfo.gcom \
            file://getcarrier.gcom \
            file://getcnum.gcom \
            file://getsn.gcom \
            file://getrev.gcom \
            file://getimsi.gcom \
            file://getreg.gcom \
            file://modem3g.gcom \
            file://getstrength.gcom \
            file://setmode.gcom \
            file://setpin.gcom \
            file://001-compile_fix.patch \
            "

LIC_FILES_CHKSUM = "file://gpl.txt;md5=393a5ca445f6965873eca0259a17f833"

S="${WORKDIR}/${BPN}.${PV}"

do_install() {
    install -d ${D}/${bindir}
    install -d ${D}/${sysconfdir}/chatscripts
    install -d ${D}/${sysconfdir}/gcom
    install -d ${D}/${sysconfdir}/hotplug.d/tty

    install -m 0755 ${WORKDIR}/*.chat ${D}/${sysconfdir}/chatscripts
    install -m 0755 ${WORKDIR}/3g.chat.comgt ${D}/${sysconfdir}/chatscripts
    install -m 0755 ${WORKDIR}/*.gcom ${D}/${sysconfdir}/gcom
    install -m 0755 ${WORKDIR}/3g.usb ${D}/${sysconfdir}/hotplug.d/tty/30-3g

    install -d ${D}/${base_libdir}/netifd/proto
    install -m 0755 ${WORKDIR}/3g.sh ${D}/${base_libdir}/netifd/proto

    install -d ${D}/lib/netifd/proto
    install -m 0755 ${WORKDIR}/3g.sh ${D}/lib/netifd/proto

    install -m 0755 ${S}/comgt ${D}/${bindir}
    cd ${D}/${bindir};ln -sf comgt gcom
}

FILES_${PN} += "${base_libdir}/netifd/* /lib/netifd"

SRC_URI[md5sum] = "db2452680c3d953631299e331daf49ef"
SRC_URI[sha256sum] = "0cedb2a5aa608510da66a99aab74df3db363df495032e57e791a2ff55f1d7913"

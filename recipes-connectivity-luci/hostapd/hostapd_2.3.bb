#
# Copyright (C) 2015 Wind River Systems, Inc.
#
SUMMARY = "hostapd is a user space daemon for access point and authentication servers."
DESCRIPTION = "\
hostapd is designed to be a "daemon" program \
that runs in the background and acts as the backend component \
controlling authentication. It implements IEEE 802.11 access \
point management, IEEE 802.1X/WPA/WPA2/EAP Authenticators, \
RADIUS client, EAP server, and RADIUS authentication server. \
The current version supports Linux (Host AP, madwifi, Prism54, \
mac80211-based drivers) and FreeBSD (net80211). hostapd supports \
separate frontend programs and an example text-based frontend, \
hostapd_cli, is included with hostapd. \
"
HOMEPAGE = "http://hostap.epitest.fi/hostapd/"

DEPENDS = "libnl openssl"

LICENSE = "GPLv2.0"

LIC_FILES_CHKSUM = "file://COPYING;md5=ab87f20cd7e8c0d0a6539b34d3791d0e"

SECTION = "idp"
PR = "r1"
FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

SRC_URI = "\
	http://hostap.epitest.fi/releases/${BPN}-${PV}.tar.gz \
	file://config \
	file://hostapd.sh \
	file://netifd.sh \
	"

SRC_URI[md5sum] = "40b89c61036add0c2dd1fc10767d3b5f"
SRC_URI[sha256sum] = "c94c2b76876fad4c80a1063a06f958a2189ba5003475016fa7658a1ca49bb4df"

do_configure() {
	cp ${WORKDIR}/config ${S}/hostapd/.config
}

EXTRA_OEMAKE = "-C ${S}/hostapd"
# Enable netlink 3 support
EXTRA_OEMAKE += 'EXTRACFLAGS="-I${STAGING_INCDIR}/libnl3"'

do_compile() {
	make ${EXTRA_OEMAKE} CC="${CC}" V=1
}

do_install() {
	install -m 0755 -d ${D}/usr/sbin
	cp ${S}/hostapd/hostapd ${D}/usr/sbin
	cp ${S}/hostapd/hostapd_cli ${D}/usr/sbin
	install -m 0755 -d ${D}/etc
	install -m 0600 ${S}/hostapd/hostapd.conf ${D}/etc
}

do_install_append () {
    install -d -m 0755 ${D}/${base_libdir}/wifi
    install -d -m 0755 ${D}/lib/netifd
    install -m 0755 ${WORKDIR}/hostapd.sh ${D}/${base_libdir}/wifi
    install -m 0755 ${WORKDIR}/netifd.sh ${D}/lib/netifd/hostapd.sh
}

FILES_${PN} += "${base_libdir}/wifi/hostapd.sh"
FILES_${PN} += "/lib/netifd/hostapd.sh"

# Copyright (C) 2015 Khem Raj <raj.khem@gmail.com>
# Released under the MIT license (see COPYING.MIT for the terms)

DESCRIPTION = "OpenWrt uqmi utility"
HOMEPAGE = "http://git.openwrt.org/?p=project/uqmi.git;a=summary"
LICENSE = "GPL-2.0"
LIC_FILES_CHKSUM = "file://main.c;beginline=1;endline=20;md5=3f7041e5710007661d762bb6043a69c6"
SECTION = "base"
DEPENDS = "libubox json-c"

SRCREV = "8ceeab690d8c6f1e3afbd4bcaee7bc2ba3fbe165"
SRC_URI = "git://git.openwrt.org/project/uqmi.git \
	   file://qmi.sh \
          "

inherit cmake pkgconfig

S = "${WORKDIR}/git"
B = "${S}"

FILES_SOLIBSDEV = ""

do_install_append() {
	install -d ${D}/lib/netifd/proto/
	install -m 0755 ${WORKDIR}/qmi.sh ${D}/lib/netifd/proto/
}

FILES_${PN}  += "${libdir}/* /lib/netifd"

# Copyright (C) 2015 Khem Raj <raj.khem@gmail.com>
# Released under the MIT license (see COPYING.MIT for the terms)

DESCRIPTION = "OpenWrt MBIM modem utility"
HOMEPAGE = "http://git.openwrt.org/?p=project/umbim.git;a=summary"
LICENSE = "GPL-2.0"
LIC_FILES_CHKSUM = "file://mbim.h;beginline=1;endline=13;md5=8c7ce85ebfe23634010c75c30c3eb223"
SECTION = "base"
DEPENDS = "libubox"

SRCREV = "af9c293c1f1d8a97fbd8adf9c6070ead4920ca84"
SRC_URI = "git://git.openwrt.org/project/umbim.git \
	   file://mbim.sh \
          "

inherit cmake pkgconfig

S = "${WORKDIR}/git"

do_install_append(){
	install -d ${D}/lib/netifd/proto/
	install -m 0755 ${WORKDIR}/mbim.sh ${D}/lib/netifd/proto/
}

FILES_SOLIBSDEV = ""

FILES_${PN}  += "${libdir}/* /lib/netifd"

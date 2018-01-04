# Copyright (C) 2015 Khem Raj <raj.khem@gmail.com>
# Released under the MIT license (see COPYING.MIT for the terms)

DESCRIPTION = "OpenWrt MBIM modem utility"
HOMEPAGE = "http://git.openwrt.org/?p=project/umbim.git;a=summary"
LICENSE = "GPL-2.0"
LIC_FILES_CHKSUM = "file://mbim.h;beginline=1;endline=13;md5=8c7ce85ebfe23634010c75c30c3eb223"
SECTION = "base"
DEPENDS = "libubox"

SRCREV = "29aaf43b097ee57f7aa1bb24341db6cc4148cbf3"
SRC_URI = "git://git.openwrt.org/project/umbim.git \
	   file://mbim.sh \
          "

inherit cmake pkgconfig

S = "${WORKDIR}/git"

do_install_append(){
	if [ -n "${@bb.utils.contains('DISTRO_FEATURES', 'usrmerge', 'y', '', d)}" ]; then
            install -d ${D}/${nonarch_base_libdir}/netifd/proto/
	    install -m 0755 ${WORKDIR}/mbim.sh ${D}/${nonarch_base_libdir}/netifd/proto/ 
        else
            install -d ${D}/lib/netifd/proto/
            install -m 0755 ${WORKDIR}/mbim.sh ${D}/lib/netifd/proto/
	fi
}

FILES_SOLIBSDEV = ""

FILES_${PN}  += "${libdir}/* /lib/netifd"

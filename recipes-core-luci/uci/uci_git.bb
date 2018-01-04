SUMMARY = "UCI is the successor of the nvram based configuration \
found in the White Russian series of OpenWrt."
DESCRIPTION = "\
The abbreviation UCI stands for Unified Configuration \
Interface and is intended to centralize the whole \
configuration of your project. Configuring should be easy, \
more straight forward."
HOMEPAGE = "http://wiki.openwrt.org/doc/uci"
SECTION = "console/utils"

DEPENDS = "lua libubox"
LICENSE = "GPLv2 & LGPLv2.1+"
LIC_FILES_CHKSUM = "file://cli.c;beginline=3;endline=12;md5=61ac10aebfcddf1cf7371220687237d3 \
                    file://delta.c;beginline=3;endline=12;md5=c7e4171d506df594d5b9d080e356308d \
                    file://sh/uci.sh;beginline=2;endline=3;md5=e46ee36d30bc35775d45e04b783dd11f \
                    file://ucimap.c;beginline=3;endline=12;md5=125da5ab3833a1b42e517efefa61d75a"

SRC_URI += "git://nbd.name/uci.git;protocol=git \
	    file://0001-uci-fix-the-issue-of-wrong-intalling-path.patch \
            file://uci.sh"

S = "${WORKDIR}/git"

SRCREV = "a536e300370cc3ad7b14b052b9ee943e6149ba4d"


PR = "r2"
inherit cmake

PARALLEL_MAKE = ""

EXTRA_OECMAKE="-DCMAKE_SKIP_RPATH:BOOL=YES \
               -DBUILD_LUA=ON -DLUAPATH=${libdir}/lua/5.1/"

B = "${S}"
do_install_append() {
    mkdir -p ${D}/usr/sbin
    ln -s /usr/bin/uci ${D}/usr/sbin/uci

    if [ -n "${@bb.utils.contains('DISTRO_FEATURES', 'usrmerge', 'y', '', d)}" ]; then
        install -Dm 0755 ${WORKDIR}/uci.sh  ${D}/${nonarch_libdir}/config/uci.sh
    else
	mkdir -p ${D}/sbin
        ln -s /usr/bin/uci ${D}/sbin/uci
	install -Dm 0755 ${WORKDIR}/uci.sh ${D}/lib/config/uci.sh
    fi
}

PACKAGES += "${PN}-lua"

FILES_${PN} += "${base_sbindir}/uci \
	${libdir}/libuci.so \
	${sysconfdir}/ucisa/* \
	${sysconfdir}/uci-defaults \
	/lib/config/*  \
        ${nonarch_libdir}/config/uci.sh"

FILES_${PN}-lua = "${libdir}/lua/5.1/uci.so"
FILES_${PN}-dev="${includedir}/*"
FILES_${PN}-dbg += "${libdir}/lua/5.1/.debug/*"

FILES_${PN}-staticdev="${libdir}/*.a"

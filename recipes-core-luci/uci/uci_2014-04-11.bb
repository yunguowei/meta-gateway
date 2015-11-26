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
            file://0001-fix-bug-that-leaves-a-null-pointer-gap.patch \
            file://uci.sh"

S = "${WORKDIR}/git"

SRCREV = "e339407372ffc70b1451e4eda218c01aa95a6a7f"


PR = "r2"
inherit cmake 

PARALLEL_MAKE = ""

EXTRA_OECMAKE="-DCMAKE_SKIP_RPATH:BOOL=YES \
               -DBUILD_LUA=ON -DLUAPATH=${libdir}/lua/5.1/"
B = "${S}"
do_install() {
    install -d -m 0755 ${D}/${base_sbindir}
    install -d -m 0755 ${D}/${base_libdir}
    install -d -m 0755 ${D}/${libdir}
    install -d -m 0755 ${D}/${libdir}/lua/5.1
    install -d -m 0755 ${D}/${includedir}
    install -d -m 0755 ${D}/lib/config
    install -d -m 0755 ${D}/${sysconfdir}/uci-defaults

    install -m 0755 ${B}/uci ${D}/${base_sbindir}
    install -m 0755 ${B}/libuci.so ${D}/${base_libdir}
    install -m 0755 ${B}/libucimap.a ${D}/${libdir}
    install -m 0755 ${B}/lua/uci.so ${D}/${libdir}/lua/5.1

    install -m 0644 ${S}/uci*.h ${D}/${includedir}

    install -m 0755 ${WORKDIR}/uci.sh ${D}/lib/config
}

PACKAGES += "${PN}-lua"

FILES_${PN}="${base_sbindir}/uci \
	${base_libdir}/libuci.so \
	${sysconfdir}/ucisa/* \
	${sysconfdir}/uci-defaults \
	/lib/config/*"
FILES_${PN}-lua = "${libdir}/lua/5.1/uci.so"
FILES_${PN}-dev="${includedir}/*"
FILES_${PN}-dbg += "${libdir}/lua/5.1/.debug/*"

FILES_${PN}-staticdev="${libdir}/*.a"

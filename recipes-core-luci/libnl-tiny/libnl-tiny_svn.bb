SUMMARY = "netlink socket library"
DESCRIPTION = "This package contains a stripped down version of libnl"
LICENSE = "GPLv2 & LGPLv2.1"
SECTION = "console/network"
PR = "r0"

SRC_URI = "svn://svn.openwrt.org/openwrt/trunk/package/libs;module=libnl-tiny;protocol=svn \
           file://0001-set-correct-pkgconfig-path-in-libnl-tiny.pc.patch"
SRCREV = "33861"
LIC_FILES_CHKSUM = "file://src/genl.c;beginline=4;endline=9;md5=816ecbc3ccf845571fe4677727066d3f"

S="${WORKDIR}/${BPN}"

CFLAGS += "-fPIC"

do_compile() {
    cd ${S}/src
    oe_runmake all
}

do_install() {
    install -d ${D}/${base_libdir}
    install -d ${D}/${libdir}/pkgconfig
    install -d ${D}/${includedir}/libnl-tiny
    install -m 0755 ${S}/src/libnl-tiny.so ${D}/${base_libdir}
    install -m 0755 ${S}/files/libnl-tiny.pc ${D}/${libdir}/pkgconfig
    cp -r ${S}/src/include/* ${D}/${includedir}/libnl-tiny
}

FILES_${PN} = "${base_libdir}/libnl-tiny.so"
FILES_${PN}-dev = "${includedir}/libnl-tiny/* ${libdir}/pkgconfig/*"

SRC_URI[md5sum] = "517956fbd7756fddba221fd807055d6d"
SRC_URI[sha256sum] = "20d14ab5ad69db7e5461efc7ec0e999cb30b50ef712ed55189968b02b5e776d3"

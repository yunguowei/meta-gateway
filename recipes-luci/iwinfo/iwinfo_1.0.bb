SUMMARY = "Wireless information library"
DESCRIPTION = "Command line frontend for the wireless information library."
LICENSE = "GPLv2"
SECTION = "webconsole"
DEPENDS = "libubox lua libnl-tiny"
PR = "r0"

SRC_URI = "file://iwinfo"

S = "${WORKDIR}/iwinfo"

LIC_FILES_CHKSUM = "file://COPYING;md5=751419260aa954499f7abaabaa882bbe"

PACKAGES = "${PN} ${PN}-lua ${PN}-dev ${PN}-dbg"

FILES_${PN} += " \
    ${bindir}/iwinfo \
    ${libdir}/libiwinfo.so \
"
FILES_${PN}-lua = "${libdir}/lua/5.1/iwinfo.so"
FILES_${PN}-dbg += " ${libdir}/lua/5.1/.debug/*"

CFLAGS += " -D_GNU_SOURCE -fPIC"
CFLAGS += " -I${STAGING_DIR_TARGET}/usr/include/"
CFLAGS += " -I${STAGING_DIR_TARGET}/usr/include/libnl-tiny/"
EXTRA_OEMAKE += " BACKENDS=nl80211"

do_install() {
    install -d ${D}/${bindir}/
    install -d ${D}/${libdir}/lua/5.1/

    install -m 0755 ${S}/iwinfo ${D}/${bindir}
    install -m 0755 ${S}/libiwinfo.so ${D}/${libdir}
    install -m 0755 ${S}/iwinfo.so ${D}/${libdir}/lua/5.1/

    install -d ${D}/${includedir}/iwinfo
    install -m 0644 ${S}/include/iwinfo.h ${D}/${includedir}
    install -m 0644 ${S}/include/iwinfo/* ${D}/${includedir}/iwinfo
}


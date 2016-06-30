SUMMARY = "Wireless information library"
DESCRIPTION = "Command line frontend for the wireless information library."
LICENSE = "GPLv2"
SECTION = "webconsole"
DEPENDS = "libubox lua libnl uci"
RDEPENDS_${PN} = "libnl lua uci"
RDEPENDS_${PN}-lua = "lua uci iwinfo libnl"
PR = "r1"

SRC_URI = "git://git.openwrt.org/project/iwinfo.git \
	   file://0001-fix-iwinfo-undefined-reference-error.patch \
	   file://0002-Fixed-the-link-issue-of-the-library-iwinfo-and-libiw.patch \
	   file://0003-iwinfo-try-to-fix-the-compile-issue.patch \
	   file://0004-Extend-the-array-size-of-key-mgmt.patch \
	   file://0005-do-not-wait-for-scan-results.patch \
	   file://0006-filter-no-IR-and-radar-detection-channel.patch \
	   file://0007-fixed-scan-issue-when-connect-to-an-non-exist-AP.patch"

S = "${WORKDIR}/git"

SRCREV = "e4aca3910dff532ed878d0ceaf1ab6e8ad7719bf"

LIC_FILES_CHKSUM = "file://COPYING;md5=751419260aa954499f7abaabaa882bbe"

CFLAGS += " -D_GNU_SOURCE -fPIC"
CFLAGS += " -I${STAGING_DIR_TARGET}/usr/include/"
CFLAGS += " -I${STAGING_DIR_TARGET}/usr/include/libnl3/"
EXTRA_OEMAKE += " BACKENDS=nl80211"

PACKAGES = "${PN} ${PN}-lua ${PN}-dev ${PN}-dbg"
FILES_${PN} += " \
    ${bindir}/iwinfo \
    ${libdir}/libiwinfo.so \
"
FILES_${PN}-lua = "${libdir}/lua/5.1/iwinfo.so"
FILES_${PN}-dbg += " ${libdir}/lua/5.1/.debug/*"

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


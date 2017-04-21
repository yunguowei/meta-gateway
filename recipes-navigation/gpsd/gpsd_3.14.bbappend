FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

SRC_URI += "file://device-hook \
	    file://0001-libgpsd-core-Fix-issue-with-ACTIVATE-hook-not-being-.patch \
	  "

do_install_append() {
    install -d ${D}/${sysconfdir}/gpsd
    install -m 0755 ${WORKDIR}/device-hook ${D}/${sysconfdir}/gpsd/
}


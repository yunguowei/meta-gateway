FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

SRC_URI += "file://device-hook \
	  "

do_install_append() {
    install -d ${D}/${sysconfdir}/gpsd
    install -m 0755 ${WORKDIR}/device-hook ${D}/${sysconfdir}/gpsd/
}


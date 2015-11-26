FILESEXTRAPATHS_prepend := "${THISDIR}/busybox:"

SRC_URI += "\
	file://lock-util.patch \
	file://idp.cfg"

# remove udhcpc script because we use the netifd one
do_install_append() {
	rm -rf ${D}${datadir}/udhcpc
}

# Turn off systemd busybox service for syslog
SYSTEMD_PACKAGES = ""
SYSTEMD_SERVICE_${PN}-syslog = ""

FILES_${PN} += "/usr/share/"

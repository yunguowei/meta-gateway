
FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

SRC_URI += "file://hostapd.sh \
            file://netifd.sh"


do_install_append () {
	install -d -m 0755 ${D}/${base_libdir}/wifi
	install -d -m 0755 ${D}/lib/netifd
	install -m 0755 ${WORKDIR}/hostapd.sh ${D}/${base_libdir}/wifi
	install -m 0755 ${WORKDIR}/netifd.sh ${D}/lib/netifd/hostapd.sh
}

FILES_${PN} += "${base_libdir}/wifi/hostapd.sh"
FILES_${PN} += "/lib/netifd/hostapd.sh"


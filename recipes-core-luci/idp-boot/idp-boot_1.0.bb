DESCRIPTION = "This package provide IDP specific services scripts."
LICENSE = "windriver"
LICENSE_FLAGS = "commercial_windriver"
LIC_FILES_CHKSUM = "file://${WORKDIR}/copyright;md5=9e3c8ee3041b22f72c93b00e06a188f4"

PR = "r0"

SRC_URI = " \
	file://copyright \
	file://boot \
	file://boot.service \
	file://runonce.service \
	file://runonce.sh \
"

do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_install() {
	install -m 0755 -d ${D}${sysconfdir}/init.d
	install -m 0755 ${WORKDIR}/boot ${D}${sysconfdir}/init.d

	install -m 0755 -d ${D}${bindir}
	install -m 0755 ${WORKDIR}/boot ${D}${bindir}/boot-systemd-wrapper
	install -d ${D}/etc/runonce
	install -m 0755 ${WORKDIR}/runonce.sh ${D}/etc/runonce/runonce.sh
	install -d ${D}${systemd_unitdir}/system
	install -m 0644 ${WORKDIR}/boot.service ${D}${systemd_unitdir}/system
	install -m 0644 ${WORKDIR}/runonce.service ${D}${systemd_unitdir}/system
}

inherit update-rc.d systemd

INITSCRIPT_NAME = "boot"
INITSCRIPT_PARAMS = "start 40 S ."

SYSTEMD_SERVICE_${PN} = "boot.service runonce.service"

FILES_${PN} += "${sysconfdir}/init.d/*"

DESCRIPTION = "This package provide Wind River gateway bootup services."
LICENSE = "windriver"
LICENSE_FLAGS = "commercial_windriver"
LIC_FILES_CHKSUM = "file://${WR_EXTRA_LIC_DIR}/windriver;md5=eb3421117285c0b7ccbe9fbc5f1f37d7"

PR = "r0"
RDEPENDS_${PN} = "python"
SRC_URI = " \
	file://boot-stage1 \
	file://boot-stage2 \
	file://boot-stage3 \
	file://boot-stage4 \
	file://boot-stage1.service \
	file://boot-stage2.service \
	file://boot-stage3.service \
	file://boot-stage4.service \
	file://runonce.service \
	file://runonce.sh \
	file://utils \
"

do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_install() {
	install -m 0755 -d ${D}${bindir}
	install -m 0755 ${WORKDIR}/boot-stage1 ${D}${bindir}
	install -m 0755 ${WORKDIR}/boot-stage2 ${D}${bindir}
	install -m 0755 ${WORKDIR}/boot-stage3 ${D}${bindir}
	install -m 0755 ${WORKDIR}/boot-stage4 ${D}${bindir}
	install -d ${D}/etc/runonce
	install -m 0755 ${WORKDIR}/runonce.sh ${D}/etc/runonce/runonce.sh
	install -d ${D}${systemd_unitdir}/system
	install -m 0644 ${WORKDIR}/boot-stage1.service ${D}${systemd_unitdir}/system
	install -m 0644 ${WORKDIR}/boot-stage2.service ${D}${systemd_unitdir}/system
	install -m 0644 ${WORKDIR}/boot-stage3.service ${D}${systemd_unitdir}/system
	install -m 0644 ${WORKDIR}/boot-stage4.service ${D}${systemd_unitdir}/system
	install -m 0644 ${WORKDIR}/runonce.service ${D}${systemd_unitdir}/system
	install -d ${D}/lib/network
	install -m 0755 ${WORKDIR}/utils/detect-modem.py ${D}/lib/network/detect-modem.py

        if [ -n "${@bb.utils.contains('DISTRO_FEATURES', 'usrmerge', 'y', '', d)}" ]; then
            cp -a ${D}/lib/*  ${D}/${nonarch_libdir}/
	    rm -rf ${D}/lib
	fi
}

inherit systemd

SYSTEMD_SERVICE_${PN} = "\
	boot-stage1.service \
	boot-stage2.service \
	boot-stage3.service \
	boot-stage4.service \
	runonce.service \
"
FILES_${PN} += "/lib/network/* ${nonarch_libdir}"

FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

SRC_URI += "file://always-show-the-package-info-for-smart-update.patch \
            file://rpm_repo \
            file://rpm_repo.sh"

do_install_append () {
    install -d -m 0755 ${D}/${sysconfdir}/config
    install -d -m 0755 ${D}/${sbindir}
    install -m 0755 ${WORKDIR}/rpm_repo ${D}/${sysconfdir}/config/
    install -m 0755 ${WORKDIR}/rpm_repo.sh ${D}/${sbindir}
}

FILES_${PN} +="${sysconfdir}/config/*"
FILES_${PN} +="${sbindir}/*"

PACKAGECONFIG_append += "proxy"

PROXY_RDEP = "python-pycurl"
PROXY_RDEP_class-native = ""
PROXY_RDEP_class-nativesdk = ""

PACKAGECONFIG[proxy] = ",,${PROXY_RDEP},${PROXY_RDEP}"

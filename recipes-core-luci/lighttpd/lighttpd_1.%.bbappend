FILESEXTRAPATHS_prepend := "${THISDIR}/lighttpd:"

DEPENDS += "openssl"

SRC_URI += "file://lighttpd.pem"

EXTRA_OECONF += " --with-openssl \
               "

RDEPENDS_${PN} += " \
               lighttpd-module-redirect \
	       "

do_install_append(){
    install -d ${D}${sysconfdir}/lighttpd/certs/
    install -m 0400 ${WORKDIR}/lighttpd.pem ${D}${sysconfdir}/lighttpd/certs
}

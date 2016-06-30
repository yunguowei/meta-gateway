FILESEXTRAPATHS_prepend := "${THISDIR}/lighttpd:"

DEPENDS += "openssl"

RDEPENDS_${PN} += " \
               lighttpd-module-cgi \
               lighttpd-module-redirect \
               bash \
               util-linux-getopt \
	       "
SRC_URI += "\
        file://lighttpd.service \
        file://lighttpd-ssl-cert \
        file://0001-Remove-the-source-file-path-in-log-messages.patch \
           "

inherit systemd

CONFIG_FILE = "lighttpd.conf"

EXTRA_OECONF = " \
             --without-bzip2 \
             --without-ldap \
             --without-memcache \
             --with-pcre \
             --without-webdav-props \
             --without-webdav-locks \
             --disable-static \
             --with-openssl \
             "

do_install_append () {
        install -d -m 0755 ${D}/${sbindir}
	install -m 0755 ${WORKDIR}/lighttpd-ssl-cert  ${D}/${sbindir}

	install -d ${D}${systemd_unitdir}/system
        install -m 0644 ${WORKDIR}/lighttpd.service ${D}${systemd_unitdir}/system
}

SYSTEMD_SERVICE_${PN} = "lighttpd.service"

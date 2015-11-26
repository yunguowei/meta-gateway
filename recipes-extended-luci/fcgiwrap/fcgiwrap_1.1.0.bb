SUMMARRY = "Simple FastCGI wrapper for CGI scripts (CGI support for nginx)"
HOMEPAGE = "http://wiki.nginx.org/Fcgiwrap"

LICENSE = "BSD"
LIC_FILES_CHKSUM = "file://fcgiwrap.c;beginline=1;endline=24;md5=019e6e884fdc648ecb877ba29ec2ac93"

SRC_URI = "https://github.com/gnosek/${BPN}/archive/${PV}.tar.gz;downloadfilename=${BPN}-${PV}.tar.gz \
	file://fcgiwrap.service \
	file://fcgiwrap.socket \
	file://systemd-spawn-fcgi"

SRC_URI[md5sum] = "d14f56bda6758a6e02aa7b3fb125cbce"
SRC_URI[sha256sum] = "4c7de0db2634c38297d5fcef61ab4a3e21856dd7247d49c33d9b19542bd1c61f"

DEPENDS = "fcgi"

inherit autotools pkgconfig systemd

B = "${S}"

EXTRA_OECONF = "--prefix=/"

SYSTEMD_SERVICE_${PN} = "fcgiwrap.socket"

do_install_append() {
	install -d ${D}${systemd_unitdir}/system
	install -m 0644 ${WORKDIR}/fcgiwrap.service ${D}${systemd_unitdir}/system
	install -m 0644 ${WORKDIR}/fcgiwrap.socket ${D}${systemd_unitdir}/system
	install -d ${D}${sbindir}
	install -m 0755 ${WORKDIR}/systemd-spawn-fcgi ${D}${sbindir}
}

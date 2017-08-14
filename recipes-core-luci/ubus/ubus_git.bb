SUMMARY = "OpenWrt's micro bus architecture"
DESCRIPTION = "OpenWrt's micro bus architecture, similar to DBus; \
it contains a RPC DAEMON 'ubusd' and a command line utility tool \
'ubus', the 'ubus' command line client allows to interact with the \
'ubusd' rpc server."
LICENSE = "LGPLv2.1"
SECTION = "console/utils"

DEPENDS = "libubox lua json-c"

PR = "r0"
SRC_URI = "git://git.openwrt.org/project/ubus.git \
	   file://0001-add-libubus-version-number-for-qa-issue.patch \
	   file://0001-ubus-fix-the-installing-path-issue.patch \
           file://ubus.service \
	  "

SRCREV = "763b9b2cf293fb60b5c2ddf34e2500f95200b6b5"

S = "${WORKDIR}/git"
LIC_FILES_CHKSUM = "file://cli.c;beginline=1;endline=12;md5=8bfdfc5dd171023c4eb2bf8c954bda77"

inherit cmake systemd

SYSTEMD_SERVICE_${PN} = "ubus.service"

EXTRA_OECMAKE="-DCMAKE_SKIP_RPATH:BOOL=YES -DBUILD_LUA=ON  -DLUAPATH=${libdir}/lua/5.1/"

PACKAGES += "${PN}-lua"
FILES_${PN}-lua = "${libdir}/lua/5.1/ubus.so"
FILES_${PN}-dbg += "${libdir}/lua/5.1/.debug/*"

do_install_append() {
	install -dm 0755 ${D}/sbin
	ln -s /usr/sbin/ubusd ${D}/sbin/ubusd

	install -d ${D}${systemd_unitdir}/system
	install -m 0644 ${WORKDIR}/ubus.service ${D}${systemd_unitdir}/system

}

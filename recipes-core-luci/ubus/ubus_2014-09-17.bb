SUMMARY = "OpenWrt's micro bus architecture"
DESCRIPTION = "OpenWrt's micro bus architecture, similar to DBus; \
it contains a RPC DAEMON 'ubusd' and a command line utility tool \
'ubus', the 'ubus' command line client allows to interact with the \
'ubusd' rpc server."
LICENSE = "LGPLv2.1"
SECTION = "console/utils"

DEPENDS = "libubox lua"

PR = "r0"
SRC_URI = "git://git.openwrt.org/project/ubus.git \
           file://ubus.service \
           file://0001-add-libubus-version-number-for-qa-issue.patch"

SRCREV = "fcf5d8af65f41d6a106ad08d1df5de9729f5399a"

S = "${WORKDIR}/git"
LIC_FILES_CHKSUM = "file://cli.c;beginline=1;endline=12;md5=8bfdfc5dd171023c4eb2bf8c954bda77"

inherit cmake systemd

SYSTEMD_SERVICE_${PN} = "ubus.service"

EXTRA_OECMAKE="-DCMAKE_SKIP_RPATH:BOOL=YES -DBUILD_LUA=ON  -DLUAPATH=${libdir}/lua/5.1/"

PACKAGES += "${PN}-lua"
FILES_${PN}-lua = "${libdir}/lua/5.1/ubus.so"
FILES_${PN}-dbg += "${libdir}/lua/5.1/.debug/*"

do_install() {
	install -d ${D}/${base_bindir}
	install -d ${D}/${base_sbindir}
	install -d ${D}/${base_libdir}
	install -d ${D}/${libdir}/lua/5.1/
	install -d ${D}/${includedir}/libubus
	install -m 0755 ${B}/libubus.so.0.0.0 ${D}/${base_libdir}
	install -m 0755 ${B}/ubus ${D}/${base_bindir}
	install -m 0755 ${B}/ubusd ${D}/${base_sbindir}

	install -d ${D}/${libdir}
	rel_lib_prefix=`echo ${libdir} | sed 's,\(^/\|\)[^/][^/]*,..,g'`
	ln -sf libubus.so.0.0.0 ${D}/${base_libdir}/libubus.so.0
	ln -sf ${rel_lib_prefix}${base_libdir}/libubus.so.0 ${D}${libdir}/libubus.so

	install -m 0755 ${S}/*.h ${D}/${includedir}/libubus/
	install -m 0755 ${B}/lua/ubus.so  ${D}/${libdir}/lua/5.1/

	install -d ${D}${systemd_unitdir}/system
	install -m 0644 ${WORKDIR}/ubus.service ${D}${systemd_unitdir}/system

}

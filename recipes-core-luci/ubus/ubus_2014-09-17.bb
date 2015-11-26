SUMMARY = "OpenWrt's micro bus achitecture"
DESCRIPTION = "OpenWrt's micro bus achitecture, similar to DBus; \
it contains a RPC DAEMON 'ubusd' and a command line utility tool \
'ubus', the 'ubus' command line client allows to interact with the \
'ubusd' rpc server."
LICENSE = "LGPLv2.1"
SECTION = "console/utils"

DEPENDS = "libubox lua"

ALLOW_EMPTY_${PN} = "1"
PR = "r0"
SRC_URI = "git://nbd.name/luci2/ubus.git \
           file://ubus.init \
           file://ubus.service \
           file://0001-add-libubus-version-number-for-qa-issue.patch"

SRCREV = "4c4f35cf2230d70b9ddd87638ca911e8a563f2f3"


S = "${WORKDIR}/git"
LIC_FILES_CHKSUM = "file://cli.c;beginline=1;endline=12;md5=8bfdfc5dd171023c4eb2bf8c954bda77"

inherit cmake systemd

SYSTEMD_SERVICE_${PN} = "ubus.service"

EXTRA_OECMAKE="-DCMAKE_SKIP_RPATH:BOOL=YES -DBUILD_LUA=ON -DLUAPATH=${libdir}/lua/5.1/"
OECMAKE_C_FLAGS += "-I ${S}"

PACKAGES += "${PN}-lua"
FILES_${PN}-lua = "${libdir}/lua/5.1/ubus.so"
FILES_${PN}-dbg += "${libdir}/lua/5.1/.debug/*"

do_install() {
	install -d ${D}/${base_bindir}
	install -d ${D}/${base_sbindir}
	install -d ${D}/${base_libdir}
	install -d ${D}/${libdir}/lua/5.1/
	install -d ${D}/${includedir}/libubus
	install -d ${D}/${sysconfdir}/init.d
	install -m 0755 ${B}/libubus.so.0.0.0 ${D}/${base_libdir}
	install -m 0755 ${B}/ubus ${D}/${base_bindir}
	install -m 0755 ${B}/ubusd ${D}/${base_sbindir}

	install -d ${D}/${libdir}
	rel_lib_prefix=`echo ${libdir} | sed 's,\(^/\|\)[^/][^/]*,..,g'`
	ln -sf libubus.so.0.0.0 ${D}/${base_libdir}/libubus.so.0
	ln -sf ${rel_lib_prefix}${base_libdir}/libubus.so.0 ${D}${libdir}/libubus.so

	install -m 0755 ${S}/*.h ${D}/${includedir}/libubus/
	install -m 0755 ${B}/lua/ubus.so  ${D}/${libdir}/lua/5.1/
	install -m 0755 ${WORKDIR}/ubus.init ${D}/${sysconfdir}/init.d/ubus

	install -d ${D}${systemd_unitdir}/system
	install -m 0644 ${WORKDIR}/ubus.service ${D}${systemd_unitdir}/system

}

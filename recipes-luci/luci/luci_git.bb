SUMMARY = "LUCI Web Interface"
DESCRIPTION = "Standard Web Interface including full admin features."
LICENSE = "GPLv2"
SECTION = "webconsole"

SRC_URI = "git://github.com/openwrt/luci.git \
	file://0001-luci-add-the-CMake-files.patch \
	file://version.lua \
	file://etc/config/system \
      "

SRCREV = "afe2d2c18743b0f85efb030fb279c4e5ac169197"

S = "${WORKDIR}/git/"

DEPENDS = "lua-native iwinfo uci lua libubox openssl"

inherit pkgconfig cmake

OECMAKE_C_FLAGS += "-I${STAGING_INCDIR}/libnl3"


LIC_FILES_CHKSUM = "file://LICENSE;md5=2b42edef8fa55315f34f2370b4715ca9"

RDEPENDS_${PN} = "uci-lua ubus-lua iwinfo iwinfo-lua"

#prefix=""
includedir="/usr/include"

do_configure_append() {
	cp ${WORKDIR}/version.lua ${S}/modules/luci-base/luasrc/version.lua
}

do_install_append() {
	cp -rf ${WORKDIR}/etc ${D}
	rm -rf ${D}/lib/upgrade
}

CONFFILES_${PN} += "${sysconfdir}/config/system"

OECMAKE_C_FLAGS += "-DLUA_COMPAT_5_1"
EXTRA_OECMAKE += "-DLUAPATH=${libdir}/lua/5.1 -DCMAKE_INSTALL_PREFIX= "

FILES_${PN}  += "${libdir}/* ${datadir}/lua/5.*/ /www /usr/share /lib"
FILES_${PN}-dbg  += "${libdir}/lua/5.*/.debug \
		${libdir}/lua/5.1/luci/template/.debug \
"

DEPENDS += "lua-native"
OECMAKE_C_FLAGS += "-I${STAGING_INCDIR}/lua5.1"
CFLAGS += "-I${STAGING_INCDIR}/lua5.1"


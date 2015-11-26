SUMMARY = "LUCI Web Interface"
DESCRIPTION = "Standard Web Interface including full admin features."
LICENSE = "GPLv2"
SECTION = "webconsole"
PR = "r17"

SRC_URI = "http://download.windriver.org/proprietary-downloads/luci-${PV}-${PR}.tar.gz \
	file://etc/config/system \
	file://no-auto-channel-for-wifi.patch"

SRC_URI[md5sum] = "ab9fd0189331b689b65fe700cd3d49c8"
SRC_URI[sha256sum] = "e0c38fbb026efefb781667172ed0f5fc3d14fe67c3255a0c41bb8d9b9882b55c"

DEPENDS = "lua-native iwinfo uci lua libubox"

LIC_FILES_CHKSUM = "file://LICENSE;md5=2b42edef8fa55315f34f2370b4715ca9"

RDEPENDS_${PN} = "uci-lua ubus-lua iwinfo iwinfo-lua"
FILES_${PN} += " \
	/www/* \
	/lib/* \
	${sysconfdir}/config/* \
	${libdir}/lua/5.1/bit.lua \
	${libdir}/lua/5.1/luci/ \
	${libdir}/lua/5.1/luci/*.lua \
	${libdir}/lua/5.1/luci/model \
	${libdir}/lua/5.1/luci/sgi \
	${libdir}/lua/5.1/luci/sys \
	${libdir}/lua/5.1/luci/tools \
	${libdir}/lua/5.1/luci/cbi \
	${libdir}/lua/5.1/luci/controller \
	${libdir}/lua/5.1/luci/http \
	${libdir}/lua/5.1/luci/i18n \
	${libdir}/lua/5.1/luci/view \
	${libdir}/lua/5.1/luci/template/parser.so \
	${libdir}/lua/5.1/nixio/ \
	${libdir}/lua/5.1/nixio.so \
	${libdir}/lua/5.1/px5g/ \
	${libdir}/lua/5.1/px5g.so \
	${libdir}/lua/5.1/neightbl.so \
"
FILES_${PN}-dbg += "\
	${libdir}/lua/5.1/.debug/ \
	${libdir}/lua/5.1/luci/template/.debug \
"

PARALLEL_MAKE = ""

LUCI_APPS = "luci-commands luci-firewall luci-hd-idle luci-mmc-over-gpio \
	luci-multiwan luci-ntpc"
LUCI_MODULES = "modules/admin-full modules/base libs/* themes/bootstrap i18n/english i18n/chinese \
	protocols/3g protocols/ppp"

# set LUCI_SUBDIRS = "applications/luci-ahcp ... modules/admin-full ..."
python() {
    d.setVar("LUCI_SUBDIRS", " ".join("applications/" + i for i in d.getVar("LUCI_APPS").split()) + " " + d.getVar("LUCI_MODULES"))
}

EXTRA_OEMAKE = "\
	MODULES='${LUCI_SUBDIRS}' \
	OS=linux \
	CC='${CC}' CFLAGS='${CFLAGS}' LDFLAGS='${LDFLAGS}' \
	LUA_MODULEDIR='${libdir}/lua/5.1' \
	LUA_LIBRARYDIR='${libdir}/lua/5.1' \
"

do_install() {
	for i in ${LUCI_SUBDIRS}; do
		cp -rf $i/dist/* ${D}
	done

	cp -rf ${WORKDIR}/etc ${D}
	rm -rf ${D}/lib/upgrade
}


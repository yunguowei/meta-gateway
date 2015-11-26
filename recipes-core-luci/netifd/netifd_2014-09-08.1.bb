SUMMARY = "OpenWrt Network Interface Configuration Daemon"
DESCRIPTION = "netifd is an RPC-capable daemon written in C for \
better access to kernel APIs with the ability to listen on netlink events."
LICENSE = "GPLv2"
SECTION = "console/utils"
DEPENDS = "uci libnl-tiny json-c libubox ubus"
RDEPENDS_${PN} += "hotplug2 libnl-tiny"
RPROVIDES_${PN} += "ifupdown"

PR = "r2"

SRC_URI = "git://git.openwrt.org/project/netifd.git;protocol=git \
            file://sbin/devstatus \
            file://sbin/ifdown \
            file://sbin/ifstatus \
            file://sbin/ifup \
            file://usr/share/udhcpc/default.script \
            file://001-musl_af_inet_include.patch \
            file://etc \
            file://lib \
            file://netifd.service \
            file://netifd-systemd-wrapper \
           "
SRCREV = "46c569989f984226916fec28dd8ef152a664043e"
LIC_FILES_CHKSUM = "file://config.c;beginline=1;endline=13;md5=572cd47ba0e377b26331e67e9f3bc4b3"

S = "${WORKDIR}/git"

inherit cmake systemd

SYSTEMD_SERVICE_${PN} = "netifd.service"

EXTRA_OECMAKE="-DBUILD_LUA=OFF -DLIBNL_LIBS=-lnl-tiny -DDEBUG=1"
OECMAKE_C_FLAGS += "-I ${STAGING_INCDIR}/libnl-tiny -I ${STAGING_INCDIR}/libubus -I ${STAGING_INCDIR}"

do_install() {
    local devicename="${MACHINE}"
    install -d ${D}/${sysconfdir}/hotplug.d
    install -d ${D}/${sysconfdir}/init.d
    install -d ${D}/${base_sbindir}
    install -d ${D}/${base_libdir}/netifd
    install -d ${D}/lib/netifd
    install -m 0755 ${B}/netifd ${D}/${base_sbindir}
    install -m 0755 ${S}/scripts/* ${D}/lib/netifd/
    echo "${devicename}" > ${D}/etc/device_name
    echo "${devicename}" > ${D}/etc/board_name
    for i in etc lib sbin usr; do
        cp -a ${WORKDIR}/$i ${D}/
    done
    cd ${D}/sbin; ln -sf ifup ifdown

    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/netifd.service ${D}${systemd_unitdir}/system
    install -m 0755 ${WORKDIR}/netifd-systemd-wrapper ${D}/${base_sbindir}
}

FILES_${PN} = "${sysconfdir}/* ${base_libdir}/* ${base_sbindir}/* /usr/* /etc/*"
FILES_${PN}_append_x86-64 += "/lib/*"

SUMMARY = "OpenWrt Network Interface Configuration Daemon"
DESCRIPTION = "netifd is an RPC-capable daemon written in C for \
better access to kernel APIs with the ability to listen on netlink events."
LICENSE = "GPLv2"
SECTION = "console/utils"
DEPENDS = "uci libnl json-c libubox ubus"
RDEPENDS_${PN} += "libnl dhcp-client comgt ppp hostapd libqmi"

inherit update-alternatives

ALTERNATIVE_PRIORITY = "150"
ALTERNATIVE_${PN} = "ifup ifdown"

ALTERNATIVE_LINK_NAME[ifup] = "${base_sbindir}/ifup"
ALTERNATIVE_LINK_NAME[ifdown] = "${base_sbindir}/ifdown"

PR = "r2"

SRC_URI = "git://git.openwrt.org/project/netifd.git;protocol=git \
            file://sbin/devstatus \
            file://sbin/ifdown \
            file://sbin/ifstatus \
            file://sbin/ifup \
            file://0001-Make-netifd-to-clean-resolv.conf.auto-when-link-down.patch \
            file://0002-system-linux-Fix-IFF_LOWER_UP-define.patch \
            file://0003-replace_is_error_helper_with_null_check.patch \
            file://etc/hotplug.d/iface/00-netstate \
            file://etc/modem_cell_default \
            file://etc/config/network \
            file://lib/wifi/mac80211.sh \
            file://lib/netifd/wireless/mac80211.sh \
            file://lib/netifd/proto/dhcp.sh \
            file://lib/netifd/dhclient-script \
            file://lib/netifd/hostapd.sh \
            file://lib/network/config.sh \
            file://netifd.service \
            file://netifd-systemd-wrapper \
            file://hostapd.sh \
            file://wpa_supplicant.sh \
           "
SRCREV = "34afb764077768a361d76256fb05cbba172de223"
LIC_FILES_CHKSUM = "file://config.c;beginline=1;endline=13;md5=572cd47ba0e377b26331e67e9f3bc4b3"

S = "${WORKDIR}/git"

inherit cmake systemd

SYSTEMD_SERVICE_${PN} = "netifd.service"

EXTRA_OECMAKE="-DBUILD_LUA=OFF  -DDEBUG=1"
OECMAKE_C_FLAGS += "-I ${STAGING_INCDIR}/libnl3 -I ${STAGING_INCDIR}/libubus -I ${STAGING_INCDIR}"

do_install() {
    local devicename="${MACHINE}"
    install -d ${D}/${sysconfdir}/hotplug.d
    install -d ${D}/${base_sbindir}
    install -d ${D}/lib/netifd
    install -m 0755 ${B}/netifd ${D}/${base_sbindir}
    install -m 0755 ${S}/scripts/* ${D}/lib/netifd/
    (
	cd ${WORKDIR}
        for i in `find etc lib sbin -type d`; do
            install -d ${D}/$i
        done
        for i in `find lib sbin -type f`; do
            install -m 0755 $i ${D}/$i
        done
        for i in `find etc -type f`; do
            install -m 0644 $i ${D}/$i
        done
    )
    ln -sf ifup ${D}/${base_sbindir}/ifdown

    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/netifd.service ${D}${systemd_unitdir}/system
    install -m 0755 ${WORKDIR}/netifd-systemd-wrapper ${D}/${base_sbindir}
}

CONFFILES_${PN} += "${sysconfdir}/config/network"
CONFFILES_${PN} += "${sysconfdir}/config/dhcp"

FILES_${PN} = "${sysconfdir}/* ${base_sbindir}/* /etc/* /lib/*"

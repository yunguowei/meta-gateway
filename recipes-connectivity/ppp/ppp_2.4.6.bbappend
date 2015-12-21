
FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

SRC_URI += "file://100-debian_ip-ip_option.patch \
            file://101-debian_close_dev_ppp.patch \
            file://103-debian_fix_link_pidfile.patch \
            file://105-debian_demand.patch \
            file://120-debian_ipv6_updown_option.patch \
            file://205-no_exponential_timeout.patch \
            file://206-compensate_time_change.patch \
            file://207-lcp_mtu_max.patch \
            file://208-fix_status_code.patch \
            file://320-custom_iface_names.patch \
            file://321-multilink_support_custom_iface_names.patch \
            file://330-retain_foreign_default_routes.patch \
            file://340-populate_default_gateway.patch \
            file://500-add-pptp-plugin.patch \
            file://aeris \
            file://aeris_chat \
            file://rogers \
            file://rogers_chat \
            file://VZW_Telit \
            file://VZW_Telit_chat \
            file://lib/netifd/ppp-up \
            file://lib/netifd/ppp-down \
            file://lib/netifd/proto/ppp.sh"

do_configure_prepend() {
        # Change hard code to load plugin by different target arch
        sed -i 's:/lib:${base_libdir}:g' ${S}/pppd/pathnames.h
}

do_install_append() {
    install -d ${D}${sysconfdir}/ppp/chat/
    install -m 0644 ${WORKDIR}/aeris_chat ${D}${sysconfdir}/ppp/chat/aeris_chat
    install -m 0644 ${WORKDIR}/rogers_chat ${D}${sysconfdir}/ppp/chat/rogers_chat
    install -m 0644 ${WORKDIR}/VZW_Telit_chat ${D}${sysconfdir}/ppp/chat/VZW_Telit_chat
    install -d ${D}${sysconfdir}/ppp/peers/
    install -m 0644 ${WORKDIR}/aeris ${D}${sysconfdir}/ppp/peers/aeris
    install -m 0644 ${WORKDIR}/rogers ${D}${sysconfdir}/ppp/peers/rogers
    install -m 0644 ${WORKDIR}/VZW_Telit ${D}${sysconfdir}/ppp/peers/VZW_Telit

    install -d -m 0755 ${D}/${base_libdir}/netifd/proto
    install -m 0755 ${WORKDIR}/lib/netifd/ppp-up ${D}/${base_libdir}/netifd/ppp-up
    install -m 0755 ${WORKDIR}/lib/netifd/ppp-down ${D}/${base_libdir}/netifd/ppp-down
    install -m 0755 ${WORKDIR}/lib/netifd/proto/ppp.sh ${D}/${base_libdir}/netifd/proto/ppp.sh

    install -d -m 0755 ${D}/lib/netifd/proto
    install -m 0755 ${WORKDIR}/lib/netifd/ppp-up ${D}/lib/netifd/ppp-up
    install -m 0755 ${WORKDIR}/lib/netifd/ppp-down ${D}/lib/netifd/ppp-down
    install -m 0755 ${WORKDIR}/lib/netifd/proto/ppp.sh ${D}/lib/netifd/proto/ppp.sh
}

FILES_${PN} += "${base_libdir}/netifd/* ${libdir}/pppd/${PV}/pptp.so /lib/netifd/*"

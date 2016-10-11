DESCRIPTION = "This package provide GWP basic rootfs infrastructure support."
LICENSE = "windriver"
LICENSE_FLAGS = "commercial_windriver"
LIC_FILES_CHKSUM = "file://${WR_EXTRA_LIC_DIR}/windriver;md5=eb3421117285c0b7ccbe9fbc5f1f37d7"

PR = "r1"

RDEPENDS_${PN} = "btrfs-tools"

SRC_URI += " \
    file://firstboot.service \
    file://sysctl.conf \
    file://bin/ipcalc.sh \
    file://bin/login.sh \
    file://etc/rc.common \
    file://etc/conntrackd/conntrackd.conf \
    file://etc/sysconfig/arptables \
    file://lib/functions/boot.sh \
    file://lib/functions/network.sh \
    file://lib/functions/service.sh \
    file://lib/functions/partition.sh \
    file://lib/functions/uci-defaults.sh \
    file://lib/functions.sh \
    file://sbin/firstboot \
    file://sbin/hotplug-call \
    file://sbin/led.sh \
    file://sbin/wifi \
"

inherit systemd

SYSTEMD_SERVICE_${PN} = "firstboot.service"

do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_install() {
    #install /etc
    install -d -m 0755 ${D}/etc
    install -m 0755 ${WORKDIR}/etc/rc.common ${D}/etc/

    install -d -m 0755 ${D}/etc/conntrackd
    install -m 0644 ${WORKDIR}/etc/conntrackd/conntrackd.conf  ${D}/etc/conntrackd

    install -d -m 0755 ${D}/etc/sysconfig
    install -m 0644 ${WORKDIR}/etc/sysconfig/arptables  ${D}/etc/sysconfig

    #install /lib
    install -d -m 0755 ${D}/lib
    install -m 0755 ${WORKDIR}/lib/functions.sh ${D}/lib

    install -d -m 0755 ${D}/lib/functions
    for file in `ls ${WORKDIR}/lib/functions`;
    do
        install -m 0755 ${WORKDIR}/lib/functions/${file} ${D}/lib/functions
    done

    #install /sbin
    install -d -m 0755 ${D}/sbin
    for file in `ls ${WORKDIR}/sbin`;
    do
        install -m 0755 ${WORKDIR}/sbin/${file} ${D}/sbin
    done

    #install /bin
    install -d -m 0755 ${D}/bin
    for file in `ls ${WORKDIR}/bin`;
    do
        install -m 0755 ${WORKDIR}/bin/${file} ${D}/bin
    done

    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/firstboot.service ${D}${systemd_unitdir}/system

    install -d ${D}${sysconfdir}
    install -m 0644 ${WORKDIR}/sysctl.conf ${D}${sysconfdir}/sysctl-gwp.conf
    if ${@bb.utils.contains('DISTRO_FEATURES','systemd','true','false',d)}; then
        install -d ${D}${sysconfdir}/sysctl.d
        ln -sf ../sysctl-gwp.conf ${D}${sysconfdir}/sysctl.d/50-gwp.conf
    fi

    echo "${MACHINE}" > ${D}${sysconfdir}/device_name
    echo "${MACHINE}" > ${D}${sysconfdir}/board_name
}

FILES_${PN} += "/*"

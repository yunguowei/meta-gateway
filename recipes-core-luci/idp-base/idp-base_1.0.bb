DESCRIPTION = "This package provide IDP base infrastructure support."
LICENSE = "windriver"
LICENSE_FLAGS = "commercial_windriver"
LIC_FILES_CHKSUM = "file://${WORKDIR}/copyright;md5=9e3c8ee3041b22f72c93b00e06a188f4"

PR = "r1"

SRC_URI += " \
    file://copyright \
    file://firstboot.service \
    file://sysctl.conf \
    file://bin/ipcalc.sh \
    file://bin/login.sh \
    file://etc/functions.sh \
    file://etc/hosts \
    file://etc/hotplug2-common.rules \
    file://etc/hotplug2-init.rules \
    file://etc/protocols \
    file://etc/rc.common \
    file://etc/conntrackd/conntrackd.conf \
    file://etc/services \
    file://etc/sysconfig/arptables \
    file://etc/hotplug.d/ieee1394/10-ieee1394 \
    file://etc/hotplug.d/usb/10-usb \
    file://lib/functions/boot.sh \
    file://lib/functions/network.sh \
    file://lib/functions/service.sh \
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
    install -m 0755 ${WORKDIR}/etc/functions.sh  ${D}/etc/
    install -m 0644 ${WORKDIR}/etc/hosts  ${D}/etc/
    install -m 0644 ${WORKDIR}/etc/hotplug2-common.rules ${D}/etc/
    install -m 0644 ${WORKDIR}/etc/hotplug2-init.rules ${D}/etc/
    install -m 0644 ${WORKDIR}/etc/protocols ${D}/etc/
    install -m 0755 ${WORKDIR}/etc/rc.common ${D}/etc/
    install -m 0644 ${WORKDIR}/etc/services ${D}/etc/

    install -d -m 0755 ${D}/etc/conntrackd
    install -m 0644 ${WORKDIR}/etc/conntrackd/conntrackd.conf  ${D}/etc/conntrackd

    install -d -m 0755 ${D}/etc/hotplug.d
    install -d -m 0755 ${D}/etc/hotplug.d/ieee1394
    install -m 0644 ${WORKDIR}/etc/hotplug.d/ieee1394/10-ieee1394 ${D}/etc/hotplug.d/ieee1394
    install -d -m 0755 ${D}/etc/hotplug.d/usb
    install -m 0644 ${WORKDIR}/etc/hotplug.d/usb/10-usb ${D}/etc/hotplug.d/usb

    install -d -m 0755 ${D}/etc/sysconfig
    install -m 0644 ${WORKDIR}/etc/sysconfig/arptables  ${D}/etc/sysconfig

    #install /lib
    install -d -m 0755 ${D}/lib
    install -m 0755 ${WORKDIR}/lib/functions.sh ${D}/lib

    install -d -m 0755 ${D}/lib/functions
    for file in `ls ${WORKDIR}/lib/functions`;
    do
	echo "file is ${file}"
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
    install -m 0644 ${WORKDIR}/sysctl.conf ${D}${sysconfdir}/sysctl-idp.conf
    if ${@bb.utils.contains('DISTRO_FEATURES','systemd','true','false',d)}; then
        install -d ${D}${sysconfdir}/sysctl.d
        ln -sf ../sysctl-idp.conf ${D}${sysconfdir}/sysctl.d/50-idp.conf
    fi
}

FILES_${PN} += "/*"

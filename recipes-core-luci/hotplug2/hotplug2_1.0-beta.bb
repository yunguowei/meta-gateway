SUMMARY = "Dynamic device management subsystem for embedded systems"
DESCRIPTION = "\
Hotplug2 is a lightweight udev replacement, responsible for exporting Linux \
kernel uevents into the userspace and allowing users to write simple rules \
for their processing. Development is focused on very small binary footprint, \
as well as fast runtime, rather than feature-richness. This makes it a perfect \
candidate for embedded devices and other environments with severely limited resources."
LICENSE = "GPLv2"
SECTION = "console/utils"
PR = "r0"
PKG_REV = "201"

SRC_URI =  "http://svn.nomi.cz/svn/isteve/hotplug2/${BPN}-${PKG_REV}.tar.gz \
            file://patches/100-env_memleak.patch \
            file://patches/110-static_worker.patch \
            file://patches/120-sysfs_path_fix.patch \
            file://patches/130-cancel_download_fix.patch \
            file://patches/140-worker_fork_fix.patch \
            file://patches/150-force_fork_slow.patch \
            file://patches/160-event_block_fix.patch \
            file://patches/170-non_fatal_include.patch \
            file://files/hotplug2.rules \
            file://files/block.sh \
            file://files/20-mount \
            file://src/udevtrigger.c \
            "

acpaths = "-I ./m4"

S = "${WORKDIR}/${BPN}-${PKG_REV}"
LIC_FILES_CHKSUM = "file://COPYING;md5=751419260aa954499f7abaabaa882bbe"

CFLAGS += "-I ${STAGING_INCDIR} -fPIC -ldl"
EXTRA_OEMAKE = "COPTS='${CFLAGS}' STATIC_WORKER='fork'"

do_compile() {
    ${CC} ${CFLAGS} ${LDFLAGS} -o ${S}/udevtrigger ${WORKDIR}/src/udevtrigger.c
    oe_runmake all
    cd ${S}/workers; oe_runmake all
}

do_install() {
    install -d ${D}/${sysconfdir}/hotplug.d/block
    install -d ${D}/${base_sbindir}
    install -d ${D}/${base_libdir}/hotplug2
    install -d ${D}/${base_libdir}/functions
    install -m 0755 ${WORKDIR}/files/hotplug2.rules ${D}/${sysconfdir}
    install -m 0755 ${WORKDIR}/files/block.sh ${D}/${base_libdir}/functions
    install -m 0644 ${WORKDIR}/files/20-mount ${D}/${sysconfdir}/hotplug.d/block
    install -m 0755 ${S}/udevtrigger ${D}/${base_sbindir}
    install -m 0755 ${S}/hotplug2 ${D}/${base_sbindir}
    install -m 0755 ${S}/workers/*.so ${D}/${base_libdir}/hotplug2
}

FILES_${PN}-dbg += "${base_libdir}/hotplug2/.debug"
FILES_${PN} += "${base_libdir}/*"

SRC_URI[md5sum] = "9a8e64f89558998bb824525105cdbe6b"
SRC_URI[sha256sum] = "28b6aa4d6018477f21d518b5d34ec13211094320b46413c65d24369206b64445"

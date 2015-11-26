SUMMARY = "Basic utility library"
DESCRIPTION = "libubox is a general purpose library which provides \
things like an event loop, binary blob message formatting and handling, \
the Linux linked list implementation, and some JSON helpers."
LICENSE = "GPLv2"
SECTION = "console/utils"
DEPENDS = "json-c"
PR = "r0"


SRC_URI = "git://git.openwrt.org/project/libubox.git;protocol=git"

SRCREV = "dffbc09baf71b294185a36048166d00066d433b5"
S = "${WORKDIR}/git"

LIC_FILES_CHKSUM = "file://uloop.c;beginline=4;endline=9;md5=3361bb03ea8b932e950a2e457cab516a"

inherit cmake

EXTRA_OECMAKE="-DCMAKE_SKIP_RPATH:BOOL=YES -DBUILD_LUA=OFF"
OECMAKE_C_FLAGS += "-I ${STAGING_INCDIR}"

PACKAGES += "libblobmsg-json jshn"
FILES_libblobmsg-json = "${base_libdir}/libblobmsg_json.so"
do_install() {
    install -d ${D}/${base_libdir}
    install -d ${D}/lib/libubox
    install -d ${D}/${bindir}
    install -d ${D}/${includedir}/libubox
    install -d ${D}/usr/share/libubox
    install -m 0755 ${B}/jshn ${D}/${bindir}
    install -m 0755 ${B}/libubox.so ${D}/${base_libdir}
    install -m 0755 ${B}/libblobmsg_json.so ${D}/${base_libdir}
    install -m 0755 ${S}/sh/jshn.sh ${D}/lib/libubox
    install -m 0755 ${S}/sh/jshn.sh ${D}/usr/share/libubox
    cp -r ${S}/*.h ${D}/${includedir}/libubox
}


FILES_jshn = "${bindir}/jshn ${base_libdir}/libubox/jshn.sh"
FILES_jshn_append_x86-64 += "/lib/libubox/jshn.sh"
FILES_${PN} = "${base_libdir}/libubox.so ${bindir} /lib/libubox/* /usr/share/libubox/*"
FILES_${PN}-dev = "${includedir}/libubox/*"


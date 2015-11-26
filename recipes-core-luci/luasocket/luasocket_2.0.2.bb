DESCRIPTION = "LuaSocket is the most comprehensive networking support library for the Lua language."
LICENSE = "MIT"
HOMEPAGE = "http://luaforge.net/projects/luasocket"

DEPENDS = "lua"

PR = "r0"

SRC_URI = "http://luaforge.net/frs/download.php/2664/luasocket-${PV}.tar.gz \
           file://lua-socket_${PV}-make.patch"

LIC_FILES_CHKSUM = "file://LICENSE;md5=9a2d4d6957816b949774b39793409af1"

LUA_LIB_DIR =  "${libdir}/lua/5.1"
LUA_SHARE_DIR = "${datadir}/lua/5.1"

FILES_${PN}-dbg = " \
    ${LUA_LIB_DIR}/mime/.debug/core.so \
    ${LUA_LIB_DIR}/socket/.debug/core.so \
    /usr/src/debug/*"

FILES_${PN} = "${LUA_LIB_DIR}/mime/core.so \
               ${LUA_LIB_DIR}/socket/core.so \
               ${LUA_SHARE_DIR}/*.lua \
               ${LUA_SHARE_DIR}/socket/*.lua"

CFLAGS += " -O2 -fpic"
LDFLAGS += "-O -shared -fpic"

EXTRA_OEMAKE = "MYFLAGS='${CFLAGS} ${LDFLAGS}'"

do_install() {
        oe_runmake install INSTALL_TOP_SHARE=${D}${LUA_SHARE_DIR} INSTALL_TOP_LIB=${D}${LUA_LIB_DIR}
        install -d ${D}/${docdir}/${PN}-${PV}
        install -m 0644 doc/*.html ${D}/${docdir}/${PN}-${PV}
}

SRC_URI[md5sum] = "a3c1a8c556c4de81ee5023a1e07c94cf"
SRC_URI[sha256sum] = "ce519337250349c2d4165ea569a1b85534dfdc0a25f274f3c9049d8aae2eed1e"

SUMMARY = "A container image for domain gateway which is used as a headless, full development server"
DESCRIPTION = "Launched from the essential image, this is a container image \
               which provides a headless install capable of being used as a server \
               and as a development platform. \
              "
HOMEPAGE = "http://www.windriver.com"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302 \
                    file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"


CUBE_DOM_GW_EXTRA_INSTALL ?= ""

IMAGE_FEATURES += "package-management doc-pkgs"
IMAGE_FSTYPES = "tar.bz2"

PACKAGE_EXCLUDE = "busybox*"

IMAGE_INSTALL += "packagegroup-core-boot \
                  packagegroup-core-ssh-openssh \
                  packagegroup-core-full-cmdline \
                  packagegroup-util-linux \
		  packagegroup-dom0 \
		  flatpak \
                  "

# USB
IMAGE_INSTALL += "usbutils"

# WiFi and Bluetooth
IMAGE_INSTALL += "hostapd hostap-utils"
IMAGE_INSTALL += "wireless-tools wpa-supplicant"
IMAGE_INSTALL += "linux-firmware"
IMAGE_INSTALL += "bluez5"

# GW Protocols
IMAGE_INSTALL += "dnsmasq"
IMAGE_INSTALL += "ppp"
IMAGE_INSTALL += "ntp"

# Gateway specific server management 
IMAGE_INSTALL += "luci"
IMAGE_INSTALL += "lighttpd"
IMAGE_INSTALL += "lighttpd-module-cgi"
IMAGE_INSTALL += "iwinfo"
IMAGE_INSTALL += "iw"
IMAGE_INSTALL += "netifd"
IMAGE_INSTALL += "dnf"
IMAGE_INSTALL += "firewall"
IMAGE_INSTALL += "multiwan"
IMAGE_INSTALL += "gw-base"
IMAGE_INSTALL += "gw-boot"
IMAGE_INSTALL += "comgt"
IMAGE_INSTALL += "uqmi"
IMAGE_INSTALL += "umbim"
IMAGE_INSTALL += "dpkg"

#VPN support
IMAGE_INSTALL += "xl2tpd"
IMAGE_INSTALL += "strongswan"
IMAGE_INSTALL += "pptpd"

# docker support
IMAGE_INSTALL += "docker"

# Extras as defined externally
IMAGE_INSTALL += "${CUBE_DOM_GW_EXTRA_INSTALL}"

inherit flux-ota core-image
inherit builder-base

SUMMARY = "A container image for domain gateway which is used as a graphical interface, full development server"
DESCRIPTION = "Launched from the essential image, this is a container image \
               which provides a graphical interface install capable of being used as a server \
               and as a development platform. \
              "
HOMEPAGE = "http://www.windriver.com"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302 \
                    file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"


CUBE_DOM_GW_GFX_EXTRA_INSTALL ?= ""

IMAGE_FEATURES += "package-management doc-pkgs x11-base"
IMAGE_FSTYPES = "tar.bz2"

PACKAGE_EXCLUDE = "busybox*"

IMAGE_INSTALL += "packagegroup-core-boot \
                  packagegroup-core-ssh-openssh \
                  packagegroup-core-full-cmdline \
                  packagegroup-util-linux \
                  packagegroup-builder \
                  packagegroup-dom0 \
                  packagegroup-container \
		  packagegroup-xfce \
                  "

# WiFi and Bluetooth
IMAGE_INSTALL += "hostapd"
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
IMAGE_INSTALL += "python-smartpm"
IMAGE_INSTALL += "firewall"
IMAGE_INSTALL += "multiwan"
IMAGE_INSTALL += "gw-base"
IMAGE_INSTALL += "gw-boot"
IMAGE_INSTALL += "comgt"
IMAGE_INSTALL += "uqmi"
IMAGE_INSTALL += "umbim"

# Extras as defined externally
IMAGE_INSTALL += "${CUBE_DOM_GW_GFX_EXTRA_INSTALL}"

XSERVER_append = "xserver-xorg \
                  xserver-xorg-extension-dri \
                  xserver-xorg-extension-dri2 \
                  xserver-xorg-extension-glx \
                  xserver-xorg-extension-extmod \
                  xserver-xorg-extension-dbe \
                  xserver-xorg-module-libint10 \
                  xf86-input-evdev \
                  xf86-input-keyboard \
                  xf86-input-mouse \
                  xf86-input-synaptics \
                  xf86-input-vmmouse \
                  xf86-video-ati \
                  xf86-video-fbdev \
                  xf86-video-intel \
                  xf86-video-mga \
                  xf86-video-modesetting \
                  xf86-video-nouveau \
                  xf86-video-vesa \
                  xf86-video-vmware \
                 "

ALTERNATIVE_PRIORITY_xfce4-session[x-session-manager] = "60"

inherit core-image
inherit builder-base


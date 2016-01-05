FILESEXTRAPATHS_prepend := "${THISDIR}/lighttpd:"

DEPENDS += "openssl"

EXTRA_OECONF += " --with-openssl \
               "

RDEPENDS_${PN} += " \
               lighttpd-module-redirect \
	       "

do_install_append(){
    install -d ${D}${sysconfdir}/runonce
    cat << EOF >${D}${sysconfdir}/runonce/customer-runonce.sh
#!/bin/bash
certsfile="${sysconfdir}/lighttpd/certs/lighttpd.pem"
if ! [ -e \$certsfile ]; then
    if ! [ -d "${sysconfdir}/lighttpd/certs" ]; then
        mkdir -p ${sysconfdir}/lighttpd/certs
    fi
    openssl req -new -x509  -keyout \${certsfile} -out \${certsfile} -days 365 -nodes -batch
fi
chmod 0400 \${certsfile}

EOF  

    chmod +x ${D}${sysconfdir}/runonce/customer-runonce.sh
}

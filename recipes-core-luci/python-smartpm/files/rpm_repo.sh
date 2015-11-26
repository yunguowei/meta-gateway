#!/bin/sh

# Copyright (c) 2012 Wind River Systems, Inc.
# The right to copy, distribute, modify, or otherwise
# make use of this software may be licensed only pursuant
# to the terms of an applicable Wind River license agreement.
#

#usage: ./rpm_repo.sh -a <Repo Name> <Repo URL>
#                     -d <Repo Name>
#                     -l <Local RPM Dir>
#                     -u [Repo Name]

SMART=$(which smart)
repo_add_log="/tmp/repo_add.log"
repo_del_log="/tmp/repo_del.log"
repo_local_log="/tmp/repo_local.log"
repo_update_log="/tmp/repo_update.log"
repo_status_log="/tmp/repo_status.log"

Usage()
{
    echo "Usage:rpm_repo.sh -a <Repo Name> <Repo URL>"
    echo "                  -d <Repo Name>"
    echo "                  -l <Local RPM Dir>"
    echo "                  -u [Repo Name]"
    exit 1;
}

repo_status()
{
    local name="$@"
    $SMART channel --show "${name}" > ${repo_status_log} 2>&1
    cat ${repo_status_log} | grep "warning" | grep "not found" > /dev/null 2>&1
    return $?
}

repo_add()
{   
    local name="$1"
    local url="$2"
    local type="rpm-md"
    local ret=0

    repo_status "${name}"
    if [ "$?" -eq 0 ]; then
        $SMART channel -y --add "${name}" type="${type}" baseurl="${url}/" > ${repo_add_log} 2>&1 
    else
        $SMART channel -y --set "${name}" baseurl="${url}/" > ${repo_add_log} 2>&1
    fi
    ret=$?
    $SMART update ${name} > ${repo_update_log} 2>&1
    return $ret
}

repo_del()
{
    local name="$@"
    local ret=0

    repo_status "$name"
    if [ "$?" -eq 1 ]; then
        $SMART channel -y --remove "${name}" > ${repo_del_log} 2>&1
    fi
    ret=$?
    $SMART update ${name} > ${repo_update_log} 2>&1
    return $ret
}

repo_local()
{
    local LOCAL_RPM_DIR="$1"
    local ret=0

    repo_status "localrpmchannel"
    if [ "$?" -eq 0 ]; then
        $SMART channel -y --add localrpmchannel type=rpm-dir name="default rpm channel" path="${LOCAL_RPM_DIR}" > ${repo_local_log} 2>&1
    else
        $SMART channel -y --set localrpmchannel path="${LOCAL_RPM_DIR}" > ${repo_local_log} 2>&1
    fi
    ret=$?
    $SMART update localrpmchannel > ${repo_update_log} 2>&1
    return $ret
}

repo_update()
{
    local name="$@"
    $SMART update ${name} > ${repo_update_log} 2>&1
    return $?
}

if [ ! -e "$SMART" ]; then
    echo "Command \"smart\" does not exist!"
    exit 1
fi

case $1 in
    -a)
        repo_name="$2"
        repo_url="$3"
        [ -z "${repo_name}" ] || [ -z "${repo_url}" ] && Usage;
        repo_add "${repo_name}" "${repo_url}";;
    -d)
        shift
        repo_name="$@"
        [ -z "${repo_name}" ] && Usage;
        repo_del "${repo_name}";;
    -l)
        shift
        [ -z "$@" ] && Usage;
        repo_local "$@";;
    -u)    
        shift
        repo_update "$@";;
    *)
        Usage;;
esac

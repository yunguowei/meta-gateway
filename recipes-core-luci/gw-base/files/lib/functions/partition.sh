#!/bin/sh
# Copyright (C) 2016 Wind River Systems, Inc.

sys_get_partition_fstype() {
    mount | grep "^$1 on" | grep -o "type [^ ]*" | awk '{print $2}'
}

sys_get_cmdline_root() {
    local root

    # root=XXX=yyy
    for i in UUID PARTUUID LABEL LVM SFR; do
        root=$(grep -o "\<root=${i}=[^ ]*" /proc/cmdline | sed 's/root=//g')
        [ -n "$root" ] && echo $root && return 0
    done

    # root=/dev/xxx
    root=$(grep -o '\<root=[^= ]*' /proc/cmdline | sed 's/root=//g')
    [ -n "$root" ] && echo "DEVICE=$root" && return 0

    return 1
}

sys_get_partnode_from_partuuid() {
    local partuuid=$1 pnode=""

    if [ -n "$partuuid" ] ; then
        pnode=$(blkid 2>/dev/null | grep "PARTUUID=\"$partuuid\"" | grep -o "^[^: ]*")
        [ -n "$pnode" ] && echo $pnode && return 0

        if [ -e /dev/disk/by-partuuid/$partuuid ] ; then
            pnode=$(readlink -sf /dev/disk/by-partuuid/$partuuid 2>dev/null)
            [ -n "$pnode" ] && echo $pnode && return 0
        fi
    fi
    return 1
}

sys_get_partnode_from_lvm() {
    local lvm=$1 pnode="" vg=""

    if [ -n $lvm ] ; then
        vg=$(basename $lvm | awk -F'-' '{print $1}')
        pnode=$(pvs 2>/dev/null | grep "$vg" | tail -n1 | awk '{print $1}')
        [ -n "$pnode" ] && echo $pnode && return 0
    fi
    return 1
}

sys_get_partnode_from_root() {
    local root=$1
    local root_type root_name root_node

    [ -z "$root" ] && return 1
    root_type=$(echo $root | cut -d "=" -f 1)
    root_name=$(echo $root | cut -d "=" -f 2)

    # convert root name to partition node
    case $root_type in
        UUID)     root_node=$(blkid -U $root_name) ;;
        LABEL)    root_node=$(blkid -L $root_name) ;;
        PARTUUID) root_node=$(sys_get_partnode_from_partuuid $root_name) ;;
        LVM|SFR)  root_node=$(sys_get_partnode_from_lvm $root_name) ;;
        DEVICE)   root_node=$root_name ;;
    esac

    [ -b "$root_node" ] || return 1
    echo $root_node && return 0
}

sys_get_rootfs_device() {
    local root=$(sys_get_cmdline_root)
    local root_node=$(sys_get_partnode_from_root $root)

    [ -b "$root_node" ] || return 1

    # return the device node
    if echo $root_node | grep -q "mmcblk" ; then
        echo ${root_node:0:-2}
    else
        echo ${root_node:0:-1}
    fi
    return 0
}

sys_get_partition_name() {
    local idx=$1 device=$2 name=""

    if [ -z "$device" ] ; then
        device=$(sys_get_rootfs_device)
    fi
    if [ -z "$device" ] ; then
        return 1
    fi

    if echo "$device" | grep -q "mmcblk" ; then
        name=${device}p${idx}
    else
        name=${device}${idx}
    fi

    echo "$name"
    return 0
}

sys_mount_boot_partition() {
    local mount_point="/boot/efi"
    local boot_partition=""

    # Just try mount it if it's not already mounted.
    grep -q "$mount_point" /proc/mounts && return 0

    # Just get the first partition of the rootfs device.
    # Deploytool has this fixed layout when deploy image.
    # We don't check it carefully, because there may be
    # many changes to the details of each check point.
    # For example, boot partition may is not fat type at all.
    # Or the boot flag in current boot partition will not be
    # set by someone who create the partition manually.
    # The installed files and paths in boot partition may also
    # be changed in the future, so this simple way is enough.
    boot_partition=$(sys_get_partition_name 1)
    [ -z "$boot_partition" ] && return 1

    echo "Trying to mount boot partition $boot_partition at /boot/efi ..."
    mkdir -p /boot/efi 2>/dev/null
    umount $boot_partition 2>/dev/null
    mount $boot_partition $mount_point
}

sys_remount_rootfs_partittion() {
    local root=$(sys_get_cmdline_root)
    local root_node=$(sys_get_partnode_from_root $root)
    local mnt_dir="/rootfs" root_type="" option=""

    if [ -n "$root_node" ] ; then
        mkdir -p $mnt_dir
        root_type=$(sys_get_partition_fstype $root_node)
        [ "$root_type" == "btrfs" ] && option="-o subvol=rootfs"
        umount $mnt_dir 2>/dev/null
        mount $option $root_node $mnt_dir
    fi
}


----Thanks for xen0n, ferhung, fire855 who are contributing to the working CyanogenMod of MTK hardware.---

This is a device tree for Lenovo S650 ROW which is based on MT6582 SoC. Powered by Hikari no Tenshi.
# Build

* init
  Sync CyanogenMod source:

        # repo init -u git://github.com/ResurrectionRemix-mtk/platform_manifest.git -b marshmallow        
        # repo sync -f --force-sync --no-clone-bundle

* full build
        
        # source build/envsetup.sh

        # brunch rr_S650_ROW-userdebug

# Limitations

Services requires root:

`system/core/rootdir/init.rc`

  * surfaceflinger depends on sched_setscheduler calls, unable to change process priority from 'system' user (default user 'system')

  * mediaserver depends on /data/nvram folder access, unable to do voice calls from 'media' user (default user 'media')

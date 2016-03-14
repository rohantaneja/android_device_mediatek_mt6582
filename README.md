Generic MediaTek MT6582 device.
==============

Basic   | Spec Sheet
-------:|:-------------------------
CPU     | 1.3GHz Quad-Core MT6582
GPU     | Mali-400MP
Memory  | 1GB RAM
Shipped Android Version | 4.2.2 - 4.4.2
Storage | 8GB (varies)
Display | 6.0" 1280 x 720 px (varies)
Camera  | 8MPx, LED Flash

This branch is for building CyanogenMod 12.1 (or CM-12.1 based) ROMS.

# Build

* Working
  * Dual SIM
  * Wifi
  * Bluetooth
  * Audio
  * Sensors
  * Camera
  * GPS
  * OTG
  * Tethering (Wifi, Bluetooth and USB)
  * FM Radio

* Compilation

        # repo init -u git://github.com/rohantaneja/android.git -b cm-12.1
        
        # repo sync
        
        # source build/envsetup.sh
        
        # brunch cm_mt6582-userdebug

# MTK

Few words about mtk related binaries, services and migration peculiarities.

# Limitations

Services requires root:

`system/core/rootdir/init.rc`

  * surfaceflinger depends on sched_setscheduler calls, unable to change process priority from 'system' user (default user 'system')

  * mediaserver depends on /data/nvram folder access, unable to do voice calls from 'media' user (default user 'media')
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

This branch is for building 5.1.1 based ROMs.

* Compilation

        # repo init -u git://github.com/rohantaneja/android.git -b cm-12.1
        
        # repo sync
        
        # source build/envsetup.sh
        
        # brunch cm_mt6582-userdebug

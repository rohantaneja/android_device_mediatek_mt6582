#
# Copyright (C) 2016 The CyanogenMod Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# The gps config appropriate for this device
$(call inherit-product, device/common/gps/gps_us_supl.mk)

# Inherit from the common Open Source product configuration
$(call inherit-product, $(SRC_TARGET_DIR)/product/languages_full.mk)
$(call inherit-product, $(SRC_TARGET_DIR)/product/full_base_telephony.mk)

# Inherit vendor blobs
$(call inherit-product-if-exists, vendor/mediatek/mt6582/mt6582-vendor.mk)

LOCAL_PATH := device/mediatek/mt6582

PRODUCT_CHARACTERISTICS := default

DEVICE_PACKAGE_OVERLAYS += $(LOCAL_PATH)/overlay

ifeq ($(TARGET_PREBUILT_KERNEL),)
    LOCAL_KERNEL := $(LOCAL_PATH)/kernel
else
    LOCAL_KERNEL := $(TARGET_PREBUILT_KERNEL)
endif

PRODUCT_TAGS += dalvik.gc.type-precise

PRODUCT_PACKAGES += \
    Torch

PRODUCT_PACKAGES += \
    libxlog

# Lights
PRODUCT_PACKAGES += \
    lights.mt6582

# Audio
PRODUCT_PACKAGES += \
    audio.primary.mt6582 \
    audio_policy.default \
    audio.a2dp.default \
    audio.usb.default \
    audio.r_submix.default \
    libaudio-resampler \
    tinymix

PRODUCT_PACKAGES += \
    lib_driver_cmd_mt66xx

PRODUCT_COPY_FILES += \
    $(LOCAL_PATH)/audio/audio_policy.conf:system/etc/audio_policy.conf
    
# Bluetooth
PRODUCT_PACKAGES += \
    libbt-vendor

# GSM
PRODUCT_PACKAGES += libmt6582

PRODUCT_PROPERTY_OVERRIDES += \
    ro.telephony.ril_class=MediaTekRIL

# Rootdir
PRODUCT_PACKAGES += \
    fstab.mt6582 \
    init.recovery.mt6582.rc \
    init.mt6582.rc \
    init.mt6582_common.rc \
    init.modem.rc \
    ueventd.mt6582.rc \
    init.mt6582.usb.rc \
    enableswap.sh \
    factory_init.rc \
    twrp.fstab

# Kernel
PRODUCT_COPY_FILES += \
    $(LOCAL_KERNEL):kernel

# Permissions
PRODUCT_COPY_FILES += \
    frameworks/native/data/etc/android.hardware.bluetooth_le.xml:system/etc/permissions/android.hardware.bluetooth_le.xml \
    frameworks/native/data/etc/android.hardware.bluetooth.xml:system/etc/permissions/android.hardware.bluetooth.xml \
    frameworks/native/data/etc/android.hardware.camera.autofocus.xml:system/etc/permissions/android.hardware.camera.autofocus.xml \
    frameworks/native/data/etc/android.hardware.camera.flash-autofocus.xml:system/etc/permissions/android.hardware.camera.flash-autofocus.xml \
    frameworks/native/data/etc/android.hardware.camera.front.xml:system/etc/permissions/android.hardware.camera.front.xml \
    frameworks/native/data/etc/android.hardware.faketouch.xml:system/etc/permissions/android.hardware.faketouch.xml \
    frameworks/native/data/etc/android.hardware.location.gps.xml:system/etc/permissions/android.hardware.location.gps.xml \
    frameworks/native/data/etc/android.hardware.sensor.accelerometer.xml:system/etc/permissions/android.hardware.sensor.accelerometer.xml \
    frameworks/native/data/etc/android.hardware.sensor.compass.xml:system/etc/permissions/android.hardware.sensor.compass.xml \
    frameworks/native/data/etc/android.hardware.sensor.gyroscope.xml:system/etc/permissions/android.hardware.sensor.gyroscope.xml \
    frameworks/native/data/etc/android.hardware.sensor.light.xml:system/etc/permissions/android.hardware.sensor.light.xml \
    frameworks/native/data/etc/android.hardware.sensor.proximity.xml:system/etc/permissions/android.hardware.sensor.proximity.xml \
    frameworks/native/data/etc/android.hardware.telephony.gsm.xml:system/etc/permissions/android.hardware.telephony.gsm.xml \
    frameworks/native/data/etc/android.hardware.touchscreen.multitouch.distinct.xml:system/etc/permissions/android.hardware.touchscreen.multitouch.distinct.xml \
    frameworks/native/data/etc/android.hardware.touchscreen.multitouch.jazzhand.xml:system/etc/permissions/android.hardware.touchscreen.multitouch.jazzhand.xml \
    frameworks/native/data/etc/android.hardware.touchscreen.multitouch.xml:system/etc/permissions/android.hardware.touchscreen.multitouch.xml \
    frameworks/native/data/etc/android.hardware.touchscreen.xml:system/etc/permissions/android.hardware.touchscreen.xml \
    frameworks/native/data/etc/android.hardware.usb.accessory.xml:system/etc/permissions/android.hardware.usb.accessory.xml \
    frameworks/native/data/etc/android.hardware.wifi.direct.xml:system/etc/permissions/android.hardware.wifi.direct.xml \
    frameworks/native/data/etc/android.hardware.wifi.xml:system/etc/permissions/android.hardware.wifi.xml \
    frameworks/native/data/etc/android.software.midi.xml:system/etc/permissions/android.software.midi.xml \
    frameworks/native/data/etc/android.software.sip.voip.xml:system/etc/permissions/android.software.sip.voip.xml \
    frameworks/native/data/etc/handheld_core_hardware.xml:system/etc/permissions/handheld_core_hardware.xml \
    packages/wallpapers/LivePicker/android.software.live_wallpaper.xml:system/etc/permissions/android.software.live_wallpaper.xml \
    $(LOCAL_PATH)/configs/platform.xml:system/etc/permissions/platform.xml

# Keylayout overrides
PRODUCT_COPY_FILES_OVERRIDES += \
    system/usr/keylayout/Generic.kl

# Permissions overrides
PRODUCT_COPY_FILES_OVERRIDES += \
    system/etc/permissions/android.hardware.camera.xml

# Media codecs overrides
PRODUCT_COPY_FILES_OVERRIDES += \
    system/etc/media_codecs_google_video.xml

# Keylayout
PRODUCT_COPY_FILES += \
    $(LOCAL_PATH)/keylayout/mtk-kpd.kl:system/usr/keylayout/mtk-kpd.kl

# Wifi
PRODUCT_PACKAGES += \
    libwpa_client \
    hostapd \
    dhcpcd.conf \
    wpa_supplicant \
    wpa_supplicant.conf

PRODUCT_COPY_FILES += \
    $(LOCAL_PATH)/configs/hostapd/hostapd_default.conf:system/etc/hostapd/hostapd_default.conf \
    $(LOCAL_PATH)/configs/hostapd/hostapd.accept:system/etc/hostapd/hostapd.accept \
    $(LOCAL_PATH)/configs/hostapd/hostapd.deny:system/etc/hostapd/hostapd.deny

# Charger
PRODUCT_PACKAGES += \
    charger \
    charger_res_images \
    libnl_2 \
    libtinyxml

# Camera
PRODUCT_PACKAGES += \
    Snap

# FMRadio
PRODUCT_PACKAGES += \
    FMRadio \
    libfmcust \
    libfmjni \
    libfmmt6620 \
    libfmmt6627 \
    libfmmt6628 \
    libfmmt6630 \
    libmtkplayer

# GPS
PRODUCT_COPY_FILES += \
    $(LOCAL_PATH)/configs/agps_profiles_conf2.xml:system/etc/agps_profiles_conf2.xml

PRODUCT_PACKAGES += \
    gps.mt6582 \
    YGPS

PRODUCT_COPY_FILES += \
    $(LOCAL_PATH)/configs/media_codecs.xml:system/etc/media_codecs.xml \
    frameworks/av/media/libstagefright/data/media_codecs_google_audio.xml:system/etc/media_codecs_google_audio.xml \
    frameworks/av/media/libstagefright/data/media_codecs_google_telephony.xml:system/etc/media_codecs_google_telephony.xml \
    frameworks/av/media/libstagefright/data/media_codecs_google_video_le.xml:system/etc/media_codecs_google_video_le.xml \
    $(LOCAL_PATH)/configs/media_codecs_performance.xml:system/etc/media_codecs_performance.xml \
    $(LOCAL_PATH)/configs/media_profiles.xml:system/etc/media_profiles.xml

ADDITIONAL_DEFAULT_PROPERTIES += \
    ro.adb.secure=0 \
    ro.secure=0 \
    ro.allow.mock.location=1 \
    persist.service.adb.enable=1 \
    persist.service.debuggable=1 \
    persist.sys.usb.config=mtp

# Set default player to AwesomePlayer
PRODUCT_PROPERTY_OVERRIDES += \
persist.sys.media.use-awesome=true

PRODUCT_PROPERTY_OVERRIDES := \
    ro.adb.secure=0 \
    ro.secure=0 \
    ro.mediatek.version.release=ALPS.W10.24.p0 \
    ro.mediatek.platform=MT6582 \
    ro.mediatek.chip_ver=S01 \
    ro.mediatek.version.branch=KK1.MP1 \
    ro.mediatek.version.sdk=2 \
    ro.telephony.sim.count=2 \
    ro.allow.mock.location=1 \
    ro.debuggable=1 \
    persist.sys.usb.config=mtp,adb \
    persist.service.adb.enable=1 \
    persist.service.debuggable=1 \
    persist.mtk.wcn.combo.chipid=-1

$(call inherit-product, build/target/product/full.mk)

PRODUCT_BUILD_PROP_OVERRIDES += BUILD_UTC_DATE=0
PRODUCT_NAME := full_mt6582
PRODUCT_DEVICE := mt6582

# Boot animation
TARGET_SCREEN_HEIGHT := 1280
TARGET_SCREEN_WIDTH := 720

PRODUCT_AAPT_CONFIG := normal hdpi
PRODUCT_AAPT_PREF_CONFIG := hdpi

PRODUCT_PACKAGES += \
    librs_jni \
    com.android.future.usb.accessory

# Remove unwanted packages
PRODUCT_PACKAGES_OVERRIDES += \
    audio.primary.goldfish \
    camera.goldfish.jpeg \
    camera.goldfish \
    fingerprint.goldfish \
    fingerprint.ranchu \
    gps.goldfish \
    lights.goldfish \
    power.goldfish \
    ResurrectionOTA \
    ResurrectionStats \
    sensors.goldfish \
    sensors.ranchu \
    vibrator.goldfish

# Remove unwanted files
PRODUCT_COPY_FILES_OVERRIDES += \
    root/fstab.goldfish \
    root/fstab.ranchu \
    root/init.goldfish.rc \
    root/init.ranchu.rc \
    root/ueventd.goldfish.rc \
    root/ueventd.ranchu.rc \
    recovery/root/fstab.goldfish \
    recovery/root/fstab.ranchu \
    recovery/root/ueventd.goldfish.rc \
    recovery/root/ueventd.ranchu.rc

$(call inherit-product, frameworks/native/build/phone-xhdpi-1024-dalvik-heap.mk)

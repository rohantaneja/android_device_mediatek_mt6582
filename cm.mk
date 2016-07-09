# Release name
PRODUCT_RELEASE_NAME := MT6582

# Inherit some common CM stuff.
$(call inherit-product, vendor/cm/config/common_full_phone.mk)

# Inherit from hardware-specific part of the product configuration
$(call inherit-product, device/mediatek/mt6582/device.mk)

# Device identifier. This must come after all inclusions
PRODUCT_DEVICE := mt6582
PRODUCT_NAME := cm_mt6582
PRODUCT_BRAND := Mediatek
PRODUCT_MODEL := MT6582
PRODUCT_MANUFACTURER := MediaTek

PRODUCT_GMS_CLIENTID_BASE := android-mediatek

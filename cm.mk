## Specify phone tech before including full_phone

# Release name
PRODUCT_RELEASE_NAME := MT6582

# Inherit some common CM stuff.
$(call inherit-product, vendor/cm/config/common_full_phone.mk)

# Inherit device configuration
$(call inherit-product, device/mediatek/mt6582/device_mt6582.mk)
 
## Device identifier. This must come after all inclusions
PRODUCT_DEVICE := mt6582
PRODUCT_NAME := cm_mt6582
PRODUCT_BRAND := MediaTek
PRODUCT_MODEL := MT6582
PRODUCT_MANUFACTURER := MediaTek

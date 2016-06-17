/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 */
/* MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

package com.mediatek.ygps;

import java.util.ArrayList;
import java.util.List;



public class SatelliteInfoManager {

    private static final String TAG = "SatelliteInfoManager";
    public static final int PRN_ANY = -1;
    public static final int PRN_ALL = -2;

    List<SatelliteInfo> mSatelInfoList;

    public SatelliteInfoManager() {
        mSatelInfoList = new ArrayList<SatelliteInfo>();
    }

    public void updateSatelliteInfo(SatelliteInfoAdapter adapter) {
        if (mSatelInfoList != null) {
            mSatelInfoList.clear();
        } else {
            mSatelInfoList = new ArrayList<SatelliteInfo>();
        }
        for (SatelliteInfo si : adapter) {
            mSatelInfoList.add(si);
        }
    }

    public List<SatelliteInfo> getSatelInfoList() {
        return mSatelInfoList;
    }

    public SatelliteInfo getSatelliteInfo(int prn) {
        for (SatelliteInfo si : mSatelInfoList) {
            if (si.prn == prn) {
                return si;
            }
        }
        return null;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{Satellite Count:").append(mSatelInfoList.size());
        for (SatelliteInfo info : mSatelInfoList) {
            builder.append(info.toString());
        }
        builder.append("}");
        return builder.toString();
    }

    public void clearSatelInfos() {
        mSatelInfoList.clear();
    }

    public boolean isUsedInFix(int prn) {
        boolean result = false;
        if (prn == PRN_ALL && mSatelInfoList.size() > 0) {
            result = true;
        }
        for (SatelliteInfo si : mSatelInfoList) {
            if (prn == PRN_ALL) {
                if (!si.usedInFix) {
                    result = false;
                    break;
                }
            } else if (prn == PRN_ANY) {
                if (si.usedInFix) {
                    result = true;
                    break;
                }
            } else if (prn == si.prn) {
                result = si.usedInFix;
                break;
            }
        }
        return result;
    }
}

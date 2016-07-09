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

import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.util.AttributeSet;

public class SatelSignalChartView extends SatelliteBaseView {

    private static final String TAG = "SatelSignalChartView";
    private static final float PERCENT_80 = 0.8f;
    private static final float PERCENT_75 = 0.75f;
    private static final float PERCENT_50 = 0.5f;
    private static final float PERCENT_25 = 0.25f;
    private static final float RATIO_BASE = 100;
    private static final int BASE_LINE_OFFSET = 6;
    private static final int TEXT_OFFSET = 10;
    private static final int[] DIVIDER_RANKS = {15, 20, 25, 30, 35};
    private Paint mLinePaint;
    private Paint mLine2Paint;
    private Paint mRectPaint;
    private Paint mRectLinePaint;
    private Paint mTextPaint;
    private Paint mBgPaint;

    public SatelSignalChartView(Context context) {
        this(context, null, 0);
    }

    public SatelSignalChartView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SatelSignalChartView(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
        onCreateView();
    }

    private void onCreateView() {
        mRectPaint = new Paint();
        mRectPaint.setAntiAlias(false);
        mRectPaint.setStrokeWidth(2.0f);

        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setColor(getResources().getColor(R.color.sigview_line_color));
        mLinePaint.setStyle(Style.STROKE);
        mLinePaint.setStrokeWidth(1.0f);

        mLine2Paint = new Paint(mLinePaint);
        mLine2Paint.setStrokeWidth(0.5f);

        mRectLinePaint = new Paint(mRectPaint);
        mRectLinePaint.setColor(getResources().getColor(R.color.bar_outline));
        mRectLinePaint.setStyle(Style.STROKE);

        mTextPaint = new Paint();
        mTextPaint.setTextAlign(Align.CENTER);
        mTextPaint.setTextSize(10.0f);
        mTextPaint.setColor(getResources().getColor(R.color.sigview_text_color));

        mBgPaint = new Paint();
        mBgPaint.setColor(getResources().getColor(R.color.sigview_background));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float viewWidth = getWidth();
        float viewHeight = getHeight();
        float sigRectMaxHeight = (float) Math.floor(viewHeight * PERCENT_80);
        float baseLineY = sigRectMaxHeight + BASE_LINE_OFFSET;
        float rectRatio = sigRectMaxHeight / RATIO_BASE;

        canvas.drawPaint(mBgPaint);
        canvas.drawLine(0, baseLineY, viewWidth, baseLineY, mLinePaint);
        float[] line2YArr = {baseLineY - sigRectMaxHeight * PERCENT_25,
                baseLineY - sigRectMaxHeight * PERCENT_50,
                baseLineY - sigRectMaxHeight * PERCENT_75,
                baseLineY - sigRectMaxHeight};
        for (int i = 0; i < line2YArr.length; i++) {
            canvas.drawLine(0, line2YArr[i], viewWidth, line2YArr[i], mLine2Paint);
        }
        SatelliteInfoManager simgr = getSatelliteInfoManager();
        if (simgr != null) {
            List<SatelliteInfo> stInfoList = simgr.getSatelInfoList();
            float bgRectWidth = computeBgRectWidth(stInfoList.size());
            float barWidth = (float) Math.floor(bgRectWidth * PERCENT_75);
            float margin = (bgRectWidth - barWidth) / 2;
            for (int i = 0; i < stInfoList.size(); i++) {
                SatelliteInfo si = stInfoList.get(i);
                float barHeight = si.snr * rectRatio;
                float left = i * bgRectWidth + margin;
                float top = baseLineY - barHeight;
                float center = left + bgRectWidth / 2;
                canvas.drawRect(left, top, left + barWidth, baseLineY, getSigBarPaint(si, simgr));
                mRectLinePaint.setColor(si.color);
                canvas.drawRect(left, top, left + barWidth, baseLineY, mRectLinePaint);
                float textOffset = bgRectWidth - barWidth;
                canvas.drawText(String.valueOf(si.prn), center, baseLineY + textOffset + TEXT_OFFSET, mTextPaint);
                String snrStr = String.format("%3.1f", si.snr);
                canvas.drawText(snrStr, center, top - textOffset, mTextPaint);
            }
        }
    }

    private Paint getSigBarPaint(SatelliteInfo info, SatelliteInfoManager manager) {
        if (!manager.isUsedInFix(SatelliteInfoManager.PRN_ANY)) {
            mRectPaint.setColor(getResources().getColor(R.color.bar_used));
            mRectPaint.setStyle(Paint.Style.STROKE);
        } else {
            if (manager.isUsedInFix(info.prn)) {
                mRectPaint.setColor(getResources().getColor(R.color.bar_used));
                mRectPaint.setStyle(Paint.Style.FILL);
            } else {
                mRectPaint.setColor(getResources().getColor(R.color.bar_unused));
                mRectPaint.setStyle(Paint.Style.FILL);
            }
        }
        return mRectPaint;
    }


    private float computeBgRectWidth(int satelliteCount) {
        int divider = 0;
        for (int i = 0; i < DIVIDER_RANKS.length; i++) {
            int d = DIVIDER_RANKS[i];
            if (satelliteCount < d) {
                divider = d;
                break;
            }
        }
        if (divider == 0) {
            divider = satelliteCount;
        }
        float width = getWidth();
        float bgRectWidth = (float) Math.floor(width / divider);
        return bgRectWidth;
    }

}

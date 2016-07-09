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

import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources.NotFoundException;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.GpsStatus.NmeaListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.StrictMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Spinner;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class YgpsActivity extends TabActivity {

    private static final String TAG = "YGPS/Activity";
    private static final String TAG_BG = "EM/YGPS_BG";
    private static final String COMMAND_END = "*";
    private static final String COMMAND_START = "$";
    private static final int LOCATION_MAX_LENGTH = 12;
    private static final String INTENT_ACTION_SCREEN_OFF = "android.intent.action.SCREEN_OFF";
    private static final String SHARED_PREF_KEY_BG = "RunInBG";
    private static final String FIRST_TIME = "first.time";
    private static final String NMEA_LOG_SUFX = ".txt";
    private static final String NMEA_LOG_PREX = "Nmealog";
    private static final int MESSAGE_ARG_1 = 1;
    private static final int MESSAGE_ARG_0 = 0;
    private static final int INPUT_VALUE_MAX = 999;
    private static final int INPUT_VALUE_MIN = 0;
    private static final int ONE_SECOND = 1000;
    private static final int HANDLE_MSG_DELAY = 200;
    private static final int HANDLE_MSG_DELAY_300 = 300;

    private static final boolean NMEALOG_SD = true;
    private static final String NMEALOG_PATH = "/data/misc/nmea_log";
    private static final int COUNT_PRECISION = 500;
    private static final int EXCEED_SECOND = 999;

    private static final int HANDLE_COUNTER = 1000;
    private static final int HANDLE_UPDATE_RESULT = 1001;
    private static final int HANDLE_CLEAR = 1002;
    private static final int HANDLE_CHECK_SATEREPORT = 1003;
    private static final int HANDLE_SET_CURRENT_TIMES = 1030;
    private static final int HANDLE_START_BUTTON_UPDATE = 1040;
    private static final int HANDLE_SET_COUNTDOWN = 1050;
    private static final int HANDLE_SET_MEANTTFF = 1070;
    private static final int HANDLE_EXCEED_PERIOD = 1080;
    private static final int HANDLE_SET_PARAM_RECONNECT = 1090;
    private static final int HANDLE_COMMAND_JAMMINGSCAN = 1101;
    private static final int HANDLE_COMMAND_GETVERSION = 1102;
    private static final int HANDLE_COMMAND_OTHERS = 1103;
    private static final int HANDLE_REMOVE_UPDATE = 1201;
    private static final int HANDLE_REQUEST_UPDATE = 1202;
    private static final int HANDLE_DELETE_DATA = 1203;
    private static final int HANDLE_ENABLE_BUTTON = 1204;

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat(
            "yyyyMMddhhmmss");
    private static final int DIALOG_WAITING_FOR_STOP = 0;
    private static final String GPS_EXTRA_POSITION = "position";
    private static final String GPS_EXTRA_EPHEMERIS = "ephemeris";
    private static final String GPS_EXTRA_TIME = "time";
    private static final String GPS_EXTRA_IONO = "iono";
    private static final String GPS_EXTRA_UTC = "utc";
    private static final String GPS_EXTRA_HEALTH = "health";
    private static final String GPS_EXTRA_ALL = "all";
    private static final String GPS_EXTRA_RTI = "rti";
    private static final String GPS_EXTRA_A1LMANAC = "almanac";
    private static final int YEAR_START = 1900;
    private static final int GRAVITE_Y_OFFSET = 150;
    private static final int RESPONSE_ARRAY_LENGTH = 4;
    private static final int SATE_RATE_TIMEOUT = 3;

    private static final int GPS_TEST_HOT_START = 0;
    private static final int GPS_TEST_WARM_START = 1;
    private static final int GPS_TEST_COLD_START = 2;
    private static final int GPS_TEST_FULL_START = 3;

    private int mTtffValue = 0;
    private int mSatellites = 0;
    private int mTotalTimes = 0;
    private int mCurrentTimes = 0;
    private int mTestInterval = 0;
    private int mTestType = 0;
    private float mMeanTTFF = 0f;

    private boolean mShowLoc = false;
    private boolean mStartNmeaRecord = false;
    private boolean mFirstFix = false;
    private boolean mIsNeed3DFix = false;
    private boolean mIsTestRunning = false;
    private boolean mIsExit = false;

    private boolean mIsRunInBg = false;
    private boolean mShowFirstFixLocate = true;
    private boolean mIsShowVersion = false;
    private int mSateReportTimeOut = 0;

    private ClientSocket mSocketClient = null;

    private SatelLocationView mSatelliteView = null;
    private SatelSignalChartView mSignalView = null;
    private NmeaListenClass mNmeaListener = null;
    private LocationManager mLocationManager = null;
    private YgpsWakeLock mYgpsWakeLock = null;
    private Location mLastLocation = null;
    private Button mBtnColdStart = null;
    private Button mBtnWarmStart = null;
    private Button mBtnHotStart = null;
    private Button mBtnFullStart = null;
    private Button mBtnReStart = null;
    private Button mBtnHotStill = null;
    private Button mBtnNmeaStart = null;
    private Button mBtnNMEAStop = null;
    private Button mBtnNMEADbgDbg = null;
    private Button mBtnNmeaDbgNmea = null;
    private Button mBtnNmeaDbgDbgFile = null;
    private Button mBtnNmeaDbgNmeaDdms = null;
    private Button mBtnNmeaClear = null;
    private Button mBtnNmeaSave = null;
    private Button mBtnGpsTestStart = null;
    private Button mBtnGpsTestStop = null;
    private Button mBtnSuplLog = null;
    private EditText mEtTestTimes = null;
    private CheckBox mCbNeed3DFix = null;
    private EditText mEtTestInterval = null;
    private Spinner mTestSpinner = null;

    private AutoTestThread mAutoTestThread = null;
    private String mProvider = "";
    private String mStatus = "";
    // added by chaozhong @2010.10.12
    private boolean mStopPressedHandling = false;
    private boolean mStartPressedHandling = false;
    // added end
    private TextView mTvNmeaLog = null;
    private TextView mTVNMEAHint = null;
    private FileOutputStream mOutputNMEALog = null;
    // added by Ben Niu @ 2012.03.09 for Jamming Scan
    private Button mBtnGpsHwTest = null;
    private Button mBtnGpsJamming = null;
    private EditText mEtGpsJammingTimes = null;
    // added end
    // added to receive PowerKey pressed
    private IntentFilter mPowerKeyFilter = null;
    private BroadcastReceiver mPowerKeyReceiver = null;
    // added end
    private Toast mPrompt = null;
    private Toast mStatusPrompt = null;
    String[] mType = {"Hot start", "Warm start", "Cold start", "Full start"};
    private FileOutputStream mOutputTestLog = null;
    private SatelliteInfoManager mSatelInfoManager = null;
    private NmeaParser mNmeaParser = null;
    private NmeaParser.NmeaUpdateViewListener mNmeaUpdateListener = null;
    private boolean mRestarted = false;
    private boolean mNmeaFixed = false;
    private volatile boolean mIsForceStopGpsTest = false;

    /**
     * Convert Integer array to string with specified length
     *
     * @param array
     *            Integer array
     * @param count
     *            Specified length
     * @return Integer array numbers string
     */
    private String toString(int[] array, int count) {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("(");
        for (int idx = 0; idx < count; idx++) {
            strBuilder.append(Integer.toString(array[idx]));
            strBuilder.append(",");
        }
        strBuilder.append(")");
        return strBuilder.toString();
    }

    /**
     * Convert Float array to string with specified length
     *
     * @param array
     *            Float array
     * @param count
     *            Specified length
     * @return Float array numbers string
     */
    private String toString(float[] array, int count) {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("(");
        for (int idx = 0; idx < count; idx++) {
            strBuilder.append(Float.toString(array[idx]));
            strBuilder.append(",");
        }
        strBuilder.append(")");
        return strBuilder.toString();
    }

    /**
     * Store satellite status
     *
     * @param list
     *            The list contains satellite status
     */
    public void setSatelliteStatus(SatelliteInfoAdapter adapter) {
        Log.v(TAG, "Enter setSatelliteStatus function");
        if (null == adapter) {
            mSatelInfoManager.clearSatelInfos();
        } else {
            mSatelInfoManager.updateSatelliteInfo(adapter);
        }
        Log.v(TAG, mSatelInfoManager.toString());
        mNmeaFixed = mSatelInfoManager.isUsedInFix(SatelliteInfoManager.PRN_ANY);
        if (!mNmeaFixed) {
            clearLayout();
            mStatus = getString(R.string.gps_status_unavailable);
        } else {
            mStatus = getString(R.string.gps_status_available);
        }
        TextView tvStatus = (TextView) findViewById(R.id.tv_status);
        tvStatus.setText(mStatus);
        Log.d(TAG, "setSatelliteStatus: status:" + mStatus);

        mSatelliteView.requestUpdate(mSatelInfoManager);
        mSignalView.requestUpdate(mSatelInfoManager);
    }

    /**
     * Component initial
     */
    private void setLayout() {
        mSatelliteView = (SatelLocationView) findViewById(R.id.sky_view);
        mSignalView = (SatelSignalChartView) findViewById(R.id.signal_view);

        mBtnColdStart = (Button) findViewById(R.id.btn_cold);
        mBtnColdStart.setOnClickListener(mBtnClickListener);
        mBtnWarmStart = (Button) findViewById(R.id.btn_warm);
        mBtnWarmStart.setOnClickListener(mBtnClickListener);
        mBtnHotStart = (Button) findViewById(R.id.btn_hot);
        mBtnHotStart.setOnClickListener(mBtnClickListener);
        mBtnFullStart = (Button) findViewById(R.id.btn_full);
        mBtnFullStart.setOnClickListener(mBtnClickListener);
        mBtnReStart = (Button) findViewById(R.id.btn_restart);
        mBtnReStart.setOnClickListener(mBtnClickListener);
        mBtnHotStill = (Button) findViewById(R.id.btn_hotstill);
        mBtnHotStill.setOnClickListener(mBtnClickListener);
        mBtnSuplLog = (Button) findViewById(R.id.btn_supllog);
        mBtnSuplLog.setOnClickListener(mBtnClickListener);
        mTvNmeaLog = (TextView) findViewById(R.id.tv_nmea_log);
        mTVNMEAHint = (TextView) findViewById(R.id.tv_nmea_hint);
        mBtnNmeaStart = (Button) findViewById(R.id.btn_nmea_start);
        mBtnNmeaStart.setOnClickListener(mBtnClickListener);
        mBtnNMEAStop = (Button) findViewById(R.id.btn_nmea_stop);
        mBtnNMEAStop.setOnClickListener(mBtnClickListener);
        mBtnNMEAStop.setEnabled(false);
        mBtnNMEADbgDbg = (Button) findViewById(R.id.btn_nmea_dbg_dbg);
        mBtnNMEADbgDbg.setOnClickListener(mBtnClickListener);
        mBtnNmeaDbgNmea = (Button) findViewById(R.id.btn_nmea_dbg_nmea);
        mBtnNmeaDbgNmea.setOnClickListener(mBtnClickListener);
        mBtnNmeaDbgDbgFile = (Button) findViewById(R.id.btn_nmea_dbg_dbg_file);
        mBtnNmeaDbgDbgFile.setOnClickListener(mBtnClickListener);
        mBtnNmeaDbgNmeaDdms = (Button) findViewById(R.id.btn_nmea_dbg_nmea_file);
        mBtnNmeaDbgNmeaDdms.setOnClickListener(mBtnClickListener);
        mBtnNmeaClear = (Button) findViewById(R.id.btn_nmea_clear);
        mBtnNmeaClear.setOnClickListener(mBtnClickListener);
        mBtnNmeaSave = (Button) findViewById(R.id.btn_nmea_save);
        mBtnNmeaSave.setOnClickListener(mBtnClickListener);
        mBtnGpsTestStart = (Button) findViewById(R.id.btn_gps_test_start);
        mBtnGpsTestStart.setOnClickListener(mBtnClickListener);
        mBtnGpsTestStop = (Button) findViewById(R.id.btn_gps_test_stop);
        mBtnGpsTestStop.setOnClickListener(mBtnClickListener);
        mBtnGpsTestStop.setEnabled(false);
        mEtTestTimes = (EditText) findViewById(R.id.et_gps_test_times);
        mCbNeed3DFix = (CheckBox) findViewById(R.id.cb_need_3d_fix);
        mEtTestInterval = (EditText) findViewById(R.id.et_gps_test_interval);
        mBtnGpsHwTest = (Button) findViewById(R.id.btn_gps_hw_test);
        mBtnGpsHwTest.setOnClickListener(mBtnClickListener);
        mBtnGpsJamming = (Button) findViewById(R.id.btn_gps_jamming_scan);
        mBtnGpsJamming.setOnClickListener(mBtnClickListener);
        mEtGpsJammingTimes = (EditText) findViewById(R.id.et_gps_jamming_times);
        mEtGpsJammingTimes.setText(getString(R.string.jamming_scan_times));
        mEtGpsJammingTimes.setSelection(mEtGpsJammingTimes.getText().length());
        mBtnNMEADbgDbg.setVisibility(View.GONE);
        mBtnNmeaDbgNmea.setVisibility(View.GONE);
        mBtnNmeaSave.setVisibility(View.GONE);
        mBtnNmeaClear.setVisibility(View.GONE);
        mTestSpinner = (Spinner) findViewById(R.id.start_type_Spinner);
        ArrayAdapter<String> TestAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item);
        TestAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for (int i = 0; i < mType.length; i++) {
            TestAdapter.add(mType[i]);
        }
        mTestSpinner.setAdapter(TestAdapter);
        mTestSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> arg0, View arg1,
                    int arg2, long arg3) {
                mTestType = arg2;
                Log.i(TAG, "The mTestType is : " + arg2);
            }

            public void onNothingSelected(AdapterView<?> arg0) {
                Log.v(TAG, "onNothingSelected");
            }
        });
        // Update buttons status
        String ss = GpsMnlSetting.getMnlProp(
                GpsMnlSetting.KEY_DEBUG_DBG2SOCKET, GpsMnlSetting.PROP_VALUE_0);
        if (ss.equals(GpsMnlSetting.PROP_VALUE_0)) {
            mBtnNMEADbgDbg.setText(R.string.btn_name_dbg2socket_enable);
        } else {
            mBtnNMEADbgDbg.setText(R.string.btn_name_dbg2socket_disable);
        }
        ss = GpsMnlSetting.getMnlProp(GpsMnlSetting.KEY_DEBUG_NMEA2SOCKET,
                GpsMnlSetting.PROP_VALUE_0);
        if (ss.equals(GpsMnlSetting.PROP_VALUE_0)) {
            mBtnNmeaDbgNmea.setText(R.string.btn_name_nmea2socket_enable);
        } else {
            mBtnNmeaDbgNmea.setText((R.string.btn_name_nmea2socket_disable));
        }
        ss = GpsMnlSetting.getMnlProp(GpsMnlSetting.KEY_DEBUG_DBG2FILE,
                GpsMnlSetting.PROP_VALUE_0);
        if (ss.equals(GpsMnlSetting.PROP_VALUE_0)) {
            mBtnNmeaDbgDbgFile.setText((R.string.btn_name_dbg2file_enable));
        } else {
            mBtnNmeaDbgDbgFile.setText((R.string.btn_name_dbg2file_disable));
        }
        ss = GpsMnlSetting.getMnlProp(GpsMnlSetting.KEY_DEBUG_DEBUG_NMEA,
                GpsMnlSetting.PROP_VALUE_1);
        if (ss.equals(GpsMnlSetting.PROP_VALUE_1)) {
            mBtnNmeaDbgNmeaDdms.setText((R.string.btn_name_dbg2ddms_disable));
        } else {
            mBtnNmeaDbgNmeaDdms.setText((R.string.btn_name_dbg2ddms_enable));
        }
        ss = GpsMnlSetting.getMnlProp(GpsMnlSetting.KEY_BEE_ENABLED,
                GpsMnlSetting.PROP_VALUE_1);
        if (ss.equals(GpsMnlSetting.PROP_VALUE_1)) {
            mBtnHotStill.setText((R.string.btn_name_hotstill_disable));
        } else {
            mBtnHotStill.setText((R.string.btn_name_hotstill_enable));
        }
        ss = GpsMnlSetting.getMnlProp(GpsMnlSetting.KEY_SUPLLOG_ENABLED,
                GpsMnlSetting.PROP_VALUE_0);
        if (ss.equals(GpsMnlSetting.PROP_VALUE_1)) {
            mBtnSuplLog.setText((R.string.btn_name_supllog_disable));
        } else {
            mBtnSuplLog.setText((R.string.btn_name_supllog_enable));
        }
        boolean bClearHwTest = false;
        final SharedPreferences preferences = this.getSharedPreferences(
                FIRST_TIME, android.content.Context.MODE_PRIVATE);
        ss = preferences.getString(FIRST_TIME, null);
        if (ss != null) {
            if (ss.equals(GpsMnlSetting.PROP_VALUE_1)) {
                preferences.edit().putString(FIRST_TIME,
                        GpsMnlSetting.PROP_VALUE_2).commit();
            } else if (ss.equals(GpsMnlSetting.PROP_VALUE_2)) {
                bClearHwTest = true;
            }
        }
        ss = GpsMnlSetting.getMnlProp(GpsMnlSetting.KEY_TEST_MODE,
                GpsMnlSetting.PROP_VALUE_0);
        if (ss.equals(GpsMnlSetting.PROP_VALUE_0)) {
            mBtnGpsHwTest.setText((R.string.btn_name_dbg2gpsdoctor_enable));
        } else {
            if (bClearHwTest) {
                GpsMnlSetting.setMnlProp(GpsMnlSetting.KEY_TEST_MODE,
                        GpsMnlSetting.PROP_VALUE_0);
                mBtnGpsHwTest.setText((R.string.btn_name_dbg2gpsdoctor_enable));
            } else {
                mBtnGpsHwTest
                        .setText((R.string.btn_name_dbg2gpsdoctor_disable));
            }
        }
    }

    /**
     * Clear location information
     */
    private void clearLayout() {
        // clear all information in layout
        ((TextView) findViewById(R.id.tv_date)).setText(R.string.empty);
        ((TextView) findViewById(R.id.tv_time)).setText(R.string.empty);
        ((TextView) findViewById(R.id.tv_latitude)).setText(R.string.empty);
        ((TextView) findViewById(R.id.tv_longitude)).setText(R.string.empty);
        ((TextView) findViewById(R.id.tv_altitude)).setText(R.string.empty);
        ((TextView) findViewById(R.id.tv_accuracy)).setText(R.string.empty);
        ((TextView) findViewById(R.id.tv_bearing)).setText(R.string.empty);
        ((TextView) findViewById(R.id.tv_speed)).setText(R.string.empty);
        ((TextView) findViewById(R.id.tv_distance)).setText(R.string.empty);
        // ((TextView) findViewById(R.id.tv_provider)).setText(R.string.empty);
        // ((TextView) findViewById(R.id.tv_status)).setText(R.string.empty);
        if (mShowFirstFixLocate) {
            ((TextView) findViewById(R.id.first_longtitude_text))
                    .setText(R.string.empty);
            ((TextView) findViewById(R.id.first_latitude_text))
                    .setText(R.string.empty);
        }
    }

    /**
     * Create file to record nmea log
     *
     * @return True if create file success, or false
     */
    private boolean createFileForSavingNMEALog() {
        Log.v(TAG, "Enter startSavingNMEALog function");
        if (NMEALOG_SD) {
            if (!(Environment.getExternalStorageState()
                    .equals(Environment.MEDIA_MOUNTED))) {
                Log.v(TAG, "saveNMEALog function: No SD card");
                Toast.makeText(this, R.string.no_sdcard, Toast.LENGTH_LONG)
                        .show();
                return false;
            }
        }

        String strTime = DATE_FORMAT
                .format(new Date(System.currentTimeMillis()));
        File file = null;
        if (NMEALOG_SD) {
            file = new File(Environment.getExternalStorageDirectory(),
                    NMEA_LOG_PREX + strTime + NMEA_LOG_SUFX);
        } else {
            File nmeaPath = new File(NMEALOG_PATH);
            if (!nmeaPath.exists()) {
                nmeaPath.mkdirs();
            }
            file = new File(nmeaPath, NMEA_LOG_PREX + strTime + NMEA_LOG_SUFX);
        }
        if (file != null) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                Log.w(TAG, "create new file failed!");
                Toast.makeText(this, R.string.toast_create_file_failed,
                        Toast.LENGTH_LONG).show();
                return false;
            }
        }
        try {
            mOutputNMEALog = new FileOutputStream(file);
        } catch (FileNotFoundException e1) {
            Log.w(TAG, "output stream FileNotFoundException: "
                    + e1.getMessage());
            return false;
        }
        // set nmea hint
        mTVNMEAHint.setText((R.string.nmea_hint));
        return true;
    }

    /**
     * Record nmea log to output file
     *
     * @param nmea
     *            The nmea log to save
     */
    private void saveNMEALog(String nmea) {
        boolean bSaved = true;
        try {
            mOutputNMEALog.write(nmea.getBytes(), 0, nmea.getBytes().length);
            mOutputNMEALog.flush();
        } catch (IOException e) {
            bSaved = false;
            Log.v(TAG, "write NMEA log to file failed!");
        } finally {
            if (!bSaved) {
                finishSavingNMEALog();
                Toast.makeText(this, "Please check your SD card",
                        Toast.LENGTH_LONG).show();
            }
        }

    }

    /**
     * Finish record nmea log
     */
    private void finishSavingNMEALog() {
        try {
            mStartNmeaRecord = false;
            mBtnNMEAStop.setEnabled(false);
            mBtnNmeaStart.setEnabled(true);

            mTVNMEAHint.setText(R.string.empty);
            mTvNmeaLog.setText(R.string.empty);

            mOutputNMEALog.close();
            mOutputNMEALog = null;
            Toast.makeText(
                    this,
                    String.format(getString(R.string.toast_nmealog_save_at),
                            NMEALOG_SD ? Environment
                                    .getExternalStorageDirectory()
                                    .getAbsolutePath() : NMEALOG_PATH),
                    Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.w(TAG, "Close file failed!");
        }
    }

    /**
     * Save NMEA log to file
     */
    private void saveNMEALog() {
        if (NMEALOG_SD) {
            if (Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {
                String strTime = DATE_FORMAT.format(new Date(System
                        .currentTimeMillis()));
                File file = new File(Environment.getExternalStorageDirectory(),
                        NMEA_LOG_PREX + strTime + NMEA_LOG_SUFX);
                FileOutputStream fileOutputStream = null;
                boolean flag = true;
                try {
                    if (!file.createNewFile()) {
                        Toast.makeText(this, R.string.toast_create_file_failed,
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    fileOutputStream = new FileOutputStream(file);
                    String nmea = ((TextView) findViewById(R.id.tv_nmea_log))
                            .getText().toString();
                    if (0 == nmea.getBytes().length) {
                        Toast.makeText(this, R.string.toast_no_log,
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    fileOutputStream.write(nmea.getBytes(), 0,
                            nmea.getBytes().length);
                    fileOutputStream.flush();
                    fileOutputStream.close();
                } catch (NotFoundException e) {
                    Log.v(TAG, "Save nmealog NotFoundException: "
                            + e.getMessage());
                    flag = false;
                } catch (IOException e) {
                    Log.v(TAG, "Save nmealog IOException: " + e.getMessage());
                    flag = false;
                } finally {
                    if (null != fileOutputStream) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e) {
                            Log.v(TAG, "Save nmealog exception in finally: "
                                    + e.getMessage());
                            flag = false;
                        }
                    }
                }
                if (flag) {
                    Log.v(TAG, "Save Nmealog to file Finished");
                    Toast.makeText(
                                    this,
                                    String
                                            .format(
                                                    getString(R.string.toast_save_log_succeed_to),
                                                    Environment
                                                            .getExternalStorageDirectory()
                                                            .getAbsolutePath()),
                                    Toast.LENGTH_LONG).show();
                } else {
                    Log.w(TAG, "Save Nmealog Failed");
                    Toast.makeText(this, R.string.toast_save_log_failed,
                            Toast.LENGTH_LONG).show();
                }
            } else {
                Log.v(TAG, "saveNMEALog function: No SD card");
                Toast.makeText(this, (R.string.no_sdcard), Toast.LENGTH_LONG)
                        .show();
            }
        } else {
            String strTime = DATE_FORMAT.format(new Date(System
                    .currentTimeMillis()));
            File nmeaPath = new File(NMEALOG_PATH);
            if (!nmeaPath.exists()) {
                nmeaPath.mkdirs();
            }
            File file = new File(nmeaPath, NMEA_LOG_PREX + strTime
                    + NMEA_LOG_SUFX);
            if (file != null) {
                FileOutputStream outs = null;
                boolean flag = true;
                try {
                    file.createNewFile();
                    outs = new FileOutputStream(file);
                    String nmea = ((TextView) findViewById(R.id.tv_nmea_log))
                            .getText().toString();
                    if (0 == nmea.getBytes().length) {
                        Toast.makeText(this, R.string.toast_no_log,
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    outs.write(nmea.getBytes(), 0, nmea.getBytes().length);
                    outs.flush();
                } catch (IOException e) {
                    Log.v(TAG, "Save nmealog IOException: " + e.getMessage());
                    flag = false;
                } finally {
                    if (null != outs) {
                        try {
                            outs.close();
                        } catch (IOException e) {
                            Log.v(TAG, "Save nmealog exception in finally: "
                                    + e.getMessage());
                            flag = false;
                        }
                    }
                }
                if (flag) {
                    Log.v(TAG, "Save Nmealog to file Finished");
                    Toast
                            .makeText(
                                    this,
                                    String
                                            .format(
                                                    getString(R.string.toast_save_log_succeed_to),
                                                    NMEALOG_PATH),
                                    Toast.LENGTH_LONG).show();
                } else {
                    Log.w(TAG, "Save NmeaLog failed!");
                    Toast.makeText(this, R.string.toast_save_log_failed,
                            Toast.LENGTH_LONG).show();
                }
            }
        }

    }

    // added by chaozhong @2010.10.12 handle the start/stop state in uniform way
    // when start button is pressed, views excepts mBtnGPSTestStop must be
    // disabled
    /**
     * Refresh GPS auto test UI to initial status
     */
    private void setViewToStartState() {
        mBtnGpsTestStart.setFocusableInTouchMode(false);
        mBtnGpsTestStart.refreshDrawableState();
        mBtnGpsTestStart.setEnabled(false);
        if (null != mEtTestTimes) {
            mEtTestTimes.setFocusable(false);
            mEtTestTimes.refreshDrawableState();
            mEtTestTimes.setEnabled(false);
        }
        if (null != mCbNeed3DFix) {
            mCbNeed3DFix.setFocusable(false);
            mCbNeed3DFix.refreshDrawableState();
            mCbNeed3DFix.setEnabled(false);
        }
        if (null != mEtTestInterval) {
            mEtTestInterval.setFocusable(false);
            mEtTestInterval.refreshDrawableState();
            mEtTestInterval.setEnabled(false);
        }
        mBtnGpsTestStop.setEnabled(true);
        clearLayout();
    }

    // added by chaozhong @2010.10.12 handle the start/stop state in uniform way
    // when start button is pressed, views excepts mBtnGPSTestStop must be
    // disabled
    /**
     * Refresh GPS auto test UI to running status
     */
    private void setViewToStopState() {

        mBtnGpsTestStop.setEnabled(false);
        if (null != mEtTestTimes) {
            mEtTestTimes.setEnabled(true);
            mEtTestTimes.setFocusableInTouchMode(true);
            mEtTestTimes.refreshDrawableState();
        }
        if (null != mCbNeed3DFix) {
            mCbNeed3DFix.setEnabled(true);
            mCbNeed3DFix.refreshDrawableState();
        }
        if (null != mEtTestInterval) {
            mEtTestInterval.setEnabled(true);
            mEtTestInterval.setFocusableInTouchMode(true);
            mEtTestInterval.refreshDrawableState();
        }
        mBtnGpsTestStart.setEnabled(true);
        mBtnGpsTestStart.setFocusableInTouchMode(false);
        mBtnGpsTestStart.refreshDrawableState();
    }

    /**
     * Start GPS auto test
     */
    private void startGPSAutoTest() {
        // check Times
        if (null != mEtTestTimes) {
            if (0 == mEtTestTimes.getText().length()) {
                Toast.makeText(YgpsActivity.this, R.string.toast_input_times,
                        Toast.LENGTH_LONG).show();
                mBtnGpsTestStart.setEnabled(true);
                return;
            } else {
                Integer nTimes = Integer.valueOf(mEtTestTimes.getText()
                        .toString());
                if (nTimes.intValue() < INPUT_VALUE_MIN
                        || nTimes.intValue() > INPUT_VALUE_MAX) {
                    Toast.makeText(YgpsActivity.this,
                            R.string.toast_input_range, Toast.LENGTH_LONG)
                            .show();
                    mBtnGpsTestStart.setEnabled(true);
                    return;
                }
                mTotalTimes = nTimes.intValue();
            }
        }

        // check Interval
        if (null != mEtTestInterval) {
            if (0 == mEtTestInterval.getText().length()) {
                Toast.makeText(YgpsActivity.this,
                        R.string.toast_input_interval, Toast.LENGTH_LONG)
                        .show();
                mBtnGpsTestStart.setEnabled(true);
                return;
            } else {
                Integer nInterval = Integer.valueOf(mEtTestInterval.getText()
                        .toString());
                if (nInterval.intValue() < INPUT_VALUE_MIN
                        || nInterval.intValue() > INPUT_VALUE_MAX) {
                    Toast.makeText(YgpsActivity.this,
                            R.string.toast_input_range, Toast.LENGTH_LONG)
                            .show();
                    mBtnGpsTestStart.setEnabled(true);
                    return;
                }
                mTestInterval = nInterval.intValue();
            }
        }

        // need 3D fix? check it
        if (null != mCbNeed3DFix) {
            mIsNeed3DFix = mCbNeed3DFix.isChecked();
        }
        mIsTestRunning = true;
        mIsForceStopGpsTest = false;
        resetTestView();
        // start test now
        // the next if statement is added by chaozhong @2010.10.12, to prevent
        // start been pressed more times
        if (!mStartPressedHandling) {
            mStartPressedHandling = true;
            setViewToStartState();
            // original code
            mAutoTestThread = new AutoTestThread();
            if (null != mAutoTestThread) {
                if (mIsNeed3DFix) {
                    createFileForSavingAutoTestLog();
                }
                mAutoTestThread.start();
            } else {
                Log.w(TAG, "new matThread failed");
            }

        } else {
            Log.w(TAG, "start button has been pushed.");
            mBtnGpsTestStart.refreshDrawableState();
            mBtnGpsTestStart.setEnabled(false);
        }

    }

    /**
     * Stop GPS auto test
     */
    private void stopGPSAutoTest() {
        resetTestParam();
        // Bundle extras = new Bundle();
        // extras.putBoolean("ephemeris", true);
        // resetParam(extras, false); // do connect when stop test
        setTestParam();
    }

    /**
     * force Stop GPS Auto Test.
     */
    private void forceStopGpsAutoTest() {
        synchronized (this) {
            mIsForceStopGpsTest = true;
        }
        resetTestParam();
        clearRestartMsgs();
        setViewToStopState();
        mStartPressedHandling = false;
        mStopPressedHandling = false;
    }

    /**
     * Reset GPS auto test parameters.
     */
    private void resetTestParam() {
        mIsNeed3DFix = false;
        mTotalTimes = 0;
        mCurrentTimes = 0;
        mTestInterval = 0;
        mMeanTTFF = 0f;
        mIsTestRunning = false;
    }

    /**
     * Reset GPS auto test UI
     */
    private void resetTestView() {
        // ((TextView)YGPSActivity.this.findViewById(R.id.tv_CurrentTimes)).setText("");
        // ((TextView)YGPSActivity.this.findViewById(R.id.tv_Reconnect_Countdown)).setText("");
        ((TextView) YgpsActivity.this.findViewById(R.id.tv_mean_ttff))
                .setText("");
        ((TextView) YgpsActivity.this.findViewById(R.id.tv_last_ttff))
                .setText("");
    }

    /**
     * Calculate mean TTFF value
     *
     * @param n
     *            Test times
     * @return Mean TTFF value
     */
    private float meanTTFF(int n) {
        return (mMeanTTFF * (n - 1) + mTtffValue) / n;
    }

    /**
     * Update GPS auto test UI
     */
    private Handler mAutoTestHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case HANDLE_SET_CURRENT_TIMES:
                ((TextView) YgpsActivity.this
                        .findViewById(R.id.tv_current_times)).setText(Integer
                        .valueOf(msg.arg1).toString());
                break;
            case HANDLE_SET_COUNTDOWN:
                ((TextView) YgpsActivity.this
                        .findViewById(R.id.tv_reconnect_countdown))
                        .setText(Integer.valueOf(msg.arg1).toString());
                break;
            case HANDLE_START_BUTTON_UPDATE:
                mBtnGpsTestStart.setEnabled(MESSAGE_ARG_1 == msg.arg1);
                mBtnGpsTestStop.setEnabled(MESSAGE_ARG_0 == msg.arg1);
                if (msg.arg1 == MESSAGE_ARG_1) {
                    setViewToStopState();
                }
                break;
            case HANDLE_EXCEED_PERIOD:
                Toast.makeText(
                        YgpsActivity.this,
                        String.format(getString(R.string.toast_exceed_time,
                                Integer.valueOf(msg.arg1).toString())),
                        Toast.LENGTH_LONG).show();
                break;
            case HANDLE_SET_MEANTTFF:
                ((TextView) YgpsActivity.this.findViewById(R.id.tv_mean_ttff))
                        .setText(Float.valueOf(mMeanTTFF).toString());
                break;
            case HANDLE_SET_PARAM_RECONNECT:
                Bundle extras = new Bundle();
                extras.putBoolean(GPS_EXTRA_EPHEMERIS, true);
                //resetParam(extras, false);
                enableBtns(false);
                finishSavingAutoTestLog();
                resetParamForRestart(extras);
                if (!mBtnGpsTestStart.isEnabled()) {
                    setStartButtonEnable(true);
                    removeDialog(DIALOG_WAITING_FOR_STOP);
                    mStopPressedHandling = false;
                    mStartPressedHandling = false;
                }

                break;
            default:
                break;
            }
        }
    };

    /**
     * GPS auto test thread
     *
     * @author mtk54046
     *
     */
    private class AutoTestThread extends Thread {
        @Override
        public void run() {
            super.run();
            Looper.prepare();
            // try {
            setStartButtonEnable(false);
            reconnectTest();
            setStartButtonEnable(true);
            interrupt();
            // } catch (Exception e) {
            // e.printStackTrace();
            // }
        }
    }
    private boolean createFileForSavingAutoTestLog() {
        Log.v(TAG, "Enter createFileForSavingAutoTestLog function");
        if (!(Environment.getExternalStorageState()
                    .equals(Environment.MEDIA_MOUNTED))) {
                Log.v(TAG, "saveAutoTestLog function: No SD card");
                Toast.makeText(this, R.string.no_sdcard, Toast.LENGTH_LONG)
                        .show();
                return false;
        }
        String strTime = DATE_FORMAT
                .format(new Date(System.currentTimeMillis()));
        File file = null;
        file = new File(Environment.getExternalStorageDirectory(),
                    "AutoTestLog" + strTime + ".txt");
        if (file != null) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                Log.w(TAG, "create new file failed!");
                Toast.makeText(this, R.string.toast_create_file_failed,
                        Toast.LENGTH_LONG).show();
                return false;
            }
        }
        try {
            mOutputTestLog = new FileOutputStream(file);
        } catch (FileNotFoundException e1) {
            Log.w(TAG, "output stream FileNotFoundException: "
                    + e1.getMessage());
            return false;
        }
        return true;
    }

    private void saveAutoTestLog(String nmea) {
        boolean bSaved = true;
        if (mIsTestRunning == false || mOutputTestLog == null)
            return;
        try {
            mOutputTestLog.write(nmea.getBytes(), 0, nmea.getBytes().length);
            mOutputTestLog.flush();
        } catch (IOException e) {
            bSaved = false;
            Log.v(TAG, "write autotest log to file failed!");
        } finally {
            if (!bSaved) {
                finishSavingAutoTestLog();
                Toast.makeText(this, "Please check your SD card",
                        Toast.LENGTH_LONG).show();
            }
        }

    }
    /**
     * Finish record nmea log
     */
    private void finishSavingAutoTestLog() {
        Log.v(TAG, "finishSavingAutoTestLog");
        if (mOutputTestLog == null) {
            return;
        }
        try {
            mOutputTestLog.close();
            mOutputTestLog = null;
            Toast.makeText(
                    this,
                    String.format(getString(R.string.toast_autotestlog_save_at),
                                    Environment
                                    .getExternalStorageDirectory()
                                    .getAbsolutePath()),
                    Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.w(TAG, "Close file failed!");
        }
    }

    private void waitAutoTestInterval() {
        try {
            if (mTestInterval != 0) {
                for (int i = mTestInterval; i >= 0 && mIsTestRunning; --i) {
                    setCountDown(i);
                    Thread.sleep(1 * ONE_SECOND);

                }
                if (!mIsTestRunning) {
                    setCountDown(0);
                }
            } else {
                Thread.sleep(ONE_SECOND / 2);
            }
        } catch (InterruptedException e) {
            Log.d(TAG, "waitAutoTestInterval InterruptedException: " + e.getMessage());
        }
    }
    private void wait3DFix() {
        boolean bExceed = false;
        Long beginTime = Calendar.getInstance().getTime().getTime()
                / ONE_SECOND;
        for (; mIsTestRunning; ) {
            Long nowTime = Calendar.getInstance().getTime()
                    .getTime()
                    / ONE_SECOND;
            if (mFirstFix) {
                break;
            } else if (nowTime - beginTime > EXCEED_SECOND) {
                bExceed = true;
                showExceedPeriod(EXCEED_SECOND);
                break;
            }
        }
        if (bExceed) {
            //break;
            Log.d(TAG, "wait3DFix , Exceed period");
        }  else {
            try {
                Thread.sleep(ONE_SECOND / 2); // wait ttff and lat,longitude to file.
            } catch (InterruptedException e) {
               Log.w(TAG, "wait3DFix interrupted: " + e.getMessage());
            }
        }
    }
    /**
     * GPS re-connect test
     */
    private void reconnectTest() {
        Bundle extras = new Bundle();

        if (mTestType == GPS_TEST_HOT_START) {
            extras.putBoolean(GPS_EXTRA_RTI, true);
            saveAutoTestLog("Hot Start ");
        } else if (mTestType == GPS_TEST_WARM_START) {
            extras.putBoolean(GPS_EXTRA_EPHEMERIS, true);
            saveAutoTestLog("Warm Start ");
        } else if (mTestType == GPS_TEST_COLD_START) {
            extras.putBoolean(GPS_EXTRA_EPHEMERIS, true);
            extras.putBoolean(GPS_EXTRA_POSITION, true);
            extras.putBoolean(GPS_EXTRA_TIME, true);
            extras.putBoolean(GPS_EXTRA_IONO, true);
            extras.putBoolean(GPS_EXTRA_UTC, true);
            extras.putBoolean(GPS_EXTRA_HEALTH, true);
            saveAutoTestLog("Cold Start ");
        } else if (mTestType == GPS_TEST_FULL_START) {
            extras.putBoolean(GPS_EXTRA_ALL, true);
            saveAutoTestLog("Full Start ");
        }

        saveAutoTestLog("Total times: " + Integer.toString(mTotalTimes));

        try {
            for (int i = 1; i <= mTotalTimes && mIsTestRunning; ++i) {
                saveAutoTestLog("\n" + Integer.toString(i) + " ");
                mCurrentTimes = i;
                Log.v(TAG, "reconnectTest function: "
                        + Integer.valueOf(mCurrentTimes).toString());
                setCurrentTimes(i);
                //mShowFirstFixLocate = true;
                //
                //resetParam(extras, true);
                synchronized (YgpsActivity.this) {
                    if (mIsForceStopGpsTest) {
                        return;
                    }
                    resetParamForAutoTest(extras);
                }
                waitAutoTestInterval();
                if (mIsNeed3DFix) {
                    wait3DFix();
                } else {
                    Thread.sleep(2 * ONE_SECOND);
                }
            }
            Thread.sleep(ONE_SECOND);
            synchronized (YgpsActivity.this) {
                if (mIsForceStopGpsTest) {
                    return;
                }
                stopGPSAutoTest();
            }
        } catch (InterruptedException e) {
            Log.w(TAG, "GPS auto test thread interrupted: " + e.getMessage());
            Toast.makeText(YgpsActivity.this, R.string.toast_test_interrupted,
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Set test parameters
     */
    private void setTestParam() {
        Message msg = mAutoTestHandler
                .obtainMessage(YgpsActivity.HANDLE_SET_PARAM_RECONNECT);
        mAutoTestHandler.sendMessage(msg);
    }

    /**
     * Refresh auto test UI
     *
     * @param bEnable
     *            Auto test start or not
     */
    private void setStartButtonEnable(boolean bEnable) {
        Message msg = mAutoTestHandler
                .obtainMessage(YgpsActivity.HANDLE_START_BUTTON_UPDATE);
        msg.arg1 = bEnable ? MESSAGE_ARG_1 : MESSAGE_ARG_0;
        mAutoTestHandler.sendMessage(msg);
    }

    /**
     * Update current test time
     *
     * @param nTimes
     *            Current test time
     */
    private void setCurrentTimes(int nTimes) {
        Message msg = mAutoTestHandler
                .obtainMessage(YgpsActivity.HANDLE_SET_CURRENT_TIMES);
        msg.arg1 = nTimes;
        mAutoTestHandler.sendMessage(msg);
    }

    /**
     * Update test time count down
     *
     * @param num
     *            Count down number
     */
    private void setCountDown(int num) {
        Message msg = mAutoTestHandler
                .obtainMessage(YgpsActivity.HANDLE_SET_COUNTDOWN);
        msg.arg1 = num;
        mAutoTestHandler.sendMessage(msg);
    }

    /**
     * Show exceed period
     *
     * @param period
     *            Time period
     */
    private void showExceedPeriod(int period) {
        Message msg = mAutoTestHandler
                .obtainMessage(YgpsActivity.HANDLE_EXCEED_PERIOD);
        msg.arg1 = period;
        mAutoTestHandler.sendMessage(msg);
    }

    private long mLastTimestamp = -1;

    /**
     * NmeaListener implementation, to receive NMEA log
     *
     * @author mtk54046
     *
     */
    public class NmeaListenClass implements NmeaListener {

        @Override
        public void onNmeaReceived(long timestamp, String nmea) {
            if (!mIsShowVersion) {
                if (timestamp - mLastTimestamp > ONE_SECOND) {
                    showVersion();
                    mLastTimestamp = timestamp;
                }
            }
            if (mStartNmeaRecord) {
                saveNMEALog(nmea);
                mTvNmeaLog.setText(nmea);
            }
            NmeaParser.getNMEAParser().parse(nmea);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "Enter onCreate  function of Main Activity");
        super.onCreate(savedInstanceState);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
        // .detectDiskReads()
                // .detectDiskWrites()
                .detectNetwork() // or .detectAll() for all detectable problems
                // .penaltyLog()
                .build());
        // StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
        // .detectLeakedSqlLiteObjects()
        // .detectLeakedClosableObjects()
        // .penaltyLog()
        // .penaltyDeath()
        // .build());
        TabHost tabHost = getTabHost();
        LayoutInflater.from(this).inflate(R.layout.layout_tabs,
                tabHost.getTabContentView(), true);
        // tab1
        tabHost.addTab(tabHost.newTabSpec(this.getString(R.string.satellites))
                .setIndicator(this.getString(R.string.satellites)).setContent(
                        R.id.layout_satellites));

        // tab2
        tabHost.addTab(tabHost.newTabSpec(this.getString(R.string.information))
                .setIndicator(this.getString(R.string.information)).setContent(
                        R.id.layout_info));

        // tab3
        tabHost.addTab(tabHost.newTabSpec(this.getString(R.string.nmea_log))
                .setIndicator(this.getString(R.string.nmea_log)).setContent(
                        R.id.layout_nmea));

        // tab4
        tabHost.addTab(tabHost.newTabSpec(this.getString(R.string.gps_test))
                .setIndicator(this.getString(R.string.gps_test)).setContent(
                        R.id.layout_auto_test));

        tabHost.setOnTabChangedListener(new OnTabChangeListener() {
            public void onTabChanged(String tabId) {
                Log.v(TAG, "Select: " + tabId);
            }
        });
        setLayout();
        //Intent it = new Intent(YgpsService.SERVICE_START_ACTION);
        //startService(it);
        //Log.v(TAG, "START service");
        mYgpsWakeLock = new YgpsWakeLock();
        mYgpsWakeLock.acquireScreenWakeLock(this);
        mYgpsWakeLock.acquireCpuWakeLock(this);
        mNmeaListener = new NmeaListenClass();
        try {
            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (mLocationManager != null) {
                mLocationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, 0, 0, mLocListener);
                mLocationManager.addGpsStatusListener(mGpsListener);
                mLocationManager.addNmeaListener(mNmeaListener);
                if (mLocationManager
                        .isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    mProvider = String.format(getString(
                            R.string.provider_status_enabled,
                            LocationManager.GPS_PROVIDER));
                } else {
                    mProvider = String.format(getString(
                            R.string.provider_status_disabled,
                            LocationManager.GPS_PROVIDER));
                }
                mStatus = getString(R.string.gps_status_unknown);
            } else {
                Log.w(TAG, "new mLocationManager failed");
            }
        } catch (SecurityException e) {
            Toast.makeText(this, "security exception", Toast.LENGTH_LONG)
                    .show();
            Log.w(TAG, "Exception: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "Exception: " + e.getMessage());
        }
        mHandler.sendEmptyMessage(HANDLE_COUNTER);
        final SharedPreferences preferences = this.getSharedPreferences(
                SHARED_PREF_KEY_BG, android.content.Context.MODE_PRIVATE);
        if (preferences.getBoolean(SHARED_PREF_KEY_BG, false)) {
            mIsRunInBg = true;
        } else {
            mIsRunInBg = false;
        }
        mShowFirstFixLocate = true;
        mPowerKeyFilter = new IntentFilter(INTENT_ACTION_SCREEN_OFF);
        mPowerKeyReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                Log.v(TAG, "onReceive, receive SCREEN_OFF event");
                // finish();
            }
        };
        registerReceiver(mPowerKeyReceiver, mPowerKeyFilter);
        Log.v(TAG, "registerReceiver powerKeyReceiver");
        mSocketClient = new ClientSocket(this);
        mHandler.sendEmptyMessage(HANDLE_CHECK_SATEREPORT);
        mSatelInfoManager = new SatelliteInfoManager();
        if (mNmeaParser == null) {
            mNmeaParser = NmeaParser.getNMEAParser();
        }
        mNmeaUpdateListener = new NmeaParser.NmeaUpdateViewListener() {
            @Override
            public void onViewupdateNotify() {
                Log.d(TAG, "NmeaParser onViewupdateNotify");
                mSateReportTimeOut = 0;
                setSatelliteStatus(new NmeaSatelliteAdapter(mNmeaParser.getSatelliteList()));
            }
        };
        mNmeaParser.addSVUpdateListener(mNmeaUpdateListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean supRetVal = super.onCreateOptionsMenu(menu);
        // menu.add(0, 0, 0, getString(R.string.menu_copyright));

        if (mShowLoc) {
            menu.add(0, 1, 0, R.string.menu_hideloc);
        } else {
            menu.add(0, 1, 0, R.string.menu_showloc);
        }

        final SharedPreferences preferences = this.getSharedPreferences(
                SHARED_PREF_KEY_BG, android.content.Context.MODE_PRIVATE);
        if (preferences.getBoolean(SHARED_PREF_KEY_BG, false)) {
            menu.add(0, 2, 0, R.string.menu_run_bg_disable);
        } else {
            menu.add(0, 2, 0, R.string.menu_run_bg_enable);
        }
        return supRetVal;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case 0:
            startActivity(new Intent(this, CopyrightInfo.class));
            return true;

        case 1:
            if (mShowLoc) {
                mShowLoc = false;
                if (mPrompt != null) {
                    mPrompt.cancel();
                }
                item.setTitle(R.string.menu_showloc);
            } else {
                mShowLoc = true;
                item.setTitle(R.string.menu_hideloc);
            }
            return true;

        case 2:
            final SharedPreferences preferences = this.getSharedPreferences(
                    SHARED_PREF_KEY_BG, android.content.Context.MODE_PRIVATE);
            if (preferences.getBoolean(SHARED_PREF_KEY_BG, false)) {
                item.setTitle(R.string.menu_run_bg_enable);
                preferences.edit().putBoolean(SHARED_PREF_KEY_BG, false)
                        .commit();
                Log.v(TAG_BG, "now should *not* be in bg.");
            } else {
                item.setTitle(R.string.menu_run_bg_disable);
                preferences.edit().putBoolean(SHARED_PREF_KEY_BG, true)
                        .commit();
                Log.v(TAG_BG, "now should be in bg.");
            }
            return true;
        default:
            break;
        }
        return false;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        ProgressDialog dialog = null;
        if (DIALOG_WAITING_FOR_STOP == id) {
            dialog = new ProgressDialog(this);
            dialog.setTitle(R.string.dialog_title_stop);
            dialog.setMessage(getString(R.string.dialog_message_stop));
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setCancelable(false);
            dialog.setProgress(0);
        } else {
            dialog = new ProgressDialog(this);
            dialog.setTitle(R.string.dialog_title_error);
            dialog.setMessage(getString(R.string.dialog_message_error));
        }
        return dialog;
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.v(TAG, "Enter onPause function");
        // Log.v(TAG_BG, "mbRunInBG " + mbRunInBG);
        // if (!mbRunInBG) {
        // mLocationManager.removeUpdates(mLocListener);
        // mLocationManager.removeGpsStatusListener(mGpsListener);
        // }
        // mLocationManager.removeUpdates(mLocListener);
        // mLocationManager.removeGpsStatusListener(mGpsListener);
        // Toast.makeText(this, "onPause", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG, "Enter onResume function");
        TextView tvProvider = (TextView) findViewById(R.id.tv_provider);
        tvProvider.setText(mProvider);
        TextView tvStatus = (TextView) findViewById(R.id.tv_status);
        tvStatus.setText(mStatus);
    }

    /**
     * Show GPS version
     */
    private void showVersion() {
        Log.v(TAG, "Enter show version");
        if (mIsExit) {
            return;
        }
        TextView tvChipVersion = (TextView) findViewById(R.id.tv_chip_version);
        // TextView txt_mnl_version = (TextView)
        // findViewById(R.id.tv_mnl_version);
        tvChipVersion.setText(GpsMnlSetting
                .getChipVersion(getString(R.string.gps_status_unknown)));
        // if (null != txt_mnl_version) {
        // txt_mnl_version.setText(FWVersion.getMNLVersion());
        // } else {
        // Log.v(TAG, "txt_mnl_version is null");
        // }
        sendCommand("PMTK605");

        // update clock type/buffer
        TextView tvClockType = (TextView) findViewById(R.id.tv_clock_type);
        TextView tvClockBuffer = (TextView) findViewById(R.id.tv_clock_buffer);
        String ss = GpsMnlSetting.getClockProp("unknown");
        if (ss.length() == 2) {
            if ("ff".equals(ss)) {
                tvClockType.setText("error");
                tvClockBuffer.setText("error");
            } else {
                char clockBuffer = ss.charAt(0);
                if (clockBuffer >= '1' && clockBuffer <= '4') {
                    tvClockBuffer.setText(String.valueOf(clockBuffer));
                } else if (clockBuffer == '0') {
                    tvClockBuffer.setText("2");
                } else if (clockBuffer == '9') {
                    tvClockBuffer.setText("9");
                }
                char clockType = ss.charAt(1);
                if (clockType == '0') {
                    tvClockType.setText("TCXO");
                } else if (clockType == '1') {
                    tvClockType.setText("Co-clock");
                }
            }
        }
    }

    private void removeNmeaParser() {
        Log.d(TAG, "removeNmeaParser()");
        mNmeaParser.removeSVUpdateListener(mNmeaUpdateListener);
        mNmeaParser.clearSatelliteList();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.v(TAG, "Enter onStop function");
        Log.v(TAG_BG, "mbRunInBG " + mIsRunInBg);
        if (!mIsRunInBg) {
            forceStopGpsAutoTest();
            mLocationManager.removeUpdates(mLocListener);
            mLocationManager.removeGpsStatusListener(mGpsListener);
            mYgpsWakeLock.releaseCpuWakeLock();
            removeNmeaParser();
        }
        mYgpsWakeLock.releaseScreenWakeLock();
        if (mPrompt != null) {
            mPrompt.cancel();
        }
        if (mStatusPrompt != null) {
            mStatusPrompt.cancel();
            // Toast.makeText(this, "onStop", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onRestart() {
        Log.v(TAG, "Enter onRestart function");
        Log.v(TAG_BG, "mbRunInBG " + mIsRunInBg);
        if (!mIsRunInBg) {
            mFirstFix = false;
            if (mLocationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                mProvider = String.format(getString(
                        R.string.provider_status_enabled,
                        LocationManager.GPS_PROVIDER));
            } else {
                mProvider = String.format(getString(
                        R.string.provider_status_disabled,
                        LocationManager.GPS_PROVIDER));
            }
            mStatus = getString(R.string.gps_status_unknown);
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 0, 0, mLocListener);
            mLocationManager.addGpsStatusListener(mGpsListener);
            mNmeaParser.addSVUpdateListener(mNmeaUpdateListener);
        }
        // TextView first_longitude = (TextView)
        // findViewById(R.id.first_longtitude_text);
        // if (first_longitude != null) {
        // first_longitude.setText("");
        // }
        // TextView first_latitude = (TextView)
        // findViewById(R.id.first_latitude_text);
        // if (first_latitude != null) {
        // first_latitude.setText("");
        // }
        // mYGPSWakeLock = new YGPSWakeLock();
        if (null != mYgpsWakeLock) {
            mYgpsWakeLock.acquireScreenWakeLock(this);
            mYgpsWakeLock.acquireCpuWakeLock(this);
        } else {
            Log.d(TAG, "mYGPSWakeLock is null");
        }
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        Log.v(TAG, "enter onDestroy function");
        forceStopGpsAutoTest();
        mLocationManager.removeUpdates(mLocListener);
        mLocationManager.removeGpsStatusListener(mGpsListener);
        mLocationManager.removeNmeaListener(mNmeaListener);
        removeNmeaParser();
        mHandler.removeMessages(HANDLE_UPDATE_RESULT);
        mHandler.removeMessages(HANDLE_COUNTER);
        mHandler.removeMessages(HANDLE_CHECK_SATEREPORT);
        mIsExit = true;
        if (mOutputNMEALog != null) {
            finishSavingNMEALog();
        }
        if (mOutputTestLog != null) {
            finishSavingAutoTestLog();
        }
        //Intent it = new Intent(YgpsService.SERVICE_START_ACTION);
        //getBaseContext().stopService(it);
        //Log.v(TAG, "STOP service");
        unregisterReceiver(mPowerKeyReceiver);
        Log.v(TAG, "unregisterReceiver powerKeyReceiver");
        mSocketClient.endClient();
        final SharedPreferences preferences = this.getSharedPreferences(
                FIRST_TIME, android.content.Context.MODE_PRIVATE);
        String ss = preferences.getString(FIRST_TIME, null);
        if (ss != null && ss.equals(GpsMnlSetting.PROP_VALUE_2)) {
            GpsMnlSetting.setMnlProp(GpsMnlSetting.KEY_TEST_MODE,
                    GpsMnlSetting.PROP_VALUE_0);
        }
        mYgpsWakeLock.releaseCpuWakeLock();
        super.onDestroy();
    }

    public final LocationListener mLocListener = new LocationListener() {

        // @Override
        public void onLocationChanged(Location location) {
            Log.v(TAG, "Enter onLocationChanged function");
            if (!mFirstFix) {
                Log.w(TAG, "mFirstFix is false, onLocationChanged");
            }
            if (mShowLoc) {
                String str = null;
                String tmp = null;
                Date da = null;

                da = new Date(location.getTime());
                str = da.toString() + "\n";
                tmp = String.valueOf(location.getLatitude());
                if (tmp.length() > LOCATION_MAX_LENGTH) {
                    tmp = tmp.substring(0, LOCATION_MAX_LENGTH);
                }
                str += tmp + ",";
                tmp = String.valueOf(location.getLongitude());
                if (tmp.length() > LOCATION_MAX_LENGTH) {
                    tmp = tmp.substring(0, LOCATION_MAX_LENGTH);
                }
                str += tmp;
                if (mPrompt == null) {
                    mPrompt = Toast.makeText(YgpsActivity.this, str,
                            Toast.LENGTH_SHORT);
                    mPrompt.setGravity(Gravity.BOTTOM, 0, GRAVITE_Y_OFFSET);
                } else {
                    mPrompt.setText(str);
                }
                mPrompt.show();
                da = null;
            }
            Date d = new Date(location.getTime());
            String date = String.format("%s %+02d %04d/%02d/%02d", "GMT", d
                    .getTimezoneOffset(), d.getYear() + YEAR_START, d
                    .getMonth() + 1, d.getDate());
            String time = String.format("%02d:%02d:%02d", d.getHours(), d
                    .getMinutes(), d.getSeconds());

            TextView tvTime = (TextView) findViewById(R.id.tv_time);
            if (tvTime != null) {
                tvTime.setText(time);
            }

            TextView tvDate = (TextView) findViewById(R.id.tv_date);
            tvDate.setText(date);

            if (mShowFirstFixLocate) {
                mShowFirstFixLocate = false;
                TextView firstLon = (TextView) findViewById(R.id.first_longtitude_text);
                firstLon.setText(String.valueOf(location.getLongitude()));
                TextView firstLat = (TextView) findViewById(R.id.first_latitude_text);
                firstLat.setText(String.valueOf(location.getLatitude()));
                saveAutoTestLog("Longitude=" + location.getLongitude() + " ");
                saveAutoTestLog("Latitude=" + location.getLatitude() + " ");
            }
            TextView tvLat = (TextView) findViewById(R.id.tv_latitude);
            tvLat.setText(String.valueOf(location.getLatitude()));
            TextView tvLon = (TextView) findViewById(R.id.tv_longitude);
            tvLon.setText(String.valueOf(location.getLongitude()));
            TextView tvAlt = (TextView) findViewById(R.id.tv_altitude);
            tvAlt.setText(String.valueOf(location.getAltitude()));
            TextView tvAcc = (TextView) findViewById(R.id.tv_accuracy);
            tvAcc.setText(String.valueOf(location.getAccuracy()));
            TextView tvBear = (TextView) findViewById(R.id.tv_bearing);
            tvBear.setText(String.valueOf(location.getBearing()));
            TextView tvSpeed = (TextView) findViewById(R.id.tv_speed);
            tvSpeed.setText(String.valueOf(location.getSpeed()));
            if (mLastLocation != null) {
                TextView tvDist = (TextView) findViewById(R.id.tv_distance);
                tvDist.setText(String.valueOf(location
                        .distanceTo(mLastLocation)));
            }

            TextView tvTtff = (TextView) findViewById(R.id.tv_ttff);
            tvTtff.setText(mTtffValue + getString(R.string.time_unit_ms));

            // TextView txt_test_ttff =
            // (TextView)findViewById(R.id.txt_test_ttff);
            // txt_test_ttff.setText(mTTFF+
            // getString(R.string.time_unit_ms+txt_padding);

            TextView tvProvider = (TextView) findViewById(R.id.tv_provider);
            tvProvider.setText(mProvider);
            TextView tvStatus = (TextView) findViewById(R.id.tv_status);
            tvStatus.setText(mStatus);
            d = null;
            mLastLocation = location;
        }

        // @Override
        public void onProviderDisabled(String provider) {
            Log.v(TAG, "Enter onProviderDisabled function");
            mProvider = String.format(getString(R.string.provider_status_disabled,
                    LocationManager.GPS_PROVIDER));
            TextView tvProvider = (TextView) findViewById(R.id.tv_provider);
            tvProvider.setText(mProvider);
        }

        // @Override
        public void onProviderEnabled(String provider) {
            Log.v(TAG, "Enter onProviderEnabled function");
            mProvider = String.format(getString(R.string.provider_status_enabled,
                    LocationManager.GPS_PROVIDER));
            TextView tvProvider = (TextView) findViewById(R.id.tv_provider);
            tvProvider.setText(mProvider);
            mTtffValue = 0;
        }

        // @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.v(TAG, "Enter onStatusChanged function");
        }
    };

    public final GpsStatus.Listener mGpsListener = new GpsStatus.Listener() {
        private void onFirstFix(int ttff) {
            Log.v(TAG, "Enter onFirstFix function: ttff = " + ttff);
            int currentTimes = mCurrentTimes;
            mHandler.removeMessages(HANDLE_COUNTER);
            mTtffValue = ttff;
            if (ttff != mTtffValue) {
                Log.w(TAG, "ttff != mTTFF");
                mTtffValue = ttff;
            }
            mFirstFix = true;
            Toast.makeText(
                    YgpsActivity.this,
                    String.format(getString(R.string.toast_first_fix), ttff,
                            getString(R.string.time_unit_ms)),
                    Toast.LENGTH_LONG).show();
            TextView tvTtff = (TextView) findViewById(R.id.tv_ttff);
            tvTtff.setText(mTtffValue + getString(R.string.time_unit_ms));
            if (mIsTestRunning) {
                TextView tvLastTtff = (TextView) findViewById(R.id.tv_last_ttff);
                tvLastTtff.setText(mTtffValue
                        + getString(R.string.time_unit_ms));

                mMeanTTFF = meanTTFF(currentTimes);
                ((TextView) findViewById(R.id.tv_mean_ttff)).setText(Float
                        .valueOf(mMeanTTFF).toString()
                        + getString(R.string.time_unit_ms));
                saveAutoTestLog("ttff=" + Integer.toString(mTtffValue) + " ");
            }
        }

        private void onPreFix(int ttff) {
            Log.v(TAG, "Enter onPreFix function: ttff = " + ttff);
            int currentTimes = mCurrentTimes;
            mHandler.removeMessages(HANDLE_COUNTER);
            mTtffValue = ttff;
            mFirstFix = true;
            TextView tvTtff = (TextView) findViewById(R.id.tv_ttff);
            tvTtff.setText(mTtffValue + getString(R.string.time_unit_ms));
        }

        private boolean isLocationFixed(Iterable<GpsSatellite> list) {
            boolean fixed = false;
            synchronized (this) {
                for (GpsSatellite sate : list) {
                    if (sate.usedInFix()) {
                        fixed = true;
                        break;
                    }
                }
            }
            return fixed;
        }

        public void onGpsStatusChanged(int event) {
            Log.v(TAG, "Enter onGpsStatusChanged function");
            GpsStatus status = mLocationManager.getGpsStatus(null);
            switch (event) {
            case GpsStatus.GPS_EVENT_STARTED:
                mStatus = getString(R.string.gps_status_started);
                break;
            case GpsStatus.GPS_EVENT_STOPPED:
                mStatus = getString(R.string.gps_status_stopped);
                break;
            case GpsStatus.GPS_EVENT_FIRST_FIX:
                onFirstFix(status.getTimeToFirstFix());
                mStatus = getString(R.string.gps_status_first_fix);
                break;
            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                //mSateReportTimeOut = 0;
                //setSatelliteStatus(new GpsSatelliteAdapter(status.getSatellites()));

                if (mNmeaFixed && !mFirstFix && !mRestarted) {
                    onPreFix(status.getTimeToFirstFix());
                }
                if (!mIsShowVersion) {
                    showVersion();
                }
                break;
            default:
                break;
            }
            TextView tvStatus = (TextView) findViewById(R.id.tv_status);
            tvStatus.setText(mStatus);
            Log.v(TAG, "onGpsStatusChanged:" + event + " Status:" + mStatus);
        }
    };
    /**
     * only for hot/warm/cold/full/restart button
     * order is stop->restart->start
     * the button should be enabled after the 3 steps is finished
     */
    private void resetParamForRestart(Bundle extras) {
        Log.v(TAG, "Enter resetParamForRestart function");
        /* Below code come from HANDLE_REMOVE_UPDATE
         * Avoid HANDLE_REMOVE_UPDATE->onLocationChanged(previous)
         */
        mRestarted = true;
        mLocationManager.removeUpdates(mLocListener);
        mHandler.sendEmptyMessage(HANDLE_REMOVE_UPDATE);  // stop

        Message msg = mHandler
                .obtainMessage(YgpsActivity.HANDLE_DELETE_DATA);
        msg.setData(extras);
        mHandler.sendMessageDelayed(msg, HANDLE_MSG_DELAY_300); // restart

        mHandler.sendEmptyMessageDelayed(HANDLE_REQUEST_UPDATE, HANDLE_MSG_DELAY_300 * 2); // start

        mHandler.sendEmptyMessageDelayed(HANDLE_ENABLE_BUTTON, HANDLE_MSG_DELAY_300 * 3);
    }

    private void clearRestartMsgs() {
        mHandler.removeMessages(HANDLE_REMOVE_UPDATE);
        mHandler.removeMessages(HANDLE_DELETE_DATA);
        mHandler.removeMessages(HANDLE_REQUEST_UPDATE);
    }

     /**
     * only for Auto GPS test start button
     * order is stop->restart->start
     * the button should be enabled after the 3 steps is finished
     */
    private void resetParamForAutoTest(Bundle extras) {
        Log.v(TAG, "Enter resetParamForAutoTest function");
        mLocationManager.removeUpdates(mLocListener);
        mHandler.sendEmptyMessage(HANDLE_REMOVE_UPDATE);  // stop

        Message msg = mHandler
                .obtainMessage(YgpsActivity.HANDLE_DELETE_DATA);
        msg.setData(extras);
        mHandler.sendMessageDelayed(msg, HANDLE_MSG_DELAY_300); // restart

        mHandler.sendEmptyMessageDelayed(HANDLE_REQUEST_UPDATE, HANDLE_MSG_DELAY_300 * 2); // start

        //mHandler.sendEmptyMessageDelayed(HANDLE_ENABLE_BUTTON, HANDLE_MSG_DELAY_300*3);
    }
    /**
     * Set reset parameters to GPS driver
     *
     * @param extras
     *            Data need to reset
     * @param bAutoConnectTest
     *            Whether auto test testing
     */
    private void resetParam(Bundle extras, boolean bAutoConnectTest) {
        Log.v(TAG, "Enter resetParam function");
        mLocationManager.removeUpdates(mLocListener);
        try {
            Thread.sleep(ONE_SECOND / 2);  // wait for stop gps done.
        } catch (InterruptedException e) {
            Log.d(TAG, "resetParam InterruptedException: " + e.getMessage());
        }
        if (!bAutoConnectTest) {
            mLocationManager.sendExtraCommand(LocationManager.GPS_PROVIDER,
                    "delete_aiding_data", extras);
        }

        if (!bAutoConnectTest) {
            clearLayout();
        }
        mFirstFix = false;
        mTtffValue = 0;
        if (!mHandler.hasMessages(HANDLE_COUNTER)) {
            mHandler.sendEmptyMessage(HANDLE_COUNTER);
        }
        try {
            if (bAutoConnectTest && mTestInterval != 0) {
                for (int i = mTestInterval; i >= 0 && mIsTestRunning; --i) {
                    setCountDown(i);
                    Thread.sleep(1 * ONE_SECOND);

                }
                if (!mIsTestRunning) {
                    setCountDown(0);
                }
            } else {
                Thread.sleep(ONE_SECOND / 2);
            }
        } catch (InterruptedException e) {
            Log.d(TAG, "resetParam InterruptedException: " + e.getMessage());
        }
        if (!mIsExit) {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 0, 0, mLocListener);
        }
        // reset autotest start button
        if (!bAutoConnectTest && !mBtnGpsTestStart.isEnabled()) {
            setStartButtonEnable(true);
            removeDialog(DIALOG_WAITING_FOR_STOP);
            // added by chaozhong @2010.10.12
            mStopPressedHandling = false;
            mStartPressedHandling = false;

            // add end
        }
    }

    /**
     * Get GPS test status
     *
     * @return Whether gps auto test is running
     */
    private boolean gpsTestRunning() {
        if (mIsTestRunning) {
            Toast.makeText(this, R.string.gps_test_running_warn,
                    Toast.LENGTH_LONG).show();
            return true;
        }
        return false;
    }

    public final OnClickListener mBtnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mStatusPrompt != null) {
                mStatusPrompt.cancel();
            }
            Bundle extras = new Bundle();
            if (v == (View) mBtnGpsTestStart) {
                mBtnGpsTestStart.refreshDrawableState();
                mBtnGpsTestStart.setEnabled(false);
                Log.v(TAG, "GPSTest Start button is pressed");
                mHandler.sendEmptyMessageDelayed(HANDLE_CLEAR, HANDLE_MSG_DELAY);
                startGPSAutoTest();
            } else if (v == (View) mBtnGpsTestStop) {
                // the next line is added by chaozhong @2010.10.11
                mBtnGpsTestStop.setEnabled(false);
                mBtnGpsTestStop.refreshDrawableState();
                // the next if statement is added by chaozhong @2010.10.11
                // purpose is to test weather mProgressDialog is already exist
                // or not, to resolve the multi ProgressDialog created problem
                // mBtnGPSTestStop.setEnabled(false);//added by chaozhong ,in
                // case that as soon as mProgressDialog is dismissed,
                // mBtnGPSTestStop can still be pressed, and this dialog will
                // pop up but will not be dismissed automatically
                // if(null == mProgressDialog)
                if (!mStopPressedHandling) {
                    mStopPressedHandling = true;
                    showDialog(DIALOG_WAITING_FOR_STOP);
                    mIsTestRunning = false;
                } else {
                    Log.v(TAG, "stop has been clicked.");
                }
            } else if (v == (View) mBtnHotStart) {
                if (gpsTestRunning()) {
                    return;
                }
                enableBtns(false);
                // nothing should be put
                Log.v(TAG, "Hot Start button is pressed");
                extras.putBoolean(GPS_EXTRA_RTI, true);
                resetParamForRestart(extras);
            } else if (v == (View) mBtnWarmStart) {
                if (gpsTestRunning()) {
                    return;
                }
                enableBtns(false);
                Log.v(TAG, "Warm Start button is pressed");
                extras.putBoolean(GPS_EXTRA_EPHEMERIS, true);
                resetParamForRestart(extras);
            } else if (v == (View) mBtnColdStart) {
                if (gpsTestRunning()) {
                    return;
                }
                enableBtns(false);
                Log.v(TAG, "Cold Start button is pressed");
                extras.putBoolean(GPS_EXTRA_EPHEMERIS, true);
                extras.putBoolean(GPS_EXTRA_POSITION, true);
                extras.putBoolean(GPS_EXTRA_TIME, true);
                extras.putBoolean(GPS_EXTRA_IONO, true);
                extras.putBoolean(GPS_EXTRA_UTC, true);
                extras.putBoolean(GPS_EXTRA_HEALTH, true);
                resetParamForRestart(extras);
            } else if (v == (View) mBtnFullStart) {
                if (gpsTestRunning()) {
                    return;
                }
                enableBtns(false);
                Log.v(TAG, "Full Start button is pressed");
                extras.putBoolean(GPS_EXTRA_ALL, true);
                resetParamForRestart(extras);
            } else if (v == (View) mBtnReStart) {
                if (gpsTestRunning()) {
                    return;
                }
                enableBtns(false);
                Log.v(TAG, "Restart button is pressed");
                extras.putBoolean(GPS_EXTRA_EPHEMERIS, true);
                extras.putBoolean(GPS_EXTRA_A1LMANAC, true);
                extras.putBoolean(GPS_EXTRA_POSITION, true);
                extras.putBoolean(GPS_EXTRA_TIME, true);
                extras.putBoolean(GPS_EXTRA_IONO, true);
                extras.putBoolean(GPS_EXTRA_UTC, true);
                resetParamForRestart(extras);
            } else if (v == (View) mBtnNmeaStart) {
                Log.v(TAG, "NMEA Start button is pressed");
                if (!createFileForSavingNMEALog()) {
                    Log.i(TAG, "createFileForSavingNMEALog return false");
                    return;
                }
                mStartNmeaRecord = true;
                mBtnNmeaStart.setEnabled(false);
                mBtnNMEAStop.setEnabled(true);
            } else if (v == (View) mBtnNMEAStop) {
                Log.v(TAG, "NMEA Stop button is pressed");
                mStartNmeaRecord = false;
                finishSavingNMEALog();

            } else if (v == (View) mBtnNMEADbgDbg) {
                Log.v(TAG, "NMEA DbgDbg is pressed");
                String ss = GpsMnlSetting.getMnlProp(
                        GpsMnlSetting.KEY_DEBUG_DBG2SOCKET,
                        GpsMnlSetting.PROP_VALUE_0);
                if (ss.equals(GpsMnlSetting.PROP_VALUE_0)) {
                    mBtnNMEADbgDbg
                            .setText(R.string.btn_name_dbg2socket_disable);
                    GpsMnlSetting.setMnlProp(
                            GpsMnlSetting.KEY_DEBUG_DBG2SOCKET,
                            GpsMnlSetting.PROP_VALUE_1);
                } else {
                    mBtnNMEADbgDbg.setText(R.string.btn_name_dbg2socket_enable);
                    GpsMnlSetting.setMnlProp(
                            GpsMnlSetting.KEY_DEBUG_DBG2SOCKET,
                            GpsMnlSetting.PROP_VALUE_0);
                }

            } else if (v == (View) mBtnNmeaDbgNmea) {
                Log.v(TAG, "NMEA DbgNmea button is pressed");
                String ss = GpsMnlSetting.getMnlProp(
                        GpsMnlSetting.KEY_DEBUG_NMEA2SOCKET,
                        GpsMnlSetting.PROP_VALUE_0);
                if (ss.equals(GpsMnlSetting.PROP_VALUE_0)) {
                    mBtnNmeaDbgNmea
                            .setText(R.string.btn_name_nmea2socket_disable);
                    GpsMnlSetting.setMnlProp(
                            GpsMnlSetting.KEY_DEBUG_NMEA2SOCKET,
                            GpsMnlSetting.PROP_VALUE_1);
                } else {
                    mBtnNmeaDbgNmea
                            .setText(R.string.btn_name_nmea2socket_enable);
                    GpsMnlSetting.setMnlProp(
                            GpsMnlSetting.KEY_DEBUG_NMEA2SOCKET,
                            GpsMnlSetting.PROP_VALUE_0);
                }
            } else if (v == (View) mBtnNmeaDbgDbgFile) {
                Log.v(TAG, "NMEA DbgDbgFile is pressed");
                String ss = GpsMnlSetting.getMnlProp(
                        GpsMnlSetting.KEY_DEBUG_DBG2FILE,
                        GpsMnlSetting.PROP_VALUE_0);
                if (ss.equals(GpsMnlSetting.PROP_VALUE_0)) {
                    mBtnNmeaDbgDbgFile
                            .setText(R.string.btn_name_dbg2file_disable);
                    GpsMnlSetting.setMnlProp(GpsMnlSetting.KEY_DEBUG_DBG2FILE,
                            GpsMnlSetting.PROP_VALUE_1);
                } else {
                    mBtnNmeaDbgDbgFile
                            .setText(R.string.btn_name_dbg2file_enable);
                    GpsMnlSetting.setMnlProp(GpsMnlSetting.KEY_DEBUG_DBG2FILE,
                            GpsMnlSetting.PROP_VALUE_0);
                }

            } else if (v == (View) mBtnNmeaDbgNmeaDdms) {
                Log.v(TAG, "NMEA debug2ddms button is pressed");
                String ss = GpsMnlSetting.getMnlProp(
                        GpsMnlSetting.KEY_DEBUG_DEBUG_NMEA,
                        GpsMnlSetting.PROP_VALUE_1);
                if (ss.equals(GpsMnlSetting.PROP_VALUE_1)) { // default enabled
                    mBtnNmeaDbgNmeaDdms
                            .setText(R.string.btn_name_dbg2ddms_enable);
                    GpsMnlSetting.setMnlProp(
                            GpsMnlSetting.KEY_DEBUG_DEBUG_NMEA,
                            GpsMnlSetting.PROP_VALUE_0);
                } else {
                    mBtnNmeaDbgNmeaDdms
                            .setText(R.string.btn_name_dbg2ddms_disable);
                    GpsMnlSetting.setMnlProp(
                            GpsMnlSetting.KEY_DEBUG_DEBUG_NMEA,
                            GpsMnlSetting.PROP_VALUE_1);
                }
            } else if (v == (View) mBtnHotStill) {
                Log.v(TAG, "Hot still button is pressed");
                String ss = GpsMnlSetting.getMnlProp(
                        GpsMnlSetting.KEY_BEE_ENABLED,
                        GpsMnlSetting.PROP_VALUE_1);
                if (ss.equals(GpsMnlSetting.PROP_VALUE_1)) {
                    mBtnHotStill.setText(R.string.btn_name_hotstill_enable);
                    GpsMnlSetting.setMnlProp(GpsMnlSetting.KEY_BEE_ENABLED,
                            GpsMnlSetting.PROP_VALUE_0);
                } else {
                    mBtnHotStill.setText(R.string.btn_name_hotstill_disable);
                    GpsMnlSetting.setMnlProp(GpsMnlSetting.KEY_BEE_ENABLED,
                            GpsMnlSetting.PROP_VALUE_1);
                }
            } else if (v == (View) mBtnSuplLog) {
                Log.v(TAG, "supllog button is pressed");
                String ss = GpsMnlSetting.getMnlProp(
                        GpsMnlSetting.KEY_SUPLLOG_ENABLED,
                        GpsMnlSetting.PROP_VALUE_0);
                if (ss.equals(GpsMnlSetting.PROP_VALUE_1)) {
                    mBtnSuplLog.setText(R.string.btn_name_supllog_enable);
                    GpsMnlSetting.setMnlProp(GpsMnlSetting.KEY_SUPLLOG_ENABLED,
                            GpsMnlSetting.PROP_VALUE_0);
                } else {
                    mBtnSuplLog.setText(R.string.btn_name_supllog_disable);
                    GpsMnlSetting.setMnlProp(GpsMnlSetting.KEY_SUPLLOG_ENABLED,
                            GpsMnlSetting.PROP_VALUE_1);
                }
            }  else if (v == (View) mBtnNmeaClear) {
                Log.v(TAG, "NMEA Clear button is pressed");
                mTvNmeaLog.setText(R.string.empty);
            } else if (v == (View) mBtnNmeaSave) {
                Log.v(TAG, "NMEA Save button is pressed");
                saveNMEALog();
            } else if (v == (View) mBtnGpsHwTest) {
                Log.v(TAG, "mBtnGPSHwTest Button is pressed");
                onGpsHwTestClicked();
            } else if (v == (View) mBtnGpsJamming) {
                Log.v(TAG, "mBtnGPSJamming Button is pressed");
                onGpsJammingScanClicked();
            } else {
                return;
            }
        }
    };

    /**
     * Send command to MNL server
     *
     * @param command
     *            PMTK command to be send
     */
    private void sendCommand(String command) {
        Log.v(TAG, "GPS Command is " + command);
        if (null == command || command.trim().length() == 0) {
            Toast.makeText(this, R.string.command_error, Toast.LENGTH_LONG)
                    .show();
            return;
        }
        int index1 = command.indexOf(COMMAND_START);
        int index2 = command.indexOf(COMMAND_END);
        String com = command;
        if (index1 != -1 && index2 != -1) {
            if (index2 < index1) {
                Toast.makeText(this, R.string.command_error, Toast.LENGTH_LONG)
                        .show();
                return;
            }
            com = com.substring(index1 + 1, index2);
        } else if (index1 != -1) {
            com = com.substring(index1 + 1);
        } else if (index2 != -1) {
            com = com.substring(0, index2);
        }
        mSocketClient.sendCommand(com.trim());
    }

    /**
     * Invoked when get GPS server respond
     *
     * @param res
     *            Response message
     */
    public void onResponse(String res) {
        Log.v(TAG, "Enter getResponse: " + res);
        if (null == res || res.isEmpty()) {
            return;
        }
        Message m = mHandler.obtainMessage(HANDLE_UPDATE_RESULT);
        if (res.startsWith("$PMTK705")) {
            m.arg1 = HANDLE_COMMAND_GETVERSION;
        } else if (res.contains("PMTK001")) {
            m.arg1 = HANDLE_COMMAND_JAMMINGSCAN;
        } else {
            m.arg1 = HANDLE_COMMAND_OTHERS;
        }
        m.obj = res;
        mHandler.sendMessage(m);
    }

    /**
     * Invoked when GPS HW test button clicked
     */
    private void onGpsHwTestClicked() {
        String ss = GpsMnlSetting.getMnlProp(GpsMnlSetting.KEY_TEST_MODE,
                GpsMnlSetting.PROP_VALUE_0);
        if (ss.equals(GpsMnlSetting.PROP_VALUE_0)) {
            mBtnGpsHwTest.setText(R.string.btn_name_dbg2gpsdoctor_disable);
            GpsMnlSetting.setMnlProp(GpsMnlSetting.KEY_TEST_MODE,
                    GpsMnlSetting.PROP_VALUE_1);
            mBtnNmeaDbgDbgFile.setText(R.string.btn_name_dbg2file_disable);
            GpsMnlSetting.setMnlProp(GpsMnlSetting.KEY_DEBUG_DBG2FILE,
                    GpsMnlSetting.PROP_VALUE_1);
        } else {
            mBtnGpsHwTest.setText(R.string.btn_name_dbg2gpsdoctor_enable);
            GpsMnlSetting.setMnlProp(GpsMnlSetting.KEY_TEST_MODE,
                    GpsMnlSetting.PROP_VALUE_0);
        }
        final SharedPreferences preferences = this.getSharedPreferences(
                FIRST_TIME, android.content.Context.MODE_PRIVATE);
        preferences.edit().putString(FIRST_TIME, GpsMnlSetting.PROP_VALUE_1)
                .commit();
    }

    /**
     * Invoked when GPS Jamming Scan test button clicked
     */
    private void onGpsJammingScanClicked() {
        if (0 == mEtGpsJammingTimes.getText().length()) {
            Toast.makeText(YgpsActivity.this,
                    "Please input Jamming scan times", Toast.LENGTH_LONG)
                    .show();
            return;
        } else {
            Integer times = Integer.valueOf(mEtGpsJammingTimes.getText()
                    .toString());
            if (times <= INPUT_VALUE_MIN || times > INPUT_VALUE_MAX) {
                Toast.makeText(YgpsActivity.this, "Jamming scan times error",
                        Toast.LENGTH_LONG).show();
                return;
            }
            sendCommand("PMTK837,1," + times);
        }
    }

    /**
     * Refresh button status
     *
     * @param bEnable
     *            Set button status
     */
    private void enableBtns(boolean bEnable) {
        mBtnHotStart.setClickable(bEnable);
        mBtnWarmStart.setClickable(bEnable);
        mBtnColdStart.setClickable(bEnable);
        mBtnFullStart.setClickable(bEnable);
        mBtnReStart.setClickable(bEnable);
        mBtnHotStill.setClickable(bEnable);
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case HANDLE_COUNTER:
                if (!mFirstFix) {
                    mTtffValue += COUNT_PRECISION;
                    TextView tvTtff = (TextView) findViewById(R.id.tv_ttff);
//                    tvTtff.setText(mTtffValue
//                            + getString(R.string.time_unit_ms));
                    tvTtff.setText(mTtffValue % 1000 == 0 ? "Counting" : "");
                    this.sendEmptyMessageDelayed(HANDLE_COUNTER,
                            COUNT_PRECISION);
                }
                break;
            case HANDLE_UPDATE_RESULT:
                String response = msg.obj.toString();
                switch (msg.arg1) {
                case HANDLE_COMMAND_JAMMINGSCAN:
                    if (response.contains("PMTK001,837")) {
                        Toast.makeText(YgpsActivity.this,
                                R.string.toast_jamming_succeed,
                                Toast.LENGTH_LONG).show();
                    }
                    break;
                case HANDLE_COMMAND_GETVERSION:
                    if (response.startsWith("$PMTK705")) {
                        String[] strA = response.split(",");
                        if (strA.length >= RESPONSE_ARRAY_LENGTH) {
                            TextView tMnlVersion = (TextView) findViewById(R.id.tv_mnl_version);
                            if (null != tMnlVersion) {
                                if (!tMnlVersion.getText().toString()
                                        .startsWith("MNL")) {
                                    tMnlVersion
                                            .setText(strA[RESPONSE_ARRAY_LENGTH - 1]);
                                    mIsShowVersion = true;
                                }
                            } else {
                                Log.v(TAG, "txt_mnl_version is null");
                            }
                        }
                    }
                    break;
                case HANDLE_COMMAND_OTHERS:
                    break;
                default:
                    break;
                }
                break;
            case HANDLE_CLEAR:
                Log.v(TAG, "handleClear-msg");
                setSatelliteStatus(null);
                clearLayout();
                break;
            case HANDLE_CHECK_SATEREPORT:
                mSateReportTimeOut++;
                if (SATE_RATE_TIMEOUT < mSateReportTimeOut) {
                    mSateReportTimeOut = 0;
                    sendEmptyMessage(HANDLE_CLEAR);
                }
                sendEmptyMessageDelayed(HANDLE_CHECK_SATEREPORT, ONE_SECOND);
                break;
            case HANDLE_ENABLE_BUTTON:
                Log.v(TAG, "handleEnableButton-msg");
                enableBtns(true); // avoid continue press button
                break;
            case HANDLE_REMOVE_UPDATE:
                Log.v(TAG, "removeUpdates-msg");
                removeNmeaParser();
                //mLocationManager.removeUpdates(mLocListener);
                mFirstFix = false;
                mTtffValue = 0;
                mShowFirstFixLocate = true;
                setSatelliteStatus(null);
                clearLayout();
                if (!this.hasMessages(HANDLE_COUNTER)) {
                    this.sendEmptyMessage(HANDLE_COUNTER);
                }
                break;
            case HANDLE_DELETE_DATA:
                Log.v(TAG, "delete_aiding_data-msg");
                Bundle b = msg.getData();
                mLocationManager.sendExtraCommand(LocationManager.GPS_PROVIDER,
                    "delete_aiding_data", b);
                break;
            case HANDLE_REQUEST_UPDATE:
                mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 0, 0, mLocListener);
                mNmeaParser.addSVUpdateListener(mNmeaUpdateListener);
                Log.v(TAG, "requestLocationUpdates-msg");
                break;
            default:
                break;
            }
            super.handleMessage(msg);
        }
    };

    class YgpsWakeLock {
        private PowerManager.WakeLock mScreenWakeLock = null;
        private PowerManager.WakeLock mCpuWakeLock = null;

        /**
         * Acquire CPU wake lock
         *
         * @param context
         *            Getting lock context
         */
        void acquireCpuWakeLock(Context context) {
            Log.v(TAG, "Acquiring cpu wake lock");
            if (mCpuWakeLock != null) {
                return;
            }

            PowerManager pm = (PowerManager) context
                    .getSystemService(Context.POWER_SERVICE);

            mCpuWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK
                    | PowerManager.ACQUIRE_CAUSES_WAKEUP, TAG);
            // | PowerManager.ON_AFTER_RELEASE, TAG);
            mCpuWakeLock.acquire();
        }

        /**
         * Acquire screen wake lock
         *
         * @param context
         *            Getting lock context
         */
        void acquireScreenWakeLock(Context context) {
            Log.v(TAG, "Acquiring screen wake lock");
            if (mScreenWakeLock != null) {
                return;
            }

            PowerManager pm = (PowerManager) context
                    .getSystemService(Context.POWER_SERVICE);

            mScreenWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
                    | PowerManager.ACQUIRE_CAUSES_WAKEUP, TAG);
            // | PowerManager.ON_AFTER_RELEASE, TAG);
            mScreenWakeLock.acquire();
        }

        /**
         * Release wake locks
         */
        void releaseScreenWakeLock() {
            Log.v(TAG, "Releasing wake lock");

            if (mScreenWakeLock != null) {
                mScreenWakeLock.release();
                mScreenWakeLock = null;
            }
        }

        void releaseCpuWakeLock() {
            Log.v(TAG, "Releasing cpu wake lock");
            if (mCpuWakeLock != null) {
                mCpuWakeLock.release();
                mCpuWakeLock = null;
            }
        }
    }

}

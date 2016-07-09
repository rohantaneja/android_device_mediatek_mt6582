package com.mediatek.ygps;

import java.util.HashMap;
import java.util.ArrayList;
import android.util.Log;
import android.os.Handler;
import android.os.Message;


import android.location.Location;
import android.location.LocationManager;

/**
 * Parser class for parsing NMEA sentences.
 */
public class NmeaParser {
    private final static int SV_UPDATE        = 0;
    private static final String DELIMITER = ",";
    private final String[] mTalker = {"GP", "GL", "GA", "BD"};
    private static NmeaParser mNmeaParser = null;
    private int mSatelliteCount;
    private HashMap<String, ArrayList> mSatelInfoList;
    private HashMap<String, SVExtraInfro> mExtraInfoList;
    private HashMap<String, Location> mLocRecord;
    private boolean mbUpdated = false;
    private String mCurrentTalker = "none";
    private final ArrayList<NmeaUpdateViewListener> mListener = new ArrayList<NmeaUpdateViewListener>();
    private HashMap<String, ArrayList<Integer>> mUsedFixIdMapList = new HashMap<String, ArrayList<Integer>>();

    public static NmeaParser getNMEAParser() {
        if (mNmeaParser == null) {
            mNmeaParser = new NmeaParser();
        }
        return mNmeaParser;
    }

    private boolean isUsedInFix(int prn) {
        ArrayList<Integer> usedFixIdList = mUsedFixIdMapList.get(mCurrentTalker);
        if (usedFixIdList == null) {
            usedFixIdList = new ArrayList<Integer>();
            mUsedFixIdMapList.put(mCurrentTalker, usedFixIdList);
            return false;
        }
        for (Integer id : usedFixIdList) {
            if (prn == id) {
                return true;
            }
        }
        return false;
    }

    private void addUsedFixId(int prn) {
        ArrayList<Integer> usedFixIdList = mUsedFixIdMapList.get(mCurrentTalker);
        if (usedFixIdList == null) {
            usedFixIdList = new ArrayList<Integer>();
            mUsedFixIdMapList.put(mCurrentTalker, usedFixIdList);
        }
        usedFixIdList.add(prn);
    }

    private void clearUsedFixList() {
        ArrayList<Integer> usedFixIdList = mUsedFixIdMapList.get(mCurrentTalker);
        if (usedFixIdList == null) {
            usedFixIdList = new ArrayList<Integer>();
            mUsedFixIdMapList.put(mCurrentTalker, usedFixIdList);
            return;
        }
        usedFixIdList.clear();
    }

    private NmeaParser() {
        this.mSatelInfoList = new HashMap<String, ArrayList>();
        this.mExtraInfoList = new HashMap<String, SVExtraInfro>();
        this.mLocRecord = new HashMap<String, Location>();
        this.mSatelliteCount = 0;

        for (String s :mTalker) {
            ArrayList<SatelliteInfo> mSatlist = new ArrayList<SatelliteInfo>();
            mSatelInfoList.put(s, mSatlist);

            SVExtraInfro mextra = new SVExtraInfro();
            mExtraInfoList.put(s, mextra);

            Location loc = new Location(LocationManager.GPS_PROVIDER);
            mLocRecord.put(s, loc);
        }
    }

    /** Get satellite count */
    public synchronized int getSatelliteCount() {
        return mSatelliteCount;
    }

    /**
     * Get Current All SV infomation.
     */
    public ArrayList<SatelliteInfo> getSatelliteList() {
        ArrayList<SatelliteInfo> mList = new ArrayList<SatelliteInfo>();
        for (ArrayList<SatelliteInfo> svlist : mSatelInfoList.values()) {
            mList.addAll(svlist);
        }
        return mList;
    }

    /**
     * Clear All SV infomation.
     */
    public void clearSatelliteList() {
        for (ArrayList<SatelliteInfo> svlist : mSatelInfoList.values()) {
            svlist.clear();
        }
    }

    public synchronized boolean isViewNeedUpdated() {
        boolean result = mbUpdated;
        if (mbUpdated) {
            mbUpdated ^= true;
        }
        return result;
    }

    /** Get satellite */
    private ArrayList<SatelliteInfo> getSatelliteList(String talker) {
        ArrayList<SatelliteInfo> svlist = null;
        if (mSatelInfoList.containsKey(talker)) {
            svlist = mSatelInfoList.get(talker);
        } else {
            log("No this talker " + talker + " SV exist");
        }
        return svlist;
    }

    private void clearSatelliteList(String talker) {
        ArrayList<SatelliteInfo> svlist = getSatelliteList(talker);
        if (svlist != null) {
            for (SatelliteInfo sv : svlist) {
                sv.mUsedInFix = false;
            }
        }
    }

    private Location getTalkerLocation(String talker) {
        Location loc = null;
        if (mLocRecord.containsKey(talker)) {
            loc = mLocRecord.get(talker);
        } else {
            log("No this talker " + talker + " Loc exist");
        }
        return loc;
    }

    private SVExtraInfro getTalkerExtra(String talker) {
        SVExtraInfro xtra = null;
        if (mExtraInfoList.containsKey(talker)) {
            xtra = mExtraInfoList.get(talker);
        } else {
            log("No this talker " + talker + " Extra exist");
        }
        return xtra;
    }

    private String checkTalker(String record) {
        String result = "none";
        for (String s :mTalker) {
            if (record.contains(s)) {
                result = s;
                break;
            }
        }
        return result;
    }


    private int checkTalkerColor(String record) {
        int result = 0xffff0000; //red
        if (record.equals("GP")) {
            result = 0xff00ffff; //cyan
        } else if (record.equals("GL")) {
            result = 0xffffff00; //yellow
        } else if (record.equals("GA")) {
            result = 0xffffffff; //white
        } else if (record.equals("BD")) {
            result = 0xff0000ff; //blue
        }
        return result;
    }

    private String removeFirstZero(String record) {
        String result = record;
        int ind = 0;
        while (record.charAt(ind++) == '0');
        if (ind != 0) {
            result = record.substring(--ind);
        }
        //log("ori:"+ record + " res:" + result);
        return result;
    }


    /** Parse GPS position */
    public synchronized void parse(String record) {
        mCurrentTalker = checkTalker(record);
        if (record.contains("RMC")) {
            try {
                parseRMC(record);
            } catch (Exception e) {
                log("Exception in parseRMC()");
            }
        } else if (record.contains("GSA")) {
            try {
                parseGSA(record);
            } catch (Exception e) {
                log("Exception in parseGSA()" + e);
            }
        } else if (record.contains("GGA")) {
            try {
                parseGGA(record);
            } catch (Exception e) {
                log("Exception in parseGGA()" + e);
            }
        } else if (record.contains("GSV")) {
            try {
                parseGSV(record);
            } catch (Exception e) {
                log("Exception in parseGSV()" + e);
            }
        } else {
            //log("undefined format");
        }

    }



    /**
     * <$GPRMC>
     * Recommended minimum specific GPS/Transit data
     *
     *      eg. $GPRMC,hhmmss.ss,A,llll.ll,a,yyyyy.yy,a,x.x,x.x,ddmmyy,x.x,a*hh
     *      1    = UTC of position fix
     *      2    = Data status (V=navigation receiver warning)
     *      3    = Latitude of fix
     *      4    = N or S
     *      5    = Longitude of fix
     *      6    = E or W
     *      7    = Speed over ground in knots
     *      8    = Track made good in degrees True
     *      9    = UT date
     *      10   = Magnetic variation degrees (Easterly var. subtracts from true course)
     *      11   = E or W
     *      12   = Checksum
     *
     */
    private synchronized void parseRMC(String record) {

        String[] values = split(record);

        // First value = $GPRMC
        // Date time of fix (eg. 041107.000)
        // String dateTimeOfFix = values[1];

        // Warning (eg. A:valid, V:warning)
        final String warning = values[2];

        // Latitude (eg. 6131.2028)
        final String latitude = values[3];

        // Lattitude direction (eg. N/S)
        final String latitudeDirection = values[4];

        // Longitude (eg. 02356.8782)
        final String longitude = values[5];

        // Longitude direction (eg. E/W)
        final String longitudeDirection = values[6];

        // Ground speed (eg. 18.28[knots])
        final double groundSpeed = parseDouble(values[7]);

        // Course (198.00)
        final String courseString = values[8];

        double longitudeDouble = 0.0;
        double latitudeDouble = 0.0;
        double speed = -2.0;
        if (longitude.length() > 0 && latitude.length() > 0) {
            longitudeDouble = parseDouble(longitude);
            if (longitudeDirection.equals("E") == false) {
                longitudeDouble = -longitudeDouble;
            }

            latitudeDouble = parseDouble(latitude);
            if (latitudeDirection.equals("N") == false) {
                latitudeDouble = -latitudeDouble;
            }
        } else {
            log("Error with lat or long");
        }

        int course = 0;
        if (courseString.length() > 0) {
            try {
                course = (int) parseDouble(courseString);
            } catch (Exception e) {
                course = 180;
            }
        }


        // if we have a speed value, work out the Miles Per Hour
        // if we have a speed value, work out the Km Per Hour
        if (groundSpeed > 0) {
            // km/h = knots * 1.852
            speed = ((groundSpeed) * 1.852);
        }
        // A negative speed doesn't make sense.
        if (speed < 0) {
            speed = 0;
        }

        if (warning.equals("A")) {
            Location loc = getTalkerLocation(mCurrentTalker);
            if (loc != null) {
                loc.setLatitude(latitudeDouble);
                loc.setLongitude(longitudeDouble);
                loc.setBearing(course);
                loc.setSpeed((float) speed * 1000);
                mLocRecord.put(mCurrentTalker, loc);
            }
        } else {
            //log("$GPRMC: Warning NOT A, so no position written: (" + warning + ")");
        }

    }

    /**
     * <$GPGSA>
     * GPS DOP and active satellites
     *
     *      eg1. $GPGSA,A,3,,,,,,16,18,,22,24,,,3.6,2.1,2.2*3C
     *      1    = Mode:
     *      M = Manual, forced to operate in 2D or 3D
     *      A = Automatic, 3D/2D
     *      2 = Mode:
     *       1=Fix not available
     *       2=2D
     *       3=3D
     *      3-14 = IDs of SVs used in position fix (null for unused fields)
     *      15   = Position Dilution of Precision (PDOP)
     *      16   = Horizontal Dilution of Precision (HDOP)
     *      17   = Vertical Dilution of Precision (VDOP)
     *
     * @param record
     */
    private synchronized void parseGSA(String record) {
        String[] values = split(record);
        //String mode=values[1];
        SVExtraInfro mInfo = getTalkerExtra(mCurrentTalker);
        clearUsedFixList();
        if (mInfo != null && values.length >= 17) {
            if (values[2].equals("1")) {
                //no fix
        clearSatelliteList(mCurrentTalker);
                return;
            }
            int[] svid = new int[13];
            mInfo.mfixtype = values[2];
            ArrayList<SatelliteInfo> SVlist = getSatelliteList(mCurrentTalker);
            clearSatelliteList(mCurrentTalker);
            for (int i = 2; i < 15; i++) {
                int prn = parseInt(values[i]);
                if (prn > 0) {
                    addUsedFixId(prn);
                }
            }

            mInfo.mPdop = values[15];
            mInfo.mHdop = values[16];
            mInfo.mVdop = values[17];
        } else {
            // no fix
            clearSatelliteList(mCurrentTalker);
        }
    }

    /**
     * <$GGA>
     * Global Positioning System Fix Data
     *      eg3. $GPGGA,hhmmss.ss,llll.ll,a,yyyyy.yy,a,x,xx,x.x,x.x,M,x.x,M,x.x,xxxx*hh
     *      1    = UTC of Position
     *      2    = Latitude
     *      3    = N or S
     *      4    = Longitude
     *      5    = E or W
     *      6    = GPS quality indicator (0=invalid; 1=GPS fix; 2=Diff. GPS fix)
     *      7    = Number of satellites in use [not those in view]
     *      8    = Horizontal dilution of position
     *      9    = Antenna altitude above/below mean sea level (geoid)
     *      10   = Meters  (Antenna height unit)
     *      11   = Geoidal separation (Diff. between WGS-84 earth ellipsoid and
     *      mean sea level.  -=geoid is below WGS-84 ellipsoid)
     *      12   = Meters  (Units of geoidal separation)
     *      13   = Age in seconds since last update from diff. reference station
     *      14   = Diff. reference station ID#
     *      15   = Checksum
     */
    private synchronized void parseGGA(String record) {
        String[] values = split(record);
        Location mInfo = getTalkerLocation(mCurrentTalker);
        long utcTime = (new Double(parseDouble(values[1]))).longValue();
        double lat = parseDouble(values[2]);
        double longt = parseDouble(values[4]);
        double alti = parseDouble(values[9]);
        if (values[3].equals("N") == false) {
            lat = -lat;
        }

        if (values[5].equals("E") == false) {
            longt = -longt;
        }

        if (mInfo != null) {
            mInfo.setTime(utcTime);
            mInfo.setLatitude(lat);
            mInfo.setLongitude(longt);
            mInfo.setAltitude(alti);
        }
    }




    /**
     * <$GPGSV>
     * GPS Satellites in view
     *
     *      eg:$GPGSV,1,1,13,02,02,213,,03,-3,000,,11,00,121,,14,13,172,05*67
     *      1    = Total number of messages of this type in this cycle, A: clean all SV
     *      2    = Message number
     *      3    = Total number of SVs in view
     *      4    = SV PRN number
     *      5    = Elevation in degrees, 90 maximum
     *      6    = Azimuth, degrees from true north, 000 to 359
     *      7    = SNR, 00-99 dB (null when not tracking)
     *      8-11 = Information about second SV, same as field 4-7
     *      12-15= Information about third SV, same as field 4-7
     *      16-19= Information about fourth SV, same as field 4-7
     *
     */
    private synchronized void parseGSV(String record) {
        String[] values = split(record);
        ArrayList<SatelliteInfo> SVlist = getSatelliteList(mCurrentTalker);

        if (SVlist == null) {
            log("parseGSV get SVlist Error" + SVlist + " Current Talker:" + mCurrentTalker);
            return;
        }

        int mTotalNum = parseInt(values[1]);
        int mMsgInd = parseInt(values[2]);

        if (mTotalNum > 0 && mMsgInd == 1) {
            //clear all SV record
            SVlist.clear();
            mSatelInfoList.put(mCurrentTalker, SVlist);
        }

        int index = 4;
        while (index + 3 < values.length) {
            int satelliteNumber = parseInt(values[index++]);
            float elevation = parseFloat(values[index++]);
            float azimuth = parseFloat(values[index++]);
            float satelliteSnr = 0;
            if (values[index].contains("*")) {
                String[] mStrl = values[index].split("\\*");;
                satelliteSnr = parseFloat(mStrl[0]);
                index++;
            } else {
                satelliteSnr = parseFloat(values[index++]);
            }

            if (satelliteNumber > 0) {
                SatelliteInfo sat = new SatelliteInfo(satelliteNumber, checkTalkerColor(mCurrentTalker));
                sat.mSnr = satelliteSnr;
                sat.mElevation = elevation;
                sat.mAzimuth = azimuth;
                if (isUsedInFix(satelliteNumber)) {
                    sat.mUsedInFix = true;
                }
                SVlist.add(sat);
            }
        }

        if (values[1].equals(values[2])) {
            //report location update
            log("message SV_UPDATE : " + mCurrentTalker + " size:" + SVlist.size());
            mSatelInfoList.put(mCurrentTalker, SVlist);
            sendMessage(SV_UPDATE);
        }
    }

    private void log(String msg) {
        Log.d("nmeaParser", msg);
    }


    public String[] split(String str) {
        String[] result = null;
        try {
            String delims = "[,]";
            result = str.split(delims);
        } catch (Exception e) {
            Log.d("nmeaParser", "split:" + e);
        }
        return result;
    }



    public float parseFloat(String str) {
        float d = 0;
        if (str.equals("")) {
            return d;
        }
        String mStr = removeFirstZero(str);
        try
        {
            d = Float.parseFloat(mStr);
        }
        catch (Exception e) {
            Log.d("nmeaParser", "parseFloat:" + e);
        }
        return d;
    }


    public double parseDouble(String str) {
        double d = 0;
        if (str.equals("")) {
            return d;
        }
        String mStr = removeFirstZero(str);
        try
        {
            d = Double.parseDouble(mStr);
        }
        catch (Exception e) {
            Log.d("nmeaParser", "parseDouble:" + e);
        }
        return d;
    }

    public int parseInt(String str) {
        int d = 0;
        if (str.equals("")) {
            return d;
        }
        String mStr = removeFirstZero(str);
        try
        {
            d = Integer.valueOf(mStr);
        }
        catch (Exception e) {
            Log.d("nmeaParser", "parseDouble:" + e);
        }
        return d;
    }

    public long parseLong(String str) {
        long d = 0;
        if (str.equals("")) {
            return d;
        }
        String mStr = removeFirstZero(str);
        try
        {
            d = Long.parseLong(mStr);
        }
        catch (Exception e) {
            Log.d("nmeaParser", "parseLong:" + e);
        }
        return d;
    }

    private void reportSVupdate() {
        synchronized (mListener) {
            int size = mListener.size();
            for (int i = 0; i < size; i++) {
                NmeaUpdateViewListener listener = mListener.get(i);
                listener.onViewupdateNotify();
            }
        }
    }

    public void addSVUpdateListener(NmeaUpdateViewListener l) {
        synchronized (mListener) {
            mListener.add(l);
        }
    }

    public void removeSVUpdateListener(NmeaUpdateViewListener l) {
        synchronized (mListener) {
            mListener.remove(l);
        }
    }

    private void sendMessage(int what) {
        Message m = new Message();
        m.what = what;
        mHandler.sendMessage(m);
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch(msg.what) {
            case SV_UPDATE: //timer update
                reportSVupdate();
                break;
            default:
                log("WARNING: unknown handle event recv!!");
                break;
            }
        }
    };


    public interface NmeaUpdateViewListener {
        public void onViewupdateNotify();
    }

    public class SVExtraInfro {

        public String mPdop = "";
        public String mHdop = "";
        public String mVdop = "";
        public String mfixtype = "";

        private SVExtraInfro() {
        }
    }


    public class SatelliteInfo {

        int mPrn;
        float mSnr = 0;
        float mElevation = 0;
        float mAzimuth = 0;
        boolean mUsedInFix = false;
        int mColor = 0xffffffff; // white default

        public SatelliteInfo(int prn, int color) {
            mPrn = prn;
            mColor = color;
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("[").append(mPrn).append(", ");
            builder.append(mSnr).append(", ");
            builder.append(mElevation).append(", ");
            builder.append(mAzimuth).append(", ");
            builder.append(mUsedInFix).append("]");
            return builder.toString();
        }
    }
}

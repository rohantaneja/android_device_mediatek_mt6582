package com.mediatek.ygps;

import java.util.Iterator;

public class NmeaSatelliteAdapter implements SatelliteInfoAdapter {

    private Iterator<NmeaParser.SatelliteInfo> mNmeaIterator = null;
    public NmeaSatelliteAdapter(Iterable<NmeaParser.SatelliteInfo> nmeaItr) {
        if (nmeaItr != null) {
            mNmeaIterator = nmeaItr.iterator();
        }
    }

    public Iterator<SatelliteInfo> iterator() {

        return new Iterator<SatelliteInfo>() {

            public boolean hasNext() {
                if (mNmeaIterator == null) {
                    return false;
                }
                return mNmeaIterator.hasNext();
            }

            public SatelliteInfo next() {
                if (mNmeaIterator == null) {
                    return null;
                }
                NmeaParser.SatelliteInfo nmeaSatel = mNmeaIterator.next();
                return toSatelliteInfo(nmeaSatel);
            }

            public void remove() {
                if (mNmeaIterator != null) {
                    mNmeaIterator.remove();
                }
            }

        };
    }

    private SatelliteInfo toSatelliteInfo(NmeaParser.SatelliteInfo nmeaSatel) {
        if (nmeaSatel == null) {
            return null;
        }
        SatelliteInfo satInfo = new SatelliteInfo();
        satInfo.prn = nmeaSatel.mPrn;
        satInfo.snr = nmeaSatel.mSnr;
        satInfo.elevation = nmeaSatel.mElevation;
        satInfo.azimuth = nmeaSatel.mAzimuth;
        satInfo.usedInFix = nmeaSatel.mUsedInFix;
        satInfo.color = nmeaSatel.mColor;
        return satInfo;
    }

}

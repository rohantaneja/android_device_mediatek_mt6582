package com.mediatek.ygps;

import java.util.Iterator;

import android.location.GpsSatellite;

public class GpsSatelliteAdapter implements SatelliteInfoAdapter {
    private Iterator<GpsSatellite> mGpsIterator = null;

    public GpsSatelliteAdapter(Iterable<GpsSatellite> gpsItr) {
        if (gpsItr != null) {
            mGpsIterator = gpsItr.iterator();
        }
    }


    public Iterator<SatelliteInfo> iterator() {

        return new Iterator<SatelliteInfo>() {
            public boolean hasNext() {
                if (mGpsIterator == null) {
                    return false;
                }
                return mGpsIterator.hasNext();
            }

            public SatelliteInfo next() {
                if (mGpsIterator == null) {
                    return null;
                }
                GpsSatellite satel = mGpsIterator.next();
                return toSatelliteInfo(satel);
            }

            public void remove() {
                if (mGpsIterator != null) {
                    mGpsIterator.remove();
                }
            }

        };
    }

    private  SatelliteInfo toSatelliteInfo(GpsSatellite satel) {
        if (satel == null) {
            return null;
        }
        SatelliteInfo satInfo = new SatelliteInfo();
        satInfo.prn = satel.getPrn();
        satInfo.snr = satel.getSnr();
        satInfo.elevation = satel.getElevation();
        satInfo.azimuth = satel.getAzimuth();
        satInfo.usedInFix = satel.usedInFix();
        return satInfo;
    }

}

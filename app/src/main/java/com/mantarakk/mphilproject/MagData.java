package com.mantarakk.mphilproject;

import com.mbientlab.metawear.data.MagneticField;

public class MagData {

    private MagneticField rawData;
    private float xVal;
    private float yVal;
    private float zVal;

    public MagData(MagneticField rawData) {
        this.rawData = rawData;
        this.xVal = rawData.x();
        this.yVal = rawData.y();
        this.zVal = rawData.z();
    }

    public MagneticField getRawData() {
        return rawData;
    }

    public float getxVal() {
        return xVal;
    }

    public float getyVal() {
        return yVal;
    }

    public float getzVal() {
        return zVal;
    }
}

package com.mantarakk.mphilproject;

import java.util.ArrayList;
import com.mbientlab.metawear.Data;
import com.mbientlab.metawear.data.Acceleration;

public class AccelData {

    private Acceleration rawData;
    private float xVal;
    private float yVal;
    private float zVal;

    public AccelData(Acceleration rawData) {
        this.rawData = rawData;
        this.xVal = rawData.x();
        this.yVal = rawData.y();
        this.zVal = rawData.z();
    }

    public Acceleration getRawData() {
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

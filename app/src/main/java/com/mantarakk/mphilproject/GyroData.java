package com.mantarakk.mphilproject;

import com.mbientlab.metawear.data.AngularVelocity;
import com.mbientlab.metawear.module.GyroBmi160;

public class GyroData {

    private AngularVelocity rawData;
    private float xVal;
    private float yVal;
    private float zVal;

    public GyroData(AngularVelocity rawData) {
        this.rawData = rawData;
        this.xVal = rawData.x();
        this.yVal = rawData.y();
        this.zVal = rawData.z();
    }

    public AngularVelocity getRawData() {
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

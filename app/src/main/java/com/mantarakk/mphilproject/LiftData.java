package com.mantarakk.mphilproject;

import java.util.ArrayList;
import com.mbientlab.metawear.Data;

public class LiftData {

    private String placement;
    private String lift;
    private String form;
    private ArrayList<AccelData> accelData;
    private ArrayList<GyroData> gyroData;
    private ArrayList<MagData> magData;
    private ArrayList<Double> elapsedTimes;

    public LiftData(String placement, String lift, String form, ArrayList<Double> elapsedTimes, ArrayList<AccelData> accelData, ArrayList<GyroData> gyroData, ArrayList<MagData> magData) {
        this.placement = placement;
        this.lift = lift;
        this.form = form;
        this.accelData = accelData;
        this.gyroData = gyroData;
        this.magData = magData;
        this.elapsedTimes = elapsedTimes;
    }

    public ArrayList<Double> getElapsedTimes() {
        return elapsedTimes;
    }

    public void setElapsedTimes(ArrayList<Double> elapsedTimes) {
        this.elapsedTimes = elapsedTimes;
    }

    public String getPlacement() {
        return placement;
    }

    public void setPlacement(String placement) {
        this.placement = placement;
    }

    public String getLift() {
        return lift;
    }

    public void setLift(String lift) {
        this.lift = lift;
    }

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public ArrayList<AccelData> getAccelData() {
        return accelData;
    }

    public void setAccelData(ArrayList<AccelData> accelData) {
        this.accelData = accelData;
    }

    public ArrayList<GyroData> getGyroData() {
        return gyroData;
    }

    public void setGyroData(ArrayList<GyroData> gyroData) {
        this.gyroData = gyroData;
    }

    public ArrayList<MagData> getMagData() {
        return magData;
    }

    public void setMagData(ArrayList<MagData> magData) {
        this.magData = magData;
    }
}

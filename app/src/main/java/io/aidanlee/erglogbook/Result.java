package io.aidanlee.erglogbook;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class Result implements Parcelable {
    private final static double C2CONSTANT = 2.80;
    private final static double SPLIT_METERS_CONSTANT = 500;

    private String recordedUnits;
    private Time time;
    private int meters;
    private Time split;
    private int spm;
    private int heartRate;
    private int watts;
    private int cals;

    /**
     @param line: A line of "numbers" or "times" (format ##:##.##) from the erg monitor separated by
     spaces. There must only be 4 or 5 "numbers" or "strings" in total in the following order.

     String must contain:
     - time (time format)
     - meters
     - split (time format), watts, or calories
     - strokes per minute
     String may additionally contain:
     - heart rate

     @param recordedUnits: A String that defines the input type of the recorded units. This will
     be found above the 3rd column in the erg data. This must be "Split", "Watts", or "Cals".
     */
    public Result(String line, String recordedUnits) {
        String[] data = line.split(" ");
        this.time = new Time(data[0]);
        this.meters = Integer.valueOf(data[1]);
        String[] list = recordedUnits.split(" ");
        this.recordedUnits = list[2];
        switch (this.recordedUnits) {
            case "/500m":
                this.split = new Time(data[2]);
                break;
            case "watts":
                this.watts = Integer.valueOf(data[2]);
                break;
            case "cals":
                this.cals = Integer.valueOf(data[2]);
                break;
            default:
                Log.d("RecordedUnitsError", "recordedUnits type is invalid.");
                break;
        }

        makeConversions(this.recordedUnits);

        this.spm = Integer.valueOf(data[3]);

        if(data.length == 5)
            this.heartRate = Integer.valueOf(data[4]);
    }
    public Result() {

    }
    protected Result(Parcel in) {
        recordedUnits = in.readString();
        time = in.readParcelable(Time.class.getClassLoader());
        meters = in.readInt();
        split = in.readParcelable(Time.class.getClassLoader());
        spm = in.readInt();
        heartRate = in.readInt();
        watts = in.readInt();
        cals = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(recordedUnits);
        dest.writeParcelable(time, flags);
        dest.writeInt(meters);
        dest.writeParcelable(split, flags);
        dest.writeInt(spm);
        dest.writeInt(heartRate);
        dest.writeInt(watts);
        dest.writeInt(cals);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Result> CREATOR = new Creator<Result>() {
        @Override
        public Result createFromParcel(Parcel in) {
            return new Result(in);
        }

        @Override
        public Result[] newArray(int size) {
            return new Result[size];
        }
    };

    /**
     * Decides what conversions must be made based on the inputType
     * @param inputType Cals, Watts, Split.
     */
    private void makeConversions(String inputType) {
        if(inputType.equals("watts") || inputType.equals("cals" ))
            calcSplit(inputType);

        if(inputType.equals("/500m") || inputType.equals("watts"))
            calcCals(inputType);

        if(inputType.equals("cals") || inputType.equals("/500m"))
            calcWatts(inputType);
    }

    /**
     * Calculates the split equivalent of the input.
     * @param inputType Watts or Cals.
     */
    private void calcSplit(String inputType) {
        // have: watts
        // calculate: split
        if(inputType.equals("watts")) {
            double pace = (int) (Math.cbrt(C2CONSTANT/watts));
            double seconds = pace * SPLIT_METERS_CONSTANT;
            split = new Time(seconds);
        }
        // have: cals
        // calculate: split
        if(inputType.equals("cals")) {
            if (watts == 0)
                calcWatts("cals");
            calcSplit("watts");
        }
    }

    /**
     * Calculates the wattage equivalent of the input.
     * @param inputType: cals or splits;
     */
    private void calcWatts(String inputType) {
        if(inputType.equals("cals")) {
            double decHr = (time.getSec()/3600) + (time.getMin()/60) + time.getHr(); // total time represented in hours
            watts = (int) ((1.1639*(cals - 300*decHr))/4);
        }
        if(inputType.equals("/500m")) {
            double pace = split.getSec()/SPLIT_METERS_CONSTANT;
            watts = (int) (C2CONSTANT/(pace*pace*pace));
        }
    }

    /**
     * Calculates the calorie equivalent of the input.
     * @param inputType Splits or Watts.
     */
    private void calcCals(String inputType) {
        if(inputType.equals("watts")) {
            double decHr = (time.getSec()/3600) + (time.getMin()/60) + time.getHr(); // total time represented in hours
            cals = (int) (((4*watts)/1.1639) + (300*decHr));
        }
        if(inputType.equals("/500m")) {
            if (watts == 0)
                calcWatts("/500m");
            calcCals("watts");
        }
    }
    public int getMeters() {
        return meters;
    }
    public int getSpm() {
        return spm;
    }
    public int getHeartRate() {
        return heartRate;
    }
    public int getWatts() {
        return watts;
    }
    public int getCals() {
        return cals;
    }
    public Time getTime() {
        return time;
    }
    public Time getSplit() {
        return split;
    }
    public void setTime(Time time) {
        this.time = time;
    }
    public void setSplit(Time split) {
        this.split = split;
    }
    public void setMeters(int meters) {
        this.meters = meters;
    }
    public void setSpm() {
        this.spm = spm;
    }
    public void setHeartRate(int hr) {
        this.heartRate = hr;
    }
    public void setWatts(int watts) {
        this.watts = watts;
    }
    public void setCals(int cals) {
        this.cals = cals;
    }
    public void setCalsAndConvert(int cals) {
        this.cals = cals;
        makeConversions("cals");
    }
    public void setWattsAndConvert(int watts) {
        this.watts = watts;
        makeConversions("watts");
    }
    public void setSplitAndConvert(Time split) {
        this.split = split;
        makeConversions("/500m");
    }

    @Override
    public String toString() {
        return time.toString() + "    " + meters + "     " + split.toString() + "     " + spm;
    }

    public String getRecordedUnits() {
        return recordedUnits;
    }

    public void setRecordedUnits(String recordedUnits) {
        this.recordedUnits = recordedUnits;
    }

    public void setSpm(int spm) {
        this.spm = spm;
    }

}

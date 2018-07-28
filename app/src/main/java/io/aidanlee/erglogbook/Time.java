package io.aidanlee.erglogbook;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Aidan Lee on 12/13/2017.
 */

public class Time implements Parcelable {
    private int hr = 0;     // Hours
    private int min = 0;    // Minutes
    private int sec = 0;    // Seconds
    private int ms = 0;     // Milliseconds

    /**
     * Separates the information of a time string into hours, minutes, seconds, and milliseconds.
     * @param timeString a string representing a time (format ##:##:##.##)
     */
    public Time(String timeString) { // Takes input with format HR:MIN:SEC.MS or MIN:SEC.MS
        // Current index we are assigning
        int index = 0;
        // As we iterate the string we do not know which number is which unit yet
        int[] times = new int[4];
        // Current time to concatenate characters to
        StringBuilder ct = new StringBuilder();
        // Records if the string has a milliseconds
        Boolean hasMs = false;
        for(int i = 0; i < timeString.length(); i++) {
            char c = timeString.charAt(i);
            if(c == ':') {
                times[index] = Integer.parseInt(ct.toString());
                ct = new StringBuilder();
                index++;
            }
            else if(c == '.'){
                times[index] = Integer.parseInt(ct.toString());
                ct = new StringBuilder();
                index++;
                hasMs = true;
            }
            else {
                ct.append(c);
            }
        }
        times[index] = Integer.parseInt(ct.toString());
        if(hasMs) {
            if (index == 3) {
                hr = times[0];
                min = times[1];
                sec = times[2];
                ms = times[3];
            }
            if (index == 2) {
                hr = 0;
                min = times[0];
                sec = times[1];
                ms = times[2];
            }
            if (index == 1) {
                hr = 0;
                min = 0;
                sec = times[0];
                ms = times[1];
            }
        }
        else {
            if (index == 2) {
                hr = times[0];
                min = times[1];
                sec = times[2];
                ms = 0;
            }
            if (index == 1) {
                hr = 0;
                min = times[0];
                sec = times[1];
                ms = 0;
            }
        }
    }
    public Time() {

    }
    public Time(double seconds) {
        hr = (int) (seconds / 3600);
        seconds = seconds % 3600;
        min = (int) (seconds / 60);
        seconds = seconds % 60;
        sec = (int) seconds;
        seconds = seconds - sec;
        ms = (int) seconds * 10;
    }

    protected Time(Parcel in) {
        hr = in.readInt();
        min = in.readInt();
        sec = in.readInt();
        ms = in.readInt();
    }

    public static final Creator<Time> CREATOR = new Creator<Time>() {
        @Override
        public Time createFromParcel(Parcel in) {
            return new Time(in);
        }

        @Override
        public Time[] newArray(int size) {
            return new Time[size];
        }
    };

    public int getMin() {
        return min;
    }
    public int getHr() {
        return hr;
    }
    public int getSec() {
        return sec;
    }
    public int getMs() {
        return ms;
    }
    public void setMin(int min) {
        this.min = min;
    }
    public void setHr(int hr) {
        this.hr = hr;
    }
    public void setSec(int sec) {
        this.sec = sec;
    }
    public void setMs(int ms) {
        this.ms = ms;
    }
    @Override
    public String toString() {
        String displayString = "";
        if(hr != 0)
            displayString += (hr + ":");
        displayString += (min + ":");
        displayString += sec;
        displayString += ms;
        return displayString;
    }

    /**
     * Describe the kinds of special objects contained in this Parcelable
     * instance's marshaled representation. For example, if the object will
     * include a file descriptor in the output of {@link #writeToParcel(Parcel, int)},
     * the return value of this method must include the
     * {@link #CONTENTS_FILE_DESCRIPTOR} bit.
     *
     * @return a bitmask indicating the set of special object types marshaled
     * by this Parcelable object instance.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Flatten this object in to a Parcel.
     *
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(hr);
        dest.writeInt(min);
        dest.writeInt(sec);
        dest.writeInt(ms);
    }
}

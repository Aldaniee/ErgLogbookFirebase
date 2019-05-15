package io.aidanlee.erglogbook;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Entry implements Parcelable {
    // UNIQUE IDENTIFYING VALUES
    private String username;
    private String key;

    // OPTIONAL VALUES
    private String workout;
    private String photoUrl;
    private Result overall;
    private List<Result> breakdown;

    public Entry(String username, String key) {
        this.username = username;
        this.key = key;
    }
    public Entry(String username, String key, String photoUrl) {
        this.username = username;
        this.key = key;
        this.photoUrl = photoUrl;
    }

    public Entry() {
    }

    protected Entry(Parcel in) {
        username = in.readString();
        key = in.readString();
        workout = in.readString();
        photoUrl = in.readString();
        overall = in.readParcelable(Result.class.getClassLoader());
        breakdown = in.createTypedArrayList(Result.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(username);
        dest.writeString(key);
        dest.writeString(workout);
        dest.writeString(photoUrl);
        dest.writeParcelable(overall, flags);
        dest.writeTypedList(breakdown);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Entry> CREATOR = new Creator<Entry>() {
        @Override
        public Entry createFromParcel(Parcel in) {
            return new Entry(in);
        }

        @Override
        public Entry[] newArray(int size) {
            return new Entry[size];
        }
    };

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getWorkout() {
        return workout;
    }

    public void setWorkout(String workout) {
        this.workout = workout;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Result getOverall() {
        return overall;
    }

    public void setOverall(Result overall) {
        this.overall = overall;
    }

    public List<Result> getBreakdown() {
        return breakdown;
    }

    public void setBreakdown(List<Result> breakdown) {
        this.breakdown = breakdown;
    }
    @Exclude
    public void setBreakdownString(String[] breakdownResults, int breakdownLines, String recordedUnits) {
        this.breakdown = new ArrayList<Result>();
        for (int i = 0; i < breakdownResults.length; i++) {
            if(!breakdownResults[i].equals(""))
                breakdown.add(new Result(breakdownResults[i], recordedUnits));
        }
    }
    @Exclude
    public void setOverallString(String overallResultLine, String recordedUnits) {
        this.overall = new Result(overallResultLine, recordedUnits);
    }
}

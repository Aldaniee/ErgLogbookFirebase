package io.aidanlee.erglogbook;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class EntryDisplayActivity extends AppCompatActivity {
    public static final int DEFAULT_EDIT_TEXT_LENGTH_LIMIT = 1000;
    private static final String TAG = "EntryDisplayActivity";
    private static final String ENTRY_EXTRA = "entry_extra";
    public static final int RC_EDIT_ENTRY = 3;
    private static final int DEFAULT_EDIT_POSITION = -1;

    private static final int BUTTON_STATUS_EXIT = 0;
    private static final int BUTTON_STATUS_DONE = 1;
    private static final int BUTTON_STATUS_SAVE_EXIT = 2;


    private List<Result> results;
    private ResultAdapter resultAdapter;
    private ListView resultListView;
    private Entry entry;
    private String editAttribute;
    private int editPosition = DEFAULT_EDIT_POSITION;
    private boolean entryChanged = false;

    private TextView date;
    private TextView workout;
    private EditText writeEditText;

    private Button doneSaveButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_display);
        entry = getIntent().getParcelableExtra(ENTRY_EXTRA);
        //date = findViewById(R.id.lbl_date);
        //date.setText(displayTimeAndDate(entry.getKey()));
        workout = findViewById(R.id.lbl_workout);
        if (entry.getWorkout() != null)
            workout.setText(entry.getWorkout());
        else
            workout.setText(R.string.lbl_workout_not_found);
        workout.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        writeEditText.setEnabled(true);
                        editAttribute = "workout";
                        editPosition = DEFAULT_EDIT_POSITION;
                    }
                });
        results = new ArrayList<Result>();
        results.add(entry.getOverall());
        results.addAll(entry.getBreakdown());
        resultAdapter = new ResultAdapter(this, R.layout.item_result, results, new TextClickListener() {
            @Override
            public void onTxtClick(int position, String label) {
                writeEditText.setEnabled(true);
                editBreakdown(position, label);
            }
        });
        resultListView = findViewById(R.id.resultListView);
        resultListView.setAdapter(resultAdapter);
        doneSaveButton = findViewById(R.id.saveDoneButton);
        doneSaveButton.setEnabled(true);
        doneSaveButton.setTag(BUTTON_STATUS_EXIT);
        doneSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if((int) v.getTag() == BUTTON_STATUS_DONE) {
                    if(!writeEditText.getText().toString().equals("")) {
                        changeEntry(writeEditText.getText().toString(), editAttribute, editPosition);
                        closeKeyboard();
                        writeEditText.setText("");
                        doneSaveButton.setTag(BUTTON_STATUS_SAVE_EXIT);
                    }
                }
                else {
                    Intent intent = new Intent();
                    if(entryChanged)
                        intent.putExtra(ENTRY_EXTRA, entry);
                    setResult(RC_EDIT_ENTRY, intent);
                    finish();
                }
            }
        });

        writeEditText = findViewById(R.id.writeEditText);
        writeEditText.setEnabled(false);
        writeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    doneSaveButton.setText(R.string.lbl_done_btn);
                    doneSaveButton.setTag(BUTTON_STATUS_DONE);
                } else if(entryChanged) {
                    doneSaveButton.setText(R.string.lbl_save_and_exit_btn);
                    doneSaveButton.setTag(BUTTON_STATUS_SAVE_EXIT);
                } else {
                    doneSaveButton.setText(R.string.lbl_exit_btn);
                    doneSaveButton.setTag(BUTTON_STATUS_EXIT);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        writeEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_EDIT_TEXT_LENGTH_LIMIT)});

    }
    private void closeKeyboard() {
        View v = this.getCurrentFocus();
        if(v != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            assert imm != null;
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

/*
    private String displayTimeAndDate(String timeAndDate) {
        String[] split = timeAndDate.split("/");
        String year = split[0];
        String month = split[1];
        String day = split[2];
        if (day.charAt(0) == '0')
            day = day.substring(1);
        String time = split[3];
        time = time.substring(0, 5);
        String ending;
        String AMPM = "am";
        int hours = Integer.parseInt(time.substring(0, 2));
        if (hours > 12) {
            hours = hours - 12;
            AMPM = "pm";
            time = hours + time.substring(2);
        }
        switch (month) {
            case "01": month = "January"; break;
            case "02": month = "February"; break;
            case "03": month = "March"; break;
            case "04": month = "April"; break;
            case "05": month = "May"; break;
            case "06": month = "June"; break;
            case "07": month = "July"; break;
            case "08": month = "August"; break;
            case "09": month = "September"; break;
            case "10": month = "October"; break;
            case "11": month = "November"; break;
            case "12": month = "December"; break;
        }
        switch (day) {
            case "01":
                ending = "st";
                break;
            case "02":
                ending = "nd";
                break;
            case "03":
                ending = "rd";
                break;
            default:
                ending = "th";
        }
        String formatted = time + AMPM + " " + month + " " + day + ending + " " + year;
        return formatted;
    }
*/
    public void editBreakdown(int position, String label) {
        if (position != 0) {
            switch (label) {
                case "time":
                    writeEditText.setText(entry.getBreakdown().get(position - 1).getTime().toString());
                    break;
                case "meters":
                    writeEditText.setText(entry.getBreakdown().get(position - 1).getMeters() + "");
                    break;
                case "measurement":
                    writeEditText.setText(entry.getBreakdown().get(position - 1).getSplit().toString());
                    break;
                case "spm":
                    writeEditText.setText(entry.getBreakdown().get(position - 1).getSpm() + "");
                    break;
                case "hr":
                    writeEditText.setText(entry.getBreakdown().get(position - 1).getHeartRate() + "");
                    break;
            }
        } else {
            switch (label) {
                case "time":
                    writeEditText.setText(entry.getOverall().getTime().toString());
                    break;
                case "meters":
                    writeEditText.setText("" + entry.getOverall().getMeters());
                    break;
                case "measurement":
                    writeEditText.setText(entry.getOverall().getSplit().toString());
                    break;
                case "spm":
                    writeEditText.setText(entry.getOverall().getSpm() + "");
                    break;
                case "hr":
                    writeEditText.setText(entry.getOverall().getHeartRate() + "");
                    break;
            }
        }
        editAttribute = label;
        editPosition = position;
    }

    private void changeEntry(String text, String attribute, int position) {
        if(position == DEFAULT_EDIT_POSITION) {
            switch (attribute) {
                case "workout":
                    entry.setWorkout(text);
                    break;
                case "date":
                    entry.setKey(text);
                    break;
            }
        }
        else if (position == 0) {
            switch (attribute) {
                case "time":
                    entry.getOverall().setTime(new Time(text));
                    break;
                case "meters":
                    entry.getOverall().setMeters(Integer.valueOf(text));
                    break;
                case "measurement":
                    entry.getOverall().setSplitAndConvert(new Time(text));
                    break;
                case "spm":
                    entry.getOverall().setSpm(Integer.valueOf(text));
                    break;
                case "hr":
                    entry.getOverall().setHeartRate(Integer.valueOf(text));
                    break;
            }
        } else {
            switch (attribute) {
                case "time":
                    entry.getBreakdown().get(position - 1).setTime(new Time(text));
                    break;
                case "meters":
                    entry.getBreakdown().get(position - 1).setMeters(Integer.valueOf(text));
                    break;
                case "measurement":
                    entry.getBreakdown().get(position - 1).setSplit(new Time(text));
                    break;
                case "spm":
                    entry.getBreakdown().get(position - 1).setSpm(Integer.valueOf(text));
                    break;
                case "hr":
                    entry.getBreakdown().get(position - 1).setHeartRate(Integer.valueOf(text));
                    break;
            }
        }
        entryChanged = true;
    }
    private void makeToast(String input) {
        Toast.makeText(getApplicationContext(), input, Toast.LENGTH_SHORT).show();
    }
}

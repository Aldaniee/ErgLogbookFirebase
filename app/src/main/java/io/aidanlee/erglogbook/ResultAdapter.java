package io.aidanlee.erglogbook;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ResultAdapter extends ArrayAdapter<Result> {
    private TextClickListener textClickListener;
    public ResultAdapter(Context context, int resource, List<Result> objects, TextClickListener textClickListener) {
        super(context, resource, objects);
        this.textClickListener = textClickListener;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_result, parent, false);
        }

        TextView timeTextView = convertView.findViewById(R.id.timeTextView);
        TextView meterTextView = convertView.findViewById(R.id.metersTextView);
        TextView measurementTextView = convertView.findViewById(R.id.measurementTextView);
        TextView rateTextView = convertView.findViewById(R.id.rateTextView);
        TextView hrTextView = convertView.findViewById(R.id.hrTextView);

        final Result result = getItem(position);
        timeTextView.setText(result.getTime().toString());
        meterTextView.setText(Integer.toString(result.getMeters()));
        measurementTextView.setText(result.getSplit().toString());
        rateTextView.setText(Integer.toString(result.getSpm()));
        hrTextView.setText(Integer.toString(result.getHeartRate()));

        timeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(textClickListener != null)
                    textClickListener.onTxtClick(position, "time");
            }
        });
        meterTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(textClickListener != null)
                    textClickListener.onTxtClick(position, "meters");
            }
        });
        measurementTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(textClickListener != null)
                    textClickListener.onTxtClick(position, "measurement");
            }
        });
        rateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(textClickListener != null)
                    textClickListener.onTxtClick(position, "rate");
            }
        });
        hrTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(textClickListener != null)
                    textClickListener.onTxtClick(position, "hr");
            }
        });

        return convertView;
    }
}

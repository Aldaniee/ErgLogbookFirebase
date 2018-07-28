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

public class EntryAdapter extends ArrayAdapter<Entry> {
    public EntryAdapter(Context context, int resource, List<Entry> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_entry, parent, false);
        }

        ImageView photoImageView = convertView.findViewById(R.id.photoImageView);
        TextView workoutTextView = convertView.findViewById(R.id.workoutTextView);
        TextView authorTextView = convertView.findViewById(R.id.nameTextView);

        Entry entry = getItem(position);
        Glide.with(photoImageView.getContext())
                .load(entry.getPhotoUrl())
                .into(photoImageView);
        workoutTextView.setText(entry.getWorkout());
        authorTextView.setText(entry.getUsername());

        return convertView;
    }
}

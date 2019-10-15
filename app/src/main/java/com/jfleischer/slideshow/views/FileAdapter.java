package com.jfleischer.slideshow.views;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jfleischer.slideshow.R;
import com.jfleischer.slideshow.models.FileActivityMode;
import com.jfleischer.slideshow.models.FileItem;

import java.util.LinkedList;

public class FileAdapter extends BaseAdapter {
    private final LinkedList<FileItem> mItems;
    private final LayoutInflater mInflater;
    private final FileActivityMode mMode;

    public FileAdapter(LayoutInflater inflater, FileActivityMode mode) {
        mInflater = inflater;
        mItems = new LinkedList<FileItem>();
        mMode = mode;
    }

    public void clear() {
        mItems.clear();
    }

    public void add(FileItem item) {
        mItems.add(item);
        notifyDataSetChanged();
    }

    public int getCount() {
        return mItems.size();
    }

    public Object getItem(int i) {
        return null;
    }

    public long getItemId(int arg0) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View v;
        if (convertView == null) {
            v = mInflater.inflate(R.layout.picker_entry, null);
        } else {
            v = convertView;
        }
        final FileItem item = mItems.get(position);
        ((TextView) v.findViewById(R.id.name)).setText(item.name);
        ImageView icon = v.findViewById(R.id.icon);
        switch (item.type) {
            case DOC:
                switch (mMode) {
                    case Add:
                        ((ImageView) v.findViewById(R.id.icon_info)).setImageResource(R.drawable.add);
                        break;
                    case Delete:
                        ((ImageView) v.findViewById(R.id.icon_info)).setImageResource(R.drawable.delete);
                        break;
                }
                icon.setImageResource(R.drawable.ic_doc);
                break;
            case DIR:
                ((ImageView) v.findViewById(R.id.icon_info)).setImageResource(android.R.color.transparent);
                icon.setImageResource(R.drawable.ic_dir);
                break;
            case PARENT:
                ((ImageView) v.findViewById(R.id.icon_info)).setImageResource(android.R.color.transparent);
                icon.setImageResource(R.drawable.ic_arrow_up);
                break;
        }
        icon.setColorFilter(Color.argb(255, 0, 0, 0));
        return v;
    }
}

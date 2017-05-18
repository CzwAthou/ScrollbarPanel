package com.athou.scrollbarpanel;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements ScrollbarPanelListView.OnPositionChangedListener {

    ScrollbarPanelListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ScrollbarPanelListView) findViewById(android.R.id.list);
        listView.setAdapter(new DummyAdapter());
        listView.setCacheColorHint(Color.TRANSPARENT);
        listView.setOnPositionChangedListener(this);
    }

    private class DummyAdapter extends BaseAdapter {

        private int mNumDummies = 100;

        @Override
        public int getCount() {
            return mNumDummies;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.list_item, parent, false);
            }
            TextView textView = (TextView) convertView;
            textView.setText("" + position);
            return convertView;
        }
    }

    @Override
    public void onPositionChanged(ScrollbarPanelListView listView, int firstVisiblePosition, View scrollBarPanel) {
        ((TextView) scrollBarPanel).setText("Position " + firstVisiblePosition);
    }
}

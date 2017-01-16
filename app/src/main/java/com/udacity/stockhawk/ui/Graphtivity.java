package com.udacity.stockhawk.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.DbHelper;

import java.util.ArrayList;


public class Graphtivity extends Activity implements
        OnChartGestureListener, OnChartValueSelectedListener {

    public String symbols, history;

    private LineChart mChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graphtivity);

        getBundles();

        setChart();
    }

    public void setChart(){



        Context context = getApplicationContext();
        mChart = (LineChart) findViewById(R.id.chart);

        mChart.clear();

        mChart.setOnChartGestureListener(this);
        mChart.setOnChartValueSelectedListener(this);
        mChart.setDrawGridBackground(false);

        mChart.getDescription().setEnabled(true);
        mChart.setContentDescription(symbols);

        mChart.setTouchEnabled(true);

        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);

        mChart.setPinchZoom(true);

        setData();
        LimitLine llXAxis = new LimitLine(10f, "Index 10");
        llXAxis.setLineWidth(4f);
        llXAxis.enableDashedLine(10f, 10f, 0f);
        llXAxis.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        llXAxis.setTextSize(10f);

        XAxis xAxis = mChart.getXAxis();

        LimitLine ll1 = new LimitLine(maxLimit, context.getString(R.string.graph_high)+": "+maxLimit);
        ll1.setLineWidth(3f);
        ll1.enableDashedLine(10f, 10f, 0f);
        ll1.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_TOP);
        ll1.setTextSize(20f);

        LimitLine ll2 = new LimitLine(minLimit, context.getString(R.string.graph_low)+": "+minLimit);
        ll2.setLineWidth(3f);
        ll2.enableDashedLine(10f, 10f, 0f);
        ll2.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_BOTTOM);
        ll2.setTextSize(20f);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.removeAllLimitLines();
        leftAxis.addLimitLine(ll1);
        leftAxis.addLimitLine(ll2);
        leftAxis.setAxisMaximum(maxLimit+10f);
        leftAxis.setAxisMinimum(minLimit-10f);

        leftAxis.enableGridDashedLine(12f, 5f, 0f);
        leftAxis.setDrawZeroLine(false);


        leftAxis.setDrawLimitLinesBehindData(true);

        mChart.getAxisRight().setEnabled(false);

        mChart.animateX(2500);

        Legend l = mChart.getLegend();

        l.setForm(Legend.LegendForm.LINE);
}

    public void getBundles(){

        Intent intent = getIntent();
        String message = intent.getStringExtra("symbol");

        Log.v("krkeco",message);
        Toast.makeText(this, message,
                Toast.LENGTH_LONG).show();

        DbHelper dbhelper = new DbHelper(this);
        Cursor cursor = dbhelper.getReadableDatabase().query("quotes",

                new String[] { Contract.Quote._ID, Contract.Quote.COLUMN_SYMBOL, Contract.Quote.COLUMN_PRICE, Contract.Quote.COLUMN_PERCENTAGE_CHANGE, Contract.Quote.COLUMN_HISTORY},

                null, null, null, null, null);


        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {

    symbols = cursor.getString(1);

    Log.v("krkeco", "check "+message + " is " + symbols);
    if(symbols.equals(message)){

        Log.v("krkeco", message + " is " + symbols);

        history = cursor.getString(4);
        break;

    }
            cursor.moveToNext();
}

        cursor.close();


    }

    public static int valueFinish, valueStart;

   public ArrayList<Entry> values;
    float maxLimit;
    float minLimit;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }


   private void setData() {

        if(history !=null) {

            values = new ArrayList<Entry>();

            int set = 0;

            boolean first = true;

            valueFinish = history.length()-1;

            for (int x = history.length()-1; x >1 ; x--) {

                String valueString;
                Character c = history.charAt(x);
                if ((c == ',')
                        && first) {
                    valueStart = x + 2;
                    first = false;
                    valueString = history.substring(valueStart, valueFinish);
                    Log.v("krkeco","value:"+valueString+":");

                    Float valueFloat = Float.parseFloat(valueString);

                    values.add(new Entry(set+1, valueFloat));
                    if (set == 0) {
                        maxLimit = valueFloat;
                        minLimit = valueFloat;
                    }
                    if (maxLimit < valueFloat) {
                        maxLimit = valueFloat;
                    }
                    if (minLimit > valueFloat) {
                        minLimit = valueFloat;
                    }
                    set += 1;
                } else if (c == '\n'
                        && !first) {
                    first = true;
                    valueFinish = x-1;

                }

            }
        }else{
            Log.v("krkeco","quote history is not found");
        }

        LineDataSet set1;

        if (mChart.getData() != null &&
                mChart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet)mChart.getData().getDataSetByIndex(0);
            set1.setValues(values);
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
        } else {
            set1 = new LineDataSet(values, symbols);

            set1.enableDashedLine(10f, 5f, 0f);
            set1.enableDashedHighlightLine(10f, 5f, 0f);
            set1.setColor(Color.BLACK);
            set1.setCircleColor(Color.BLACK);
            set1.setLineWidth(2f);
            set1.setCircleRadius(3f);
            set1.setDrawCircleHole(false);
            set1.setValueTextSize(9f);
            set1.setDrawFilled(true);
            set1.setFormLineWidth(1f);
            set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            set1.setFormSize(15.f);

                set1.setFillColor(Color.BLACK);

            ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
            dataSets.add(set1);
            LineData data = new LineData(dataSets);

            mChart.setData(data);
        }
    }

    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        Log.i("Gesture", "START, x: " + me.getX() + ", y: " + me.getY());
    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        Log.i("Gesture", "END, lastGesture: " + lastPerformedGesture);

         if(lastPerformedGesture != ChartTouchListener.ChartGesture.SINGLE_TAP)
            mChart.highlightValues(null); // or highlightTouch(null) for callback to onNothingSelected(...)
    }

    @Override
    public void onChartLongPressed(MotionEvent me) {
        Log.i("LongPress", "Chart longpressed.");
    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {
        Log.i("DoubleTap", "Chart double-tapped.");
    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {
        Log.i("SingleTap", "Chart single-tapped.");
    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
        Log.i("Fling", "Chart flinged. VeloX: " + velocityX + ", VeloY: " + velocityY);
    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
        Log.i("Scale / Zoom", "ScaleX: " + scaleX + ", ScaleY: " + scaleY);
    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {
        Log.i("Translate / Move", "dX: " + dX + ", dY: " + dY);
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Log.i("Entry selected", e.toString());
        Log.i("LOWHIGH", "low: " + mChart.getLowestVisibleX() + ", high: " + mChart.getHighestVisibleX());
        Log.i("MIN MAX", "xmin: " + mChart.getXChartMin() + ", xmax: " + mChart.getXChartMax() + ", ymin: " + mChart.getYChartMin() + ", ymax: " + mChart.getYChartMax());
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");

}

}

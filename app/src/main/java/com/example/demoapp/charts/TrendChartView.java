package com.example.demoapp.charts;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * 可复用折线图组件
 * 支持默认 7 天 / 切换 30 天
 * X 轴：7 天显示星期，30 天显示 MM/DD
 * Y 轴数据可自定义
 * 可在多处复用
 */
public class TrendChartView extends LineChart {

    private int days = 7; // 默认显示 7 天
    private List<Float> trendData = new ArrayList<>();
    private String label = "Trend";

    public TrendChartView(Context context) {
        super(context);
        initChart();
    }

    public TrendChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initChart();
    }

    public TrendChartView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initChart();
    }

    /**
     * 初始化图表样式
     */
    private void initChart() {
        // 基础设置
        getDescription().setEnabled(false);
        setTouchEnabled(true);
        setPinchZoom(true);
        getAxisRight().setEnabled(false); // 禁用右侧 Y 轴

        // X 轴
        XAxis xAxis = getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);

        // Y 轴
        YAxis leftAxis = getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGranularity(1f);
        leftAxis.setAxisMinimum(0f); // 从 0 开始
    }

    /**
     * 设置数据
     * @param trendData 趋势数据列表，长度与 days 对应
     * @param label 图例标签
     * @param days 显示天数 (7 或 30)
     */
    public void setTrendData(List<Float> trendData, String label, int days) {
        this.trendData.clear();
        this.trendData.addAll(trendData);
        this.label = label;
        this.days = days;
        updateChart();
    }

    /**
     * 刷新图表显示
     */
    private void updateChart() {
        ArrayList<Entry> entries = new ArrayList<>();
        for (int i = 0; i < trendData.size(); i++) {
            entries.add(new Entry(i, trendData.get(i)));
        }

        LineDataSet dataSet = new LineDataSet(entries, label);
        dataSet.setColor(Color.BLUE);
        dataSet.setCircleColor(Color.parseColor("#CBC3E3"));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setValueTextSize(10f);
        dataSet.setDrawValues(days <= 7);

        LineData lineData = new LineData(dataSet);
        setData(lineData);

        // 设置 X 轴显示
        getXAxis().setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                Calendar cal = Calendar.getInstance();
                int intValue = (int) Math.floor(value); // 将 float 转 int
                cal.add(Calendar.DAY_OF_YEAR, intValue - (days - 1));

                if (days <= 7) {
                    switch (cal.get(Calendar.DAY_OF_WEEK)) {
                        case Calendar.MONDAY: return "Mon";
                        case Calendar.TUESDAY: return "Tue";
                        case Calendar.WEDNESDAY: return "Wed";
                        case Calendar.THURSDAY: return "Thu";
                        case Calendar.FRIDAY: return "Fri";
                        case Calendar.SATURDAY: return "Sat";
                        case Calendar.SUNDAY: return "Sun";
                    }
                } else {
                    int month = cal.get(Calendar.MONTH) + 1;
                    int day = cal.get(Calendar.DAY_OF_MONTH);
                    return month + "/" + day;
                }
                return "";
            }
        });


        getXAxis().setLabelRotationAngle(-45f); // 倾斜显示，防止重叠
        getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

        // 刷新图表
        invalidate();
    }

    /**
     * 切换天数
     */
    public void toggleDays() {
        if (days == 7) {
            days = 30;
        } else {
            days = 7;
        }
        updateChart();
    }
}

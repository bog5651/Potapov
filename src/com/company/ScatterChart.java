package com.company;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class ScatterChart extends JFrame {
    private final ArrayList<ArrayList<DoublePoint>> arg;
    private final ChartPanel panel;
    private String postfix = "";

    public ScatterChart(String title, ArrayList<ArrayList<DoublePoint>> points) {
        super(title);
        this.arg = points;
        XYDataset dataset = createDataset();

        JFreeChart chart = ChartFactory.createXYLineChart(
                "",
                "X-Axis",
                "Y-Axis",
                dataset
        );
        final XYPlot plot = chart.getXYPlot();
        plot.setRenderer(new XYLineAndShapeRenderer());
        panel = new ChartPanel(chart);

        setSize(1600, 900);
        setContentPane(panel);
    }

    public void setLabels(String xLabel, String yLabel) {
        XYPlot plot = panel.getChart().getXYPlot();
        plot.getDomainAxis().setLabel(xLabel);
        plot.getRangeAxis().setLabel(yLabel);
    }

    public void setLinePostfix(String postfix) {
        this.postfix = postfix;
        XYDataset dataset = createDataset();
        panel.getChart().getXYPlot().setDataset(dataset);
    }

    private XYDataset createDataset() {
        final XYSeriesCollection dataset = new XYSeriesCollection();

        for (int i = 0; i < arg.size(); i++) {
            final XYSeries ser = new XYSeries(String.format("%d %s", i, postfix));

            ArrayList<DoublePoint> points = arg.get(i);
            for (DoublePoint point : points) {
                ser.add(point.getX(), point.getY());
            }
            dataset.addSeries(ser);
        }

        return dataset;
    }
}

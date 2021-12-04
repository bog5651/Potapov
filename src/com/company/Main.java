package com.company;

import com.company.Core.ResultOfProcess;

import javax.swing.*;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

import static com.company.Utils.printArray;

public class Main {
    public static final boolean useDefault = true;

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        Core core = new Core();
        if (!useDefault) {
            while (true) {
                try {
                    System.out.print("m = ");
                    core.withMCount(in.nextInt());
                    System.out.print("q = ");
                    core.withQ(in.nextInt());
                    System.out.print("tf = ");
                    core.withTF(in.nextInt());
                    System.out.print("e = ");
                    core.withE(in.nextDouble());
                    System.out.print("func(i, t) = ");
                    core.withLambdaFunction(in.next());
                    break;
                } catch (InputMismatchException ex) {
                    in.reset();
                    in.next();
                    System.out.println("Не число");
                }
            }
        }

        ArrayList<Integer> nList = new ArrayList<>();
        if (useDefault) {
            nList.add(1);
            nList.add(1);
            nList.add(1);
            nList.add(1);
        } else {
            for (int i = 0; i < core.q; i++) {
                System.out.printf("n(%d) = ", i);
                int n = in.nextInt();
                nList.add(n);
            }
        }

        if (useDefault) {
            core.withMCount(4)
                    .withQ(4)
                    .withTF(2)
                    .withE(0.01)
                    .withLambdaFunction("i+t/3");
        }

        ResultOfProcess result = core
                .withNList(nList)
                .withDebugLevel(Core.DebugLevel.LOW)
                .process();

        ArrayList<ArrayList<DoublePoint>> set = new ArrayList<>();
        for (Double[] pSystemMaxRow : result.pSystem) {
            //get all <p> for <k> errors
            ArrayList<DoublePoint> row = new ArrayList<>();
            for (int v = 0; v < pSystemMaxRow.length; v++) {
                Double val = pSystemMaxRow[v];
                row.add(new DoublePoint(v, val));
            }
            set.add(row);
        }

        String finalSLine = printArray("s =", result.sLine);

        ScatterChart pChart = new ScatterChart(finalSLine, set);
        pChart.setLabels("Время", "Вероятность");
        pChart.setLinePostfix("k");
        pChart.setVisible(true);
        pChart.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        set = new ArrayList<>();
        for (int i = 0; i < core.q; i++) {
            ArrayList<DoublePoint> row = new ArrayList<>();
            for (int v = 0; v <= result.r; v++) {
                row.add(new DoublePoint(v, core.lambda(i, core.t[v])));
            }
            set.add(row);
        }

        ScatterChart lambdaChart = new ScatterChart(finalSLine, set);
        lambdaChart.setLabels("Время", "lambda");
        lambdaChart.setVisible(true);
        lambdaChart.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
}

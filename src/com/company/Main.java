package com.company;

import com.company.Core.ResultOfProcess;
import com.company.Utils.GridBagHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

import static com.company.Utils.printArray;

public class Main {
    public static final boolean useDefault = true;
    public static final boolean useConsoleInput = false;

    public static void main(String[] args) {
        Core core = new Core();
        ArrayList<Integer> nList = new ArrayList<>();

        JFrame progress = createProgressFrame();
        if (useDefault) {
            core.withMCount(4)
                    .withQ(4)
                    .withTF(1)
                    .withE(0.01)
                    .withLambdaFunction("i+t/3");
            nList.add(1);
            nList.add(1);
            nList.add(1);
            nList.add(1);

            core.withNList(nList).withDebugLevel(Core.DebugLevel.LOW);

            Thread runThread = new Thread(() -> run(core, progress));
            progress.setVisible(true);
            runThread.start();
        } else if (useConsoleInput) {
            Scanner in = new Scanner(System.in);
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
            for (int i = 0; i < core.q; i++) {
                System.out.printf("n(%d) = ", i);
                int n = in.nextInt();
                nList.add(n);
            }

            Thread runThread = new Thread(() -> run(core, progress));
            progress.setVisible(true);
            runThread.start();
        } else {
            JFrame inputValuesFrame = createInputFrame(core, () -> {
                core.withNList(nList).withDebugLevel(Core.DebugLevel.LOW);

                JFrame inputNlistFrame = createInputNlistFrame(core, () -> {
                    Thread runThread = new Thread(() -> run(core, progress));
                    progress.setVisible(true);
                    runThread.start();
                });
                inputNlistFrame.setVisible(true);
            });
            inputValuesFrame.setVisible(true);
        }
    }

    public static void run(Core core, JFrame progress) {
        ResultOfProcess result = core.process();

        progress.setVisible(false);

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

    public static JFrame createInputFrame(Core core, Callback callback) {
        JFrame result = new JFrame("Input");
        result.setSize(400, 200);
        result.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        GridBagLayout layout = new GridBagLayout();
        GridBagHelper helper = new GridBagHelper();
        result.setLayout(layout);

        helper.nextCell().gap(5);
        result.add(new JLabel("m"), helper.get());
        helper.nextCell().span();
        JTextField mInputField = new JTextField(20);
        mInputField.addActionListener(new IntegerListener(result));
        result.add(mInputField, helper.get());

        helper.nextRow().nextCell().gap(5);
        result.add(new JLabel("q"), helper.get());
        helper.nextCell().span();
        JTextField qInputField = new JTextField(20);
        qInputField.addActionListener(new IntegerListener(result));
        result.add(qInputField, helper.get());

        helper.nextRow().nextCell().gap(5);
        result.add(new JLabel("tf"), helper.get());
        helper.nextCell().span();
        JTextField tfInputField = new JTextField(20);
        tfInputField.addActionListener(new IntegerListener(result));
        result.add(tfInputField, helper.get());

        helper.nextRow().nextCell().gap(5);
        result.add(new JLabel("e"), helper.get());
        helper.nextCell().span();
        JTextField eInputField = new JTextField(20);
        eInputField.addActionListener(new DoubleListener(result));
        result.add(eInputField, helper.get());

        helper.nextRow().nextCell().gap(5);
        result.add(new JLabel("lambda function(i, t)"), helper.get());
        helper.nextCell().span();
        JTextField lambdaInputField = new JTextField(20);
        result.add(lambdaInputField, helper.get());

        helper.nextRow().nextCell().nextCell().gap(5);
        JButton buttonNext = new JButton("Далее");
        buttonNext.addActionListener((event) -> {
            try {
                core.withMCount(Integer.parseInt(mInputField.getText()))
                        .withQ(Integer.parseInt(qInputField.getText()))
                        .withTF(Integer.parseInt(tfInputField.getText()))
                        .withE(Double.parseDouble(eInputField.getText().replaceAll(",", ".")))
                        .withLambdaFunction(lambdaInputField.getText());

                if (callback != null) {
                    callback.onNextButtonClick();
                }

                result.setVisible(false);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(result, "Где-то некорректные данные");
                e.printStackTrace();
            }
        });
        result.add(buttonNext, helper.get());

        return result;
    }

    public static JFrame createInputNlistFrame(Core core, Callback callback) {
        JFrame result = new JFrame("Input N List");
        result.setSize(400, 200);
        result.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        GridBagHelper helper = new GridBagHelper();
        panel.setLayout(layout);

        helper.nextCell();
        JTextField[] list = new JTextField[core.q];
        for (int i = 0; i < core.q; i++) {
            helper.nextRow().nextCell().gap(5);
            panel.add(new JLabel(String.format("n(%d) = ", i)), helper.get());
            helper.nextCell().span();
            JTextField input = new JTextField(20);
            panel.addKeyListener(new KeyAdapter() {
                @Override
                public void keyTyped(KeyEvent event) {
                    String text = input.getText();
                    try {
                        Integer.parseInt(text);
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(result, String.format("<%s> не целое число", text));
                    }
                }
            });
            panel.add(input, helper.get());
            list[i] = input;
        }

        helper.nextRow().nextCell().nextCell().gap(5);
        JButton buttonNext = new JButton("Далее");
        buttonNext.addActionListener((event) -> {
            try {
                ArrayList<Integer> nList = new ArrayList();
                for (JTextField jTextField : list) {
                    nList.add(Integer.parseInt(jTextField.getText()));
                }

                core.withNList(nList);

                if (callback != null) {
                    callback.onNextButtonClick();
                }

                result.setVisible(false);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(result, "Где-то некорректные данные");
                e.printStackTrace();
            }
        });
        panel.add(buttonNext, helper.get());

        JScrollPane scrollPane = new JScrollPane(panel);
        result.add(scrollPane);

        return result;
    }

    public static JFrame createProgressFrame() {
        JFrame result = new JFrame("Loading");
        result.setSize(200, 200);
        result.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        GridBagLayout layout = new GridBagLayout();
        result.setLayout(layout);

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);

        result.add(progressBar);
        return result;
    }

    public interface Callback {
        void onNextButtonClick();
    }

    public static class IntegerListener implements ActionListener {
        private Component rootPanel;

        public IntegerListener(Component rootPanel) {
            this.rootPanel = rootPanel;
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            String text = event.getActionCommand();
            try {
                Integer.parseInt(text);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(rootPanel, String.format("<%s> не целое число", text));
            }
        }
    }

    public static class DoubleListener implements ActionListener {
        private Component rootPanel;

        public DoubleListener(Component rootPanel) {
            this.rootPanel = rootPanel;
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            String text = event.getActionCommand();
            try {
                Double.parseDouble(text.replaceAll(",", "."));
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(rootPanel, String.format("<%s> не вещественное число", text));
            }
        }
    }
}

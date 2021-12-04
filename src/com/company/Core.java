package com.company;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import static com.company.Utils.printArray;

public class Core {
    private static final HashMap<Integer, Long> factorialHash = new HashMap<>();
    public double[] t;
    public int mCount, nCount, q, tf;
    private double e;
    private ArrayList<Integer> s_line;
    private ArrayList<Integer> nList;
    private String lambdaFunction;
    private MathParser parser;

    private DebugLevel logLevel = DebugLevel.ALL;

    public Core() {
        this.parser = new MathParser();
    }

    public Core(int mCount, int q, int tf, double e, String lambdaFunction, ArrayList<Integer> nList) {
        this.mCount = mCount;
        this.q = q;
        this.tf = tf;
        this.e = e;
        this.lambdaFunction = lambdaFunction;
        this.nList = nList;

        nCount = nList.stream().mapToInt(item -> item).sum();
        this.parser = new MathParser();
    }

    public Core withDebugLevel(DebugLevel level) {
        this.logLevel = level;
        return this;
    }

    public Core withMCount(int mCount) {
        this.mCount = mCount;
        return this;
    }

    public Core withQ(int q) {
        this.q = q;
        return this;
    }

    public Core withTF(int tf) {
        this.tf = tf;
        return this;
    }

    public Core withE(double e) {
        this.e = e;
        return this;
    }

    public Core withNList(ArrayList<Integer> nList) {
        this.nList = nList;
        nCount = nList.stream().mapToInt(item -> item).sum();
        return this;
    }

    public Core withLambdaFunction(String lambdaFunction) {
        this.lambdaFunction = lambdaFunction;
        return this;
    }

    private void log(String msg, DebugLevel level) {
        if (this.logLevel.level >= level.level) {
            System.out.println(msg);
        }
    }

    public ResultOfProcess process() throws IllegalStateException {
        int r = 2; //2
        for (; ; r++) {
            double[] ts = new double[r + 1];
            double dt = ((float) tf / (float) r);
            ts[0] = 0.0;
            for (int i = 1; i <= r; i++) {
                ts[i] = ts[i - 1] + dt;
            }

            ArrayList<Double> fri = new ArrayList<>();
            for (int i = 0; i <= mCount; i++) {
                ArrayList<Double> fr = new ArrayList<>();
                for (int v = 1; v <= r; v++) {
                    //double liv = 0.5 * (lambda(i, ts[v - 1]) + lambda(i, ts[v])); //6
                    double friv = 0.5 * (lambda(i, ts[v]) - lambda(i, ts[v - 1])); //7
                    fr.add(friv);
                }
                double maxfriv = Collections.max(fr); //10
                fri.add(maxfriv);
            }

            double maxfri = Collections.max(fri); //13
            if (maxfri <= e) { //14
                log("finish 14 algorithm's step", DebugLevel.ALL);
                log(String.format("r = %d", r), DebugLevel.MEDIUM);
                log(String.format("max FRI = %.8f", maxfri), DebugLevel.MEDIUM);
                t = ts;
                break;
            }
        }

        //17
        ArrayList<ArrayList<Integer>> s_list_list = getVariations(null, mCount, q);

        ResultOfProcess result = new ResultOfProcess();
        for (ArrayList<Integer> s_list : s_list_list) {
            s_line = s_list;
            log(printArray("s = ", s_list), DebugLevel.LOW);

            Double[][] pSystem = new Double[mCount + 1][r + 1];
            pSystem[0][0] = 1.0;
            for (int i = 1; i <= mCount; i++) {
                pSystem[i][0] = 0.0;
            }

            for (int v = 1; v <= r; v++) {
                log(String.format("\n|%3s|%2s|%12s|", "v", "k", "p"), DebugLevel.MEDIUM);
                for (int k = 0; k <= mCount; k++) {
                    double pResult = 0;
                    for (int j = 0; j <= k; j++) {
                        if (k == 0) {
                            pResult = pSystem[0][v - 1] * Math.exp(-getD(1, v) * t[v]);
                            break;
                        }

                        double pPrev = pSystem[j][v - 1];

                        double multResult = 1;
                        for (int i = j + 1; i <= k; i++) {
                            double A = getA(i, v);

                            double sumResult = 0;
                            for (int l = j + 1; l <= k + 1; l++) {
                                double exponent = Math.exp(-getD(l, v) * t[v]);

                                double multBResult = 1;
                                for (int o = j + 1; o <= k + 1; o++) {
                                    if (l == o) {
                                        continue;
                                    }
                                    double firstD = getD(o, v);
                                    double secondD = getD(l, v);

                                    multBResult *= (firstD - secondD);
                                }

                                sumResult += exponent / multBResult;
                            }

                            multResult *= A * sumResult;
                        }

                        pResult += pPrev * multResult;
                    }

                    pSystem[k][v] = pResult;

                    log(String.format("|%3d|%2d|%12.5f|", v, k, pResult), DebugLevel.MEDIUM);
                }
            }

            double sumP = 0;
            for (Double[] doubles : pSystem) {
                sumP += doubles[r];
            }

            if (sumP >= result.pMax) {
                result.pMax = sumP;
                result.sLine = s_list;
                result.pSystem = pSystem;
            }
        }
        if (result.pSystem == null || result.sLine == null) {
            throw new IllegalStateException("Error");
        }

        String finalSLine = printArray("s =", result.sLine);

        log(String.format("pMax = %f", result.pMax), DebugLevel.LOW);
        log(String.format("final s\n%s", finalSLine), DebugLevel.LOW);

        log(String.format("|%3s|%3s|%3s|%5s|", "m", "q", "tf", "e"), DebugLevel.MEDIUM);
        log(String.format("|%3d|%3d|%3d|%5.3f|", mCount, q, tf, e), DebugLevel.MEDIUM);

        log(printArray("stock n =", nList), DebugLevel.MEDIUM);
        log(String.format("lambda func(i, t) = %s", lambdaFunction), DebugLevel.MEDIUM);

        result.r = r;
        return result;
    }

    public double lambda(double i, double t) {
        try {
            MathParser.setVariable("i", i);
            MathParser.setVariable("t", t);
            return parser.Parse(lambdaFunction);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(123);
        }

        return Double.MIN_VALUE;
    }

    private ArrayList<ArrayList<Integer>> getVariations(ArrayList<Integer> s_List, int k, int groups) {
        ArrayList<ArrayList<Integer>> result = new ArrayList<>();
        int[] arr = new int[groups];
        while (true) {
            try {
                arr[0]++;
                if (IsValidVariation(arr, k)) {
                    ArrayList<Integer> copy = copyArray(arr);
                    result.add(copy);
                }
                for (int i = 0; i < groups; i++) {
                    if (arr[i] > k) {
                        arr[i] = 0;
                        arr[i + 1]++;
                        if (IsValidVariation(arr, k)) {
                            ArrayList<Integer> copy = copyArray(arr);
                            result.add(copy);
                        }
                    }
                }
            } catch (Exception e) {
                if (s_List != null) {
                    for (int i = 0; i < result.size(); i++) {
                        for (int j = 0; j < groups; ) {
                            if (i < result.size() && result.get(i).get(j) > s_List.get(j)) {
                                result.remove(i);
                                j = 0;
                            } else {
                                j++;
                            }
                        }
                    }
                }
                break;
            }
        }
        return result;
    }

    private ArrayList<Integer> copyArray(int[] arr) {
        ArrayList<Integer> list = new ArrayList<>();
        for (int i : arr) {
            list.add(i);
        }
        return list;
    }

    private boolean IsValidVariation(int[] arr, int k) {
        int sum = Arrays.stream(arr).sum();
        return sum == k;
    }

    private double R(ArrayList<Integer> n, ArrayList<Integer> s, ArrayList<ArrayList<Integer>> v) {
        double sum = 0;
        for (ArrayList<Integer> vList : v) {
            long result = 1;
            for (int i = 0; i < n.size(); i++) {
                int a = n.get(i) + s.get(i);
                result *= variatic(a, vList.get(i));
            }
            sum += result;
        }
        return sum;
    }

    private long variatic(int a, int b) {
        return factorial(a) / (factorial(b) * factorial(a - b));
    }

    private long factorial(int number) {
        Long cacheVal = factorialHash.get(number);
        if (cacheVal != null) {
            return cacheVal;
        }

        long result = 1;

        for (int factor = 2; factor <= number; factor++) {
            result *= factor;
        }

        factorialHash.put(number, result);

        return result;
    }

    private int getSigma(long si) {
        return si == 0 ? 0 : 1;
    }

    private int getOmega(int k, long si) {
        return k <= si ? 0 : 1;
    }

    private double getBetta(int i, int k) {
        if (k == mCount + 1) {
            return getSigma(s_line.get(i)) * nList.get(i);
        }
        if (i == 0) {
            return (mCount - k + 1) * getR(k);
        }
        return getSigma(s_line.get(i)) * nList.get(i) * getR(k) + nList.get(i) * getOmega(k, s_line.get(i));
    }

    private double getAlpha(int i, int k) {
        if (i == 0) {
            return (mCount - k + 1) * getR(k);
        } else {
            return getSigma(s_line.get(i)) * nList.get(i) * getR(k);
        }
    }

    private double getA(int k, int v) {
        double result = 0;
        for (int i = 0; i < q; i++) {
            double liv = 0.5 * (lambda(i, t[v - 1]) + lambda(i, t[v])); //6
            result += getAlpha(i, k) * liv;
        }
        return result;
    }

    private double getD(int k, int v) {
        double result = 0;
        for (int i = 0; i < q; i++) {
            double liv = 0.5 * (lambda(i, t[v - 1]) + lambda(i, t[v])); //6
            result += getBetta(i, k) * liv;
        }
        return result;
    }

    private double getR(int k) {
        ArrayList<ArrayList<Integer>> v_list = getVariations(s_line, k, q);
        double R = R(nList, s_line, v_list);
        double comb = variatic(nCount + mCount, k);
        return R / comb;
    }

    public enum DebugLevel {
        OFF(0),
        LOW(1),
        MEDIUM(2),
        ALL(3);

        public int level;

        DebugLevel(int level) {
            this.level = level;
        }
    }

    public static class ResultOfProcess {
        public ArrayList<Integer> sLine;
        public Double[][] pSystem;
        public double pMax;
        public int r;

        public ResultOfProcess() {
            this.pMax = -1;
            this.sLine = null;
            this.pSystem = null;
        }
    }
}

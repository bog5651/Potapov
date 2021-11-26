package com.company;

import java.util.*;

public class Main {

    public static int mCount, nCount, q, tf;
    public static double e;
    public static ArrayList<Integer> s_line;
    public static ArrayList<Integer> nList;
    public static double[] t;

    public static Double[][] pSystem;

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);

        while (true) {
            try {
                System.out.print("m = ");
                mCount = in.nextInt();
                System.out.print("q = ");
                q = in.nextInt();
                System.out.print("tf = ");
                tf = in.nextInt();
                System.out.print("e = ");
                e = in.nextDouble();
                break;
            } catch (InputMismatchException ex) {
                System.out.println("Не число");
            }
        }

        nList = new ArrayList<>();

        for (int i = 0; i < q; i++) {
            System.out.printf("n(%d) = ", i);
            int n = in.nextInt();
            nList.add(n);
        }
        nCount = nList.stream().mapToInt(item -> item).sum();


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
            //System.out.printf("max FRI = %f\n", maxfri);
            if (maxfri <= e) { //14
                System.out.println("OUT 14");
                System.out.printf("r = %d \n", r);
                System.out.printf("max FRI = %.8f\n", maxfri);
                t = ts;
                break;
            }
        }

        //17
        ArrayList<ArrayList<Integer>> s_list_list = getVariations(null, mCount, q);
        ArrayList<Double> pList = new ArrayList<>();
        for (ArrayList<Integer> s_list : s_list_list) {
            s_line = s_list;
            printArray("s = ", s_list);

            pSystem = new Double[mCount + 1][r + 1];
            pSystem[0][0] = 1.0;
            for (int i = 1; i <= mCount; i++) {
                pSystem[i][0] = 0.0;
            }

            for (int v = 1; v <= r; v++) {
                System.out.printf("\n|%2s|%2s|%12s|\n", "v", "k", "p");
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

                    System.out.printf("|%2d|%2d|%12.5f|\n", v, k, pResult);
                }
            }

            double sumP = 0;
            for (int i = 0; i < pSystem.length; i++) {
                sumP += pSystem[i][r];
            }

//            System.out.printf("sumP = %f\n", sumP);
            pList.add(sumP);
        }

        double pMax = pList.stream().max(Double::compareTo).get();
        int pMaxIndex = pList.indexOf(pMax);
        System.out.printf("pMax = %f\n", pMax);
        printArray("final s\n s =", s_list_list.get(pMaxIndex));
        //the end
    }

    public static void printArray(String prefix, ArrayList<Integer> list) {
        System.out.print(prefix + " { ");
        for (Integer o : list) {
            System.out.print(o + " ");
        }
        System.out.println("}");
    }

    public static double lambda(double i, double t) {
//        return i + t / 3;
        return t / 3;
    }

    private static ArrayList<ArrayList<Integer>> getVariations(ArrayList<Integer> s_List, int k, int groups) {
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

    private static ArrayList<Integer> copyArray(int[] arr) {
        ArrayList<Integer> list = new ArrayList<>();
        for (int i : arr) {
            list.add(i);
        }
        return list;
    }

    private static boolean IsValidVariation(int[] arr, int k) {
        int sum = Arrays.stream(arr).sum();
        return sum == k;
    }

    public static double R(ArrayList<Integer> n, ArrayList<Integer> s, ArrayList<ArrayList<Integer>> v) {
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

    public static long variatic(int a, int b) {
        return factorial(a) / (factorial(b) * factorial(a - b));
    }

    public static long factorial(int number) {
        long result = 1;

        for (int factor = 2; factor <= number; factor++) {
            result *= factor;
        }

        return result;
    }

    public static int getSigma(long si) {
        return si == 0 ? 0 : 1;
    }

    public static int getOmega(int k, long si) {
        return k <= si ? 0 : 1;
    }

    public static double getBetta(int i, int k) {
        if (i == 0) {
            return (mCount - k + 1) * getR(k);
        } else {
            return getSigma(s_line.get(i)) * nList.get(i) * getR(k) + nList.get(i) * getOmega(k, s_line.get(i));
        }
    }

    public static double getAlpha(int i, int k) {
        if (i == 0) {
            return (mCount - k + 1) * getR(k);
        } else {
            return getSigma(s_line.get(i)) * nList.get(i) * getR(k);
        }
    }

    public static double getA(int k, int v) {
        double result = 0;
        for (int i = 0; i < q; i++) {
            double liv = 0.5 * (lambda(i, t[v - 1]) + lambda(i, t[v])); //6
            result += getAlpha(i, k) * liv;
        }
        return result;
    }

    public static double getD(int k, int v) {
        double result = 0;
        for (int i = 0; i < q; i++) {
            double liv = 0.5 * (lambda(i, t[v - 1]) + lambda(i, t[v])); //6
            result += getBetta(i, k) * liv;
        }
        return result;
    }

    public static double getR(int k) {
        ArrayList<ArrayList<Integer>> v_list = getVariations(s_line, k, q);
        double R = R(nList, s_line, v_list);
        double comb = variatic(nCount + mCount, k);
        return R / comb;
    }
}

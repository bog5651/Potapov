package com.company;

import java.util.*;

public class Main {

    public static int mCount, nCount, q, tf;
    public static double e;
    public static ArrayList<Integer> s_line;
    public static ArrayList<Integer> nList;
    public static double[] t;

    public static HashMap<Integer, HashMap<Integer, Double>> pSystem = new HashMap<>();

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
                System.out.printf("max FRI = %f\n", maxfri);
                t = ts;
                break;
            }
        }

        //17
        ArrayList<ArrayList<Integer>> s_list_list = getVariations(null, mCount, q);
        for (ArrayList<Integer> s_list : s_list_list) {
            s_line = s_list;
            printArray("s = ", s_list);

            pSystem = new HashMap<>();
            HashMap<Integer, Double> pSystemRaw = new HashMap<>();
            pSystemRaw.put(0, 1.0);
            pSystem.put(0, pSystemRaw);
            for (int i = 1; i < mCount; i++) {
                pSystemRaw = new HashMap<>();
                pSystemRaw.put(0, 0.0);
                pSystem.put(i, pSystemRaw);
            }

            for (int v = 1; v <= r; v++) {
                System.out.printf("v = %d\n", v);
                System.out.printf("|%2s|%12s|%12s|%12s|\n", "k", "RBase", "comb", "R *");
                for (int k = 0; k < mCount; k++) {
                    ArrayList<ArrayList<Integer>> v_list = getVariations(s_list, k, q);

                    double comb = variatic(nCount + mCount, k);
                    double R = R(nList, s_list, v_list);
                    double R_star = R / comb;
                    System.out.printf("|%2d|%12.5f|%12.5f|%12.5f|\n", k, R, comb, R_star);

                    double pResult = 0;
                    for (int j = 0; j <= k; j++) {
                        if(k == 0) {
                            pResult = pSystem.get(0).get(v - 1) * Math.exp(-getD(1, v, R_star) * t[v]);
                            break;
                        }

                        double pPrev = pSystem.get(j).get(v - 1);

                        double multResult = 1;
                        for (int i = j + 1; i <= k; i++) {
                            double A = getA(i, v, R_star);

                            double sumResult = 0;
                            for (int l = j + 1; l <= k + 1; l++) {
                                double exponent = Math.exp(-getD(l, v, R_star) * t[v]);

                                double multBResult = 1;
                                for (int o = j + 1; o <= k + 1; o++) {
                                    if(l == o) {
                                        continue;
                                    }
                                    double firstD = getD(o, v, R_star);
                                    double secondD = getD(l, v, R_star);

                                    multBResult *= (firstD - secondD);
                                }

                                sumResult += exponent / multBResult;
                            }

                            multResult *= A * sumResult;
                        }

                        pResult += pPrev * multResult;
                    }

                    pSystemRaw = pSystem.get(k);
                    pSystemRaw.put(v, pResult);
                    pSystem.put(k, pSystemRaw);

                    System.out.println(pResult);

//                    for (ArrayList<Integer> list : v_list) {
//                        printArray("v = ", list);
//                    }
                }
            }
        }

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
        return i + t / 3;
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
        ArrayList<Long> mult = new ArrayList<>();
        for (ArrayList<Integer> vList : v) {
            long result = 1;
            for (int i = 0; i < n.size(); i++) {
                int a = n.get(i) + s.get(i);
                result *= variatic(a, vList.get(i));
            }
            mult.add(result);
        }
        return mult.stream().mapToLong(item -> item).sum();
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

    public static double getBetta(int i, int k, double r_star) {
        if (i == 0) {
            return (mCount - k + 1) * r_star;
        } else {
            return getSigma(s_line.get(i)) * nList.get(i) * r_star + nList.get(i) + getOmega(k, s_line.get(i));
        }
    }

    public static double getAlpha(int i, int k, double r_star) {
        if (i == 0) {
            return (mCount - k + 1) * r_star;
        } else {
            return getSigma(s_line.get(i)) * nList.get(i) * r_star;
        }
    }

    public static double getA(int k, int v, double r_star) {
        double result = 0;
        for (int i = 0; i < q; i++) {
            double liv = 0.5 * (lambda(i, t[v - 1]) + lambda(i, t[v])); //6
            result += getAlpha(i, k, r_star) * liv;
        }
        return result;
    }

    public static double getD(int k, int v, double r_star) {
        double result = 0;
        for (int i = 0; i < q; i++) {
            double liv = 0.5 * (lambda(i, t[v - 1]) + lambda(i, t[v])); //6
            result += getBetta(i, k, r_star) * liv;
        }
        return result;
    }
}

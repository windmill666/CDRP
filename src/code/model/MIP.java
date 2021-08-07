package code.model;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearIntExpr;
import ilog.cplex.IloCplex;

public class MIP {

    private int containerSum;
    private int stackHeight;
    private int stackLength;
    private int upperBound;
    private int lowerBound;
    private int[][] containerBin;
    private int[] retrievalContainer;
    private int[] restRetrievalContainer;
    private int[] storageContainer;
    private int[] restStorageContainer;

    public MIP(int containerSum, int stackHeight, int stackLength, int upperBound, int lowerBound, int[][] containerBin, int[] retrievalContainer, int[] restRetrievalContainer, int[] storageContainer, int[] restStorageContainer) {
        this.containerSum = containerSum;
        this.stackHeight = stackHeight;
        this.stackLength = stackLength;
        this.upperBound = upperBound;
        this.lowerBound = lowerBound;
        this.containerBin = containerBin;
        this.retrievalContainer = retrievalContainer;
        this.restRetrievalContainer = restRetrievalContainer;
        this.storageContainer = storageContainer;
        this.restStorageContainer = restStorageContainer;
    }

    private void objectiveFunction(IloCplex model, IloIntVar[][][] u) throws IloException {
        IloLinearIntExpr expr = model.linearIntExpr();
        for (int i = 0; i < u.length; i++) {// u.length = C
            for (int j = 0; j < u[0].length; j++) {// u[0].length = C + 1
                if (j == i) {
                    continue;
                }
                for (int t = 0; t < u[0][0].length; t++) {// u[0][0].length = T
                    expr.addTerm(1, u[i][j][t]);
                }
            }
        }
        model.addMinimize(expr);
    }
    private void constraintX(IloCplex model, IloIntVar[][][] x, IloIntVar[][][] u, IloIntVar[][][] d, IloIntVar[][][] r, IloIntVar[][][] s, int[][] C_L) throws IloException {
        // No.1 注入初始集装箱布局信息
        for (int i = 0; i < x.length; i++) {// x.length = C
            for (int j = 0; j < x[0].length; j++) {// x[0].length = C + 1
                if (j == i) {
                    continue;
                }
                model.addEq(x[i][j][0], C_L[i][j]);
            }
        }

        // No.2 前一时刻和后一时刻的集装箱位置信息变化
        for (int i = 0; i < x.length; i++) {// x.length = C
            for (int j = 0; j < x[0].length; j++) {// x[0].length = C + 1
                if (j == i) {
                    continue;
                }
                for (int t = 1; t < x[0][0].length; t++) {// x[0][0].length = T + 1，t变化范围[1,T]
                    IloLinearIntExpr expr = model.linearIntExpr();
                    expr.addTerm(1, x[i][j][t]);
                    expr.addTerm(-1, x[i][j][t - 1]);
                    expr.addTerm(1, u[i][j][t - 1]);
                    expr.addTerm(-1, d[i][j][t - 1]);
                    expr.addTerm(1, r[i][j][t - 1]);
                    expr.addTerm(-1, s[i][j][t - 1]);
                    model.addEq(expr, 0);
                }
            }
        }
    }
    private void constraintW(IloCplex model, IloIntVar[][][] x, IloIntVar[][] y, IloIntVar[] z, IloIntVar[][][] w3, IloIntVar[][][][] w4, IloIntVar[][][][][] w5) throws IloException {
        // No.3 集装箱位置信息约束，为计算BP做准备
        if (stackHeight >= 3) {
            for (int i = 0; i < w3.length; i++) {
                for (int j = 0; j < w3[0].length; j++) {
                    if (j == i) {
                        continue;
                    }
                    for (int k = 0; k < w3[0][0].length; k++) {
                        if (k == j || k == i) {
                            continue;
                        }
                        model.addGe(w3[i][k][j], model.diff(model.sum(x[i][k][upperBound], x[k][j][upperBound]), 1));
                        model.addLe(w3[i][k][j], x[i][k][upperBound]);
                        model.addLe(w3[i][k][j], x[k][j][upperBound]);
                    }
                }
            }
        }
        if (stackHeight >= 4) {
            for (int i = 0; i < w4.length; i++) {
                for (int j = 0; j < w4[0].length; j++) {
                    if (j == i) {
                        continue;
                    }
                    for (int k1 = 0; k1 < w4[0][0].length; k1++) {
                        if (k1 == j || k1 == i) {
                            continue;
                        }
                        for (int k2 = 0; k2 < w4[0][0][0].length; k2++) {
                            if (k2 == k1 || k2 == j || k2 == i) {
                                continue;
                            }
                            model.addGe(w4[i][k1][k2][j], model.diff(model.sum(model.sum(x[i][k1][upperBound], x[k1][k2][upperBound]), x[k2][j][upperBound]), 2));
                            model.addLe(w4[i][k1][k2][j], x[i][k1][upperBound]);
                            model.addLe(w4[i][k1][k2][j], x[k1][k2][upperBound]);
                            model.addLe(w4[i][k1][k2][j], x[k2][j][upperBound]);

                        }
                    }
                }
            }
        }
        if (stackHeight >= 5) {
            for (int i = 0; i < w5.length; i++) {
                for (int j = 0; j < w5[0].length; j++) {
                    if (j == i) {
                        continue;
                    }
                    for (int k1 = 0; k1 < w5[0][0].length; k1++) {
                        if (k1 == j || k1 == i) {
                            continue;
                        }
                        for (int k2 = 0; k2 < w5[0][0][0].length; k2++) {
                            if (k2 == k1 || k2 == j || k2 == i) {
                                continue;
                            }
                            for (int k3 = 0; k3 < w5[0][0][0][0].length; k3++) {
                                if (k3 == k2 || k3 == k1 || k3 == j || k3 == i) {
                                    continue;
                                }
                                model.addGe(w5[i][k1][k2][k3][j], model.diff(model.sum(model.sum(model.sum(x[i][k1][upperBound], x[k1][k2][upperBound]), x[k2][k3][upperBound]), x[k3][j][upperBound]), 3));
                                model.addLe(w5[i][k1][k2][k3][j], x[i][k1][upperBound]);
                                model.addLe(w5[i][k1][k2][k3][j], x[k1][k2][upperBound]);
                                model.addLe(w5[i][k1][k2][k3][j], x[k2][k3][upperBound]);
                                model.addLe(w5[i][k1][k2][k3][j], x[k3][j][upperBound]);
                            }
                        }
                    }
                }
            }
        }
        int C = y.length;
        for (int i = 0; i < C; i++) {
            for (int j = 0; j < C; j++) {
                if (j == i) {
                    continue;
                }
                IloLinearIntExpr expr = model.linearIntExpr();
                if (stackHeight >= 3) {
                    for (int k = 0; k < C; k++) {
                        if (k == j || k == i) {
                            continue;
                        }
                        expr.addTerm(1, w3[i][k][j]);
                    }
                }
                if (stackHeight >= 4) {
                    for (int k1 = 0; k1 < C; k1++) {
                        if (k1 == j || k1 == i) {
                            continue;
                        }
                        for (int k2 = 0; k2 < C; k2++) {
                            if (k2 == k1 || k2 == j || k2 == i) {
                                continue;
                            }
                            expr.addTerm(1, w4[i][k1][k2][j]);
                        }
                    }
                }
                if (stackHeight >= 5) {
                    for (int k1 = 0; k1 < C; k1++) {
                        if (k1 == j || k1 == i) {
                            continue;
                        }
                        for (int k2 = 0; k2 < C; k2++) {
                            if (k2 == k1 || k2 == j || k2 == i) {
                                continue;
                            }
                            for (int k3 = 0; k3 < C; k3++) {
                                if (k3 == k2 || k3 == k1 || k3 == j || k3 == i) {
                                    continue;
                                }
                                expr.addTerm(1, w5[i][k1][k2][k3][j]);
                            }
                        }
                    }
                }
                model.addEq(y[i][j], model.sum(x[i][j][upperBound], expr));
            }
        }

        // No.4 计算BP
        for (int i = 0; i < z.length; i++) {
            IloLinearIntExpr expr = model.linearIntExpr();
            for (int j = 0; j < i; j++) {
                expr.addTerm(1, y[i][j]);
            }
            model.addLe(z[i], expr);
        }

        // No.5 计算BP
        for (int i = 0; i < z.length; i++) {
            IloLinearIntExpr expr = model.linearIntExpr();
            for (int j = 0; j < i; j++) {
                expr.addTerm(1, y[i][j]);
            }
            model.addGe(z[i], model.prod(expr, 1.0 / stackHeight));
        }

        // No.6 BP约束
        IloLinearIntExpr exprBP = model.linearIntExpr();
        for (int i = 0; i < z.length; i++) {
            exprBP.addTerm(1, z[i]);
        }
        model.addLe(exprBP, 0);
    }
    private void constraintU(IloCplex model, IloIntVar[][][] u, IloIntVar[][][] x) throws IloException {
        // No.7 涉及提箱操作
        for (int t = 0; t < lowerBound; t++) {
            IloLinearIntExpr expr = model.linearIntExpr();
            for (int i = 0; i < u.length; i++) {// u.length = C
                for (int j = 0; j < u[0].length; j++) {// u[0].length = C + 1
                    if (j == i) {
                        continue;
                    }
                    expr.addTerm(1, u[i][j][t]);
                }
            }
            model.addEq(expr, 1);
        }

        // No.8 涉及提箱操作
        for (int t = lowerBound; t < u[0][0].length; t++) {// u[0][0].length = T
            if (t > 0) {
                IloLinearIntExpr expr1 = model.linearIntExpr();
                IloLinearIntExpr expr2 = model.linearIntExpr();
                for (int i = 0; i < u.length; i++) {// u.length = C
                    for (int j = 0; j < u[0].length; j++) {// u[0].length = C + 1
                        if (j == i) {
                            continue;
                        }
                        expr1.addTerm(1, u[i][j][t]);
                        expr2.addTerm(1, u[i][j][t - 1]);
                    }
                }
                model.addLe(expr1, expr2);
            }
        }

        // No.9 涉及提箱操作
        for (int i = 0; i < u.length; i++) {// u.length = C
            for (int j = 0; j < u[0].length; j++) {// u[0].length = C + 1
                if (j == i) {
                    continue;
                }
                for (int t = 0; t < u[0][0].length; t++) {// u[0][0].length = T，注意：x变量的t变化范围是[0, T + 1]
                    model.addLe(u[i][j][t], x[i][j][t]);
                }
            }
        }

        // No.10 涉及提箱操作
        for (int i = 0; i < u.length; i++) {// u.length = C
            for (int t = 0; t < u[0][0].length; t++) {// u[0][0].length = T，注意：x变量的t变化范围是[0, T + 1]
                IloLinearIntExpr expr1 = model.linearIntExpr();
                for (int j = 0; j < u[0].length; j++) {// u[0].length = C + 1
                    if (j == i) {
                        continue;
                    }
                    expr1.addTerm(1, u[i][j][t]);
                }
                IloLinearIntExpr expr2 = model.linearIntExpr();
                for (int j = 0; j < u[0].length; j++) {// u[0].length = C + 1
                    if (j == i) {
                        continue;
                    }
                    expr2.addTerm(1, x[i][j][t]);
                }
                IloLinearIntExpr expr3 = model.linearIntExpr();
                for (int j = 0; j < u.length; j++) {// u.length = C
                    if (j == i) {
                        continue;
                    }
                    expr3.addTerm(1, x[j][i][t]);
                }
                model.addLe(expr1, model.diff(expr2, expr3));
            }
        }

    }
    private void constraintD(IloCplex model, IloIntVar[][][] d, IloIntVar[][][] u, IloIntVar[][][] x) throws IloException {
        // No.11 涉及放箱操作
        for (int t = 0; t < lowerBound; t++) {
            IloLinearIntExpr expr = model.linearIntExpr();
            for (int i = 0; i < d.length; i++) {// d.length = C
                for (int j = 0; j < d[0].length; j++) {// d[0].length = C + 1
                    if (j == i) {
                        continue;
                    }
                    expr.addTerm(1, d[i][j][t]);
                }
            }
            model.addEq(expr, 1);
        }

        // No.12 涉及放箱操作
        for (int t = lowerBound; t < d[0][0].length; t++) {// d[0][0].length = T
            if (t > 0) {
                IloLinearIntExpr expr1 = model.linearIntExpr();
                IloLinearIntExpr expr2 = model.linearIntExpr();
                for (int i = 0; i < d.length; i++) {// d.length = C
                    for (int j = 0; j < d[0].length; j++) {// d[0].length = C + 1
                        if (j == i) {
                            continue;
                        }
                        expr1.addTerm(1, d[i][j][t]);
                        expr2.addTerm(1, d[i][j][t - 1]);
                    }
                }
                model.addLe(expr1, expr2);
            }
        }

        // No.13 涉及放箱操作
        for (int i = 0; i < d.length; i++) {// d.length = C
            for (int t = 0; t < d[0][0].length; t++) {// d[0][0].length = T
                IloLinearIntExpr expr1 = model.linearIntExpr();
                IloLinearIntExpr expr2 = model.linearIntExpr();
                for (int j = 0; j < d[0].length; j++) {// d[0].length = C + 1
                    if (j == i) {
                        continue;
                    }
                    expr1.addTerm(1, d[i][j][t]);
                    expr2.addTerm(1, u[i][j][t]);
                }
                model.addEq(expr1, expr2);
            }
        }

        // No.14 涉及放箱操作
        for (int j = 0; j < d[0].length; j++) {// d[0].length = C + 1
            for (int t = 0; t < d[0][0].length; t++) {// d[0][0].length = T
                IloLinearIntExpr expr1 = model.linearIntExpr();
                IloLinearIntExpr expr2 = model.linearIntExpr();
                for (int i = 0; i < d.length; i++) {// d.length = C
                    if (i == j) {
                        continue;
                    }
                    expr1.addTerm(1, d[i][j][t]);
                    expr2.addTerm(1, u[i][j][t]);
                }
                model.addLe(expr1, model.diff(1, expr2));
            }
        }

        // No.15 涉及放箱操作
        for (int i = 0; i < d.length; i++) {// d.length = C
            for (int t = 0; t < d[0][0].length; t++) {// d[0][0].length = T
                IloLinearIntExpr expr1 = model.linearIntExpr();
                IloLinearIntExpr expr2 = model.linearIntExpr();
                IloLinearIntExpr expr3 = model.linearIntExpr();
                for (int j = 0; j < d.length; j++) {// d.length = C
                    if (j == i) {
                        continue;
                    }
                    expr1.addTerm(1, d[j][i][t]);
                }
                for (int j = 0; j < d[0].length; j++) {// d[0].length = C + 1
                    if (j == i) {
                        continue;
                    }
                    expr2.addTerm(1, x[i][j][t]);
                }
                for (int j = 0; j < d.length; j++) {// d.length = C
                    if (j == i) {
                        continue;
                    }
                    expr3.addTerm(1, x[j][i][t]);
                }
                model.addLe(expr1, model.diff(expr2, expr3));
            }
        }

        // No.16 涉及放箱操作
        for (int t = 0; t < d[0][0].length; t++) {// d[0][0].length = T
            IloLinearIntExpr expr1 = model.linearIntExpr();
            IloLinearIntExpr expr2 = model.linearIntExpr();
            for (int i = 0; i < d.length; i++) {// d.length = C
                expr1.addTerm(1, d[i][containerSum][t]);
                expr2.addTerm(1, x[i][containerSum][t]);
            }
            model.addLe(expr1, model.diff(stackLength, expr2));
        }
    }
    private void constraintR(IloCplex model, IloIntVar[][][] r, int[] C_R, int[] N_C_R, IloIntVar[][][] d, IloIntVar[][][] u, IloIntVar[][][] x) throws IloException {
        if (N_C_R != null && N_C_R.length > 0) {
            // No.17 涉及取箱操作
            for (int i = 0; i < N_C_R.length; i++) {
                for (int j = 0; j < r[0].length; j++) {
                    if (j == N_C_R[i]) {
                        continue;
                    }
                    for (int t = 0; t < r[0][0].length; t++) {
                        model.addEq(r[N_C_R[i]][j][t], 0);
                    }
                }
            }
        }

        if (C_R != null && C_R.length > 0) {
            // No.18 涉及取箱操作
            for (int i = 0; i < C_R.length; i++) {// C_R.length = retrievalContainer.length
                for (int t = 0; t < r[0][0].length; t++) {// r[0][0].length = T
                    IloLinearIntExpr expr1 = model.linearIntExpr();
                    IloLinearIntExpr expr2 = model.linearIntExpr();
                    IloLinearIntExpr expr3 = model.linearIntExpr();
                    for (int j = C_R[i] + 1; j < r[0].length; j++) {// r[0].length = C + 1
                        expr1.addTerm(1, r[C_R[i]][j][t]);
                    }
                    for (int j = 0; j < r[0].length; j++) {// r[0].length = C + 1
                        expr2.addTerm(1, x[C_R[i]][j][t]);
                    }
                    for (int j = C_R[i] + 1; j < r.length; j++) {// r.length = C
                        expr3.addTerm(1, x[j][C_R[i]][t]);
                        expr3.addTerm(-1, u[j][C_R[i]][t]);
                        expr3.addTerm(1, d[j][C_R[i]][t]);
                    }
                    model.addLe(expr1, model.diff(expr2, expr3));
                }
            }

            // No.19 涉及取箱操作
            for (int i = 0; i < C_R.length; i++) {
                for (int t = 0; t < r[0][0].length; t++) {
                    IloLinearIntExpr expr1 = model.linearIntExpr();
                    IloLinearIntExpr expr2 = model.linearIntExpr();
                    for (int j = C_R[i] + 1; j < r[0].length; j++) {
                        expr1.addTerm(1, r[C_R[i]][j][t]);
                    }
                    for (int j = 0; j < r.length; j++) {
                        for (int k = 0; k < r[0].length; k++) {
                            if (k == j) {
                                continue;
                            }
                            expr2.addTerm(1, u[j][k][t]);
                        }
                    }
                    model.addLe(expr1, expr2);
                }
            }

            // No.20 涉及取箱操作
            for (int i = 1; i < C_R.length; i++) {
                for (int t = 0; t < r[0][0].length; t++) {
                    IloLinearIntExpr expr1 = model.linearIntExpr();
                    IloLinearIntExpr expr2 = model.linearIntExpr();
                    for (int j = C_R[i] + 1; j < r[0].length; j++) {
                        for (int temp = 0; temp <= t; temp++) {
                            expr1.addTerm(1, r[C_R[i]][j][temp]);
                        }
                    }
                    for (int j = C_R[i - 1] + 1; j < r[0].length; j++) {// 由于C_R数据是连续的，所以C_R[i - 1] + 1 = i - 1 + 1 = i
                        for (int temp = 0; temp <= t; temp++) {
                            expr2.addTerm(1, r[C_R[i - 1]][j][temp]);
                        }
                    }
                    model.addLe(expr1, expr2);
                }
            }
        }
    }
    private void constraintS(IloCplex model, IloIntVar[][][] s, int[] C_S, int[] C_R, int[] N_C_S, IloIntVar[][][] d, IloIntVar[][][] u, IloIntVar[][][] x, IloIntVar[][][] r) throws IloException {
        if (N_C_S != null && N_C_S.length > 0) {
            // No.21 涉及存箱操作
            for (int i = 0; i < N_C_S.length; i++) {
                for (int j = 0; j < s[0].length; j++) {
                    if (j == N_C_S[i]) {
                        continue;
                    }
                    for (int t = 0; t < s[0][0].length; t++) {
                        model.addEq(s[N_C_S[i]][j][t], 0);
                    }
                }
            }
        }

        if (C_S != null && C_S.length > 0) {
            // No.22 涉及存箱操作
            for (int i = 0; i < C_S.length; i++) {
                for (int j = 0; j < C_S.length; j++) {
                    if (C_S[j] == C_S[i]) {
                        continue;
                    }
                    for (int t = 0; t < s[0][0].length; t++) {
                        model.addLe(s[C_S[i]][C_S[j]][t], model.diff(1, s[C_S[j]][C_S[i]][t]));
                    }
                }
            }

            // No.23 涉及存箱操作
            for (int i = 0; i < C_S.length; i++) {
                for (int t = 1; t < s[0][0].length; t++) {
                    IloLinearIntExpr expr1 = model.linearIntExpr();
                    IloLinearIntExpr expr2 = model.linearIntExpr();
                    for (int j = 0; j < s[0].length; j++) {
                        if (j == C_S[i]) {
                            continue;
                        }
                        expr1.addTerm(1, s[C_S[i]][j][t]);
                    }
                    for (int j = 0; j < s.length; j++) {
                        for (int k = 0; k < s[0].length; k++) {
                            if (k == j) {
                                continue;
                            }
                            expr2.addTerm(1, u[j][k][t]);
                        }
                    }
                    model.addLe(expr1, expr2);
                }
            }

            // No.24 涉及存箱操作
            for (int i = 1; i < C_S.length; i++) {
                for (int t = 0; t < s[0][0].length; t++) {
                    IloLinearIntExpr expr1 = model.linearIntExpr();
                    IloLinearIntExpr expr2 = model.linearIntExpr();
                    for (int j = 0; j < s[0].length; j++) {
                        if (j == C_S[i]) {
                            continue;
                        }
                        for (int temp = 0; temp <= t; temp++) {
                            expr1.addTerm(1, s[C_S[i]][j][temp]);
                        }
                    }
                    for (int j = 0; j < s[0].length; j++) {
                        if (j == C_S[i - 1]) {
                            continue;
                        }
                        for (int temp = 0; temp <= t; temp++) {
                            expr2.addTerm(1, s[C_S[i - 1]][j][temp]);
                        }
                    }
                    model.addLe(expr1, expr2);
                }
            }

            // No.25 涉及存箱操作
            for (int t = 0; t < s[0][0].length; t++) {
                for (int m = 0; m < C_S.length; m++) {
                    for (int n = m + 1; n < C_S.length; n++) {
                        model.addEq(s[C_S[m]][C_S[n]][t], 0);
                    }
                }
            }

            // No.26 涉及存箱操作
            if (C_R != null && C_R.length > 0) {
                for (int t = 0; t < s[0][0].length; t++) {
                    IloLinearIntExpr expr1 = model.linearIntExpr();
                    IloLinearIntExpr expr2 = model.linearIntExpr();
                    for (int temp = 0; temp <= t; temp++) {
                        for (int j = 0; j < s[0].length; j++) {
                            if (j == C_S[0]) {
                                continue;
                            }
                            expr1.addTerm(1, s[C_S[0]][j][temp]);
                        }
                        for (int j = C_R[C_R.length - 1] + 1; j < s[0].length; j++) {
                            expr2.addTerm(1, r[C_R[C_R.length - 1]][j][temp]);
                        }
                    }
                    model.addLe(expr1, expr2);
                }
            }

            // No.27 涉及存箱操作
//            IloLinearIntExpr exprStorage = model.linearIntExpr();
//            for (int i = 0; i < C_S.length; i++) {
//                for (int j = 0; j < s[0].length; j++) {
//                    if (j == C_S[i]) {
//                        continue;
//                    }
//                    for (int t = 0; t < s[0][0].length; t++) {
//                        exprStorage.addTerm(1, s[C_S[i]][j][t]);
//                    }
//                }
//            }
//            model.addEq(exprStorage, C_S.length);
            // No.27 涉及存箱操作
            for (int i = 0; i < C_S.length; i++) {
                IloLinearIntExpr expr = model.linearIntExpr();
                for (int j = 0; j < s[0].length; j++) {
                    if (j == C_S[i]) {
                        continue;
                    }
                    for (int t = 0; t < s[0][0].length; t++) {
                        expr.addTerm(1, s[C_S[i]][j][t]);
                    }
                }
                model.addEq(expr, 1);
            }

            // No.28 涉及存箱操作
            for (int j = 0; j < s.length; j++) {
                for (int t = 0; t < s[0][0].length; t++) {
                    IloLinearIntExpr expr = model.linearIntExpr();
                    for (int i = 0; i < C_S.length; i++) {
                        expr.addTerm(1, s[C_S[i]][j][t]);
                    }
                    model.addLe(expr, 1);
                }
            }
            // No.28-1 涉及存箱操作
            for (int i = 0; i < C_S.length; i++) {
                for (int j = 0; j < s.length; j++) {
                    if (j == C_S[i]) {
                        continue;
                    }
                    for (int t = 0; t < s[0][0].length; t++) {
                        IloLinearIntExpr expr = model.linearIntExpr();
                        for (int k = 0; k < s.length; k++) {
                            if (k == j) {
                                continue;
                            }
                            expr.addTerm(1, x[k][j][t]);
                            expr.addTerm(-1, u[k][j][t]);
                            expr.addTerm(1, d[k][j][t]);
                            expr.addTerm(-1, r[k][j][t]);
                        }
                        model.addLe(s[C_S[i]][j][t], model.diff(1, expr));
                    }
                }
            }

            // No.29 涉及存箱操作
            for (int t = 0; t < s[0][0].length; t++) {
                int floor = s[0].length - 1;
                IloLinearIntExpr expr1 = model.linearIntExpr();
                IloLinearIntExpr expr2 = model.linearIntExpr();
                for (int i = 0; i < C_S.length; i++) {
                    expr1.addTerm(1, s[C_S[i]][floor][t]);
                }
                for (int i = 0; i < s.length; i++) {
                    expr2.addTerm(1, x[i][floor][t]);
                    expr2.addTerm(-1, u[i][floor][t]);
                    expr2.addTerm(1, d[i][floor][t]);
                    expr2.addTerm(-1, r[i][floor][t]);
                }
                model.addLe(expr1, model.diff(stackLength, expr2));
            }
        }
    }
    private void constraintH(IloCplex model, IloIntVar[][] h, IloIntVar[][][] x, IloIntVar[][][] u, IloIntVar[][][] d, IloIntVar[][][] r, IloIntVar[][][] s) throws IloException {
        // No.30 高度限制
        for (int i = 0; i < h.length; i++) {
            for (int j = 0; j < h.length; j++) {
                if (j == i) {
                    continue;
                }
                for (int t = 0; t < h[0].length; t++) {
                    IloLinearIntExpr expr = model.linearIntExpr();
                    expr.addTerm(1, x[i][j][t]);
                    expr.addTerm(-1, u[i][j][t]);
                    expr.addTerm(1, d[i][j][t]);
                    expr.addTerm(-1, r[i][j][t]);
                    expr.addTerm(1, s[i][j][t]);
                    model.addGe(h[i][t], model.sum(h[j][t], model.diff(1, model.prod(stackHeight, model.diff(1, expr)))));
                }
            }
        }

    }

    private void rangeVar(IloCplex model, IloIntVar[] iloIntVar) throws IloException {
        for (int i = 0; i < iloIntVar.length; i++) {
            iloIntVar[i] = model.intVar(0, 1);
        }
    }
    private void rangeVar(IloCplex model, IloIntVar[][] iloIntVar) throws IloException {
        for (int i = 0; i < iloIntVar.length; i++) {
            iloIntVar[i] = model.intVarArray(iloIntVar[0].length, 0, 1);
        }
    }
    private void rangeVar(IloCplex model, IloIntVar[][][] iloIntVar) throws IloException {
        for (int i = 0; i < iloIntVar.length; i++) {
            rangeVar(model, iloIntVar[i]);
        }
    }
    private void rangeVar(IloCplex model, IloIntVar[][][][] iloIntVar) throws IloException {
        for (int i = 0; i < iloIntVar.length; i++) {
            rangeVar(model, iloIntVar[i]);
        }
    }
    private void rangeVar(IloCplex model, IloIntVar[][][][][] iloIntVar) throws IloException {
        for (int i = 0; i < iloIntVar.length; i++) {
            rangeVar(model, iloIntVar[i]);
        }
    }
    private void rangeVar(IloCplex model, IloIntVar[][][] w3, IloIntVar[][][][] w4, IloIntVar[][][][][] w5) throws IloException {
        // 利用case穿透来初始化多个变量
        switch (stackHeight) {
            case 5: rangeVar(model, w5);
            case 4: rangeVar(model, w4);
            case 3: rangeVar(model, w3);
        }
    }
    private void rangeVar(IloCplex model, IloIntVar[][][] x, IloIntVar[][] y, IloIntVar[] z, IloIntVar[][][] u, IloIntVar[][][] d, IloIntVar[][][] r, IloIntVar[][][] s, IloIntVar[][] h) throws IloException {
        rangeVar(model, x);
        rangeVar(model, y);
        rangeVar(model, z);
        rangeVar(model, u);
        rangeVar(model, d);
        rangeVar(model, r);
        rangeVar(model, s);
        for (int i = 0; i < h.length; i++) {
            for (int t = 0; t < h[0].length; t++) {
                h[i][t] = model.intVar(1, stackHeight);
            }
        }
    }

    /**
     * 程序入口
     * @throws IloException
     */
    public void start() throws IloException {
        // 变量别名
        int C = containerSum;
        int T = upperBound;
        int[][] C_L = containerBin;
        int[] C_R = retrievalContainer;
        int[] C_S = storageContainer;
        int[] N_C_R = restRetrievalContainer;
        int[] N_C_S = restStorageContainer;

        // 定义模型
        IloCplex model = new IloCplex();

        // 定义决策变量
        // 描述集装箱的位置，在一次turn结束时，container i放于container j之上则值为1，否则为0，i≠j
        IloIntVar[][][] x = new IloIntVar[C][C + 1][T + 1];// 注意：x变化范围是[0,T]，其他变量是[0,T-1]
        // 描述任务结束时集装箱的位置，container i位于container j之上则值为1，否则为0，i≠j
        IloIntVar[][] y = new IloIntVar[C][C];
        // 描述任务结束时集装箱的属性，container i为BP container则值为1，container i为WP container则值为0
        IloIntVar[] z = new IloIntVar[C];
        // 描述lift up动作，在一次turn期间，将container i从container j上提走则值为1，否则为0，i≠j
        IloIntVar[][][] u = new IloIntVar[C][C + 1][T];
        // 描述lift down动作，在一次turn期间，将container i放置在container j上则值为1，否则为0，i≠j
        IloIntVar[][][] d = new IloIntVar[C][C + 1][T];
        // 描述retrieval动作，在一次turn期间，将container i从container j上取走则值为1，否则为0，i≠j
        IloIntVar[][][] r = new IloIntVar[C][C + 1][T];
        // 描述storage动作，在一次turn期间，将container i存放在container j上则值1，否则为0，i≠j
        IloIntVar[][][] s = new IloIntVar[C][C + 1][T];
        // 描述高度限制，在一次 turn 结束时，其值为 container i 下方存在的集装箱数量
        IloIntVar[][] h = new IloIntVar[C][T];

        // 非线性约束线性化
        IloIntVar[][][] w3 = new IloIntVar[C][C][C];// 可探测三层
        IloIntVar[][][][] w4 = new IloIntVar[C][C][C][C];// 可探测四层
        IloIntVar[][][][][] w5 = new IloIntVar[C][C][C][C][C];// 可探测五层

        // 定义决策变量的取值范围
        rangeVar(model, x, y, z, u, d, r, s, h);
        rangeVar(model, w3, w4, w5);

        // 目标函数
        objectiveFunction(model, u);

        // 约束
        // x约束
        constraintX(model, x, u, d, r, s, C_L);
        // BP约束
        constraintW(model, x, y, z, w3, w4, w5);
        // u约束
        constraintU(model, u, x);
        // d约束
        constraintD(model, d, u, x);
        // r约束
        constraintR(model, r, C_R, N_C_R, d, u, x);
        // s约束
        constraintS(model, s, C_S, C_R, N_C_S, d, u, x, r);
        // h约束
        constraintH(model, h, x, u, d, r, s);

        //导出模型
//        model.exportModel("model.lp");

        if (model.solve()) {
            System.out.println("obj = " + model.getObjValue());
        } else {
            System.out.println("MIP model not solved");
        }
        model.end();
    }
}

package com.example.android.liberate;

/**
 * Created by abhibhat on 3/8/2015.
 */
public class Solver {
    private int imgcount;
    private int rowcount;
    private int colcount;
    private int size;
    private int n;
    private int m;
    private int r;
    private int maxr;
    private int m_size;
    private int np;
    private int[][] mat;
    private int[] cols;
    private int[][] ans;
    private int [][] m_cells;
    public Solver(int[][] cells)
    {
        m_cells = cells;
        imgcount = 2;
        rowcount = cells.length;
        colcount = cells[0].length;
        size = rowcount * colcount;
        n = size;
        m = size;
        np = size * 2;
        maxr = Math.min(m, n);
        mat = new int[n][n+1];
        for (int col = 0; col < colcount; col++)
            for (int row = 0; row < rowcount; row++) {
                int i = row * colcount + col;
                for (int j = 0; j < n; j++) mat[i][j] = 0;
                mat[i][i] = 1;
                if (col > 0)            mat[i][i - 1]        = 1;
                if (row > 0)            mat[i][i - colcount] = 1;
                if (col < colcount - 1) mat[i][i + 1]        = 1;
                if (row < rowcount - 1) mat[i][i + colcount] = 1;
            }
        cols = new int[np];
        for (int j = 0; j < np; j++) cols[j] = j;

    }
    private int modulate(int x) {
        // returns z such that 0 <= z < imgcount and x == z (mod imgcount)
        if (x >= 0) return x % imgcount;
        x = (-x) % imgcount;
        if (x == 0) return 0;
        return imgcount - x;
    }
    private Boolean sweep() {
        for (r = 0; r < maxr; r++) {
            if (!sweepStep(r)) return false; // failed in founding a solution
            if (r == maxr) break;
        }
        return true; // successfully found a solution
    }
    private int a(int i,int j)   { return mat[i][cols[j]]; }
    private void  setmat(int i,int j,int val) { mat[i][cols[j]] = modulate(val); }
    private int gcd(int x,int y) { // call when: x >= 0 and y >= 0
        if (y == 0) return x;
        if (x == y) return x;
        if (x > y)  x = x % y; // x < y
        while (x > 0) {
            y = y % x; // y < x
            if (y == 0) return x;
            x = x % y; // x < y
        }
        return y;
    }

    private int invert(int value) { // call when: 0 <= value < imgcount
        // returns z such that value * z == 1 (mod imgcount), or 0 if no such z
        if (value <= 1) return value;
        int seed = gcd(value, imgcount);
        if (seed != 1) return 0;
        int a = 1, b = 0, x = value;    // invar: a * value + b * imgcount == x
        int c = 0, d = 1, y = imgcount; // invar: c * value + d * imgcount == y
        while (x > 1) {
            int tmp = ((int) Math.floor(y / x));
            y -= x * tmp;
            c -= a * tmp;
            d -= b * tmp;
            tmp = a;  a = c;  c = tmp;
            tmp = b;  b = d;  d = tmp;
            tmp = x;  x = y;  y = tmp;
        }
        return a;
    }

    private Boolean sweepStep(int r) {
        Boolean finished = true;
        for (int j = r; j < n; j++) {
            for (int i = r; i < m; i++) {
                int aij = a(i,j);
                if (aij != 0)  finished = false;
                int inv = invert(aij);
                if (inv != 0) {
                    for (int jj = r; jj < np; jj++)
                        setmat(i,jj, a(i,jj) * inv);
                    doBasicSweep(i,j);
                    return true;
                }
            }
        }
        if (finished) { // we have: 0x = b (every matrix element is 0)
            maxr = r;   // rank(A) == maxr
            for (int j = n; j < np; j++)
                for (int i = r; i < m; i++)
                    if (a(i,j) != 0)  return false; // no solution since b != 0
            return true;    // 0x = 0 has solutions including x = 0
        }
        return false;   // failed in finding a solution
    }
    private void swap(int array[],int x,int y) {
        int tmp  = array[x];
        array[x] = array[y];
        array[y] = tmp;
    }
    private void doBasicSweep(int pivoti,int pivotj) {
        if (r != pivoti)
        {
            int[] tmp  = mat[pivoti];
            mat[pivoti] = mat[r];
            mat[r] = tmp;
        };
        if (r != pivotj)
        {
            int tmp  = cols[pivotj];
            cols[pivotj] = cols[r];
            cols[r] = tmp;
        };
        for (int i = 0; i < m; i++) {
            if (i != r) {
                int air = a(i,r);
                if (air != 0)
                    for (int j = r; j < np; j++)
                        setmat(i,j, a(i,j) - a(r,j) * air);
            }
        }
    }

    private Boolean solveProblem(int goal) {
        int size = colcount * rowcount;
        m = size;
        n = size;
        np = n + 1;
        for (int col = 0; col < colcount; col++)
            for (int row = 0; row < rowcount; row++)
                mat[row * colcount + col][n] = modulate(goal - m_cells[col][row]);
        return sweep();
    }
    public int[][] solve() {
        for (int goal = 0; goal < imgcount; goal++) {
            if (solveProblem(goal)) { // found an integer solution
                int[] anscols = new int[n];
                ans = new int[colcount][rowcount];
                int j;
                for (j = 0; j < n; j++)  anscols[cols[j]] = j;
                for (int col = 0; col < colcount; col++)
                    for (int row = 0; row < rowcount; row++) {
                        int value;
                        j = anscols[row * colcount + col];
                        if (j < r) value = a(j,n); else value = 0;
                        ans[col][row] = value;
                    }
                return ans;
            }
        }
        return ans;
    }

}

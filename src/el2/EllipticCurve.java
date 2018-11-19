package el2;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Random;


public class EllipticCurve {
    final static BigInteger ZERO = new BigInteger("0");
    final static BigInteger ONE = new BigInteger("1");
    final static BigInteger TWO = new BigInteger("2");
    final static BigInteger THREE = new BigInteger("3");
    final static BigInteger FOUR = new BigInteger("4");
    final static BigInteger SIX = new BigInteger("6");

    static BigInteger[] ECgenerator(int l) throws FileNotFoundException {
        BigInteger p = BigInteger.valueOf(0);
        BigInteger r = BigInteger.valueOf(0);
        BigInteger N = BigInteger.valueOf(0);

        boolean foundP = false;
        while (!foundP) {
            boolean findR = false;
            while (!findR) {
                p = gen(l);

                BigInteger ab[] = factorization(p);
                if (ab == null) continue;

                BigInteger a = ab[0];
                BigInteger b = ab[1];
                //       System.out.println("простое P = " + p);
                //      System.out.println("a = " + a);
                //     System.out.println("b = " + b);
                //               System.out.println("a^2 + b^2 = p");

                for (int i = 0; i < 4; i++) {
                    N = p.add(BigInteger.ONE);
                    BigInteger T;
                    if (i == 0)
                        T = a.multiply(TWO);
                    else if (i == 1)
                        T = a.multiply(TWO).negate();
                    else if (i == 2)
                        T = b.multiply(TWO);
                    else
                        T = b.multiply(TWO).negate();


                    N = N.add(T);
                    r = BigInteger.ZERO;

                    if (N.mod(TWO).compareTo(BigInteger.ZERO) == 0 && (N.divide(TWO)).isProbablePrime(100)) {
                        r = N.divide(TWO);
                        findR = true;
                        break;
                    }
                    if (N.mod(FOUR).compareTo(BigInteger.ZERO) == 0 && (N.divide(FOUR).isProbablePrime(100))) {
                        r = N.divide(FOUR);
                        findR = true;
                        break;
                    }
                }
            }
            foundP = true;
            if (p.compareTo(r) == 0) foundP = false;
            int m = 5; // параметр безопасности

            for (int i = 1; i <= m; i++) {
                if ((p.modPow(BigInteger.valueOf(i), r)).compareTo(BigInteger.ONE) == 0) {
                    foundP = false;
                    break;
                }
            }

            if (foundP) break;
        }
        System.out.println("p = " + p);
        elliptic_math.p = p;
//        System.out.println("N = " + N);
        System.out.println("r = " + r);
        elliptic_math p_infinity, p0;
        BigInteger A, x0, y0;

        boolean inf = false;
        do {
            BigInteger[] Ax0y0 = Ax0y0(p, N, r);
            A = Ax0y0[0];
            x0 = Ax0y0[1];
            y0 = Ax0y0[2];
            p0 = new elliptic_math(x0, y0);

            elliptic_math.a = A;
            inf = (elliptic_math.mult(p0, N) == null);
        } while (!inf);
//        System.out.println("Точка p0 ");
        System.out.println("A = " + A);
//        System.out.println("x0 = " + x0);
//        System.out.println("y0 = " + y0);

        elliptic_math Q = elliptic_math.mult(p0, N.divide(r));
        System.out.println("Q = " + Q);

        return new BigInteger[] {p, Q.x, Q.y, Q.a, r};
    }

    static BigInteger gen(int l) {
        Random rnd = new Random();
        BigInteger p;
        do {
            p = BigInteger.probablePrime(l, rnd);
        } while (!p.isProbablePrime(100) || !p.mod(FOUR).toString().equals("1"));
        return p;
    }

    // p = c^2 + d^2
    static BigInteger[] factorization(BigInteger p) {
        BigInteger[] cd = new BigInteger[2];
        BigInteger D = ONE;

        BigInteger minusD = p.subtract(D);
        if (sqrNotVichet(minusD, p)) return null;

        BigInteger u = sqrt_mod(minusD, p);
        int i = 0;
        ArrayList<BigInteger> ui = new ArrayList<BigInteger>();
        ArrayList<BigInteger> mi = new ArrayList<BigInteger>();
        ui.add(u);
        mi.add(p);
        do {
            BigInteger nextM = (((ui.get(i)).pow(2)).add(D)).divide(mi.get(i));
            mi.add(nextM);
            BigInteger left = ui.get(i).mod(mi.get(i + 1));
            BigInteger right = ((mi.get(i + 1)).subtract(ui.get(i))).mod(mi.get(i + 1));
            BigInteger nextU = left.compareTo(right) > 0 ? right : left;
            ui.add(nextU);
            i++;
            if (mi.get(i).compareTo(FOUR) == 0) { // зациклился
                return null;
            }
        } while (mi.get(i).compareTo(BigInteger.ONE) != 0);
        i--;

        BigInteger[] ai = new BigInteger[i + 1];
        BigInteger[] bi = new BigInteger[i + 1];
        ai[i] = ui.get(i);
        bi[i] = BigInteger.ONE;

        while (i != 0) {
            BigInteger aChisl = ((ui.get(i - 1)).multiply(ai[i])).add(D.multiply(bi[i]));
            BigInteger aZnam = (ai[i].pow(2)).add(D.multiply(bi[i].pow(2)));
            BigInteger nextA;
            if (aChisl.mod(aZnam).compareTo(BigInteger.ZERO) != 0) {
                aChisl = (D.multiply(bi[i])).subtract((ui.get(i - 1)).multiply(ai[i]));
            }
            nextA = aChisl.divide(aZnam);

            BigInteger bChisl = (ai[i].negate()).add((ui.get(i - 1)).multiply(bi[i]));
            BigInteger bZnam = (ai[i].pow(2)).add(D.multiply(bi[i].pow(2)));
            BigInteger nextB;
            if (bChisl.mod(bZnam).compareTo(BigInteger.ZERO) != 0) {
                bChisl = (ai[i].negate()).subtract((ui.get(i - 1)).multiply(bi[i]));
            }
            nextB = bChisl.divide(bZnam);

            ai[i - 1] = nextA;
            bi[i - 1] = nextB;

            i--;
        }
        BigInteger a = new BigInteger(ai[0].toString());
        BigInteger b = new BigInteger(bi[0].toString());
        if (a.compareTo(BigInteger.ZERO) < 0) a = a.negate();
        if (b.compareTo(BigInteger.ZERO) < 0) b = b.negate();
        cd[0] = a;
        cd[1] = b;

        return cd;
    }

    static BigInteger[] Ax0y0(BigInteger p, BigInteger N, BigInteger r) {
        BigInteger[] Ax0y0 = new BigInteger[3];
        BigInteger A, x0, y0;

        do {
            x0 = new BigInteger(p.bitLength(), new Random()).mod(p);
            y0 = new BigInteger(p.bitLength(), new Random()).mod(p);
            if (x0.compareTo(ZERO) == 0) x0 = ONE;
            if (y0.compareTo(ZERO) == 0) y0 = ONE;
            A = (((y0.pow(2)).subtract(x0.pow(3))).multiply(x0.modInverse(p))).mod(p);
        }
        while (!((N.compareTo(r.multiply(TWO)) == 0) && (sqrNotVichet(p.subtract(A), p)) ||
                ((N.compareTo(r.multiply(FOUR)) == 0) && (sqrVichet(p.subtract(A), p)))));

        Ax0y0[0] = A;
        Ax0y0[1] = x0;
        Ax0y0[2] = y0;
        return Ax0y0;
    }

    static boolean sqrVichet(BigInteger B, BigInteger p) {
        BigInteger bb = B.modPow((p.subtract(BigInteger.ONE)).divide(TWO), p);
        if (bb.toString().equals("1")) return true;
        else return false;
    }

    static boolean sqrNotVichet(BigInteger B, BigInteger p) {
        BigInteger bb = B.modPow((p.subtract(BigInteger.ONE)).divide(TWO), p);
        if (bb.compareTo(p.subtract(BigInteger.ONE)) == 0) return true;
        else return false;
    }

    static BigInteger sqrt_mod(BigInteger a, BigInteger p) {
        if (sqrNotVichet(a, p)) return null;
        BigInteger b = TWO;
        for (BigInteger j = TWO; j.compareTo(p) < 0; j = j.add(BigInteger.ONE)) {
            if (sqrNotVichet(j, p)) {
                b = j;
                break;
            }
        }
        BigInteger pSub1 = p.subtract(BigInteger.ONE);
        BigInteger pTemp = new BigInteger(pSub1.toString());
        BigInteger t;
        int s = 0;
        while (pTemp.mod(TWO).compareTo(BigInteger.ZERO) == 0) {
            s++;
            pTemp = pTemp.divide(TWO);
        }
        t = new BigInteger(pTemp.toString());
        BigInteger aRevers = a.modInverse(p);
        BigInteger c = b.modPow(t, p);
        BigInteger r = a.modPow((t.add(BigInteger.ONE)).divide(TWO), p);
        for (int i = 1; i <= s - 1; i++) {
            BigInteger twoPow = TWO.pow(s - i - 1);
            BigInteger d = ((r.pow(2)).multiply(aRevers)).modPow(twoPow, p);
            if (d.compareTo(pSub1) == 0) r = (r.multiply(c)).mod(p);
            c = c.modPow(TWO, p);
        }
        return r;
    }
}


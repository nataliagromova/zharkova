package el1; /**
 * Created by NATA on 30.09.2018.
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class EllipticCurve {
    final static BigInteger ZERO = new BigInteger("0");
    final static BigInteger ONE = new BigInteger("1");
    final static BigInteger TWO = new BigInteger("2");
    final static BigInteger FOUR = new BigInteger("4");

    static void ECgenerator(int l) throws FileNotFoundException {
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
                System.out.println("простое P = " + p);
                System.out.println("a = " + a);
                System.out.println("b = " + b);
                System.out.println("a^2 + b^2 = p");
                for (int i = 0; i < 4; i++) {
                    N = p.add(BigInteger.ONE);
                    BigInteger T;
                    if (i == 0) T = a.multiply(TWO);
                    else if (i == 1) T = a.multiply(TWO).negate();
                    else if (i == 2) T = b.multiply(TWO);
                    else T = b.multiply(TWO).negate();
                    N = N.add(T);
                    r = ZERO;
                    if (N.mod(TWO).compareTo(ZERO) == 0 && (N.divide(TWO)).isProbablePrime(100)) {
                        r = N.divide(TWO);
                        findR = true;
                        break;
                    }
                    if (N.mod(FOUR).compareTo(ZERO) == 0 && (N.divide(FOUR).isProbablePrime(100))) {
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
        System.out.println("Параметры подобраны");
        System.out.println("p = " + p);
        EllipticPoint.p = p;
        System.out.println("N = " + N);
        System.out.println("r = " + r);
        EllipticPoint p0;
        BigInteger A, x0, y0;
        boolean inf;
        do {
            BigInteger[] Ax0y0 = Ax0y0(p, N, r);
            A = Ax0y0[0];
            x0 = Ax0y0[1];
            y0 = Ax0y0[2];
            p0 = new EllipticPoint(x0, y0);
            EllipticPoint.a = A;
            inf = (EllipticPoint.mult(p0, N) == null);
        } while (!inf);
        System.out.println("Точка p0 найдена!");
        System.out.println("A = " + A);
        System.out.println("x0 = " + x0);
        System.out.println("y0 = " + y0);
        EllipticPoint Q = EllipticPoint.mult(p0, N.divide(r));
        System.out.println("Q = " + Q);
        pointsGen(Q, r);
    }

    static void pointsGen(EllipticPoint Q, BigInteger r) throws FileNotFoundException {
        File file1 = new File("outputX.txt");
        PrintWriter pw1 = new PrintWriter(file1);
        File file2 = new File("outputY.txt");
        PrintWriter pw2 = new PrintWriter(file2);
        if ((r.compareTo(BigInteger.valueOf(Integer.MAX_VALUE))) == -1) {
            double[] xa = new double[r.intValueExact()-1];
            double[] ya = new double[r.intValueExact()-1];
            int j = 0;
            for (BigInteger i = BigInteger.ONE; i.compareTo(r) <= 0; i = i.add(BigInteger.ONE), j++) {
                EllipticPoint p = EllipticPoint.mult(Q, i);
                if (p == null) continue;
                String pointX = p.x.toString();
                String pointY = p.y.toString();
                xa[j] = p.x.doubleValue();
                ya[j] = p.y.doubleValue();
                pw1.write(pointX + "\r\n");
                pw2.write(pointY + "\r\n");
            }
            new PlotFrame(new double[][]{xa, ya});
        } else {
            int j = 0;
            for (BigInteger i = BigInteger.ONE; i.compareTo(r) <= 0; i = i.add(BigInteger.ONE), j++) {
                EllipticPoint p = EllipticPoint.mult(Q, i);
                if (p == null) continue;
                String pointX = p.x.toString();
                String pointY = p.y.toString();
                pw1.write(pointX + "\r\n");
                pw2.write(pointY + "\r\n");
            }
        }
        pw1.flush();
        pw2.flush();
    }

    static BigInteger gen(int l) {
        Random rnd = new Random();
        int r = rnd.nextInt((int) Math.pow(2, l));
        BigInteger p = new BigInteger(String.valueOf(r));
        do {
            p = p.nextProbablePrime();
        } while (!p.isProbablePrime(100) || !p.mod(FOUR).toString().equals("1"));
        return p;
    }
    // p = a^2 + b^2

    static BigInteger[] factorization(BigInteger p) {
        BigInteger[] ab = new BigInteger[2];
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
        }
        while (mi.get(i).compareTo(BigInteger.ONE) != 0);
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
        ab[0] = a;
        ab[1] = b;
        return ab;
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
        while (!((N.compareTo(r.multiply(TWO)) == 0) && (sqrNotVichet(p.subtract(A), p)) || ((N.compareTo(r.multiply(FOUR)) == 0) && (sqrVichet(p.subtract(A), p)))));
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

    //http://math.fau.edu/bkhadka/Syllabi/A%20handbook%20of%20applied%20cryptography.pdf 100
    static BigInteger sqrt_mod(BigInteger a, BigInteger p) {
        if (sqrNotVichet(a, p)) return null;
        BigInteger b = TWO;
        for (BigInteger j = TWO; j.compareTo(p) < 0; j = j.add(ONE)) {
            if (sqrNotVichet(j, p)) {
                b = j;
                break;
            }
        }
        BigInteger pSub1 = p.subtract(ONE);
        BigInteger t = new BigInteger(pSub1.toString());
        int s = 0;
        while (t.mod(TWO).compareTo(ZERO) == 0) {
            s++;
            t = t.divide(TWO);
        }
        BigInteger aRevers = a.modInverse(p);
        BigInteger c = b.modPow(t, p);
        BigInteger r = a.modPow((t.add(ONE)).divide(TWO), p);
        for (int i = 1; i <= s - 1; i++) {
            BigInteger twoPow = TWO.pow(s - i - 1);
            BigInteger d = ((r.pow(2)).multiply(aRevers)).modPow(twoPow, p);
            if (d.compareTo(pSub1) == 0) r = (r.multiply(c)).mod(p);
            c = c.modPow(TWO, p);
        }
        return r;
    }


    public static void main(String[] args) throws FileNotFoundException {
        Scanner scan = new Scanner(System.in);
        System.out.println("Введите длину l характеристики поля: ");
        int l = scan.nextInt();
//        System.out.println("Введите параметр безопасности: ");
//        int m = scan.nextInt();
        ECgenerator(l);
    }
}

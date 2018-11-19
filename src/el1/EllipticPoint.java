package el1; /**
 * Created by NATA on 30.09.2018.
 */

import java.math.BigInteger;

public class EllipticPoint {
    BigInteger x;
    BigInteger y;
    public static BigInteger p;
    public static BigInteger a;
    final static BigInteger ZERO = new BigInteger("0");
    final static BigInteger ONE = new BigInteger("1");
    final static BigInteger TWO = new BigInteger("2");
    final static BigInteger THREE = new BigInteger("3");

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    public EllipticPoint(BigInteger x, BigInteger y) {
        this.x = x;
        this.y = y;
    }

    static EllipticPoint sum(EllipticPoint P1, EllipticPoint P2) {
        if (P1 == null) return P2;
        if (P2 == null) return P1;
        BigInteger lambda, x3, y3;
        if (P1.x.compareTo(P2.x) == 0 && P1.y.compareTo(P2.y) == 0) {
            if (P1.y.compareTo(ZERO) == 0) return null;
            lambda = (THREE.multiply(P1.x.pow(2)).add(a)).multiply(((TWO.multiply(P1.y)).modInverse(p))).mod(p);
            x3 = (lambda.pow(2).subtract(TWO.multiply(P1.x))).mod(p);
            y3 = (lambda.multiply(P1.x.subtract(x3)).subtract(P1.y)).mod(p);
        } else {
            if (P1.x.equals(P2.x)) return null;
            lambda = ((P2.y.subtract(P1.y)).multiply(((P2.x.subtract(P1.x)).modInverse(p))).mod(p));
            x3 = (lambda.pow(2).subtract(P2.x).subtract(P1.x)).mod(p);
            y3 = (lambda.multiply(P1.x.subtract(x3)).subtract(P1.y)).mod(p);
        }
        return new EllipticPoint(x3, y3);
    }

    static EllipticPoint mult(EllipticPoint P, BigInteger N) {
        if (P == null) return null;
        if (N.compareTo(BigInteger.ONE) == 0) return P;
        EllipticPoint Q = mult(P, N.divide(TWO));
        Q = sum(Q, Q);
        if (N.mod(TWO).compareTo(ZERO) != 0)
            Q = sum(Q, P);
        return Q;
    }

    public EllipticPoint reverse(BigInteger p) {
        return new EllipticPoint(x, p.subtract(y));
    }
}
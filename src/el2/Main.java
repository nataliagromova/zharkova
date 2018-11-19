package el2;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * Created by NATA on 18.11.2018.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("1  - сгенерировать ключи");
        System.out.println("2  - подписать");
        System.out.println("3  - проверить подпись");
        Scanner scanner = new Scanner(System.in);
        BigInteger mas[]=null;
        List<String> lines;
        while (true) {
            int choose = scanner.nextInt();
            if (choose == 1) {
                System.out.println("введите длину p");
                int l = scanner.nextInt();
                mas = generate(l);
            }
            if (choose == 2) {
                if(mas==null)
                    throw new Exception("нет данных о кривой");
                lines = Files.readAllLines(Paths.get("l.txt"), StandardCharsets.UTF_8);
                BigInteger l_l = new BigInteger(lines.get(0));
                sign(mas, l_l);
                System.out.println("результат в sign.txt");
            }
            if (choose == 3) {
                if(mas==null)
                    throw new Exception("нет данных о кривой");
                lines = Files.readAllLines(Paths.get("P.txt"), StandardCharsets.UTF_8);
                String[] point = lines.get(0).split("\\D+");
                BigInteger px = new BigInteger(point[1]);
                BigInteger py = new BigInteger(point[2]);
                checkSign(mas, new elliptic_math(px, py), "sign.txt");
                break;
            }
        }
    }

    public static BigInteger[] generate(int lp) throws IOException {
        System.out.println("генерация ключей:");
        BigInteger[] answer = EllipticCurve.ECgenerator(lp);
        BigInteger l = new BigInteger(30, new Random());
        elliptic_math.a = answer[3];
        elliptic_math.p = answer[0];
        elliptic_math Pl = elliptic_math.mult(new elliptic_math(answer[1], answer[2]), l);
        FileWriter out = new FileWriter("P.txt");
        out.write(Pl.toString());
        out.flush();
        out = new FileWriter("l.txt");
        out.write(l.toString());
        out.flush();
        System.out.println("открытый ключ в P.txt");
//        System.out.println("P = " + Pl);
        System.out.println("закрытый ключ в l.txt");
//        System.out.println("l=" + l);
        return answer;
    }

    static void sign(BigInteger[] mas, BigInteger l) throws IOException, NoSuchAlgorithmException {
        elliptic_math.a = mas[3];
        elliptic_math.p = mas[0];
        BigInteger r = mas[4];
        String s_hash = hash("m.txt");
        BigInteger e = new BigInteger(s_hash).mod(r);
        if (e.compareTo(BigInteger.ZERO) == 0)
            e = BigInteger.ONE;
        elliptic_math R;
        BigInteger k;
        do {
            k = new BigInteger(7, new Random());
            R = elliptic_math.mult(new elliptic_math(mas[1], mas[2]), k);
        }
        while (R.x.mod(r).compareTo(BigInteger.ZERO) == 0);
        BigInteger s = l.multiply(R.x).add(k.multiply(e)).mod(r);
        FileWriter out = new FileWriter("sign.txt");
        out.write(R.x.mod(r).toString());
        out.append('\n');
        out.write(s.toString());
        out.flush();
    }

    static String hash(String name) throws IOException, NoSuchAlgorithmException {
        List<String> lines = Files.readAllLines(Paths.get(name), StandardCharsets.UTF_8);
        String s = "";
        for (String idx : lines) {
            s += idx + '\n';
        }
        MessageDigest hash = MessageDigest.getInstance("MD5");
        hash.update(s.getBytes());
        byte[] messageDigestMD5 = hash.digest();
        StringBuffer stringBuffer = new StringBuffer();
        for (byte bytes : messageDigestMD5) {

            stringBuffer.append(bytes);
        }
        return stringBuffer.toString().replaceAll("-", "");
    }

    static void checkSign(BigInteger[] mas, elliptic_math P, String nameSign) throws IOException, NoSuchAlgorithmException {
        System.out.println("проверка подписи...");
        List<String> lines = Files.readAllLines(Paths.get(nameSign), StandardCharsets.UTF_8);
        elliptic_math.a = mas[3];
        elliptic_math.p = mas[0];
        BigInteger r = mas[4];
        BigInteger xR = new BigInteger(lines.get(0));
        BigInteger s = new BigInteger(lines.get(1));
        if (xR.compareTo(r) > 1 ||
                s.compareTo(r) > 1) {
            System.out.println("подпись не верна");
            return;
        }
        String s_hash = hash("m.txt");
        BigInteger e = new BigInteger(s_hash).mod(r);
        if (e.compareTo(BigInteger.ZERO) == 0)
            e = BigInteger.ONE;
        BigInteger inverseE = e.modInverse(r);
        BigInteger SinverseE = (s.multiply(inverseE).mod(r));
        BigInteger xRinverseE = xR.multiply(inverseE).mod(r);
        elliptic_math left = elliptic_math.mult(new elliptic_math(mas[1], mas[2]), SinverseE);
        elliptic_math right = elliptic_math.mult(P, xRinverseE);
        right.y = right.y.negate();
        elliptic_math R1 = elliptic_math.sum(left, right);
        if (xR.compareTo(R1.x.mod(r)) == 0) {
            System.out.println("xR'= "+R1.x.mod(r));
            System.out.println("xR = "+xR);
            System.out.println("подпись верна!");
        } else {
            System.out.println("подпись не верна:(");
        }
    }
}

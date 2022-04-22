package cc.minetale.postman;

import java.security.SecureRandom;

public class StringUtil {

    private static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final SecureRandom random = new SecureRandom();

    public static String repeat(String string, int times) {
        return new String(new char[times]).replace("\0", string);
    }

    public static String randomString(int len) {
        StringBuilder sb = new StringBuilder(len);

        for(int i = 0; i < len; i++)
            sb.append(AB.charAt(random.nextInt(AB.length())));

        return sb.toString();
    }

    public static String generateId() {
        return randomString(8);
    }

}

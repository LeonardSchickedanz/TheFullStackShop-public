package yorku.thefullstackshop.utils;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

public class ValidationUtil {


    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");

    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[^A-Za-z0-9\\s]).{6,}$");

    private static final Pattern NO_NUMBERS_PATTERN = Pattern.compile("^[^0-9]+$");

    private static final Pattern ONLY_NUMBERS_PATTERN = Pattern.compile("^\\d+$");

    private static final Pattern POSTAL_CODE_PATTERN = Pattern.compile("^[A-Za-z0-9\\s-]+$");

    private static final Pattern CARD_PATTERN = Pattern.compile("^\\d{16}$");

    private static final Pattern EXPIRY_PATTERN = Pattern.compile("^(0[1-9]|1[0-2])/\\d{2}$");

    private static final Pattern CVC_PATTERN = Pattern.compile("^\\d{3}$");


    public static boolean isEmpty(String text) {
        return text == null || text.trim().isEmpty();
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }

    public static boolean isTextOnly(String text) {
        return text != null && NO_NUMBERS_PATTERN.matcher(text).matches();
    }

    public static boolean isNumbersOnly(String text) {
        return text != null && ONLY_NUMBERS_PATTERN.matcher(text).matches();
    }

    public static boolean isValidPostalCode(String code) {
        return code != null && POSTAL_CODE_PATTERN.matcher(code).matches();
    }

    public static boolean isValidCreditCard(String number) {
        if (number == null) return false;
        String cleanNumber = number.replaceAll("\\s+", "");
        return CARD_PATTERN.matcher(cleanNumber).matches();
    }

    public static boolean isValidCVC(String cvc) {
        return cvc != null && CVC_PATTERN.matcher(cvc).matches();
    }

    public static boolean isValidExpiryFormat(String date) {
        return date != null && EXPIRY_PATTERN.matcher(date).matches();
    }

    public static boolean isExpiryDateInFuture(String date) {
        if (!isValidExpiryFormat(date)) return false;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yy");
            YearMonth expiry = YearMonth.parse(date, formatter);
            return expiry.isAfter(YearMonth.now()) || expiry.equals(YearMonth.now());
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
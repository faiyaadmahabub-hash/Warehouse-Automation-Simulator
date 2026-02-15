/**
 * Simplified String utility class
 */
public class StringUtils {
    
    public static String repeat(String str, int count) {
        if (count <= 0 || str == null) return "";
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
    
    public static String center(String text, int width) {
        if (text == null || text.length() >= width) return text;
        
        int padding = width - text.length();
        int leftPad = padding / 2;
        int rightPad = padding - leftPad;
        
        return repeat(" ", leftPad) + text + repeat(" ", rightPad);
    }
}
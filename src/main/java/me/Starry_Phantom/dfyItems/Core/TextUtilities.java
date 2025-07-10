package me.Starry_Phantom.dfyItems.Core;

import me.Starry_Phantom.dfyItems.DfyItems;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

import java.io.File;
import java.util.*;

public class TextUtilities {
    private static DfyItems PLUGIN;
    private static final String[] regexChars = {"^", ".", "[", "]", "{", "}", "(", ")", "|", "\\", "/", "$", "*"};
    private static final String[] ROMAN_NUMERALS = "I,II,III,IV,V,VI,VII,VIII,IX,X,XI,XII,XIII,XIV,XV,XVI,XVII,XVIII,XIX,XX,XXI,XXII,XXIII,XXIV,XXV,XXVI,XXVII,XXVIII,XXIX,XXX,XXXI,XXXII,XXXIII,XXXIV,XXXV,XXXVI,XXXVII,XXXVIII,XXXIX,XL,XLI,XLII,XLIII,XLIV,XLV,XLVI,XLVII,XLVIII,XLIX,L,LI,LII,LIII,LIV,LV,LVI,LVII,LVIII,LIX,LX,LXI,LXII,LXIII,LXIV,LXV,LXVI,LXVII,LXVIII,LXIX,LXX,LXXI,LXXII,LXXIII,LXXIV,LXXV,LXXVI,LXXVII,LXXVIII,LXXIX,LXXX,LXXXI,LXXXII,LXXXIII,LXXXIV,LXXXV,LXXXVI,LXXXVII,LXXXVIII,LXXXIX,XC,XCI,XCII,XCIII,XCIV,XCV,XCVI,XCVII,XCVIII,XCIX,C".split(",");

    public static ArrayList<String> wrapText(String text, String wrapColor) {
        ArrayList<String> output = new ArrayList<>();

        if (PLUGIN == null) {
            throw new RuntimeException("No plugin found in class :c");
        }
        String configLength = PLUGIN.getConfig().getString("wrap-length");
        int wrapLength;
        if (configLength == null) wrapLength = 32;
        else wrapLength = Integer.parseInt(configLength);

        text = wrapColor + text;

        for (int j = 0; j < text.length(); j++) {
            int limit = wrapLength;

            if (text.charAt(j) == '§') limit+=2;
            if (text.charAt(j) == '\n') {
                output.add(wrapColor + text.substring(0,j));
                text = wrapColor + text.substring(j + 1);
                j = 0;
            }

            if (j == limit) {
                j = findClosestWhitespace(j, text);
                if (j == -1) j = limit;
                if (j > limit + 10) j = limit + 10;
                output.add(wrapColor + text.substring(0,j));
                text = wrapColor + text.substring(j + 1);
                j = 0;
            }

            if (j != 0 && text.substring(j-1,j+1).equals("\\n")) {
                output.add(wrapColor + text.substring(0,j-1));
                text = wrapColor + text.substring(j+1);
                j = 0;
            }
        }

        output.add(text);

        return output;
    }

    public static void setPlugin(DfyItems plugin) {
        PLUGIN = plugin;

    }

    public static ArrayList<String> wrapText(String text) {
        return wrapText(text, "");
    }

    public static ArrayList<TextComponent> insertIntoComponents(ArrayList<String> strings) {
        ArrayList<TextComponent> output = new ArrayList<>();
        for (String s : strings) output.add(Component.text("§7" + s));
        return output;
    }

    public static int findClosestWhitespace(int index, String text) {
        int endSpace = text.indexOf(" ", index);
        int startSpace = -1;
        for (int i = index; i >= 0; i--) {
            if (text.charAt(i) == ' ') {
                startSpace = i;
            }
        }
        if (startSpace == -1 || endSpace == -1) return -1;
        if (endSpace - index > index - startSpace) return startSpace;
        return endSpace;
    }

    public static String correctPath(String path) {
        path = path.replace("/", "\\");
        if (!File.separator.equals("\\")) path = path.replace("\\", File.separator);
        return path;
    }

    public static String makeRegexSafe(String regex) {
        if (regex.length() == 1) {
            if (Arrays.stream(regexChars).toList().contains(regex)) return "\\" + regex;
        }
        for (String s : regexChars) {
            regex = regex.replace(s, "\\" + s);
        }
        return regex;
    }

    public static String getRomanNumeral(int i) {
        if (i < 1) return null;
        if (i - 1 < ROMAN_NUMERALS.length) return ROMAN_NUMERALS[i - 1];
        return Integer.toString(i);
    }

    public static String toClassCase(String string) {
        return toReadableCase(string).replace(" ", "");

    }

    public static String toReadableCase(String string) {
        if (string.length() <= 1) {
            return string.toUpperCase();
        }
        string = string.toLowerCase();

        string = string.substring(0,1).toUpperCase() + string.substring(1);
        for (int i = 1; i < string.length(); i++) {
            if (string.charAt(i - 1) == '_') string = string.substring(0, i) + string.substring(i, i+1).toUpperCase() + string.substring(i+1);
        }
        return string.replace("_", " ");

    }
}

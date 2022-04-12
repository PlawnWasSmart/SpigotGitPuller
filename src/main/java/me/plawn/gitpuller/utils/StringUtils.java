package me.plawn.gitpuller.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    public static String saveToHastebin(String s){
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL("https://www.toptal.com/developers/hastebin/documents").openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type","text/plain");
            connection.setDoOutput(true);
            connection.getOutputStream().write(s.getBytes());
            String r = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine();
            Matcher matcher = Pattern.compile("\\{\"key\":\"(.*?)\"}").matcher(r);
            matcher.find();
            return "https://hastebin.com/" +
                    matcher.group(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

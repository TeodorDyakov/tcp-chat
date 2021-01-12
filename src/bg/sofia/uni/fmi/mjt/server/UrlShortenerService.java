package bg.sofia.uni.fmi.mjt.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UrlShortenerService {

    private static final String API_KEY = "e3e030e464a2e5b701738aab87c3b48cdc772";

    public static String shortenURL(String longURL) throws IOException {
        URL url = new URL("https://cutt.ly/api/api.php?key=" + API_KEY + "&short=" + longURL +"&name=myUrl");
        System.out.println(url);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                + conn.getResponseCode());
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(
            (conn.getInputStream())));

        System.out.println("Output from Server .... \n");
        String out = br.readLine();
        conn.disconnect();
        return out;
    }

    public static void main(String[] args) throws IOException {
        System.out.println(shortenURL("www.google.com"));
    }
}

package bg.sofia.uni.fmi.mjt.server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class URLshortener {

    private static final String API_KEY = "e3e030e464a2e5b701738aab87c3b48cdc772";

    public String shorten(String longUrl) {
        HttpClient client = HttpClient.newBuilder().build();
        URI url;

        try {
            url = new URI("https://cutt.ly/api/api.php?key=" + API_KEY + "&short=" + longUrl);
        } catch (URISyntaxException e) {
            return longUrl;
        }
        Gson gson = new Gson();
        HttpRequest request = HttpRequest.newBuilder().uri(url).build();
        String response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
        } catch (IOException | InterruptedException exception) {
            return longUrl;
        }
        JsonObject jsonObject = gson.fromJson(response, JsonObject.class);
        if (jsonObject.has("url")) {
            JsonObject inner = jsonObject.getAsJsonObject("url");
            if (inner.has("shortLink")) {
                return inner.get("shortLink").getAsString();
            }
        }
        return longUrl;
    }

    public String shortenURLs(String message) {
        String[] tokens = message.split("\\s+");
        List<String> urls = new ArrayList<>();
        for (String tok : tokens) {
            try {
                URL url = new URL(tok);
                urls.add(url.toString());
            } catch (MalformedURLException ignored) {
            }
        }
        for (var uriString : urls) {
            message = message.replace(uriString, shorten(uriString));
        }
        return message;
    }

    public static void main(String[] args) {
        URLshortener urLshortener = new URLshortener();
        String s = urLshortener.shorten("https://www.fmi.uni-sofia.bg/en");
        System.out.println(s);
        String m = urLshortener.shortenURLs("https://wwww.google.com hello here is the link to fmi site https://www.fmi.uni-sofia.bg/en");
        System.out.println(m);
    }
}

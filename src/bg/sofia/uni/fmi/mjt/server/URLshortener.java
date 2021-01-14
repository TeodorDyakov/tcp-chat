package bg.sofia.uni.fmi.mjt.server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class URLshortener {

    private static final String API_KEY = "e3e030e464a2e5b701738aab87c3b48cdc772";

    public static String shorten(String longUrl) {
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

    public static void main(String[] args) {
        String s = shorten("https://www.fmi.uni-sofia.bg/en");
        System.out.println(s);
    }
}

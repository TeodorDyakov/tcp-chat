package bg.sofia.uni.fmi.mjt.server;

import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class URLshortenerTest {

    @Test
    public void shortenUrisTest() {
        URLshortener urLshortener = Mockito.mock(URLshortener.class);

        final String fmiURL = "https://www.fmi.uni-sofia.bg/en";
        final String shortURL = "https://cutt.ly/xjREZsj";

        when(urLshortener.shorten(fmiURL)).thenReturn(shortURL);
        when(urLshortener.shorteURLs(anyString())).thenCallRealMethod();

        final String message = "hi this is the link to fmi " + fmiURL + " click it";
        assertEquals(urLshortener.shorteURLs(message), "hi this is the link to fmi " + shortURL + " click it");
    }
}
package com.immortals.miniurl.utils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.net.URISyntaxException;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class UrlUtil {

    /**
     * Builds a full URL by ensuring the scheme (http/https) is present and appending the path.
     * If scheme is missing in baseUrl, defaults to http.
     *
     * @param baseUrl the base URL (e.g., "localhost" or "http://localhost")
     * @param path    the path to append (e.g., "1lgN0ZPMjK")
     * @return full absolute URL as a string
     * @throws URISyntaxException if URL is invalid
     */
    public static String buildFullUrl(String baseUrl, String path) throws URISyntaxException {
        // Check if baseUrl has a scheme; if missing, prepend "http://"
        if (!baseUrl.matches("^(http|https)://.*$")) {
            baseUrl = "http://" + baseUrl;
        }

        URI baseUri = new URI(baseUrl);

        // Resolve the path against base URI (this handles slashes correctly)
        URI fullUri = baseUri.resolve(path.startsWith("/") ? path : "/" + path);

        return fullUri.toString();
    }

}

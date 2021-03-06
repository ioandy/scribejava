package com.github.scribejava.apis.service;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.codec.CharEncoding;
import static org.apache.commons.codec.digest.DigestUtils.md5Hex;
import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthConfig;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.oauth.OAuth20Service;
import java.util.stream.Collectors;

public class MailruOAuthServiceImpl extends OAuth20Service {

    public MailruOAuthServiceImpl(DefaultApi20 api, OAuthConfig config) {
        super(api, config);
    }

    @Override
    public void signRequest(OAuth2AccessToken accessToken, OAuthRequest request) {
        // sig = md5(params + secret_key)
        request.addQuerystringParameter("session_key", accessToken.getAccessToken());
        request.addQuerystringParameter("app_id", getConfig().getApiKey());
        final String completeUrl = request.getCompleteUrl();

        try {
            final String clientSecret = getConfig().getApiSecret();
            final int queryIndex = completeUrl.indexOf('?');
            if (queryIndex != -1) {
                final String urlPart = completeUrl.substring(queryIndex + 1);
                final Map<String, String> map = new TreeMap<>();
                for (String param : urlPart.split("&")) {
                    final String[] parts = param.split("=");
                    map.put(parts[0], (parts.length == 1) ? "" : parts[1]);
                }
                final String urlNew = map.entrySet().stream()
                        .map(entry -> entry.getKey() + '=' + entry.getValue())
                        .collect(Collectors.joining());
                final String sigSource = URLDecoder.decode(urlNew, CharEncoding.UTF_8) + clientSecret;
                request.addQuerystringParameter("sig", md5Hex(sigSource));
            }
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }
}

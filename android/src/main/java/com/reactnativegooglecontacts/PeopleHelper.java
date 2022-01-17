package com.reactnativegooglecontacts;

import android.content.Context;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.people.v1.PeopleService;
import java.io.IOException;


public class PeopleHelper {
    private static final String APPLICATION_NAME = "GoogleContacts Example";
    public static PeopleService setUp(Context context, String serverAuthCode,String clientId,String clientSecret) throws IOException {
        HttpTransport httpTransport = new NetHttpTransport();
        String redirectUrl = "urn:ietf:wg:oauth:2.0:oob";
        GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                httpTransport,
                GsonFactory.getDefaultInstance(),
                clientId,
                clientSecret,
                serverAuthCode,
                redirectUrl).execute();
        // Then, create a GoogleCredential object using the tokens from GoogleTokenResponse
        GoogleCredential credential = new GoogleCredential.Builder()
                .setClientSecrets(clientId, clientSecret)
                .setTransport(httpTransport)
                .setJsonFactory(GsonFactory.getDefaultInstance())
                .build();
        credential.setFromTokenResponse(tokenResponse);
        // credential can then be used to access Google services
        return new PeopleService.Builder(httpTransport, GsonFactory.getDefaultInstance(), credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
}

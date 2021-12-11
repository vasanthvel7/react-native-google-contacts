package com.reactnativegooglecontacts;

import android.content.Context;
import android.util.Log;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.people.v1.PeopleService;
import com.reactnativegooglecontacts.R;
import java.io.IOException;


public class PeopleHelper {
    public static final String TAG = "ServerAuthCodeActivity";
    private static final String APPLICATION_NAME = "GoogleContacts Example";

    public static PeopleService setUp(Context context, String serverAuthCode) throws IOException {

        HttpTransport httpTransport = new NetHttpTransport();
        String redirectUrl = "urn:ietf:wg:oauth:2.0:oob";
        GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                httpTransport,
                GsonFactory.getDefaultInstance(),
                context.getString(R.string.server_client_id),
                context.getString(R.string.server_client_secret),
                serverAuthCode,
                redirectUrl).execute();
        Log.d(TAG, "setUp: "+tokenResponse);
        // Then, create a GoogleCredential object using the tokens from GoogleTokenResponse
        GoogleCredential credential = new GoogleCredential.Builder()
                .setClientSecrets(context.getString(R.string.server_client_id), context.getString(R.string.server_client_secret))
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

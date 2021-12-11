package com.reactnativegooglecontacts;
import androidx.annotation.NonNull;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.module.annotations.ReactModule;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.UiThreadUtil;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.EmailAddress;
import com.google.api.services.people.v1.model.ListConnectionsResponse;
import com.google.api.services.people.v1.model.ListOtherContactsResponse;
import com.google.api.services.people.v1.model.Name;
import com.google.api.services.people.v1.model.Person;
import com.google.api.services.people.v1.model.PhoneNumber;
import com.google.gdata.util.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.google.gson.Gson;
import com.reactnativegooglecontacts.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
@ReactModule(name = GoogleContactsModule.NAME)
public class GoogleContactsModule extends ReactContextBaseJavaModule {
  public static final String NAME = "GoogleContacts";
  public static final String TAG = "ServerAuthCodeActivity";
  private static final int RC_GET_AUTH_CODE = 53294;
  private GoogleSignInClient mGoogleSignInClient;
  Context context;
  Activity currentActivity;
  Intent signInIntent;
  String ClientId;
  String AppId;
  String Appname;
  String Type;
  String nextPageToken=null;
  Promise EmailListReturn;
  ListOtherContactsResponse res;
  PeopleService peopleService;
  public GoogleContactsModule(ReactApplicationContext reactContext) {

    super(reactContext);
    reactContext.addActivityEventListener(new ActivityEventListener());
    context = reactContext;
    ClientId = reactContext.getString(R.string.server_client_id);
    AppId = reactContext.getString(R.string.firebase_app_id);

  }
  GoogleSignInAccount acct ;
  private class ActivityEventListener extends BaseActivityEventListener  {
    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent intent) {
      if (requestCode == RC_GET_AUTH_CODE) {
        Log.d(TAG, "onActivityResult: "+intent);
        Log.d(TAG, "onActivityResult: "+activity);
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(intent);
        GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(intent);
        try {

          GoogleSignInAccount account = task.getResult(ApiException.class);
          GoogleSignInAccount acc = result.getSignInAccount();
          if (account == null) {
            Log.d(TAG, "onActivityResult: no data");
          } else {
            Log.d(TAG, "onActivityResult: data FOUND");
            acct = GoogleSignIn.getLastSignedInAccount(activity);
            Log.d(TAG, "onActivityResult: "+acct.getServerAuthCode());
            Log.d(TAG, "onActivityResult: "+acc.getServerAuthCode());
            new PeoplesAsync().execute(acc.getServerAuthCode());
          }
        } catch (ApiException e) {
          EmailListReturn.reject("msg", String.valueOf(e.getStatus()));
          Log.d(TAG, "onActivityResult: "+e.getStatus());
        }
      }
    }
  };
  class PeoplesAsync extends AsyncTask<String, Void, List<String>> {


    @Override
    protected void onPreExecute() {
      super.onPreExecute();
    }
    @Override
    protected List<String> doInBackground(String... params) {
      List<String> nameList = new ArrayList<>();
      Log.d(TAG,"ACTIVITY"+params[0]);
      try {
        peopleService = PeopleHelper.setUp(context, params[0]);
        if(Type == "OtherContacts") {
          fetchOtherContacts(null);
        }
        else {
          ListConnectionsResponse response = peopleService.people().connections()
            .list("people/me")
            .setPersonFields(
              "addresses,ageRanges,birthdays,coverPhotos,emailAddresses,genders,metadata,names,nicknames,occupations,organizations,phoneNumbers,photos,urls")
            .execute();
          List<Person> connections = response.getConnections();
          for (Person person : connections) {
            if (!person.isEmpty()) {
              List<Name> names = person.getNames();
              List<EmailAddress> emailAddresses = person.getEmailAddresses();
              List<PhoneNumber> phoneNumbers = person.getPhoneNumbers();
              if (phoneNumbers != null)
                for (PhoneNumber phoneNumber : phoneNumbers)
                  Log.d(TAG, "doInBackground: "+phoneNumber);
              if (emailAddresses != null)
                for (EmailAddress emailAddress : emailAddresses)
                  Log.d(TAG, "doInBackground: "+emailAddress);
              if (names != null)
                for (Name name : names)
                  nameList.add(name.getDisplayName());
              Log.d(TAG, "doInBackground: "+names);

            } else {
              Log.d(TAG, "no person found");
            }
          }
        }

      } catch (IOException e) {
        EmailListReturn.reject("msg", String.valueOf(e));
        e.printStackTrace();
        Log.d(TAG, "doInBackground: "+e);
      }

      return nameList;
    }
  }
  public void fetchContacts(String token){
    try {
      JSONArray arr = new JSONArray();
      WritableMap contactList = Arguments.createMap();
      WritableArray array = new WritableNativeArray();
      res = peopleService.otherContacts().list()
        .setReadMask("emailAddresses,names")
        .setRequestSyncToken(true)
        .setPageToken(token)
        .execute();
      contactList.putString("nextPageToken",res.getNextPageToken());
      nextPageToken=res.getNextPageToken();
      Log.d(TAG, "fetchOtherContacts: nextpagetoken"+res.getNextPageToken());
      Log.d(TAG, "fetchOtherContacts: reacttoken"+token);
      Log.d(TAG, "doInBackground: TOTALCONTACT"+res.getOtherContacts().size());
      List<Person> otherContacts = res.getOtherContacts();
      Log.d(TAG, "fetchOtherContacts: LIST PERUSU"+res.getOtherContacts());
      Log.d(TAG, "fetchOtherContacts: LIST otherContacts"+otherContacts);
      for (Person person : otherContacts) {
        List<EmailAddress> emailAddresses = person.getEmailAddresses();
        List<Name> names = person.getNames();
        if (!person.isEmpty()) {
          if (emailAddresses != null)
            for (EmailAddress emailAddress : emailAddresses) {
              WritableMap map = Arguments.createMap();
              if (names != null) {
                for (Name name : names) {
                  map.putString("name",name.getDisplayName());
                  map.putString("email",emailAddress.getValue());
                  array.pushMap(map);
                }
              }
              else {
                map.putString("name","null");
                map.putString("email",emailAddress.getValue());
                array.pushMap(map);
              }

            }
        } else {
          Log.d(TAG, "doInBackground:empty");
        }

      }
      contactList.putArray("data",array);
      Log.d(TAG, "fetchOtherContacts: contactList.putArray"+contactList);
      EmailListReturn.resolve(contactList);


    } catch (Exception e) {
      EmailListReturn.reject("msg", String.valueOf(e));
      mGoogleSignInClient.signOut();
      e.printStackTrace();
      Log.d(TAG, "doInBackground: " + e);
    }

  }
  public void fetchOtherContacts(String token){
    try {
      JSONArray arr = new JSONArray();
      WritableMap contactList = Arguments.createMap();
      WritableArray array = new WritableNativeArray();
      res = peopleService.otherContacts().list()
        .setReadMask("emailAddresses,names")
        .setRequestSyncToken(true)
        .setPageToken(token)
        .execute();
      contactList.putString("nextPageToken",res.getNextPageToken());
      nextPageToken=res.getNextPageToken();
      Log.d(TAG, "fetchOtherContacts: nextpagetoken"+res.getNextPageToken());
      Log.d(TAG, "fetchOtherContacts: reacttoken"+token);
      Log.d(TAG, "doInBackground: TOTALCONTACT"+res.getOtherContacts().size());
      List<Person> otherContacts = res.getOtherContacts();
      Log.d(TAG, "fetchOtherContacts: LIST PERUSU"+res.getOtherContacts());
      Log.d(TAG, "fetchOtherContacts: LIST otherContacts"+otherContacts);
      for (Person person : otherContacts) {
        List<EmailAddress> emailAddresses = person.getEmailAddresses();
        List<Name> names = person.getNames();
        if (!person.isEmpty()) {
          if (emailAddresses != null)
            for (EmailAddress emailAddress : emailAddresses) {
              WritableMap map = Arguments.createMap();
              if (names != null) {
                for (Name name : names) {
                  map.putString("name",name.getDisplayName());
                  map.putString("email",emailAddress.getValue());
                  array.pushMap(map);
                }
              }
              else {
                map.putString("name","null");
                map.putString("email",emailAddress.getValue());
                array.pushMap(map);
              }

            }
        } else {
          Log.d(TAG, "doInBackground:empty");
        }

      }
      contactList.putArray("data",array);
      Log.d(TAG, "fetchOtherContacts: contactList.putArray"+contactList);
      EmailListReturn.resolve(contactList);


    } catch (Exception e) {
      EmailListReturn.reject("msg", String.valueOf(e));
      mGoogleSignInClient.signOut();
      e.printStackTrace();
      Log.d(TAG, "doInBackground: " + e);
    }

  }
  @ReactMethod
  public void getContact(String nextToken) throws IOException, ServiceException {
    validateServerClientID();
    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
      .requestServerAuthCode(ClientId)
      .requestScopes(new Scope("https://www.googleapis.com/auth/contacts"))
      .requestProfile()
      .requestEmail()
      .build();
    Log.d(TAG, "getContacts: "+gso);
    mGoogleSignInClient = GoogleSignIn.getClient(context,gso);
    getAuthCode();
    Type = "Contacts";
  }
  @ReactMethod
  public void getOtherContact(String nextToken, Promise promise) throws IOException, ServiceException {
    Log.d(TAG, "getOtherContact: "+nextToken);
    if(nextToken==null) {
      if(mGoogleSignInClient!=null) {
        Log.d(TAG, "getOtherContact: "+mGoogleSignInClient);
        mGoogleSignInClient.signOut();
      }
      EmailListReturn = promise;
      validateServerClientID();
      GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestServerAuthCode(ClientId)
        .requestScopes(new Scope("https://www.googleapis.com/auth/contacts"), new Scope("https://www.googleapis.com/auth/contacts.other.readonly"))
        .requestProfile()
        .requestEmail()
        .build();
      Log.d(TAG, "getContacts: " + gso);
      mGoogleSignInClient = GoogleSignIn.getClient(context, gso);
      Log.d(TAG, "getContacts: Client" + mGoogleSignInClient);
      getAuthCode();
      Type = "OtherContacts";
    }
    else
    {
      EmailListReturn = promise;
      Log.d(TAG, "getOtherContactsssssssss: ");
      fetchOtherContacts(nextToken);
      Type = "OtherContacts";
    }

  }
  private void getAuthCode() {
    signInIntent = mGoogleSignInClient.getSignInIntent();
    Log.d(TAG, "getAuthCode: "+mGoogleSignInClient);
    signInIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

    currentActivity = getCurrentActivity();
    Log.d(TAG, "getAuthCode: "+signInIntent);
    UiThreadUtil.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        currentActivity.startActivityForResult(signInIntent, RC_GET_AUTH_CODE);
      }
    });

  }
  private void validateServerClientID() {
    String suffix = ".apps.googleusercontent.com";
    if (!ClientId.trim().endsWith(suffix)) {
      String message = "Invalid server client ID in strings.xml, must end with " + suffix;
      Log.w("log", message);
      Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
    else {
      Log.d(TAG, "validateServerClientID: login success");
    }
  }
  @Override
  @NonNull
  public String getName() {
    return NAME;
  }



  @ReactMethod
  public void multiply(int a, int b, Promise promise) {
    promise.resolve(a * b);
  }

  public static native int nativeMultiply(int a, int b);
}

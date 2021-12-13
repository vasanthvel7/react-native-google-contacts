package com.reactnativegooglecontacts;
import androidx.annotation.NonNull;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.module.annotations.ReactModule;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.UiThreadUtil;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
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
import java.util.List;

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
  String Type;
  String contactnextPageToken=null;
  String nextPageToken=null;
  Promise EmailListReturn;
  ListOtherContactsResponse res;
  PeopleService peopleService;
  GoogleSignInAccount acct ;

  public GoogleContactsModule(ReactApplicationContext reactContext) {
    super(reactContext);
    reactContext.addActivityEventListener(new ActivityEventListener());
    context = reactContext;
    ClientId = reactContext.getString(R.string.server_client_id);
    AppId = reactContext.getString(R.string.firebase_app_id);
  }

  private class ActivityEventListener extends BaseActivityEventListener  {
    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent intent) {
      if (requestCode == RC_GET_AUTH_CODE) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(intent);
        GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(intent);
        try {

          GoogleSignInAccount account = task.getResult(ApiException.class);
          GoogleSignInAccount acc = result.getSignInAccount();
          if (account == null) {
            EmailListReturn.reject("msg", "Account is Null");
          } else {
            acct = GoogleSignIn.getLastSignedInAccount(activity);
            String serverAuthCode = acc.getServerAuthCode();
            new PeoplesAsync().execute(serverAuthCode);
          }
        } catch (ApiException e) {
          EmailListReturn.reject("msg", String.valueOf(e.getStatus()));
        }
      }
    }
  };
  class PeoplesAsync extends AsyncTask<String, Void, List<String>>  {
    @Override
    protected void onPreExecute() {
      super.onPreExecute();
    }
    @Override
    protected List<String> doInBackground(String... params) {
      List<String> nameList = new ArrayList<>();
      try {
        peopleService = PeopleHelper.setUp(context, params[0]);
        if(Type == "OtherContacts") {
          fetchOtherContacts(null);
        }
        else {
          fetchContacts(null);
        }
      } catch (IOException e) {
        EmailListReturn.reject("msg", String.valueOf(e));
        e.printStackTrace();
      }
      return nameList;
    }
  }
  public void fetchContacts(String token){
    try
    {
      WritableMap contactsList = Arguments.createMap();
      WritableArray contactsarray = new WritableNativeArray();
      ListConnectionsResponse response = peopleService.people().connections()
        .list("people/me")
        .setPersonFields(
          "addresses,ageRanges,birthdays,coverPhotos,emailAddresses,genders,metadata,names,nicknames,occupations,organizations,phoneNumbers,photos,urls")
        .setPageSize(45)
        .execute();

      contactnextPageToken=response.getNextPageToken();
      if(contactnextPageToken!=null){
        contactsList.putString("nextPageToken",response.getNextPageToken());
      }
      else {
        contactsList.putString("nextPageToken","Reached end");
      }
      List<Person> Contacts = response.getConnections();

      for (Person person : Contacts) {

        List<PhoneNumber> phoneNumbers = person.getPhoneNumbers();
        List<Name> names = person.getNames();
        if (!person.isEmpty()) {
          if (phoneNumbers != null)
            for (PhoneNumber phonenumbers : phoneNumbers) {
              WritableMap contactmap = Arguments.createMap();
              if (names != null) {

                Log.d(TAG, "Name: "+names.get(0).getDisplayName());
                Log.d(TAG, "Number: "+phonenumbers.getValue());
                contactmap.putString("name",names.get(0).getDisplayName());
                contactmap.putString("email",phonenumbers.getValue());
                Log.d(TAG, "doInBackground: "+contactmap);
                contactsarray.pushMap(contactmap);

              }
              else {
                contactmap.putString("name","unknown");
                contactmap.putString("email",phonenumbers.getValue());
                contactsarray.pushString("hello");
              }

            }
        } else {
          Log.d(TAG, "doInBackground:empty");
        }

      }
      contactsList.putArray("data",contactsarray);
      Log.d(TAG, "fetchOtherContacts: contactList.putArray"+contactsList);

      EmailListReturn.resolve(contactsList);


    } catch (Exception e) {
      EmailListReturn.reject("msg", String.valueOf(e));
      e.printStackTrace();
      Log.d(TAG, "doInBackground: " + e);
    }
  }

  public void fetchOtherContacts(String token){
    try {
      WritableMap contactList = Arguments.createMap();
      WritableArray array = new WritableNativeArray();
      res = peopleService.otherContacts().list()
        .setReadMask("emailAddresses,names")
        .setRequestSyncToken(true)
        .setPageToken(token)
        .execute();

      nextPageToken=res.getNextPageToken();
      if(nextPageToken!=null) {
        contactList.putString("nextPageToken", nextPageToken);
      }
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
      return;

    } catch (Exception e) {
      EmailListReturn.reject("msg", String.valueOf(e));
      e.printStackTrace();
      Log.d(TAG, "doInBackground: " + e);
    }

  }

  @ReactMethod
  public void getContact(String nextToken,Promise promise) throws IOException, ServiceException {
    if(nextToken==null) {
      if(mGoogleSignInClient!=null) {
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
      mGoogleSignInClient = GoogleSignIn.getClient(context, gso);
      getAuthCode();
      Type = "Contacts";
    }
    else
    {
      EmailListReturn = promise;
      fetchContacts(nextToken);
      Type = "Contacts";
    }
  }

  @ReactMethod
  public void getOtherContact(String nextToken, Promise promise) throws IOException, ServiceException {
    if(nextToken==null) {
      if(mGoogleSignInClient!=null) {
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
      mGoogleSignInClient = GoogleSignIn.getClient(context, gso);
      getAuthCode();
      Type = "OtherContacts";
    }
    else
    {
      EmailListReturn = promise;
      fetchOtherContacts(nextToken);
      Type = "OtherContacts";
    }

  }
  private void getAuthCode() {
    signInIntent = mGoogleSignInClient.getSignInIntent();
    signInIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    currentActivity = getCurrentActivity();
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
      Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

  }

  @Override
  @NonNull
  public String getName() {
    return NAME;
  }

}

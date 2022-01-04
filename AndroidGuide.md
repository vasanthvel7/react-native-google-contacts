# Android Guide

### KeyStore File generation
   
   Generate a Keystore file inside  `android/app` by using this this keytool generate command
   
   ```bash
    keytool -genkey -v -keystore [keystore file name].keystore -alias [keystore-alias] -keyalg RSA -keysize 2048 -validity 10000
   ```
  To List KeyStore File
  
   ```bash
     keytool -list -v -keystore [keystore file name] -alias [keystore-alias]
   ```
    
 ### Firebase Configuration
 
    After Getting SHA1 Key from KeyStore file 
    add the SHA1 Key into the Firebase Console By Folloowing This Steps:

        1. Sign in to Firebase and open your project.
        2. Click the Settings icon and select Project settings.
        3. In the Your apps card, select the package name of the app you need a to add SHA1 to.
        4. Click "Add fingerprint".
      
   Then, go to https://console.firebase.google.com/, select your app, and add the SHA1 value under Project Settings 
   (gear icon in the upper left) -> Your Apps - SHA certificate fingerprints.
     
   You can get your `webClientId` from [Google Developer Console](https://console.developers.google.com/apis/credentials).
   
   Steps To Generate OAUTH2 webClientId
   
        1.First Step is Create Credentials By using OAUTH Client ID Configuration
        2.Select Application Type As Web Application
        3.And Then Add Your Application name an Redirect URI
        4.WebClient Id is Generated for Your Application
        
    
 ### Gradle Configuration
 
   If you're running your app in debug mode and not using `webClientId` or you're sure it's correct the problem might be signature (SHA-1 or SHA-256) mismatch. You need to add the following to `android/app/build.gradle`:

```diff
signingConfigs {
+    debug {
+        storeFile file([keystore file name])
+        storePassword [Keystore Password]
+        keyAlias [keystore-alias]
+        keyPassword [alias Password]
+    }
    release {
        ...
    }
 }
```

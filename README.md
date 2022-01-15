# react-native-google-contacts

To fetch Google contacts and Google Othercontacts using people Api 


## Installation
```sh
npm install react-native-google-contacts
```
## Project Setup

 After Installation Follow the Guide for initial setup

 > [Android Setup](AndroidGuide.md)
 
 > [IOS Setup](IosGuide.md)
 
 
## Initial Setup(Authentication)

### 1.Android Client ID setup

```js
import { SendAndroidClientToken } from "react-native-google-contacts";

// ...

SendAndroidClientToken(ClientId, appId, ClientSecret)
.then((response)=>{
  //statement
})
.catch((error)=>{
 //Error Response
})
```


### 2.IOS Client ID setup

```js
import { SendIOSClientToken } from "react-native-google-contacts";

// ...

SendIOSClientToken(ClientId)
.then((response)=>{
  //statement
})
.catch((error)=>{
 //Error Response
})
```

## Usage

This method is to be used to find out whether some user Contacts. It returns a promise which resolves Currently Signed in user Contacts and nextpageToken. If Client Authentication is Invalid it returns a promise which rejects Authentication Error.

 
```js
import { getContacts } from "react-native-google-contacts";

// ...

getContacts(Tokenvalue)      //In firstpage Tokenvalue must be null && In Secondpage Send nextpageToken as Tokenvalue     
.then((response)=>{
  //statement
})
.catch((error)=>{
 //Error Response
})
```


This method is to be used to find out whether some user OtherContacts. It returns a promise which resolves Currently Signed in user OtherContacts and nextpageToken. If Client Authentication is Invalid it returns a promise which rejects Authentication Error.

 
```js
import { getOtherContacts } from "react-native-google-contacts";

// ...

getOtherContacts(Tokenvalue)      //In firstpage Tokenvalue must be null && In Secondpage Send nextpageToken as Tokenvalue     
.then((response)=>{
  //statement
})
.catch((error)=>{
 //Error Response
})
```

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT

import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-google-contacts' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo managed workflow\n';

const GoogleContacts = NativeModules.GoogleContacts
  ? NativeModules.GoogleContacts
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );
export function SendIOSClientToken(ClientId) {
  return GoogleContacts.SendClientToken(ClientId);
}
export function getContacts(token) {
  return GoogleContacts.getContact(token);
}
export function getOtherContacts(token) {
  return GoogleContacts.getOtherContact(token);
}

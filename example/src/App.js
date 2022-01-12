import * as React from 'react';

import { StyleSheet, View, Text, TouchableOpacity } from 'react-native';
import {
  getContacts,
  getOtherContacts,
  SendIOSClientToken,
} from 'react-native-google-contacts';

export default function App() {
  const [result, setResult] = React.useState();
  const [contactstoken, setcontactstoken] = React.useState(null);
  const [Othercontactstoken, setOthercontactstoken] = React.useState(null);
  var ClientId;

  return (
    <View style={styles.container}>
      <View style={styles.container}>
        <TouchableOpacity
          onPress={() => {
            SendIOSClientToken(ClientId)
              .then((res) => {
                console.log(res, 'res');
              })
              .catch((e) => {
                console.log(e, 'err');
              });
          }}
        >
          <Text>Send ClientId</Text>
        </TouchableOpacity>
        <TouchableOpacity
          onPress={() => {
            getContacts(contactstoken)
              .then((res) => {
                console.log(res, 'res');
                // setcontactstoken(res.nextPageToken);
              })
              .catch((e) => {
                console.log(e, 'err');
              });
          }}
        >
          <Text>Get Contacts</Text>
        </TouchableOpacity>
        <TouchableOpacity
          onPress={() => {
            getOtherContacts(Othercontactstoken)
              .then((res) => {
                console.log(res, 'res');
                setOthercontactstoken(res.nextPageToken);
              })
              .catch((e) => {
                console.log(e, 'err');
              });
          }}
        >
          <Text>Get OtherContacts</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});

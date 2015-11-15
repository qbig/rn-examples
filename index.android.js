/**
* Sample React Native App
* https://github.com/facebook/react-native
*/
'use strict';

var React = require('react-native');
var {
  AppRegistry,
  StyleSheet,
  Text,
  View,
  NativeModules,
  DeviceEventEmitter,
  ToastAndroid
} = React;
var Subscribable = require('Subscribable');
var NSDModule = NativeModules.NSDModule
var RNNsd = React.createClass({
  mixins: [Subscribable.Mixin],

  respondToDiscoveredEvent: function(e) {
    //'D-Link DIR-868L Configuration Utility'
    //'D-Link SharePort Web Access'
    ToastAndroid.show("found: " + e['data'], ToastAndroid.SHORT);
    if (e['data'] === 'D-Link DIR-868L Configuration Utility') {
        NSDModule.resolve(e['data']);
    }
    // if (e['data'] == NSDModule.SPHERE_SERIVE_NAME) {
    //   NSDModule.resolve(NSDModule.SPHERE_SERIVE_NAME);
    //   ToastAndroid.show("BOX FOUND !!!! ", ToastAndroid.LONG);
    // }
  },

  respondToResolvedEvent: function(e) {
    console.log("resolved:" + e['data']);
    ToastAndroid.show("resolved:" + e['data'], ToastAndroid.SHORT)
  },

  componentWillMount: function() {
    this.addListenerOn(DeviceEventEmitter,
      NSDModule.SERVICE_RESOLVED,
      this.respondToResolvedEvent);

    this.addListenerOn(DeviceEventEmitter,
      NSDModule.SERVICE_FOUND,
      this.respondToDiscoveredEvent);
  },

  componentDidMount: function() {
    console.log("Started !!")
    console.log(NSDModule.SERVICE_RESOLVED)
    console.log(NSDModule.SERVICE_FOUND)
    NSDModule.discover();
  },

  render: function() {
    return (
      <View style={styles.container}>
        <Text style={styles.welcome}>
          Welcome to React Native!
        </Text>
        <Text style={styles.instructions}>
          To get started, edit index.android.js
        </Text>
        <Text style={styles.instructions}>
          Shake or press menu button for dev menu
        </Text>
      </View>
    );
  }
});

var styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF',
  },
  welcome: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
  },
  instructions: {
    textAlign: 'center',
    color: '#333333',
    marginBottom: 5,
  },
});

AppRegistry.registerComponent('RNNsd', () => RNNsd);

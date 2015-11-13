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
} = React;
var Subscribable = require('Subscribable');
var RNNsd = React.createClass({
  mixins: [Subscribable.Mixin],

  respondToToastEvent: function(e) {
    console.log("Event triggered !!!");
    console.log(e)
  },

  componentWillMount: function() {
    this.addListenerOn(DeviceEventEmitter,
                       'toastDidShow',
                       this.respondToToastEvent);

  },

  componentDidMount: function() {
    var MyToastAndroid = NativeModules.MyToastAndroid
    MyToastAndroid.show('Awesome', MyToastAndroid.LONG, ()=>{}, ()=>{});
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

import './src/bootstrap'
import Home from '@registry/home';
import Test from '@registry/test';
import { ComponentName } from '@registry/index';
import { AppRegistry } from 'react-native';
import { gestureHandlerRootHOC } from 'react-native-gesture-handler';

const ComponentMap = {
  [ComponentName.Home]: Home,
  [ComponentName.Test]: Test,
};

Object.keys(ComponentMap).forEach(name => {
  AppRegistry.registerComponent(name, () =>
    gestureHandlerRootHOC(ComponentMap[name]),
  );
});

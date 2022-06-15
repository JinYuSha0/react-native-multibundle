import React from 'react';
import Home from '@registry/home';
import Test from '@registry/test';
import { SmartAssets } from 'react-native-multibundle';
import { ComponentName } from '@registry/index';
import { AppRegistry, LogBox, Text } from 'react-native';
import { gestureHandlerRootHOC } from 'react-native-gesture-handler';

const ComponentMap = {
  [ComponentName.Home]: Home,
  [ComponentName.Test]: Test,
};

Object.keys(ComponentMap).forEach(name => {
  AppRegistry.registerComponent(name, () => gestureHandlerRootHOC(ComponentMap[name]));
});

// 忽略警告
LogBox.ignoreAllLogs();

// 字体默认样式
(function settingFont(Text) {
  let _render = Text.render;
  Text.render = function (...args) {
    const originText = _render.apply(this, args);
    const { style, numberOfLines, children } = originText.props;
    return React.cloneElement(originText, {
      allowFontScaling: false, // 防止字体随系统的大小而改变
      style: [
        {
          fontSize: 14,
          color: '#000000',
        },
        style,
      ],
    });
  };
})(Text);

SmartAssets.init();

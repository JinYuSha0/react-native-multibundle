import React from 'react';
import { SmartAssets } from 'react-native-multibundle';
import { LogBox, Text } from 'react-native';

// 忽略警告
LogBox.ignoreAllLogs();

// 字体默认样式
(function settingFont(Text) {
  // @ts-ignore
  let _render = Text.render;
  // @ts-ignore
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

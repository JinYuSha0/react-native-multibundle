import React, { memo } from 'react';
import { App, ScreenProps } from '@src/app.context';
import TestNavigator from '@src/navigators/test';

const Test: React.FC<ScreenProps> = props => {
  return (
    <App {...props}>
      <TestNavigator />
    </App>
  );
};

export default memo(Test);

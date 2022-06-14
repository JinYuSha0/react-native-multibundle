import React, { memo } from 'react';
import { App, ScreenProps } from '@src/app.context';
import HomeNavigator from '@src/navigators/home';

const Home: React.FC<ScreenProps> = props => {
  return (
    <App {...props}>
      <HomeNavigator />
    </App>
  );
};

export default memo(Home);

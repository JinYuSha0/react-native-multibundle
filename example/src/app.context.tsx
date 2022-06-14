import React, { useMemo, useContext, useCallback, useLayoutEffect } from 'react';
import { NavigationContainer, DarkTheme } from '@react-navigation/native';
import { StackNavigationOptions } from '@react-navigation/stack';
import { CommonScreenProps } from '@navigators/index';
import { isNil } from '@src/utils/utils';
import { StatusBarMode, goBack } from 'react-native-multibundle';
import { TouchableOpacity } from 'react-native-gesture-handler';
import { StatusBarHeight } from '@utils/constant';
import BackSVG from '@assets/images/back.svg';
import useEvent from '@hooks/useEvent';

export interface ScreenProps {
  routeName: string;
  bundleName: string;
  moduleName: string;
  statusBarMode: number;
  goBack?: boolean;
}

interface AppContextProps {
  screenProps: ScreenProps
}

const AppContext = React.createContext<AppContextProps>({
  screenProps: {
    routeName: '',
    bundleName: '',
    moduleName: '',
    statusBarMode: -1,
    goBack: false
  }
});

const AppProvider: React.FC<{ screenProps: ScreenProps }> = props => {
  const { children, screenProps } = props;
  const value = useMemo(() => ({ screenProps }), [screenProps]);
  return <AppContext.Provider value={value}>{children}</AppContext.Provider>;
};

export const useAppContext = () => {
  return useContext(AppContext);
};

function getScreenOptions(routeParams: Omit<ScreenProps, 'routeName'>) {
  const options = { ...CommonScreenProps };
  if (
    !isNil(routeParams.statusBarMode) &&
    (routeParams.statusBarMode & StatusBarMode.TRANSPARENT) > 0
  ) {
    options.headerShown = false;
  }
  if (
    !isNil(routeParams.statusBarMode) &&
    (routeParams.statusBarMode & StatusBarMode.TRANSPARENT) === 0 &&
    ((routeParams.statusBarMode & StatusBarMode.DARK) > 0 ||
      (routeParams.statusBarMode & StatusBarMode.LIGHT) > 0)
  ) {
    options.headerStatusBarHeight = StatusBarHeight;
  }
  if (routeParams.goBack) {
    options.headerLeft = props => {
      return (
        <TouchableOpacity
          activeOpacity={0.7}
          onPress={props.canGoBack ? props.onPress : goBack}>
          <BackSVG width={22} fill={'#FFF'} />
        </TouchableOpacity>
      );
    };
  }
  return options;
}

export const App: React.FC<ScreenProps> = props => {
  const { children, ...rest } = props;
  const { routeName = '', ...routeParams } = rest;
  const screenOptions = useMemo(() => getScreenOptions(routeParams), []);
  return <AppProvider screenProps={rest}>
    <NavigationContainer theme={DarkTheme}>
      {
        React.cloneElement(children as React.ReactElement, {
          routeName,
          routeParams,
          screenOptions
        })
      }
    </NavigationContainer>
  </AppProvider>;
};

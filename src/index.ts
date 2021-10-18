import { NativeModules, Platform, NativeEventEmitter } from 'react-native';
import type { TNativeConstants } from './types/nativeConstants';
import type { Component, CheckUpdateResult } from './types/muldiBundle';
import { StatusBarMode } from './types/statusBarMode';
import { EventName } from './types/eventName';

const IsIOS = Platform.OS === 'ios';

const LINKING_ERROR =
  `The package 'react-native-multibundle' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo managed workflow\n';

export const MultiBundle = NativeModules.MultiBundle
  ? NativeModules.MultiBundle
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export const NativeConstants: TNativeConstants =
  MultiBundle?.getConstants() || {};

let EventEmitter: NativeEventEmitter;

/**
 * 打开新模块
 * @param moduleName
 * @param statusBarMode
 */
export function openComponent(
  moduleName: string,
  statusBarMode: StatusBarMode = StatusBarMode.LIGHT
) {
  if (IsIOS) {
    MultiBundle?.openComponent(moduleName);
  } else {
    MultiBundle?.openComponent(moduleName, statusBarMode);
  }
}

/**
 * 获取本机所有模块
 * @returns
 */
export function getAllComponent(): Promise<Component[]> {
  return MultiBundle?.getAllComponent();
}

/**
 * ios注册事件
 * @param eventName
 * @returns
 */
export function registerEvent(eventName: string): Promise<boolean> {
  if (!IsIOS) return Promise.resolve(true);
  return MultiBundle?.registerEvent(eventName);
}

/**
 * 返回
 */
export function goBack(): void {
  return MultiBundle?.goBack();
}

/**
 * 手动检查业务包更新
 */
export function checkUpdate(): Promise<CheckUpdateResult> {
  return MultiBundle?.checkUpdate();
}

/**
 * 订阅事件
 * @param eventName
 * @param listener
 * @returns func remove
 */
export function onEvent(
  eventName: EventName,
  listener: (...args: any) => void
): () => void {
  if (!EventEmitter) {
    EventEmitter = new NativeEventEmitter(NativeModules.MultiBundle);
  }
  registerEvent(NativeConstants.prefix + eventName);
  const observer = EventEmitter.addListener(
    NativeConstants.prefix + eventName,
    listener
  );
  return observer.remove;
}

export type { Component, CheckUpdateResult } from './types/muldiBundle';
export { StatusBarMode } from './types/statusBarMode';
export { EventName } from './types/eventName';

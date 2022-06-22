// @ts-ignore
import { setCustomSourceTransformer } from "react-native/Libraries/Image/resolveAssetSource";
import { NativeModules, Platform, NativeEventEmitter } from "react-native";
import { StatusBarMode } from "./types/statusBarMode";
import { EventName } from "./types/eventName";
import type { TNativeConstants } from "./types/nativeConstants";
import type { Component, CheckUpdateResult } from "./types/muldiBundle";

const IsIOS = Platform.OS === "ios";

const LINKING_ERROR =
  `The package 'react-native-multibundle' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: "" }) +
  "- You rebuilt the app after installing the package\n" +
  "- You are not using Expo managed workflow\n";

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

/**
 * 打印日志
 * @param msg
 */
export function log(msg: string) {
  MultiBundle?.log(msg);
}

class SmartAssetsImpl {
  private jsbundleUrl: string | null = null;

  init() {
    setCustomSourceTransformer((resolver: any) => {
      if (resolver.isLoadedFromServer()) {
        return resolver.assetServerURL();
      }

      if (Platform.OS === "android") {
        resolver.jsbundleUrl = this.jsbundleUrl;
        if (resolver.isLoadedFromFileSystem()) {
          let resolvedAssetSource = resolver.drawableFolderInBundle();
          return resolvedAssetSource;
        } else {
          return resolver.resourceIdentifierWithoutScale();
        }
      } else {
        return resolver.scaledAssetURLNearBundle();
      }
    });

    onEvent(EventName.JS_BUNDLE_CHANGE, (jsbundleUrl: string) => {
      this.setJsbundleUrl(jsbundleUrl);
    });
  }

  setJsbundleUrl(newJsbundleUrl: string) {
    this.jsbundleUrl = newJsbundleUrl;
  }
}
export const SmartAssets = new SmartAssetsImpl();

export type { Component, CheckUpdateResult } from "./types/muldiBundle";
export { StatusBarMode } from "./types/statusBarMode";
export { EventName } from "./types/eventName";
export {
  CustomTextInput,
  CustomTextInputProps,
} from "./components/CustomInput";

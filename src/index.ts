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
export function openComponent(moduleName: string): void;
export function openComponent(moduleName: string, finish: boolean): void;
export function openComponent(
  moduleName: string,
  statusBarMode: StatusBarMode
): void;
export function openComponent(
  moduleName: string,
  finish: boolean,
  statusBarMode: StatusBarMode
): void;
export function openComponent(
  moduleName: string,
  finish: boolean,
  statusBarMode: StatusBarMode,
  params: any
): void;
export function openComponent(
  moduleName: string,
  arg2?: boolean | StatusBarMode,
  arg3?: StatusBarMode,
  arg4?: any
) {
  if (IsIOS) {
    MultiBundle?.openComponent(moduleName, arg2 ?? false);
  } else {
    if (arg2 == null) {
      MultiBundle?.openComponent(moduleName, false, StatusBarMode.LIGHT, null);
      return;
    }
    if (arg3 == null) {
      if (typeof arg2 === "boolean") {
        MultiBundle?.openComponent(moduleName, arg2, StatusBarMode.LIGHT, null);
      } else if (typeof arg2 === "number") {
        MultiBundle?.openComponent(moduleName, false, arg2, null);
      }
      return;
    }
    if (arg4 == null) {
      MultiBundle?.openComponent(moduleName, arg2, arg3, null);
      return;
    }
    MultiBundle?.openComponent(moduleName, arg2, arg3, arg4);
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
 * 获取外部存储文件夹路径
 * @returns
 */
export function getExternalFilesDir(): Promise<string> {
  return MultiBundle?.getExternalFilesDir();
}

/**
 * 打印日志
 * @param msg
 */
export function log(msg: string) {
  MultiBundle?.log(msg);
}

class SmartAssetsImpl {
  private static externalFilesDir: string | null = null;

  init() {
    getExternalFilesDir().then(
      (externalFilesDir) =>
        (SmartAssetsImpl.externalFilesDir = externalFilesDir)
    );
    setCustomSourceTransformer((resolver: any) => {
      if (resolver.isLoadedFromServer()) {
        return resolver.assetServerURL();
      }

      if (Platform.OS === "android") {
        if (!!resolver.asset.package) {
          resolver.jsbundleUrl = `file://${SmartAssetsImpl.externalFilesDir}${resolver.asset.package}`;
        }
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
  }
}
export const SmartAssets = new SmartAssetsImpl();

export type { Component, CheckUpdateResult } from "./types/muldiBundle";
export { ComponentType } from "./types/muldiBundle";
export { StatusBarMode } from "./types/statusBarMode";
export { EventName } from "./types/eventName";
export {
  CustomTextInput,
  CustomTextInputProps,
} from "./components/CustomInput";

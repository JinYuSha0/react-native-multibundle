//
//  MultiBundle.swift
//  MultiBundle
//
//  Created by Soul on 2021/10/15.
//

import React
import Alamofire
import SSZipArchive

@objc(MultiBundle)
public class MultiBundle: RCTEventEmitter {
  
  private static var eventEmitter: RCTEventEmitter?
  private static let PREFIX = Math.randomString(6)
  private static var registeredSupportEvents = [String]()
  
  public static var IS_DEV: Bool = false
  public static var CHECK_UPDATE_HOST = ""
  
  override init() {
    super.init()
    MultiBundle.eventEmitter = self
    initDB()
  }
  
  @objc public static func initial (rctBridge: RCTBridge, isDev: Bool, checkUpdateServer: String) {
    RNBundleLoader.setBridge(rctBridge)
    IS_DEV = isDev
    CHECK_UPDATE_HOST = checkUpdateServer
  }
  
  @objc public static func checkUpdate() {
    func onSuccess(_ data: CheckUpdateModel) -> Void {
    }
    func onError(_ errorMsg: String) -> Void {
    }
    MultiBundle.checkUpdate(onSuccess: onSuccess, onError: onError)
  }
  
  func initDB() -> Void {
    let isInit: Bool = (Preferences.getValueByKey(key: StorageKey.INIT_DB) ?? false) as! Bool
    if (!isInit) {
      guard let path = Bundle.main.path(forResource: "bundle/appSetting", ofType: "json") else { return }
      let localData = NSData.init(contentsOfFile: path)! as Data
      do {
        let setting = try JSONDecoder().decode(SettingModel.self, from: localData)
        for (key, value) in setting.components {
          let componentModel: ComponentModel = ComponentModel.init(
            ComponentName: value.componentName,
            BundleName: key,
            Version: 0,
            Hash: value.hash,
            FilePath: "assets://bundle/\(key)",
            PublishTime: setting.timestamp,
            InstallTime: Int64(Date().timeIntervalSince1970 * 1000)
          )
          RNDBHelper.manager.insertRow(row: componentModel)
        }
        Preferences.storageKV(key: StorageKey.INIT_DB, value: true)
      } catch {
        print("InitDB failure")
      }
    }
  }
  
  override public func constantsToExport() -> [AnyHashable : Any]! {
    return ["prefix": MultiBundle.PREFIX + "_"]
  }
  
  override public class func requiresMainQueueSetup() -> Bool {
    return false
  }
  
  override public func supportedEvents() -> [String]! {
    return MultiBundle.registeredSupportEvents
  }
  
  @objc(getAllComponent:rejecter:)
  public func getAllComponent(_ resolve: RCTPromiseResolveBlock, rejecter reject:RCTPromiseRejectBlock) -> Void {
    let components = RNDBHelper.manager.selectAll()
    resolve(RNConvert.convert(components))
  }
  
  @objc(registerEvent:resolver:rejecter:)
  public func registerEvent(_ eventName: String, resolver resolve: RCTPromiseResolveBlock, rejecter reject:RCTPromiseRejectBlock) -> Void {
    if (!MultiBundle.registeredSupportEvents.contains(eventName)) {
      MultiBundle.registeredSupportEvents.append(eventName)
    }
    resolve(true)
  }
  
  @objc(openComponent:)
  public func openComponent(_ moduleName: String) -> Void {
    let params: Dictionary<String, Any> = ["goBack": true]
    DispatchQueue.main.async {
      let controller: UIViewController = RNController(moduleName: moduleName, params: params)
      UIApplication.topNavigationController()?.pushViewController(controller, animated: true)
    }
  }
  
  @objc(goBack)
  public func goBack() -> Void {
    DispatchQueue.main.async {
      let popController =  UIApplication.topNavigationController()?.popViewController(animated: true)
      RNController.removeController(controller: popController as? RNController)
    }
  }
  
  @objc(checkUpdate:rejecter:)
  public func checkUpdate(_ resolve: @escaping RCTPromiseResolveBlock, rejecter reject: @escaping RCTPromiseRejectBlock) -> Void {
    func onSuccess(_ data: CheckUpdateModel) -> Void {
      resolve(RNConvert.convert(data))
    }
    func onError(_ errorMsg: String) -> Void {
      reject(nil, errorMsg, nil)
    }
    MultiBundle.checkUpdate(onSuccess: onSuccess, onError: onError)
  }
  
  static func checkUpdate(
    onSuccess: @escaping ((_ data: CheckUpdateModel) -> Void),
    onError: @escaping ((_ errorMsg: String) -> Void)
  ) {
    let componentsMap = RNDBHelper.manager.selectAllMap()
    let common = componentsMap["common"]
    MultiBundle.sendEventInner(eventName: EventName.CHECK_UPDATE_START, eventData: nil)
    AF.request("\(CHECK_UPDATE_HOST)/rn/checkUpdate", method: .get, parameters: ["platform": "ios", "commonHash": (common?.Hash ?? "") ]).responseData { response in
      switch response.result {
        case .success(let value):
          do {
            let result: CheckUpdateModel = try JSONDecoder().decode(CheckUpdateModel.self, from: value )
            MultiBundle.sendEventInner(eventName: EventName.CHECK_UPDATE_SUCCESS, eventData: result)
            if result.success {
              onSuccess(result)
              for component in result.data {
                let oldComponent = componentsMap[component.componentName]
                if oldComponent == nil || (component.version > oldComponent?.Version ?? 0 && component.hash != oldComponent?.Hash) {
                  MultiBundle.sendEventInner(eventName: EventName.CHECK_UPDATE_DOWNLOAD_NEWS, eventData: component)
                  let dest: DownloadRequest.Destination = { _, _ in
                    let fileURL = File.documentURL.appendingPathComponent("\(component.componentName)-\(component.hash).zip")
                    return (fileURL, [.removePreviousFile, .createIntermediateDirectories])
                  }
                  AF.download(component.downloadUrl, interceptor: nil, to: dest).downloadProgress(closure: { (progress) in
                    MultiBundle.sendEventInner(eventName: EventName.CHECK_UPDATE_DOWNLOAD_PROGRESS, eventData: ["componentName": component.componentName, "progress": progress.fractionCompleted])
                  }).responseData { (res) in
                    switch res.result {
                      case .success:
                        let unzipPath = File.createDirectory(dirname: "buzBundle")
                        func completionHandler(path: String, success: Bool, error: Error?) {
                          if success {
                            setupComponent(componentDir: "\(unzipPath!)/\(component.hash)", version: component.version)
                            MultiBundle.sendEventInner(eventName: EventName.CHECK_UPDATE_DOWNLOAD_NEWS_SUCCESS, eventData: component)
                          } else {
                            print(error!)
                          }
                        }
                        if unzipPath != nil {
                          SSZipArchive.unzipFile(atPath: res.fileURL!.path, toDestination: unzipPath!, progressHandler: nil, completionHandler: completionHandler)
                        }
                        break
                      case .failure:
                        MultiBundle.sendEventInner(eventName: EventName.CHECK_UPDATE_DOWNLOAD_NEWS_FAILURE, eventData: res.error?.localizedDescription)
                        break
                    }
                  }
                }
              }
            } else {
              onError(result.message!)
              MultiBundle.sendEventInner(eventName: EventName.CHECK_UPDATE_FAILURE, eventData: result.message!)
            }
          } catch {
            onError(error.localizedDescription)
            MultiBundle.sendEventInner(eventName: EventName.CHECK_UPDATE_FAILURE, eventData: error.localizedDescription)
          }
          break
        case .failure(let error):
          let errorMsg = error.errorDescription ?? "Request unknow error"
          onError(errorMsg)
          MultiBundle.sendEventInner(eventName: EventName.CHECK_UPDATE_FAILURE, eventData: errorMsg)
          break
      }
    }
  }
  
  private static func setupComponent(componentDir: String, version: Int) {
    do {
      let settingData = try Data(contentsOf: URL(fileURLWithPath: "\(componentDir)/setting.json"))
      let setting: BuzSettingModel = try JSONDecoder().decode(BuzSettingModel.self, from: settingData)
      var bundleFilePath = "\(componentDir)/\(setting.bundleName)"
      if File.isExists(path: bundleFilePath).isExists {
        let buzBundlePath = File.createDirectory(dirname: "buzBundle")!
        let range = buzBundlePath.startIndex...buzBundlePath.endIndex
        bundleFilePath.replaceSubrange(range, with: "file://")
        let componentModel: ComponentModel = ComponentModel.init(
          ComponentName: setting.componentName,
          BundleName: setting.bundleName,
          Version: version,
          Hash: setting.hash,
          FilePath: bundleFilePath,
          PublishTime: setting.timestamp,
          InstallTime: Int64(Date().timeIntervalSince1970 * 1000)
        )
        RNDBHelper.manager.insertRow(row: componentModel)
        MultiBundle.sendEventInner(eventName: EventName.CHECK_UPDATE_DOWNLOAD_NEWS_APPLY, eventData: setting.componentName)
        if !RNController.isExistsModule(moduleName: setting.componentName) {
          RNBundleLoader.load(bundleFilePath)
        }
      }
    } catch {
      print(error)
    }
  }
  
  public static func sendEvent(eventName: String, eventData: Any?) -> Void {
    if !MultiBundle.registeredSupportEvents.contains(eventName) {
      MultiBundle.registeredSupportEvents.append(eventName)
    }
    MultiBundle.eventEmitter?.sendEvent(withName: eventName, body: RNConvert.convert(eventData))
  }
  
  public static func sendEventInner(eventName: String, eventData: Any?) -> Void {
    sendEvent(eventName: "\(PREFIX)_\(eventName)", eventData: eventData)
  }
  
}

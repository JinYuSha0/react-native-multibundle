export enum ComponentType {
  Common = 0,
  Bootstrap = 1,
  Default = 2,
}

export interface Component {
  BundleName: string;
  ComponentName: string;
  ComponentType: ComponentType;
  Version: number;
  Hash: string;
  FilePath: string;
  PublishTime: number;
  InstallTime: number;
}

export interface CheckUpdateResult {
  data: {
    version: number;
    hash: string;
    commonHash: string;
    isCommon: boolean;
    componentName: string;
    downloadUrl: string;
    buildTime: number;
  }[];
  code: number;
  success: boolean;
}

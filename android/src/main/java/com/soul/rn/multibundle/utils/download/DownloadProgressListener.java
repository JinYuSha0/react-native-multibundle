package com.soul.rn.multibundle.utils.download;

import java.io.File;

public interface DownloadProgressListener {
  public void onDownloadStart(int fileSize);
  
  public void onDownloadSize(int downloadedSize, int fileSize);

  public void onDownloadFailure(Exception e);

  public void onDownLoadComplete(File file);
}

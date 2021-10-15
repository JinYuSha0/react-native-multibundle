require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "MultiBundle"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.author       = package['author']
  s.license      = package['license']
  s.homepage     = package['homepage']
  s.source       = { :git => "git@github.com:JinYuSha0/react-native-multibundle.git", :tag => "#{s.version}" }
  s.platforms    = { :ios => "10.0" }
  s.preserve_paths = '*.js'
  s.library        = 'z'
  s.source_files = "ios/MultiBundle/**/*.{h,c,cc,cpp,m,mm,swift}"

  s.dependency "React"
  s.dependency 'Alamofire', '~> 5.4'
  s.dependency 'SSZipArchive'
  s.dependency 'SQLite.swift', '~> 0.13.0'
end


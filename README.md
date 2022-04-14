# snakat-ads-android

## Installation
1. Add this to your project as a git submodule
```sh
cd ~/sample_app/
git submodule add https://github.com/khanhbui/snakat-ads-android.git snakat-ads
```
2. Create a file, named *config.gradle*, which defines sdk versions, target versions and dependencies.
```groovy
ext {
    plugins = [
            library: 'com.android.library'
    ]

    android = [
            compileSdkVersion: 31,
            buildToolsVersion: "31.0.0",
            minSdkVersion    : 14,
            targetSdkVersion : 31
    ]

    dependencies = [
            appcompat: 'androidx.appcompat:appcompat:1.4.1',
            ads: 'com.android.billingclient:billing:4.1.0'
    ]
}
```
3. Add this line on top of *build.gradle*
```groovy
apply from: "config.gradle"
```
4. Add this line to *settings.gradle*
```groovy
include ':snakat-ads'
```
5. Add this line to dependencies section of *app/build.gradle*
```groovy
implementation project(path: ':snakat-ads')
```
6. Add your AdMob app ID (identified in the AdMob UI) to your app's *AndroidManifest.xml* file
```xml
<manifest>
    <application>
      <meta-data
        android:name="com.google.android.gms.ads.APPLICATION_ID"
        android:value="ca-app-pub-xxxxxxxxxxxxxxxx~yyyyyyyyyy" />
    </application>
</manifest>
```

## Usage

### Initialization
```java
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Context context = getApplicationContext();
        AdsManager.createInstance(context);
    }

    @Override
    public void onTerminate() {
        AdsManager.destroyInstance();

        super.onTerminate();
    }
}
```

### Show a banner
```java
ViewGroup container = ...;
String adUnitId = "ca-app-pub-xxxxxxxxxxxxxxxx/zzzzzzzzzz";

AdsManager.getInstance()
  .showBanner(container, adUnitId)
  .subscribe(new Consumer<AdsManager.EventType>() {
    @Override
    public void accept(AdsManager.EventType type) throws Exception {
      switch (type) {
        case LOADED:
          // Ad finishes loading.
          break;
        case CLICKED:
          // The user clicks on an ad.
          break;
        case DISMISSED:
          // The user is about to return to the app after tapping on an ad.
          break;
      }
    }
  }, new Consumer<Throwable>() {
    @Override
    public void accept(Throwable throwable) throws Exception {
      // Handle the error occurs when loading ad.
    }
  })
```

### Show an interstitial
```java
Activity activity = ...;
String adUnitId = "ca-app-pub-xxxxxxxxxxxxxxxx/zzzzzzzzzz";

AdsManager.getInstance()
  .showInterstitial(activity, adUnitId)
  .subscribe(new Consumer<AdsManager.EventType>() {
    @Override
    public void accept(AdsManager.EventType type) throws Exception {
      switch (type) {
        case LOADED:
          // Ad finishes loading.
          break;
        case CLICKED:
          // The user clicks on an ad.
          break;
        case DISMISSED:
          // Fullscreen content is dismissed.
          break;
      }
    }
  }, new Consumer<Throwable>() {
    @Override
    public void accept(Throwable throwable) throws Exception {
      // Handle the error occurs when loading or showing ad.
    }
  });
```

## License
```
MIT License

Copyright (c) 2022 Khanh Bui

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

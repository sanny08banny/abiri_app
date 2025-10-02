
# NimbusPush

NimbusPush is a lightweight WebSocket-based push service for Android that lets your app receive real-time messages — with a plug-and-play listener model.

## 🚀 Getting Started with NimbusPush

### ✅ Step 1: Add the Dependency

Using [JitPack](https://jitpack.io):

<details>
<summary>Project-level <code>build.gradle</code></summary>

```groovy
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```
</details>

<details>
<summary>App-level <code>build.gradle</code></summary>

```groovy
dependencies {
    implementation 'com.github.sanny08banny:NimbusPush:1.0.0'
}
```
</details>

### ✅ Step 2: Implement a Custom Message Listener

Create a class that implements `PushMessageListener`:

```kotlin
class MyPushHandler : PushMessageListener {
    override fun onMessageReceived(context: Context, message: String) {
        // Custom logic (e.g., show a notification or update UI)
        Log.d("MyPushHandler", "Message received: $message")
    }
}
```

### ✅ Step 3: Declare the Listener in `AndroidManifest.xml`

Inside your `<application>` tag:

```xml
<application
    android:name=".MyApp"
    ... >

    <meta-data
        android:name="com.example.nimbuspush.LISTENER"
        android:value="com.example.yourapp.MyPushHandler" />
</application>
```

### ✅ Step 4: Start the Push Service

You can start the service from anywhere (e.g., `Application.onCreate`):

```kotlin
NimbusPushService.start(context, "YOUR_DEVICE_ID")
```

> 💡 The listener will be automatically registered using manifest metadata.

## 📦 Features

- Foreground service using WebSocket
- Auto-reconnection with exponential backoff
- Watchdog to recover from dropped connections
- Pluggable message listener via manifest
- Minimal dependencies

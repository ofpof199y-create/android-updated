-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

-keep class com.youtubescript.app.** { *; }

-dontwarn okhttp3.**
-dontwarn okio.**

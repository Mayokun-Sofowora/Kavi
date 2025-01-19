# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep line numbers for better crash reports
-keepattributes SourceFile,LineNumberTable

# Firebase
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes Exception
-keepattributes InnerClasses

# Firebase Auth
-keepattributes Signature
-keepclassmembers class com.mayor.kavi.data.models.** {
  *;
}

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Hilt
-keepclasseswithmembers class * {
    @dagger.* <methods>;
}

# Keep data models
-keep class com.mayor.kavi.data.models.** { *; }

# Keep Dokka classes
-keep class org.jetbrains.dokka.** { *; }
-keep class com.fasterxml.jackson.** { *; }
-keep class freemarker.** { *; }
-dontwarn org.jetbrains.dokka.**
-dontwarn com.fasterxml.jackson.**
-dontwarn freemarker.**
-dontwarn java.beans.**
-dontwarn javax.swing.**
-dontwarn org.jaxen.**
-dontwarn org.python.core.**
-dontwarn org.zeroturnaround.javarebel.**

# Keep documentation-related classes
-keepclasseswithmembers class * {
    @org.jetbrains.dokka.* *;
}

# Keep metadata
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keepattributes Signature
-keepattributes Exceptions

# Keep Retrofit models and annotations
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# Keep Gson models
-keep class com.google.gson.** { *; }
-keepattributes *Annotation*

# Keep your API key in BuildConfig
-keepclassmembers class **.BuildConfig {
    public static final java.lang.String ROBOFLOW_API_KEY;
}

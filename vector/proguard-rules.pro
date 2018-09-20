-dontwarn org.joda.**
-dontwarn org.slf4j.**
-dontwarn java.lang.management.**
-dontwarn org.apache.**
-dontwarn com.fasterxml.**
-dontwarn libcore.reflect.**
-dontwarn com.moceanmobile.**
-dontwarn javax.management.**
-dontwarn android.support.**
-dontwarn com.android.support.**
-dontwarn com.android.volley.**
-dontwarn jp.wasabeef.**
-dontwarn com.facebook.**
-dontwarn okhttp3.internal.**
-dontwarn retrofit2.**
-dontwarn okio.**

-keep class com.newrelic.** { *; }
-dontwarn com.newrelic.**
-keepattributes Exceptions, Signature, InnerClasses, LineNumberTable

-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable
-keep class !android.support.v7.internal.view.menu.**, android.support.v7.** { *; }

##---------------Begin: proguard configuration for Gson ----------
# Gson uses generic type information stored in a class file when working with
#fields. Proguard removes such information by default, so configure it to keep
#all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-keep class sun.misc.Unsafe { *; }
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { *; }
##---------------End: proguard configuration for Gson ----------


-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.AppGlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

-keep class com.google.android**{ *; }
-keep class com.google.**{ *; }
-keep class libcore.reflect.** { *; }
-keep class com.fasterxml.** { *; }
-keep class io.branch.**{ *; }
-keep class org.apache.**{ *; }
-keep class org.alexd.**{ *; }
-keep class com.hippoapp.**{ *; }

-keep class com.android.**{ *; }
-keep class com.google.android.gms.** { *; }

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

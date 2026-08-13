# Add project specific ProGuard rules here.
# By default, the flags in this file are applied to the debug build type as well.

# Moshi - keep all classes with @JsonClass annotation
-keep class com.simats.naturepulse.data.** { *; }
-keep class com.simats.naturepulse.BuildConfig { *; }

# Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Moshi
-keepclasseswithmembers class * {
    @com.squareup.moshi.* <methods>;
}

# OSMDroid
-keep class org.osmdroid.** { *; }

# Coil
-dontwarn coil.**
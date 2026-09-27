# Project Proguard & R8 Optimization Rules for GrowthEngine Production Release

# Preserve Line Numbers and Source info for Firebase / Crashlytics
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses,EnclosingMethod

# AndroidX Room
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public void <init>();
}
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Data Models & Entities (Do not obfuscate Room Entities & DTOs)
-keep class com.example.data.model.** { *; }
-keepclassmembers class com.example.data.model.** { *; }

# Moshi JSON Serialization
-keepclasseswithmembers class * {
    @com.squareup.moshi.* <methods>;
}
-keepclasseswithmembers class * {
    @com.squareup.moshi.* <fields>;
}
-keep @com.squareup.moshi.JsonClass class * { *; }
-dontwarn com.squareup.moshi.**

# Retrofit & OkHttp
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes RuntimeVisibleAnnotations,RuntimeInvisibleAnnotations
-dontwarn okhttp3.**
-dontwarn okio.**

# Razorpay Checkout SDK
-keep class com.razorpay.** {*;}
-dontwarn com.razorpay.**
-keepclasseswithmembers class * {
    public void onPaymentSuccess(...);
    public void onPaymentError(...);
}

# Android Keystore & Security Crypto
-keep class androidx.security.crypto.** { *; }
-dontwarn androidx.security.crypto.**

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**

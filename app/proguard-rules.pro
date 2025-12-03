# Add project specific ProGuard rules here.

# Keep Room entities
-keep class com.dsatracker.data.local.entity.** { *; }

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }

# Hilt
-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }

# Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Preserve line numbers for crash stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ── Room ──────────────────────────────────────────────────────────────────────
# Keep all @Entity, @Dao, and @Database annotated classes intact so Room's
# generated code can find them at runtime after shrinking.
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keep @androidx.room.Database class * { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase {
    abstract *;
}

# ── Hilt / Dagger ─────────────────────────────────────────────────────────────
# Hilt generates components and modules; the names are stable but R8 may strip
# members it doesn't see as reachable from non-DI entry points.
-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-keepclasseswithmembers class * {
    @javax.inject.Inject <init>(...);
}
-keepclassmembers class * {
    @javax.inject.Inject *;
}

# ── kotlinx.serialization ─────────────────────────────────────────────────────
# Keep @Serializable data classes and their generated companion serializers so
# the JSON circuit format survives minification.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.** {
    *;
}
# Keep generated $serializer companion objects
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1>$Companion {
    kotlinx.serialization.KSerializer serializer(...);
}

# ── Kotlin ────────────────────────────────────────────────────────────────────
# Kotlin metadata and coroutines
-keepattributes Signature, Exceptions, RuntimeVisibleAnnotations
-keep class kotlin.Metadata { *; }
-dontwarn kotlinx.coroutines.**

# ── Jetpack Compose ───────────────────────────────────────────────────────────
# Compose keeps its own rules via consumer rules in the AAR; nothing extra needed.

# ── FileProvider (export share sheet) ─────────────────────────────────────────
-keep class androidx.core.content.FileProvider { *; }
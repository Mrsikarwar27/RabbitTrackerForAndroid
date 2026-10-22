# Keep Room generated classes
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Keep Compose generated classes
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

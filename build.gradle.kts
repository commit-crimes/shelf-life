// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    alias(libs.plugins.ktfmt) apply false
    alias(libs.plugins.gms) apply false
    id("com.google.dagger.hilt.android") version "2.49" apply false

}
package com.example.clinchplayer.update

data class UpdateInfo(
    val versionCode: Long,
    val versionName: String,
    val apkUrl: String,
    val notes: String,
    val required: Boolean
)
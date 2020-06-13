package com.vladtruta.startphone.model.local

data class SystemSetup(
    var hasPermissions: Boolean = false,
    var hasDrawOverlay: Boolean = false,
    var hasDefaultLauncher: Boolean = false
)
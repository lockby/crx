package com.crstlnz.komikchino.data.util

fun versionCheck(apiVersion: String, appVersion: String): Boolean {
    val apiVersionParts = apiVersion.replace("v", "").split("-")[0].split(".")
    val appVersionParts = appVersion.split(" - ")[0].split(".")

    for (i in 0 until minOf(apiVersionParts.size, appVersionParts.size)) {
        val apiPart = apiVersionParts[i].toIntOrNull()
        val appPart = appVersionParts[i].toIntOrNull()

        if (apiPart != null && appPart != null) {
            if (apiPart > appPart) {
                return true
            } else if (apiPart < appPart) {
                return false
            }
        } else if (apiPart != null) {
            return true
        } else if (appPart != null) {
            return false
        }
    }

    return apiVersionParts.size > appVersionParts.size
}
package app.gomuks.android

import android.content.Intent
import android.net.Uri
import android.util.Log
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.WebRequestError

fun categoryToString(category: Int): String {
    return when (category) {
        WebRequestError.ERROR_CATEGORY_UNKNOWN -> "ERROR_CATEGORY_UNKNOWN"
        WebRequestError.ERROR_CATEGORY_SECURITY -> "ERROR_CATEGORY_SECURITY"
        WebRequestError.ERROR_CATEGORY_NETWORK -> "ERROR_CATEGORY_NETWORK"
        WebRequestError.ERROR_CATEGORY_CONTENT -> "ERROR_CATEGORY_CONTENT"
        WebRequestError.ERROR_CATEGORY_URI -> "ERROR_CATEGORY_URI"
        WebRequestError.ERROR_CATEGORY_PROXY -> "ERROR_CATEGORY_PROXY"
        WebRequestError.ERROR_CATEGORY_SAFEBROWSING -> "ERROR_CATEGORY_SAFEBROWSING"
        else -> "UNKNOWN"
    }
}

fun errorToString(error: Int): String {
    return when (error) {
        WebRequestError.ERROR_UNKNOWN -> "ERROR_UNKNOWN"
        WebRequestError.ERROR_SECURITY_SSL -> "ERROR_SECURITY_SSL"
        WebRequestError.ERROR_SECURITY_BAD_CERT -> "ERROR_SECURITY_BAD_CERT"
        WebRequestError.ERROR_NET_RESET -> "ERROR_NET_RESET"
        WebRequestError.ERROR_NET_INTERRUPT -> "ERROR_NET_INTERRUPT"
        WebRequestError.ERROR_NET_TIMEOUT -> "ERROR_NET_TIMEOUT"
        WebRequestError.ERROR_CONNECTION_REFUSED -> "ERROR_CONNECTION_REFUSED"
        WebRequestError.ERROR_UNKNOWN_PROTOCOL -> "ERROR_UNKNOWN_PROTOCOL"
        WebRequestError.ERROR_UNKNOWN_HOST -> "ERROR_UNKNOWN_HOST"
        WebRequestError.ERROR_UNKNOWN_SOCKET_TYPE -> "ERROR_UNKNOWN_SOCKET_TYPE"
        WebRequestError.ERROR_UNKNOWN_PROXY_HOST -> "ERROR_UNKNOWN_PROXY_HOST"
        WebRequestError.ERROR_MALFORMED_URI -> "ERROR_MALFORMED_URI"
        WebRequestError.ERROR_REDIRECT_LOOP -> "ERROR_REDIRECT_LOOP"
        WebRequestError.ERROR_SAFEBROWSING_PHISHING_URI -> "ERROR_SAFEBROWSING_PHISHING_URI"
        WebRequestError.ERROR_SAFEBROWSING_MALWARE_URI -> "ERROR_SAFEBROWSING_MALWARE_URI"
        WebRequestError.ERROR_SAFEBROWSING_UNWANTED_URI -> "ERROR_SAFEBROWSING_UNWANTED_URI"
        WebRequestError.ERROR_SAFEBROWSING_HARMFUL_URI -> "ERROR_SAFEBROWSING_HARMFUL_URI"
        WebRequestError.ERROR_CONTENT_CRASHED -> "ERROR_CONTENT_CRASHED"
        WebRequestError.ERROR_OFFLINE -> "ERROR_OFFLINE"
        WebRequestError.ERROR_PORT_BLOCKED -> "ERROR_PORT_BLOCKED"
        WebRequestError.ERROR_PROXY_CONNECTION_REFUSED -> "ERROR_PROXY_CONNECTION_REFUSED"
        WebRequestError.ERROR_FILE_NOT_FOUND -> "ERROR_FILE_NOT_FOUND"
        WebRequestError.ERROR_FILE_ACCESS_DENIED -> "ERROR_FILE_ACCESS_DENIED"
        WebRequestError.ERROR_INVALID_CONTENT_ENCODING -> "ERROR_INVALID_CONTENT_ENCODING"
        WebRequestError.ERROR_UNSAFE_CONTENT_TYPE -> "ERROR_UNSAFE_CONTENT_TYPE"
        WebRequestError.ERROR_CORRUPTED_CONTENT -> "ERROR_CORRUPTED_CONTENT"
        WebRequestError.ERROR_HTTPS_ONLY -> "ERROR_HTTPS_ONLY"
        WebRequestError.ERROR_BAD_HSTS_CERT -> "ERROR_BAD_HSTS_CERT"
        else -> "UNKNOWN"
    }
}

class NavigationDelegate(private val activity: MainActivity) : GeckoSession.NavigationDelegate {
    var canGoBack = false

    override fun onCanGoBack(session: GeckoSession, canGoBack: Boolean) {
        this.canGoBack = canGoBack
    }

    override fun onLoadError(
        session: GeckoSession,
        uri: String?,
        error: WebRequestError
    ): GeckoResult<String>? {
        Log.e("NavigationDelegate", "onLoadError: $uri $error (${errorToString(error.category)}: ${errorToString(error.code)}")
        val serverURL = activity.getServerURL()
        if (uri == null || (serverURL != null && uri.trimEnd('/') == serverURL.trimEnd('/'))) {
            activity.openServerInputWithError(serverURL ?: "", "Failed to load server: ${errorToString(error.code)}")
        }
        return null
    }

    override fun onNewSession(session: GeckoSession, uri: String): GeckoResult<GeckoSession>? {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        if (intent.resolveActivity(activity.packageManager) != null) {
            activity.startActivity(intent)
        }
        return null
    }
}
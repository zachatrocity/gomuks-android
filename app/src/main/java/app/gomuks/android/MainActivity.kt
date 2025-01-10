package app.gomuks.android

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView

class MainActivity : ComponentActivity() {
    private lateinit var view: GeckoView
    private lateinit var navigation: NavigationDelegate
    private lateinit var session: GeckoSession
    private lateinit var runtime: GeckoRuntime

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (this::runtime.isInitialized) {
            parseIntentURL(intent)?.let {
                session.loadUri(it)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        view = GeckoView(this)
        navigation = NavigationDelegate(this)
        session = GeckoSession()
        runtime = GeckoRuntime.create(this)
        session.open(runtime)
        view.setSession(session)

        runtime.settings.enterpriseRootsEnabled = true
        runtime.settings.consoleOutputEnabled = true
        runtime.settings.doubleTapZoomingEnabled = false

        session.promptDelegate = BasicGeckoViewPrompt(this)
        session.navigationDelegate = navigation

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (navigation.canGoBack) {
                    session.goBack()
                    return
                }
                finish()
            }
        })
        if (!loadWeb()) {
            setContent {
                ServerInput()
            }
        }
        Log.i("GomuksMainActivity", "Initialization complete")
    }

    fun getServerURL(): String? {
        val sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        return sharedPref.getString(getString(R.string.server_url_key), null)
    }

    fun openServerInputWithError(serverURL: String, error: String) {
        setContent {
            ServerInput(serverURL, error)
        }
    }

    private fun parseIntentURL(overrideIntent: Intent? = null): String? {
        var serverURL = getServerURL() ?: return null
        val intent = overrideIntent ?: this.intent
        var targetURI = intent.data
        if (intent.action == Intent.ACTION_VIEW && targetURI != null) {
            if (targetURI.host == "matrix.to") {
                targetURI = matrixToURLToMatrixURI(targetURI)
                if (targetURI == null) {
                    Log.w("GomuksMainActivity", "Failed to parse matrix.to URL ${intent.data}")
                } else {
                    Log.d("GomuksMainActivity", "Parsed matrix.to URL ${intent.data} -> $targetURI")
                }
            }
            if (targetURI?.scheme == "matrix") {
                serverURL = Uri.parse(serverURL)
                    .buildUpon()
                    .encodedFragment("/uri/${Uri.encode(targetURI.toString())}")
                    .build()
                    .toString()
                Log.d("GomuksMainActivity", "Converted view intent $targetURI -> $serverURL")
                return serverURL
            }
        }
        if (overrideIntent != null) {
            Log.w("GomuksMainActivity", "No intent URL found ${overrideIntent.action} ${overrideIntent.data}")
            return null
        }
        return serverURL
    }

    private fun loadWeb(): Boolean {
        session.loadUri(parseIntentURL() ?: return false)
        setContentView(view)
        return true
    }

    @Composable
    fun ServerInput(initialURL: String = "", error: String? = null) {
        var serverURL by remember { mutableStateOf(initialURL ?: "") }

        Column(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextField(
                value = serverURL,
                onValueChange = { serverURL = it },
                label = { Text(getString(R.string.server_url)) }
            )
            Button(onClick = {
                val sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
                with (sharedPref.edit()) {
                    putString(getString(R.string.server_url_key), serverURL)
                    apply()
                }
                loadWeb()
            }) {
                Text(getString(R.string.connect))
            }
            if (error != null) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

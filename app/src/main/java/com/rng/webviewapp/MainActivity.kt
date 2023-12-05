package com.rng.webviewapp

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.roozbehzarei.webview.databinding.ActivityMainBinding

/**
 * [WEBSITE] the URL of the website to be loaded by [webView]
 */
private const val WEBSITE = "https://instagram.com/raj__rr"

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        /**
         * Define and configure [webView]
         */
        webView = binding.webView
        webView.webViewClient = MyWebViewClient()
        webView.webChromeClient = MyWebChromeClient()
        with(webView.settings) {
            // Tell the WebView to enable JavaScript execution
            javaScriptEnabled = true
            // Enable DOM storage API
            domStorageEnabled = false
            // Disable support for zooming using webView's on-screen zoom controls and gestures
            setSupportZoom(false)
        }
        // If dark theme is turned on, automatically render all web contents using a dark theme
        if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
            when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    WebSettingsCompat.setAlgorithmicDarkeningAllowed(webView.settings, true)
                }

                Configuration.UI_MODE_NIGHT_NO, Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                    WebSettingsCompat.setAlgorithmicDarkeningAllowed(webView.settings, false)
                }
            }
        }

        webView.loadUrl(WEBSITE)

        /**
         * Define Swipe-to-refresh behaviour
         */
        binding.root.setOnRefreshListener {
            if (webView.url == null) {
                webView.loadUrl(WEBSITE)
            } else {
                webView.reload()
            }
        }

        /**
         * Theme Swipe-to-refresh layout
         */
        val spinnerTypedValue = TypedValue()
        theme.resolveAttribute(
            com.google.android.material.R.attr.colorPrimary, spinnerTypedValue, true
        )
        val spinnerColor = spinnerTypedValue.resourceId
        binding.root.setColorSchemeResources(spinnerColor)

        val backgroundTypedValue = TypedValue()
        theme.resolveAttribute(
            com.google.android.material.R.attr.colorPrimaryContainer, backgroundTypedValue, true
        )
        val backgroundColor = backgroundTypedValue.resourceId
        binding.root.setProgressBackgroundColorSchemeResource(backgroundColor)

        /**
         * Disable Swipe-to-refresh if [webView] is scrolling
         */
        webView.viewTreeObserver.addOnScrollChangedListener {
            binding.root.isEnabled = webView.scrollY == 0
        }

        /**
         * If there's no web page history, close the application
         */
        val mCallback = onBackPressedDispatcher.addCallback(this) {
            if (webView.canGoBack()) {
                webView.goBack()
            } else {
                finish()
            }
        }
        mCallback.isEnabled = true

        setContentView(binding.root)
    }

    private inner class MyWebViewClient : WebViewClient() {

        /**
         * Let [webView] load the [WEBSITE]
         * Otherwise, launch another Activity that handles URLs
         */
        override fun shouldOverrideUrlLoading(
            view: WebView?, request: WebResourceRequest?
        ): Boolean {
            if (request?.url.toString().contains(WEBSITE)) {
                return false
            }
            Intent(Intent.ACTION_VIEW, request?.url).apply {
                startActivity(this)
            }
            return true
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            binding.webView.visibility = View.VISIBLE
            binding.errorLayout.visibility = View.GONE
            binding.progressIndicator.visibility = View.VISIBLE
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            binding.root.isRefreshing = false
            binding.progressIndicator.visibility = View.INVISIBLE
        }

        override fun onReceivedError(
            view: WebView?, request: WebResourceRequest?, error: WebResourceError?
        ) {
            super.onReceivedError(view, request, error)
            binding.webView.visibility = View.GONE
            binding.errorLayout.visibility = View.VISIBLE
            binding.root.isEnabled = false
            binding.retryButton.setOnClickListener {
                if (view?.url.isNullOrEmpty()) {
                    view?.loadUrl(WEBSITE)
                } else {
                    view?.reload()
                }
            }
        }

    }

    /**
     * Update the progress bar when loading a webpage
     */
    private inner class MyWebChromeClient : WebChromeClient() {
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            binding.progressIndicator.progress = newProgress
        }
    }

}
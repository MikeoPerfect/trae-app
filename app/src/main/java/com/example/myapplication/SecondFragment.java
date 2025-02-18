package com.example.myapplication;

import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;
import android.widget.EditText;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.myapplication.databinding.FragmentSecondBinding;
import com.google.android.material.snackbar.Snackbar;

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;
    private static final String DEFAULT_URL = "https://www.baidu.com";

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentSecondBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 设置WebView
        setupWebView();

        // 设置导航按钮
        setupNavigationButtons();

        // 设置URL输入框
        setupUrlEditText();

        // 加载默认页面
        binding.webview.loadUrl(DEFAULT_URL);
    }

    private void setupWebView() {
        WebSettings webSettings = binding.webview.getSettings();
        BrowserSettings settings = BrowserSettings.getInstance(requireContext());
        
        // 应用JavaScript设置
        webSettings.setJavaScriptEnabled(settings.isJavaScriptEnabled());
        
        // 基本设置
        webSettings.setDomStorageEnabled(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        
        // 应用自定义UserAgent
        String customUA = settings.getCustomUserAgent();
        if (!customUA.isEmpty()) {
            webSettings.setUserAgentString(customUA);
        } else {
            String defaultUA = webSettings.getUserAgentString();
            webSettings.setUserAgentString(defaultUA + " Android");
        }
        
        // 应用缓存设置
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setAppCacheEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setAllowFileAccess(true);
        
        // 允许混合内容（HTTP和HTTPS）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        // 应用夜间模式
        if (settings.isNightModeEnabled()) {
            // 注入夜间模式CSS
            binding.webview.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    String nightModeCss = "body {background-color: #2b2b2b !important; color: #e0e0e0 !important;}"
                            + "a {color: #8ab4f8 !important;}"
                            + "input, textarea {background-color: #404040 !important; color: #e0e0e0 !important;}"
                            + "img {filter: brightness(0.8);}"; // 降低图片亮度
                    view.evaluateJavascript(
                            "(function() {" +
                                    "var style = document.createElement('style');" +
                                    "style.type = 'text/css';" +
                                    "style.innerHTML = '" + nightModeCss + "';" +
                                    "document.head.appendChild(style);" +
                                    "})()",
                            null
                    );
                    binding.urlEditText.setText(url);
                    updateNavigationButtons();
                }

                @Override
                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                    super.onReceivedError(view, errorCode, description, failingUrl);
                    // 显示错误信息和重试按钮，但不跳转到空白页面
                    Snackbar.make(view, "加载失败: " + description, Snackbar.LENGTH_INDEFINITE)
                            .setAction("重试", v -> {
                                // 更新URL输入框显示
                                binding.urlEditText.setText(failingUrl);
                                // 重新加载失败的URL
                                view.loadUrl(failingUrl);
                            })
                            .show();
                }

                @Override
                public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                    new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                            .setTitle("安全警告")
                            .setMessage("该网站的安全证书存在问题，是否继续访问？\n" + 
                                      "错误信息: " + getSslErrorMessage(error))
                            .setPositiveButton("继续", (dialog, which) -> handler.proceed())
                            .setNegativeButton("取消", (dialog, which) -> handler.cancel())
                            .show();
                }

                private String getSslErrorMessage(SslError error) {
                    switch (error.getPrimaryError()) {
                        case SslError.SSL_EXPIRED:
                            return "证书已过期";
                        case SslError.SSL_IDMISMATCH:
                            return "证书主机名不匹配";
                        case SslError.SSL_DATE_INVALID:
                            return "证书日期无效";
                        case SslError.SSL_INVALID:
                            return "证书无效";
                        default:
                            return "未知SSL错误";
                    }
                }
            });
        }
    }

    private void setupNavigationButtons() {
        binding.buttonBack.setOnClickListener(v -> {
            if (binding.webview.canGoBack()) {
                binding.webview.goBack();
            }
        });

        binding.buttonForward.setOnClickListener(v -> {
            if (binding.webview.canGoForward()) {
                binding.webview.goForward();
            }
        });

        binding.buttonRefresh.setOnClickListener(v -> binding.webview.reload());

        binding.buttonGo.setOnClickListener(v -> loadUrl());

        binding.buttonSettings.setOnClickListener(v -> showSettingsDialog());
    }

    private void setupUrlEditText() {
        binding.urlEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                loadUrl();
                return true;
            }
            return false;
        });
    }

    private void loadUrl() {
        String url = binding.urlEditText.getText().toString().trim();
        // 如果URL为空，不进行加载
        if (url.isEmpty()) {
            Snackbar.make(binding.getRoot(), "请输入网址", Snackbar.LENGTH_SHORT).show();
            return;
        }
        // 如果URL不是以http或https开头，添加https前缀
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
            binding.urlEditText.setText(url);
        }
        binding.webview.loadUrl(url);
    }

    private void updateNavigationButtons() {
        binding.buttonBack.setEnabled(binding.webview.canGoBack());
        binding.buttonForward.setEnabled(binding.webview.canGoForward());
    }

    private void showSettingsDialog() {
        BrowserSettings settings = BrowserSettings.getInstance(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_browser_settings, null);

        androidx.appcompat.widget.SwitchCompat jsSwitch = dialogView.findViewById(R.id.switch_javascript);
        androidx.appcompat.widget.SwitchCompat nightModeSwitch = dialogView.findViewById(R.id.switch_night_mode);
        androidx.appcompat.widget.SwitchCompat adBlockSwitch = dialogView.findViewById(R.id.switch_ad_block);
        androidx.appcompat.widget.SwitchCompat cookiesSwitch = dialogView.findViewById(R.id.switch_cookies);
        EditText uaInput = dialogView.findViewById(R.id.edit_text_user_agent);

        // 设置当前值
        jsSwitch.setChecked(settings.isJavaScriptEnabled());
        nightModeSwitch.setChecked(settings.isNightModeEnabled());
        adBlockSwitch.setChecked(settings.isAdBlockEnabled());
        cookiesSwitch.setChecked(settings.isCookiesEnabled());
        uaInput.setText(settings.getCustomUserAgent());

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("浏览器设置")
                .setView(dialogView)
                .setPositiveButton("确定", (dialog, which) -> {
                    // 保存设置
                    settings.setJavaScriptEnabled(jsSwitch.isChecked());
                    settings.setNightModeEnabled(nightModeSwitch.isChecked());
                    settings.setAdBlockEnabled(adBlockSwitch.isChecked());
                    settings.setCookiesEnabled(cookiesSwitch.isChecked());
                    settings.setCustomUserAgent(uaInput.getText().toString());

                    // 重新加载页面以应用新设置
                    setupWebView();
                    binding.webview.reload();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
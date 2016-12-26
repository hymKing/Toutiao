package com.meiji.toutiao.news.info;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.afollestad.materialdialogs.MaterialDialog;
import com.meiji.toutiao.BaseActivity;
import com.meiji.toutiao.R;
import com.meiji.toutiao.bean.news.NewsArticleBean;

/**
 * Created by Meiji on 2016/12/17.
 */

public class InfoView extends BaseActivity implements IInfo.View {

    public static final String TAG = "InfoView";
    // 新闻链接 标题 头条号 文章号 媒体名
    private String shareUrl;
    private String shareTitle;

    private Toolbar toolbar;
    private WebView webView;
    private ActionBar actionBar;
    private MaterialDialog dialog;
    private IInfo.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_info_main);
        presenter = new InfoPresenter(this);
        initView();
        initWebClient();
        onShowRefreshing();
        initData();
    }

    private void initData() {
        Intent intent = getIntent();
        NewsArticleBean.DataBean dataBean = intent.getParcelableExtra(TAG);
        presenter.doRequestData(dataBean);
        shareUrl = dataBean.getDisplay_url();
        shareTitle = dataBean.getTitle();
        actionBar.setTitle(dataBean.getMedia_name());
    }

    private void initView() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        webView = (WebView) findViewById(R.id.webview_content);
        dialog = new MaterialDialog.Builder(this)
                .progress(true, 100)
                .content(R.string.md_loading)
                .cancelable(true)
                .build();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.news_content_comment:
                presenter.doGetComment();
                break;

            case R.id.news_content_follow:
                break;


            case R.id.news_content_share:
                Intent shareIntent = new Intent()
                        .setAction(Intent.ACTION_SEND)
                        .setType("text/plain")
                        .putExtra(Intent.EXTRA_TEXT, shareTitle + "\n" + shareUrl);
                startActivity(Intent.createChooser(shareIntent, getString(R.string.share_to)));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebClient() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        // 缩放,设置为不能缩放可以防止页面上出现放大和缩小的图标
        settings.setBuiltInZoomControls(false);
        // 缓存
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        // 开启DOM storage API功能
        settings.setDomStorageEnabled(true);
        // 开启application Cache功能
        settings.setAppCacheEnabled(false);
        // 不调用第三方浏览器即可进行页面反应
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                onHideRefreshing();
                super.onPageFinished(view, url);
            }
        });

        webView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if ((keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
                    webView.goBack();
                    return true;
                }
                return false;
            }
        });

//        webView.setWebChromeClient(new WebChromeClient() {
//            @Override
//            public void onProgressChanged(WebView view, int newProgress) {
//                if (newProgress < 80) {
//                    dialog.show();
//                } else {
//                    dialog.dismiss();
//                }
//                super.onProgressChanged(view, newProgress);
//            }
//        });
    }

    @Override
    public void onSetWebView(String url, boolean flag) {
        // 是否为头条的网站
        if (flag) {
            webView.loadDataWithBaseURL(null, url, "text/html", "utf-8", null);
        } else {
            webView.loadUrl(shareUrl);
        }
    }

    @Override
    public void onFail() {
    }

    @Override
    public void onShowRefreshing() {
        dialog.show();
    }

    @Override
    public void onHideRefreshing() {
        dialog.dismiss();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.news_content_main, menu);
        return super.onCreateOptionsMenu(menu);
    }
}

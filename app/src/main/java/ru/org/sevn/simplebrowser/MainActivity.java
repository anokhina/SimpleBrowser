/*
 * Copyright 2019 Veronica Anokhina.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package ru.org.sevn.simplebrowser;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public static final String WEATHER = "weather.com";
    private static final String MyApp = "ru_org_sevn_simplebrowser_MainActivity";

    private static final String TAG = "MainActivity";

    final String W_HOURLY = "https://weather.com/ru-RU/weather/hourbyhour/l/";
    final String W_TENDAY = "https://weather.com/ru-RU/weather/tenday/l/";
    final static Map<String, String> MAP_LOCATIONS = new HashMap<>();

    private static final String LAST_URL = "LAST_URL";

    static {
        MAP_LOCATIONS.put("Moscow", "RSXX0063:1:RS");
        MAP_LOCATIONS.put("Saint-Petersburg", "4edb4827c7f66b1542f84ce1d8d644970e9b935d45d21d4d143e87d94925a4bf");
        MAP_LOCATIONS.put("Alushta", "5946d0451222d97964a0b02e146e4218993caadeedc6b007cca5be7fce45f2de");
        MAP_LOCATIONS.put("Stolbovaya", "ce4194309142ac1f67451e778317a085c504ba8a62532f02c20b12163a574feb");
        MAP_LOCATIONS.put("Kurovskoe", "a0b81ac5c6315eac872eae295dd5442eff83d7e066cb2158470031120af76feb");
    }

    private EditText editText;
    private TextView tvScale;
    private Button button;
    private Button buttonX;
    private Button buttonPlus;
    private ToggleButton buttonToggle;
    private ToggleButton buttonToggle10;
    private Button buttonMinus;
    private WebView browser;
    private Spinner spinner;
    private SharedPreferences sharedPreferences;
    private String prevUrl;
    private int scale = 100;

    public static final Map<String, Boolean> PERMISSIONS = new HashMap<>();

    private class MBrowser extends WebChromeClient {
        private static final String TAG = "CustomWebChromeClient";

        @Override
        public boolean onConsoleMessage(ConsoleMessage cm) {
            Log.d(TAG, String.format("%s @ %d: %s", cm.message(),
                    cm.lineNumber(), cm.sourceId()));
            return true;
        }
    }

    private String baseUrl;
    private class MyBrowser extends WebViewClient {

        @Override
        public void onPageFinished(WebView view, String url) {
            //region-footer
                //browser.loadUrl("javascript:if (document.getElementsByTagName('footer')) {document.getElementsByTagName('footer').style.display = 'none'; }");
            //.loadDataWithBaseURL("", data, "text/html", "UTF-8", "");
            //region-rail
            if (url.contains(WEATHER)) {
                alterWeatherHtml();
            }
//                if (!url.equals(baseUrl)) {
//                    baseUrl = url;
//                    browser.loadUrl("javascript:" + MyApp + ".resize(document.body.getBoundingClientRect().width, document.body.getBoundingClientRect().height, document.getElementsByTagName('html')[0].innerHTML)");
//                }
            if (url.startsWith("http")) {
                loadedUrl(url);
            }
        }
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            loadUrl(view, url);
            return true;
        }
        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Log.e("SB", "error loading " + request.getUrl());
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Log.e("SB", "" + error.getDescription());
            }
            Log.e("SB","ERROR loadin url");
        }
    }

    private void alterWeatherHtml() {
        browser.loadUrl("javascript: if (document.getElementsByTagName('footer').length > 0) { var elem = document.getElementsByTagName('footer')[0]; elem.parentNode.removeChild(elem); } ");
        browser.loadUrl("javascript: if (document.getElementsByClassName('region-rail').length > 0) { var elem = document.getElementsByClassName('region-rail')[0]; elem.parentNode.removeChild(elem); } ");
        browser.loadUrl("javascript: if (document.getElementsByClassName('wx-adWrapper').length > 0) { var elem = document.getElementsByClassName('wx-adWrapper')[0]; elem.parentNode.removeChild(elem); } ");
        browser.loadUrl("javascript: if (document.getElementsByClassName('todaymap').length > 0) { var elem = document.getElementsByClassName('todaymap')[0]; elem.parentNode.removeChild(elem); } ");
    }

    private void loadedUrl(final String url) {
        SharedPreferences.Editor ed = sharedPreferences.edit();
        ed.putString(LAST_URL, url);
        ed.commit();
        editText.setText(url);
    }
    private void loadUrl(final WebView view, final String url) {
        if (buttonToggle != null && buttonToggle.isChecked()) {
            if (UA_FULL.equals(browser.getSettings().getUserAgentString())) {
                // nothing
            } else {
                browser.getSettings().setUserAgentString(UA_FULL);
            }
        } else {
            if (UA_FULL.equals(browser.getSettings().getUserAgentString())) {
                browser.getSettings().setUserAgentString(UA);
            }
        }

        editText.setText(url);
        view.loadUrl(url);
        view.requestFocus();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_main);
        Util.askPermission(this, PERMISSIONS,
                Manifest.permission.INTERNET
        );
        sharedPreferences = getPreferences(MODE_PRIVATE);
        prevUrl = sharedPreferences.getString(LAST_URL, "https://ya.ru/");

        spinner = (Spinner)  findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.places_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        final MyOnItemSelectedListener onItemSelectedListener = new MyOnItemSelectedListener();
        spinner.setOnItemSelectedListener(onItemSelectedListener);

        editText = (EditText) findViewById(R.id.edittext);

        button = (Button)findViewById(R.id.button);
        buttonToggle10 = (ToggleButton) findViewById(R.id.buttonToggle10);
        buttonToggle10.setTextOff("10");
        buttonToggle10.setTextOn("H");
        buttonToggle = (ToggleButton) findViewById(R.id.buttonToggle);
        buttonToggle.setTextOff("M");
        buttonToggle.setTextOn("F");
        buttonX = (Button)findViewById(R.id.buttonX);
        buttonPlus = (Button)findViewById(R.id.buttonPlus);
        buttonMinus = (Button)findViewById(R.id.buttonMinus);
        tvScale = (TextView)findViewById(R.id.tvScale);

        buttonToggle10.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                loadUrl(browser, getWeatherUrl(weatherId));
                                            }
                                        });
        buttonToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadUrl(browser, getWeatherUrl(weatherId));
            }
        });
        buttonX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setText("");
            }
        });
        buttonPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (scale + 5 < 1000) {
                    scale += 5;
                    //zzz(browser, scale);
                    //callJavaScript(browser, " document.body.style.zoom = " + scale +";");
                    browser.setInitialScale(scale);
                    tvScale.setText(""+scale);
                }
            }
        });
        buttonMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (scale - 5 > 10) {
                    scale -= 5;
                    //callJavaScript(browser, " document.body.style.zoom = " + scale +";");
                    //zzz(browser, scale);
                    browser.setInitialScale(scale);
                    tvScale.setText(""+scale);
                }
            }
        });
        browser = (WebView) findViewById(R.id.webview);
        UA = browser.getSettings().getUserAgentString();

        browser.setWebViewClient(new MyBrowser());
        browser.setWebChromeClient(new MBrowser());
        alterBrowser(browser);

        button.setOnClickListener(new View.OnClickListener() {
                                  @Override
                                  public void onClick(View v) {
                                      String url = editText.getText().toString();
                                      browser.loadUrl(url);
                                  }
                              });
        loadUrl(browser, "https://ya.ru/");

        onItemSelectedListener.setEnabled(true);
    }

    public class MyOnItemSelectedListener implements AdapterView.OnItemSelectedListener {
        private boolean enabled;

        public void onItemSelected(AdapterView<?> parent, View view,
                                   int pos, long id) {
            if (isEnabled()) {
                final String locationId = "" + parent.getItemAtPosition(pos);
                browser.clearHistory();
                if (locationId.trim().length() > 0) {
                    loadUrl(browser, getWeatherUrl(locationId));
                }
            }
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public void onNothingSelected(AdapterView<?> parent) {
        }
    }

    private String weatherId;
    private String getWeatherUrl(final String id) {
        if (id != null) {
            weatherId = id;
            if ("LAST".equals(id)) {
                return prevUrl;
            } else {
                if (buttonToggle10 != null && buttonToggle10.isChecked()) {
                    return W_HOURLY + MAP_LOCATIONS.get(id);
                }
                return W_TENDAY + MAP_LOCATIONS.get(id);
            }
        }
        return browser.getUrl();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (browser.canGoBack()) {
                        browser.goBack();
                    } else {
                        finish();
                    }
                    return true;
            }

        }
        return super.onKeyDown(keyCode, event);
    }

    private String UA;
    private String UA_FULL = "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.4) Gecko/20100101 Firefox/4.0";
    private void alterBrowser(WebView webView) {
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        scale = getScale();
//        webView.setInitialScale(scale);
        webView.getSettings().setBuiltInZoomControls(true);

        //webView.getSettings().setLoadWithOverviewMode(true);
        //webView.getSettings().setUseWideViewPort(true);

        //webView.getSettings().setUserAgentString(UA_FULL);
        webView.addJavascriptInterface(this, MyApp);

        tvScale.setText("" + scale);
    }
    private int getScale(){
        return 100;
//        int PIC_WIDTH = 600;
//        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
//        int width = display.getWidth();
//        Double val = new Double(width)/new Double(PIC_WIDTH);
//        val = val * 100d;
//        return val.intValue();
    }
    private void callJS(WebView webView, final String jscommand) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            webView.evaluateJavascript(jscommand + ";", null);
        } else {
            webView.loadUrl("javascript:"+jscommand+";");
        }
    }
    private void zzz(WebView webView, int scale) {
        webView.loadUrl(
                "javascript:(function() { " +
                        //"alert('Hi' + window.screen.availHeight);"+
                        "document.body.style.transform = 'scale(' + window.screen.availHeight*2 + ')';" +
                        //"document.body.style.zoom = "+scale+";" +
                        "})()");
    }
    //document.body.style.zoom =
    //callJavaScript(browser, "document.body.style.zoom = " + scale);
    private void callJavaScript(WebView view, String jscode){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("try{");
        stringBuilder.append(jscode);
        stringBuilder.append("}catch(error){console.error(error.message);}");
        final String call = stringBuilder.toString();
        Log.i(TAG, "callJavaScript: call="+call);


        callJS(view, call);
    }
    private void callJavaScriptFunction(WebView view, String methodName, Object...params){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("try{");
        stringBuilder.append(methodName);
        stringBuilder.append("(");
        String separator = "";
        for (Object param : params) {
            stringBuilder.append(separator);
            separator = ",";
            if(param instanceof String){
                stringBuilder.append("'");
            }
            stringBuilder.append(param.toString().replace("'", "\\'"));
            if(param instanceof String){
                stringBuilder.append("'");
            }

        }
        stringBuilder.append(")}catch(error){console.error(error.message);}");
        final String call = stringBuilder.toString();
        Log.i(TAG, "callJavaScript: call="+call);

        callJS(view, call);
    }

    @JavascriptInterface
    public void resize(final float width, final float height, final String txt) {
        final Document doc = Jsoup.parse(txt);
        doc.outputSettings().prettyPrint(true);
        //Log.e("HTML", doc.body().html());
        System.err.println("HTML"+ doc.body().html());
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                browser.loadDataWithBaseURL(baseUrl, doc.body().html(), "text/html", "UTF-8", "");
                //browser.setLayoutParams(new RelativeLayout.LayoutParams((int)(width * getResources().getDisplayMetrics().widthPixels), (int) (height * getResources().getDisplayMetrics().density)));
                //browser.setLayoutParams(new RelativeLayout.LayoutParams(getResources().getDisplayMetrics().widthPixels, (int) (height * getResources().getDisplayMetrics().density)));
            }
        });
    }
}

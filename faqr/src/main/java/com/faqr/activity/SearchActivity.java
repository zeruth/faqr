/*
 * Copyright (c) eneve software 2013. All rights reserved.
 */

package com.faqr.activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.faqr.FaqrApp;
import com.faqr.R;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * This Activity provides a help screen for the app
 *
 * @author eneve
 */
public class SearchActivity extends BaseActivity {

    public static String content = "";
    public static String directURL = "gamefaqs.gamespot.com/ps/197343-final-fantasy-viii/faqs/51741";

    private StickyListHeadersListView listView;
    private SearchResultsListAdapter adapter;

    private ArrayList data = new ArrayList();
    private ArrayList allData = new ArrayList();
    private ArrayList titles = new ArrayList();

    private LinearLayout loading;
    private LinearLayout noResults;
    private TextView noResultsText;

    private Bundle extras;

    private String game = "";
    private boolean autoFire = false;
    private String url = "";

    private SearchView searchView;

    public final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        toolbar.getRootView().setBackgroundColor(themeBackgroundColor);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // make sure the keyboard only pops up when a user clicks into an EditText
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        extras = getIntent().getExtras();
        if (extras != null && extras.getString("game") != null && !TextUtils.isEmpty(extras.getString("game"))) {
            game = extras.getString("game");
        }
        if (extras != null && extras.getBoolean("autoFire")) {
            autoFire = extras.getBoolean("autoFire");
        }

        setTitle("");

        if (extras != null && extras.getString("url") != null && !TextUtils.isEmpty(extras.getString("url"))) {
            url = extras.getString("url");
        }

        // set the list adapter
        adapter = new SearchResultsListAdapter();
        listView = (StickyListHeadersListView) findViewById(R.id.list);
        listView.setAdapter(adapter);

        // fast scroll
        if (prefs.getBoolean("use_fast_scroll", getResources().getBoolean(R.bool.use_fast_scroll_default))) {
            listView.setFastScrollEnabled(true);
            if (prefs.getBoolean("fast_scroll_left", getResources().getBoolean(R.bool.fast_scroll_left_default))) {
                listView.setVerticalScrollbarPosition(View.SCROLLBAR_POSITION_LEFT);
            }
        } else {
            listView.setFastScrollEnabled(false);
        }

        listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                String text = allData.get(position).toString();

                String url = "";

                String[] textSplit = text.split("---");
                url = textSplit[textSplit.length - 1].trim();
                if ((textSplit.length == 6) || (textSplit.length == 7)) {
                    String title = textSplit[0].trim();
                    String date = textSplit[1].trim();
                    String author = textSplit[2].trim();
                    String version = textSplit[3].trim();
                    String size = textSplit[4].trim();
                    String href = textSplit[5].trim();

                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("curr_faq", FaqrApp.validFileName(href));

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
                    editor.putString(FaqrApp.validFileName(href) + "___last_read", sdf.format(new Date()));

                    // THIS IS WHERE WE BUILD THE FAQMETA!!!
                    Log.i(TAG, FaqrApp.validFileName(href) + " " + title + " --- " + date + " --- " + author + " --- " + version + " --- " + size + " --- " + href);
                    if (TextUtils.isEmpty(prefs.getString(FaqrApp.validFileName(href), "")))
                        editor.putString(FaqrApp.validFileName(href), title + " --- " + date + " --- " + author + " --- " + version + " --- " + size + " --- " + href);
                    editor.commit();
                }

                String[] split = url.split("/");
                String last = "";
                String secondToLast = "";
                if (split.length > 2) {
                    last = split[split.length - 1];
                    secondToLast = split[split.length - 2];
                }

                // external url
                if (!url.contains(getResources().getString(R.string.GAMEFAQS_URL))) {
                    Intent intent = new Intent(getApplicationContext(), FaqActivity.class);
                    intent.putExtra("from_search", true);
                    startActivity(intent);

                } else if (secondToLast.equals("faqs") && !TextUtils.isEmpty(last)) {
                    Intent intent = new Intent(getApplicationContext(), FaqActivity.class);
                    intent.putExtra("from_search", true);
                    startActivity(intent);

                } else {
                    Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
                    intent.putExtra("from_search_results", true);
                    intent.putExtra("game", game);
                    intent.putExtra("url", url);
                    startActivity(intent);
//                    finish();
                }
            }
        });

        // loading indicator
        loading = (LinearLayout) findViewById(R.id.loading);
        noResults = (LinearLayout) findViewById(R.id.no_results);
        noResultsText = (TextView) findViewById(R.id.no_results_text);
        noResultsText.setTextColor(themeTextColor);

        // check connectivity
        if (!isNetworkAvailable()) {
            Toast.makeText(getApplicationContext(), "There is no internet connection available, Please connect to wifi or data plan and try again.", Toast.LENGTH_LONG).show();
            loading.setVisibility(View.GONE);
        } else {
            // execute the task in another thread
            new SearchTask().execute(new String[] {});
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    /** Called when the activity will start interacting with the user. */
    @Override
    protected void onResume() {
        super.onResume();

        // prevent the searchView from getting focus
        listView.requestFocus();
    }

    /** Called when phone hard keys are pressed */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (extras == null || (extras != null && extras.getBoolean("from_search_results") != true)) {
                Intent intent = new Intent(this, FaqsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Get tracker.
        Tracker t = ((FaqrApp) getApplication()).getTracker();
        // Set screen name.
        t.setScreenName(getClass().getName());
        // Send a screen view.
        t.send(new HitBuilders.AppViewBuilder().build());

        if (searchView != null)
            searchView.clearFocus();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_search, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryHint(getResources().getString(R.string.search_hint));
        searchView.setIconifiedByDefault(false);
        // searchItem.expandActionView();
        LinearLayout searchText = (LinearLayout) searchView.findViewById(R.id.search_plate);
        LinearLayout searchTextFrame = (LinearLayout) searchView.findViewById(R.id.search_edit_frame);

        int pL = searchText.getPaddingLeft();
        int pT = searchText.getPaddingTop();
        int pR = searchText.getPaddingRight();
        int pB = searchText.getPaddingBottom();
        searchText.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        searchText.setPadding(pL, pT, pR, pB);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String s) {
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                // close the search view
                // Toast.makeText(getApplicationContext(), "onQueryTextSubmit", Toast.LENGTH_SHORT).show();

                if (null != query && !query.equals("")) {

                    // saved searches
                    String recentSearches = prefs.getString("recent_searches", "");
                    String[] split = recentSearches.split(" --- ");
                    final List<String> list = new ArrayList<String>();
                    Collections.addAll(list, split);
                    list.remove(query.trim());
                    String newRecentSearches = "";
                    newRecentSearches += query.trim();
                    for (int i = 0; i < list.size(); i++) {
                        newRecentSearches += " --- " + list.get(i);
                        if (i > 18)
                            break;
                    }
                    SharedPreferences.Editor editor = prefs.edit();

                    editor.putString("recent_searches", newRecentSearches);
                    editor.commit();

                    Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
                    intent.putExtra("game", query.trim());
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Please enter search terms.", Toast.LENGTH_SHORT).show();
                }

                return true;
            }
        });

        final SearchView.SearchAutoComplete searchAutoComplete = (SearchView.SearchAutoComplete) searchView.findViewById(R.id.search_src_text);
        // searchView.findViewById(abs__search_src_text)
        searchAutoComplete.setThreshold(0);
        String recentSearches = prefs.getString("recent_searches", "");
        String[] split = recentSearches.split(" --- ");
        final List<String> list = new ArrayList<String>();
        Collections.addAll(list, split);
        list.remove("");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.simple_spinner_dropdown_item, list);
        searchAutoComplete.setAdapter(adapter);

        searchAutoComplete.setText(game);

        searchAutoComplete.setOnItemClickListener(new OnItemClickListener() {

            /**
             * Implements OnItemClickListener
             */
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                TextView textView = (TextView) view.findViewById(android.R.id.text1);

                searchAutoComplete.setText(textView.getText());

                // saved searches
                String recentSearches = prefs.getString("recent_searches", "");
                String[] split = recentSearches.split(" --- ");
                final List<String> list = new ArrayList<String>();
                Collections.addAll(list, split);
                list.remove(searchView.getQuery().toString().trim());
                String newRecentSearches = "";
                newRecentSearches += searchView.getQuery().toString().trim();
                for (int i = 0; i < list.size(); i++) {
                    newRecentSearches += " --- " + list.get(i);
                    if (i > 18)
                        break;
                }
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("recent_searches", newRecentSearches);
                editor.commit();

                Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("game", textView.getText());
                startActivity(intent);
                finish();
            }
        });

        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                return true; // Return true to collapse action view
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                // find = true;
                Log.i(TAG, "onMenuItemActionExpand " + item.getItemId());
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_settings:
            Intent intent;
            intent = new Intent(this, PreferencesActivity.class);
            intent.putExtra("fromActivity", "SearchResults");
            intent.putExtra("fromActivityMeta", game);
            startActivity(intent);
            finish();
            return true;
        case R.id.menu_about:

            intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            return true;
        case android.R.id.home:
            if (extras == null || (extras != null && extras.getBoolean("from_search_results") != true)) {
                intent = new Intent(this, FaqsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
            finish();

            return true;
        default:
            return false;
        }
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // LIST ADAPTER

    public class SearchResultsListAdapter extends BaseAdapter implements StickyListHeadersAdapter {

        @Override
        public int getCount() {
            return allData.size();
        }

        @Override
        public Object getItem(int position) {
            return allData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (extras != null && extras.getString("url") != null && !TextUtils.isEmpty(extras.getString("url"))) {
                view = inflater.inflate(R.layout.list_item_search_2, parent, false);
            } else {
                view = inflater.inflate(R.layout.list_item_search, parent, false);
            }

            // theme
            if (prefs.getString("theme", getResources().getString(R.string.theme_default)).equals("1")) {
                // Day
                view.setBackgroundDrawable(getResources().getDrawable(R.drawable.list_item_day));
            } else if (prefs.getString("theme", getResources().getString(R.string.theme_default)).equals("2")) {
                // Night
                view.setBackgroundDrawable(getResources().getDrawable(R.drawable.list_item_night));
            } else if (prefs.getString("theme", getResources().getString(R.string.theme_default)).equals("3")) {
                // Dark
                view.setBackgroundDrawable(getResources().getDrawable(R.drawable.list_item_dark));
            } else if (prefs.getString("theme", getResources().getString(R.string.theme_default)).equals("4")) {
                view.setBackgroundDrawable(getResources().getDrawable(R.drawable.list_item_sepia));
            }

            // String line = lines[position];
            //

            String item = (String) allData.get(position);

            if (item.split(" --- ").length == 3) {

                // name
                TextView nameView = (TextView) view.findViewById(R.id.name);

                // platform
                TextView platformView = (TextView) view.findViewById(R.id.platform);

                platformView.setText(item.split(" --- ")[0]);
                nameView.setText(item.split(" --- ")[1]);

                // theme goodness
                nameView.setTextColor(themeAccentColor);
                platformView.setTextColor(themeTextColor);

            } else if (item.split(" --- ").length == 6) {

                TextView nameView = (TextView) view.findViewById(R.id.name);
                TextView authorView = (TextView) view.findViewById(R.id.author);
                TextView versionView = (TextView) view.findViewById(R.id.version);
                TextView sizeView = (TextView) view.findViewById(R.id.size);

                String dateFix = item.split(" --- ")[1];
                if (dateFix.startsWith("0"))
                    dateFix = dateFix.substring(1);

                String versionAndSize = "v" + item.split(" --- ")[3] + "/" + item.split(" --- ")[4].replaceAll("K", "k");
                if (item.split(" --- ")[3].trim().equals("") && !item.split(" --- ")[3].trim().equalsIgnoreCase("Final"))
                    versionAndSize = item.split(" --- ")[4].replaceAll("K", "k");

                nameView.setText(item.split(" --- ")[0]);
                authorView.setText(item.split(" --- ")[2]);
                versionView.setText(dateFix);
                sizeView.setText(versionAndSize);

                // theme goodness
                nameView.setTextColor(themeAccentColor);
                authorView.setTextColor(themeTextColor);
                versionView.setTextColor(themeTextColor);
                sizeView.setTextColor(themeTextColor);

            } else if (item.split(" --- ").length == 7) {
                // IT HAS 7 IF IT HAS AN IMAGE LIKE STAR, CIRCLE, HALFCIRCLE ETC.

                TextView nameView = (TextView) view.findViewById(R.id.name);
                TextView authorView = (TextView) view.findViewById(R.id.author);
                TextView versionView = (TextView) view.findViewById(R.id.version);
                TextView sizeView = (TextView) view.findViewById(R.id.size);

                // the unicode image
                String star = "\u2605 ";
                String circle = "\u25CF ";
                String halfCircle = "\u25D2 ";
                // unicode image urls
                String starSrc = "http://img.gamefaqs.net/images/default/rec.gif";
                String circleSrc = "http://img.gamefaqs.net/images/default/s3.gif";
                String halfCircleSrc = "http://img.gamefaqs.net/images/default/s2.gif";

                String marker = "";
                String imgSrc = item.split(" --- ")[6];
                if (imgSrc.equals(starSrc)) {
                    marker = star;
                }

                nameView.setText(marker + item.split(" --- ")[0]);
                //authorView.setText(item.split(" --- ")[2]);
                //versionView.setText(item.split(" --- ")[3]);
                //sizeView.setText(item.split(" --- ")[4]);

                // theme goodness
                nameView.setTextColor(themeAccentColor);
                //authorView.setTextColor(themeTextColor);
                //versionView.setTextColor(themeTextColor);
                //sizeView.setTextColor(themeTextColor);
            }

            return view;
        }

        @Override
        public View getHeaderView(int position, View convertView, ViewGroup parent) {
            View view = inflater.inflate(R.layout.list_item_search_header, null);
            TextView textView = (TextView) view.findViewById(R.id.name);

            int count = 0;
            int sectionCount = 0;
            for (int i = 0; i < allData.size(); i++) {
                count += ((ArrayList) data.get(i)).size();
                if (position < count) {
                    break;
                }
                sectionCount += 1;
            }

            textView.setText(titles.get(sectionCount).toString());
            view.setBackgroundColor(primaryColor);

            return view;
        }

        @Override
        public long getHeaderId(int position) {
            int count = 0;
            int sectionCount = 0;
            for (int i = 0; i < allData.size(); i++) {
                count += ((ArrayList) data.get(i)).size();
                if (position < count) {
                    break;
                }
                sectionCount += 1;
            }

            return sectionCount;
        }

    }


    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // TASKS

    /**
     * Save the current position
     *
     * @author eneve
     */
    private class SearchTask extends AsyncTask<String, Void, String> {

        protected void onPreExecute() {
            loading.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
            noResults.setVisibility(View.GONE);
        }

        HttpUrl getFaqBase()
        {
            return HttpUrl.parse("http://faqchecker.ddns.net");
        }

        public String getFaq(String urlPath) throws IOException
        {
            HttpUrl url = getFaqBase().newBuilder()
                    .addPathSegment("faqs")
                    .addPathSegment("direct")
                    .addQueryParameter("url", urlPath)
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .build();

            Response response = client.newCall(request).execute();

            return response.body().string();
        }

        private final char[] ILLEGAL_CHARACTERS = { '/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':', '.' };

        public String validFileName(String title) {
            String s = title;
            int len = s.length();
            StringBuilder sb = new StringBuilder(len);
            for (int i = 0; i < len; i++) {
                char ch = s.charAt(i);

                boolean found = false;
                for (char currch : ILLEGAL_CHARACTERS) {
                    if (ch == currch) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    sb.append(ch);
                } else {
                    sb.append('_');
                }

            }
            return sb.toString().replaceAll(" ", "_");
        }

        @Override
        protected String doInBackground(String... strings) {
            String result = "0";

            try {

                Log.w(TAG, "===============================================");
                Log.w(TAG, "FETCHING FROM WEB " + url);

                String gameParam = game.replace(" ", "\\+");

                String mobileParam = "&mobile=0";
                if (prefs.getBoolean("exclude_mobile", getResources().getBoolean(R.bool.exclude_mobile_default))) {
                    mobileParam = "&mobile=1";
                }

                String dlcParam = "&dlc=0";
                if (prefs.getBoolean("exclude_dlc", getResources().getBoolean(R.bool.exclude_dlc_default))) {
                    dlcParam = "&dlc=1";
                }

                content = getFaq(game);
                content = game + "\n" + content;
                File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
                File file = new File(dir + "/faqr/", validFileName(game));
                file.createNewFile();
                FileOutputStream stream = new FileOutputStream(file);
                try {
                    stream.write(content.getBytes());
                } finally {
                    stream.close();
                }
                String sectionTitle = game.replace("gamefaqs.gamespot.com/", "").split("/")[1];
                ArrayList sectionData = new ArrayList();
                sectionData.add(
                        content.split("\n")[1].split("by")[0] + // Guide Name
                                " --- " +
                                content.split("\n")[3].split("|")[1].split("Updated: ")[0] + // Updated
                                " --- " +
                                content.split("\n")[1].split("by")[1] + // Author
                                " --- " +
                                content.split("\n")[3].split("|")[0].split("Version: ")[0] + // Version
                                " --- " +
                                "size" +
                                " --- " +
                                "href" + // href
                                " --- " +
                                "img");
                titles.add(sectionTitle);
                data.add(sectionData);
                allData.addAll(sectionData);


                if (autoFire)
                {
                    if (!url.contains(getResources().getString(R.string.GAMEFAQS_URL))) {
                        Intent intent = new Intent(getApplicationContext(), FaqActivity.class);
                        intent.putExtra("from_search", true);
                        startActivity(intent);
                    }
                }

            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
                result = "-1";
            }

            return result;
        }

        protected void onPostExecute(String result) {

            if (result.equals("-1")) {
                Toast.makeText(getApplicationContext(), "Sorry an error occured. Please try again.", Toast.LENGTH_SHORT).show();

            } else {

                if (data.size() == 0) {
                    // Toast.makeText(getApplicationContext(), "No results found.", Toast.LENGTH_SHORT).show();
                    noResults.setVisibility(View.VISIBLE);
                    loading.setVisibility(View.GONE);

                } else {

                    // fancy animations
                    listView.setVisibility(View.VISIBLE);
                    loading.setVisibility(View.GONE);

                    Animation fadeInAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in_anim);
                    Animation fadeOutAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out_anim);
                    fadeInAnimation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            loading.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    // Now Set your animation
                    listView.startAnimation(fadeInAnimation);
                    loading.startAnimation(fadeOutAnimation);
                }

                ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
            }

        }

    };

}

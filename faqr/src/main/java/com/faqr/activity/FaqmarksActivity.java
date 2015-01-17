/*
 * Copyright (c) eneve software 2013. All rights reserved.
 */

package com.faqr.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.faqr.FaqrApp;
import com.faqr.R;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
//import com.google.analytics.tracking.android.EasyTracker;

/**
 * This Activity provides a help screen for the app
 * 
 * @author eneve
 */
public class FaqmarksActivity extends BaseActivity implements OnClickListener {

    // loading
    private LinearLayout loading;
    private LinearLayout error;

    private List<String> savedLineNumbers = new ArrayList<String>();
    private List<String> savedLinePercentages = new ArrayList<String>();
    private List<String> savedLines = new ArrayList<String>();
    private String lines[] = new String[] {};
    private String origLines[] = new String[] {};

    // list view
    private ListView listView;
    private FaqAdapter adapter;

    private Menu menu;
    // private Boolean hideWebViewMenuOptions = false;

    // dialogs
    protected AlertDialog deleteAllConfirmDialog;

    // our webview
    private WebView webView;

    private float autoMonoFontSize = -1.0f;

    // current faq info
    private String currFaq = "";
    private String[] currFaqMeta = new String[] {};

    /** Called when the activity is first created. */
    @SuppressLint("NewApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_faqmarks);

        // theme goodness
        if (prefs.getString("theme", getResources().getString(R.string.theme_default)).equals("1")) {

            if (prefs.getBoolean("use_immersive_mode", getResources().getBoolean(R.bool.use_immersive_mode_default))) {
                // setTheme(R.style.AppBlackOverlayTheme);
            }
        } else if (prefs.getString("theme", getResources().getString(R.string.theme_default)).equals("2")) {

            if (prefs.getBoolean("use_immersive_mode", getResources().getBoolean(R.bool.use_immersive_mode_default))) {
//                setTheme(R.style.AppBlackOverlayTheme);
            }
        } else if (prefs.getString("theme", getResources().getString(R.string.theme_default)).equals("3")) {
            if (prefs.getBoolean("use_immersive_mode", getResources().getBoolean(R.bool.use_immersive_mode_default))) {
//                setTheme(R.style.AppDarkOverlayTheme);
            }
        } else if (prefs.getString("theme", getResources().getString(R.string.theme_default)).equals("4")) {
            RelativeLayout bg = (RelativeLayout) findViewById(R.id.bg);
            bg.setBackgroundColor(0xFFECE1CA);
//            themeColor = getResources().getColor(R.color.sepia_theme_color);
            // themeColor = getResources().getColor(R.color.sepia_theme_color);
            themeBackgroundColor = getResources().getColor(R.color.sepia_theme_color);

        }


        // show back if we came from search
        // if (extras != null && extras.getBoolean("from_search") == true) {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        // }

        // set the list adapter
        adapter = new FaqAdapter();
        // setListAdapter(adapter);
        // ListView listView = listView;

        listView = (ListView) findViewById(R.id.list);
        // listView.setOnItemClickListener(adapter.itemClickListener);
        listView.setAdapter(adapter);

        // listView.setTextFilterEnabled(true);

        // loading indicator
        loading = (LinearLayout) findViewById(R.id.loading);
        error = (LinearLayout) findViewById(R.id.error);

        /** called when a list item is clicked */
        listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String savedLineNumber = savedLineNumbers.get(position);

                SharedPreferences.Editor editor = prefs.edit();
                editor = prefs.edit();
                editor.putInt("my_faqmarks_pos", listView.getFirstVisiblePosition());
                editor.commit();

                Intent intent = new Intent(getApplicationContext(), FaqActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("FAQmarkPosition", new Integer(savedLineNumber));
                startActivity(intent);
                finish();
            }
        });

        // /** called when a list item is long clicked */
        listView.setLongClickable(true);
        listView.setOnItemLongClickListener(new OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                final String savedLineNumber = savedLineNumbers.get(position);

                final String plusOne = new Integer(Integer.valueOf(savedLineNumber) + 1).toString();

                // delete dialog
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(FaqmarksActivity.this);
                dialogBuilder.setMessage("Are you sure you want to delete Location " + plusOne + "/" + lines.length + " - FAQmark?").setCancelable(true).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        String savedPosMulti = prefs.getString(FaqrApp.validFileName(currFaqMeta[5]) + "multi_saved_pos", "");
                        // Log.w(TAG, "BEFORE " + savedPosMulti);

                        boolean found = false;
                        String[] savedPosMultiList = savedPosMulti.split(",");
                        StringBuffer newSavedPosMulti = new StringBuffer();
                        for (int i = 0; i < savedPosMultiList.length; i++) {
                            if (savedPosMultiList[i].equals(savedLineNumber)) {
                                found = true;
                            } else {
                                newSavedPosMulti.append(savedPosMultiList[i] + ",");
                            }
                        }

                        if (found) {
                            savedPosMulti = newSavedPosMulti.toString();
                        }

                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString(FaqrApp.validFileName(currFaqMeta[5]) + "multi_saved_pos", savedPosMulti);
                        editor.commit();

                        editor.putInt("my_faqmarks_pos", listView.getFirstVisiblePosition());
                        editor.commit();

                        if (TextUtils.isEmpty(savedPosMulti)) {

                            Intent intent = new Intent(getApplicationContext(), FaqActivity.class);
                            startActivity(intent);
                            finish();
                        } else {

                            Intent intent = new Intent(getApplicationContext(), FaqmarksActivity.class);
                            startActivity(intent);
                            finish();
                        }

                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // do nothing
                    }
                });

                AlertDialog faqDialog = dialogBuilder.create();
                faqDialog.show();
                return true;

            }
        });

        // delete all confirm dialog
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(FaqmarksActivity.this);
        dialogBuilder.setMessage("Are you sure you want to delete all of your saved FAQmarks.").setCancelable(true).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // start the delete all task
                new DeleteAllTask().execute(new String[] {});
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // do nothing
            }
        });
        deleteAllConfirmDialog = dialogBuilder.create();
        // quitDialog.setTitle("Are you sure?");

        // auto font size reset
        autoMonoFontSize = -1.0f;

        // get the current FAQ
        if (!TextUtils.isEmpty(prefs.getString("curr_faq", ""))) {
            currFaq = prefs.getString("curr_faq", "");
            // currFaq example
            // http___m_gamefaqs_com_psp_615911-final-fantasy-iv-the-complete-collection_faqs_62211
            String faqMeta = prefs.getString(prefs.getString("curr_faq", ""), "");

            // set last read date
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(prefs.getString("curr_faq", "") + "___last_read", sdf.format(new Date()));

            // faqMeta example
            // Final Fantasy IV FAQ/Walkthrough --- 09/20/11 --- Johnathan 'Zy' Sawyer --- 1.02 --- 1267K --- http://m.gamefaqs.com/psp/615911-final-fantasy-iv-the-complete-collection/faqs/62211
            Log.w(TAG, "-----------------------------");
            Log.w(TAG, currFaq);
            Log.w(TAG, faqMeta);
            Log.w(TAG, "-----------------------------");
            currFaqMeta = faqMeta.split(" --- ");

            int curr_pos = prefs.getInt(prefs.getString("curr_faq", "") + "curr_pos", -1);
            int saved_pos = prefs.getInt(prefs.getString("curr_faq", "") + "saved_pos", -1);
            // Toast.makeText(getApplicationContext(), faqMeta + " " + curr_pos + " " + saved_pos, Toast.LENGTH_LONG).show();
            new GetFaqTask().execute(new String[] {});
        } else {
            Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent);
            finish();
        }

    }

    /** Called when the activity will start interacting with the user. */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onResume() {
        super.onResume();

        // low profile
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

            if (prefs.getBoolean("use_lights_out", getResources().getBoolean(R.bool.use_lights_out_default))) {
                listView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
            }
        }

    }

    /** Called when phone hard keys are pressed */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            Intent intent = new Intent(this, FaqActivity.class);
            startActivity(intent);
            finish();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
//        EasyTracker.getInstance(this).activityStart(this); // Add this method.
    }

    @Override
    public void onStop() {
        super.onStop();
//        EasyTracker.getInstance(this).activityStop(this); // Add this method.
    }

    @Override
    /** Called when the phone orientation is changed */
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // forces the recalculation of the mono font size
        autoMonoFontSize = -1.0f;
    }

    /** Called when a button is clicked */
    public void onClick(View view) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;

        final Menu finalMenu = menu;

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_faqmarks, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {

        case R.id.menu_delete:
            deleteAllConfirmDialog.show();
            return true;
        case R.id.menu_settings:
            intent = new Intent(this, SettingsActivity.class);
            intent.putExtra("fromActivity", "My FAQmarks");

            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("my_faqmarks_pos", listView.getFirstVisiblePosition());
            editor.commit();

            startActivity(intent);
            finish();
            return true;
        case R.id.menu_about:

            editor = prefs.edit();
            editor.putInt("my_faqmarks_pos", listView.getFirstVisiblePosition());
            editor.commit();

            intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            return true;
        case android.R.id.home:

            editor = prefs.edit();
            editor.putInt("my_faqmarks_pos", listView.getFirstVisiblePosition());
            editor.commit();

            intent = new Intent(this, FaqActivity.class);
            startActivity(intent);
            finish();
            return true;
        default:
            return false;
        }
    }

    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
            case DialogInterface.BUTTON_POSITIVE:

                // check connectivity
                if (!isNetworkAvailable()) {
                    connectionDialog.show();
                } else {
                    new GetFaqTask().execute(new String[] { "reload" });
                }
                break;

            case DialogInterface.BUTTON_NEGATIVE:
                // No button clicked
                break;
            }
        }
    };

    /**
     * The Faqr list adapter thats smart about displaying ASCII
     * 
     * @author eneve
     */
    public class FaqAdapter extends BaseAdapter {

        private final Object mLock = new Object();

        public FaqAdapter() {
        }

        public int getCount() {
            return savedLines.size();
        }

        public Object getItem(int position) {
            return savedLines.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("NewApi")
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = inflater.inflate(R.layout.faqmarks_item, parent, false);

            String line = (String) savedLines.get(position);

            // location
            TextView locationView = (TextView) view.findViewById(R.id.location);

            Integer realPos = Integer.valueOf(savedLineNumbers.get(position));
            Integer plusOne = Integer.valueOf(savedLineNumbers.get(position)) + 1;
            locationView.setText("Location " + plusOne.toString() + "/" + lines.length + " - FAQmark");

            // percentage
            TextView percentageView = (TextView) view.findViewById(R.id.percentage);
            percentageView.setText(savedLinePercentages.get(position));

            // theme goodness
            // view.setBackgroundColor(themeColor);
            locationView.setTextColor(themeColor);

            // name
            TextView nameView = (TextView) view.findViewById(R.id.name);

            if (!prefs.getBoolean("use_variable_font", getResources().getBoolean(R.bool.use_variable_font_default)) || FaqrApp.useFixedWidthFont(line)) {
                // //////////
                // MONO FONT

                // nameView.setTextScaleX(1.3f);
                // nameView.setTypeface(tf);
                nameView.setTextAppearance(getApplicationContext(), R.style.MonoText);

                String monoFontSize = prefs.getString("mono_font_size", getResources().getString(R.string.mono_font_size_default));

                // auto font-size
                if (monoFontSize.equalsIgnoreCase("auto") && autoMonoFontSize == -1.0f) {
                    // Log.i(TAG, "CALCULATING FONT SIZE --------------------");

                    int measuredWidth = 0;
                    int measuredHeight = 0;
                    Point size = new Point();
                    WindowManager w = getWindowManager();

                    // account for padding on both sides
                    // Resources r = getResources();
                    // float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, r.getDisplayMetrics());
                    // px = px * 2;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        w.getDefaultDisplay().getSize(size);
                        measuredWidth = Math.round(size.x);
                        measuredHeight = size.y;
                    } else {
                        Display d = w.getDefaultDisplay();
                        measuredWidth = Math.round(d.getWidth());
                        measuredHeight = d.getHeight();
                    }

                    int totalCharstoFit = nameView.getPaint().breakText(getResources().getString(R.string.standard_width), true, measuredWidth, null);
                    int count = 0;
                    autoMonoFontSize = 12.0f;
                    // 79 characters/line is a classic standard for text files
                    while (totalCharstoFit != 79) {
                        if (totalCharstoFit < 79) {
                            autoMonoFontSize = Float.valueOf(autoMonoFontSize) - 0.5f;
                            nameView.setTextSize(Float.valueOf(autoMonoFontSize));
                            if (autoMonoFontSize <= 6.0f)
                                break;
                        } else if (totalCharstoFit > 79) {
                            autoMonoFontSize = Float.valueOf(autoMonoFontSize) + 0.5f;
                            nameView.setTextSize(Float.valueOf(autoMonoFontSize));
                            if (autoMonoFontSize >= 15.0f)
                                break;
                        } else {
                            break;
                        }
                        totalCharstoFit = nameView.getPaint().breakText(getResources().getString(R.string.standard_width), true, measuredWidth, null);
                        count++;
                        if (count > 10)
                            break;
                    }
                }
                if (monoFontSize.equalsIgnoreCase("auto")) {
                    nameView.setTextSize(Float.valueOf(autoMonoFontSize));
                    // make bold for small fonts
                    if (autoMonoFontSize <= 7.0f) {
                        nameView.setTextAppearance(getApplicationContext(), R.style.MonoTextBold);
                    }
                } else {
                    nameView.setTextSize(Float.valueOf(monoFontSize));
                }

                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) nameView.getLayoutParams();
                lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
                lp.addRule(RelativeLayout.CENTER_VERTICAL);
                nameView.setLayoutParams(lp);

                // nameView.setGravity( Gravity.CENTER | Gravity.CENTER);
                view.setPadding(0, 10, 0, 10);

            } else {
                // //////////////
                // VARIABLE FONT

                // nameView.setTypeface(tf2);
                if (prefs.getBoolean("use_serif_font", getResources().getBoolean(R.bool.use_serif_font_default))) {
                    nameView.setTextAppearance(getApplicationContext(), R.style.SerifText);
                } else {
                    nameView.setTextAppearance(getApplicationContext(), R.style.SansText);
                }

                line = line.replaceAll("\\n", "");

                String variableFontSize = prefs.getString("variable_font_size", getResources().getString(R.string.variable_font_size_default));

                // auto font size
                if (variableFontSize.equalsIgnoreCase("auto")) {

                    if (autoMonoFontSize <= 10.0f) {
                        nameView.setTextSize(Float.valueOf("13.0f"));
                    } else if (autoMonoFontSize >= 15.0f) {
                        nameView.setTextSize(Float.valueOf("15.0f"));
                    } else {
                        nameView.setTextSize(Float.valueOf("14.0f"));
                    }

                } else {
                    nameView.setTextSize(Float.valueOf(variableFontSize));
                }

                view.setPadding(10, 10, 10, 10);
            }

            // set the text

            String previewText = "";
            String[] lineSplit = line.split("\\n");

            Integer savedPosPreview = Integer.valueOf(prefs.getString("saved_pos_preview", getResources().getString(R.string.saved_pos_preview_default)));

            int linescount = 0;
            for (int i = 0; i < savedPosPreview; i++) {
                if (i >= lineSplit.length)
                    break;
                previewText += lineSplit[i] + System.getProperty("line.separator");
                linescount++;
            }

            int nextpos = realPos;
            while (linescount < savedPosPreview) {
                nextpos = nextpos + 1;
                String nextlines = lines[nextpos];

                previewText += System.getProperty("line.separator");
                linescount++;

                String[] lineSplit2 = nextlines.split("\\n");
                for (int i = 0; i < savedPosPreview; i++) {
                    if (i >= lineSplit2.length)
                        break;
                    previewText += lineSplit2[i] + System.getProperty("line.separator");
                    linescount++;
                }
            }

            nameView.setText(previewText);
            // nameView.setText(line);

            // handle some padding at the top for when action bar is hidden

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                if (prefs.getBoolean("use_immersive_mode", getResources().getBoolean(R.bool.use_immersive_mode_default))) {
                    // nameView.setPadding(view.getPaddingLeft(), getActionBarHeight(), view.getPaddingRight(), view.getPaddingBottom());
                } else if (position == 0 && prefs.getBoolean("hide_action_bar", getResources().getBoolean(R.bool.hide_action_bar_default))) {
                    nameView.setPadding(view.getPaddingLeft(), getActionBarHeight(), view.getPaddingRight(), view.getPaddingBottom());
                }
            }

            return view;
        }
    }

    /**
     * Main Async task that loads the FAQS - trys to read from disk then web then then save the file if necessary
     * 
     * @author eneve
     */
    private class GetFaqTask extends AsyncTask<String, Void, String> {

        String title = "";
        String content = "";
        String currFaqURL = "";

        protected void onPreExecute() {
            loading.setVisibility(View.VISIBLE);
            // error.setVisibility(View.GONE);
            listView.setVisibility(View.GONE);
            // setTitle(TAG);
        }

        @Override
        protected String doInBackground(String... strings) {

            String result = "0";
            boolean success = false;

            // THIS IS WHERE WE CRASH IF THE METADATA IS CORRUPTED??
            // HOW AND WHY THE METADATA IS CORRUPTED IS CURRENTLY UNKNOWN
            try {
                currFaqURL = currFaqMeta[5];
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                return "-999";
            }

            try {
                Log.w(TAG, "===============================================");
                Log.w(TAG, "READING FROM FILE " + getFileStreamPath(FaqrApp.validFileName(currFaqURL)).getAbsolutePath());

                if (currFaqMeta.length == 8 && currFaqMeta[7].trim().equals("TYPE=IMAGE")) {

                    // IMAGE FAQ
                    return "4";

                } else if (currFaqMeta.length == 8 && currFaqMeta[7].trim().equals("TYPE=HTML")) {

                    // HTML FAQ
                    return "5";

                } else {

                    // ASCII FAQ

                    String filecontent = FaqrApp.readSavedData(openFileInput(FaqrApp.validFileName(currFaq)));
                    if (!TextUtils.isEmpty(filecontent)) {
                        lines = FaqrApp.getLinesFile(filecontent);
                        origLines = new String[lines.length];
                        System.arraycopy(lines, 0, origLines, 0, lines.length);

                        // int position = listView.getFirstVisiblePosition() + 1;

                        String savedPosMulti = prefs.getString(FaqrApp.validFileName(currFaqMeta[5]) + "multi_saved_pos", "");

                        savedLines = new ArrayList<String>();
                        if (!savedPosMulti.equals("")) {
                            String[] savedPosMultiList = savedPosMulti.split(",");

                            Integer[] ints = new Integer[savedPosMultiList.length];
                            for (int i = 0; i < savedPosMultiList.length; i++) {
                                ints[i] = Integer.valueOf(savedPosMultiList[i]);
                            }

                            Arrays.sort(ints);

                            for (int i = 0; i < ints.length; i++) {

                                // if (Integer.valueOf(ints[i]) > position && Integer.valueOf(ints[i]) > lastGotoPos) {
                                // lastGotoPos = Integer.valueOf(ints[i]);
                                // return new Integer(ints[i]).toString();
                                // }

                                // plus one so first position is not position 0 :-)
                                int plusOne = ints[i] + 1;
                                savedLineNumbers.add(new Integer(ints[i]).toString());

                                double percentage = (new Double(plusOne) / new Double(lines.length)) * 100.0;
                                DecimalFormat df = new DecimalFormat("#");
                                savedLinePercentages.add(df.format(percentage) + "%");

                                savedLines.add(lines[ints[i]]);

                            }

                            // if we didn't return then we wrap and start from beginning
                            // showToast = true;
                            // for (int i = 0; i < ints.length; i++) {
                            // lastGotoPos = 0;
                            // return new Integer(ints[i]).toString();
                            // }
                        }

                        success = true;
                    }
                    Log.w(TAG, "FILE LINES.SIZE " + lines.length);
                }
                Log.w(TAG, "===============================================");
            } catch (IOException ioe) {
                Log.e(TAG, ioe.getMessage());
            }

            return result;
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        protected void onPostExecute(String result) {

            int my_faqmarks_pos = prefs.getInt("my_faqmarks_pos", 0);
            listView.setSelection(my_faqmarks_pos);
            

//            String title = currFaqMeta[6].split("\\(")[0].trim();
//            String subtitle = currFaqMeta[6].split("\\)")[1].trim();
//            if (title.indexOf(currFaqMeta[0].split("\\(|<")[0].trim()) != -1) {
//                title = title.substring(0, title.indexOf(currFaqMeta[0].split("\\(|<")[0].trim())).trim();
//            }
//            if (subtitle.startsWith("Final Fantasy IV ")) {
//                subtitle = subtitle.replaceAll("Final Fantasy IV ", "");
//            }

//            getSupportActionBar().setTitle("My FAQmarks");
//            getSupportActionBar().setSubtitle(currFaqMeta[6]);
            
//            getSupportActionBar().setTitle("My FAQmarks");
//            getSupportActionBar().setSubtitle(currFaqMeta[6]);

            ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();

            // fancy animations
            listView.setVisibility(View.VISIBLE);
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

            // listView.setVisibility(View.VISIBLE);
            // loading.setVisibility(View.GONE);
        }

    };

    /**
     * Save the current position
     * 
     * @author eneve
     */
    private class DeleteAllTask extends AsyncTask<String, Void, String> {

        protected void onPreExecute() {
            // do nothing
        }

        @Override
        protected String doInBackground(String... strings) {

            // do nothing
            return "";
        }

        protected void onPostExecute(String result) {
            Toast.makeText(getApplicationContext(), "Deleted all saved FAQmarks.", Toast.LENGTH_SHORT).show();

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(FaqrApp.validFileName(currFaqMeta[5]) + "multi_saved_pos", "");
            editor.commit();

            editor.putInt("my_faqmarks_pos", 0);
            editor.commit();

            Intent intent = new Intent(getApplicationContext(), FaqActivity.class);
            startActivity(intent);
            finish();
        }
    };

}
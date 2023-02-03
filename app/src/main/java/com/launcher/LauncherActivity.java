package com.launcher;

import static android.content.Intent.ACTION_PACKAGE_ADDED;
import static android.content.Intent.ACTION_PACKAGE_CHANGED;
import static android.content.Intent.ACTION_PACKAGE_REMOVED;
import static android.content.Intent.ACTION_PACKAGE_REPLACED;
import static com.launcher.ext.ActivityKt.chooseLauncher;
import static com.launcher.ext.ActivityKt.isDefaultLauncher;
import static com.launcher.ext.ContextKt.openBrowserPolicy;
import static com.launcher.ext.ViewKt.click;
import static com.launcher.ext.ViewKt.getHeightOfView;
import static com.launcher.ext.ViewKt.playAnim;
import static com.launcher.utils.SpUtilsKt.KEY_READ_POLICY;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import com.BuildConfig;
import com.R;
import com.launcher.dialogs.PolicyDialog;
import com.launcher.dialogs.app.AppSettingsDialog;
import com.launcher.dialogs.launcher.GlobalSettingsDialog;
import com.launcher.dialogs.launcher.color.GlobalColorSizeDialog;
import com.launcher.dialogs.launcher.frozen.FrozenAppsDialogs;
import com.launcher.dialogs.launcher.hidden.HiddenAppsDialogs;
import com.launcher.dialogs.launcher.padding.PaddingDialog;
import com.launcher.model.Apps;
import com.launcher.model.Shortcut;
import com.launcher.utils.Constants;
import com.launcher.utils.CrashUtils;
import com.launcher.utils.DbUtils;
import com.launcher.utils.Gestures;
import com.launcher.utils.PinYinSearchUtils;
import com.launcher.utils.ShortcutUtils;
import com.launcher.utils.SpUtils;
import com.launcher.utils.Utils;
import com.launcher.views.flowLayout.FlowLayout;
import com.launcher.views.textview.AppTextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

public class LauncherActivity extends Activity implements View.OnClickListener, View.OnLongClickListener, Gestures.OnSwipeListener {

    //region Field declarations
    public static List<Apps> mAppsList;
    // home layout
    private static FlowLayout mHomeLayout;
    // when search bar appears this will be true and show search result
    private static boolean searching = false;
    //TODO: save this to db
    private static int recentlyUsedCounter = 0;
    // broadcast receiver
    private BroadcastReceiver broadcastReceiverAppInstall;
    private BroadcastReceiver broadcastReceiverShortcutInstall;
    private Typeface mTypeface;
    //multi dialogs
    private Dialog dialogs;
    //search box
    private EditText mSearchBox;
    private CardView cvSearch;

    private InputMethodManager imm;
    // gesture detector
    private Gestures detector;
    public ShortcutUtils shortcutUtils;

    private static final TextWatcher mTextWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            mSearchTask = new SearchTask();
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            mSearchTask.execute(charSequence);
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    };
    private static SearchTask mSearchTask;
    //endregion

    private static void showSearchResult(ArrayList<Apps> filteredApps) {
        mHomeLayout.removeAllViews();
        for (Apps apps : filteredApps) {
            mHomeLayout.addView(apps.getTextView(), new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        //pass touch event to detector
        detector.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // initialize the shared prefs may be done in application class
        DbUtils.init(this);
        shortcutUtils = new ShortcutUtils(this);

        if (BuildConfig.DEBUG) {
            new CrashUtils(getApplicationContext(), "");
        }

        int theme = DbUtils.getTheme();
        //theme must be set before setContentView
        setTheme(theme);

        setContentView(R.layout.a_launcher);

        // set the status bar color as per theme
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setNavigationAndStatusBarColor(theme);
        }
        // set the fonts
        setFont();

        mHomeLayout = findViewById(R.id.home_layout);
        mHomeLayout.setOnLongClickListener(this);

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        //our search box
        mSearchBox = findViewById(R.id.search_box);
        ImageView ivSettingGlobal = findViewById(R.id.iv_setting_global);
        click(ivSettingGlobal, this::showGlobalSettingsDialog);
        cvSearch = findViewById(R.id.cv_search);
        setSearchBoxListeners();

        //set alignment default is center|center_vertical
        mHomeLayout.setGravity(DbUtils.getFlowLayoutAlignment());

        //set padding ..
        mHomeLayout.setPadding(DbUtils.getPaddingLeft(), DbUtils.getPaddingTop(), DbUtils.getPaddingRight(), getPaddingBottomBaseOnSearchView());

        detector = new Gestures(this, this);

        // initGestures();

        // loads the apps
        loadApps();
        // register the receiver for installed, uninstall, update apps and shortcut pwa add
        registerForReceivers();

        mLocale = this.getResources().getConfiguration().locale;
    }

    private int getPaddingBottomBaseOnSearchView() {
        return DbUtils.getPaddingBottom() + getHeightOfView(cvSearch);
    }

    private void setSearchBoxListeners() {
        mSearchBox.addTextChangedListener(mTextWatcher);
        mSearchBox.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                mSearchBox.setText("");
                imm.hideSoftInputFromWindow(mSearchBox.getWindowToken(), 0);
                return true;
            }
            return false;
        });
    }

    /**
     * set the color of status bar and navigation bar as per theme
     * if theme color is light then pass this to system so status icon color will turn into black
     * <p>
     * theme current theme applied to launcher
     */

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void setNavigationAndStatusBarColor(int theme) {

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            // getWindow().setNavigationBarColor(getResources().getColor(android.R.color.transparent));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (theme == R.style.White) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            } else if (theme == R.style.WhiteOnGrey) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            } else if (theme == R.style.BlackOnGrey) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }
    }

    public void setFont() {
        // get and set fonts
        String fontsPath = DbUtils.getFonts();
        if (fontsPath == null) {
            mTypeface = Typeface.createFromAsset(getAssets(), "fonts/raleway_bold.ttf");
        } else {
            try {
                mTypeface = Typeface.createFromFile(fontsPath);
            } catch (Exception i) {
                mTypeface = Typeface.createFromAsset(getAssets(), "fonts/raleway_bold.ttf");
            }
        }
    }

    public void loadApps() {
        // get the apps installed on devices;
        final Intent startupIntent = new Intent(Intent.ACTION_MAIN, null);
        startupIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(startupIntent, 0);

        // check whether our app list is already initialized if yes then clear this (when new app or shortcut installed)
        if (mAppsList != null) {
            mAppsList.clear();
            mHomeLayout.removeAllViews();
            mHomeLayout.removeAllViewsInLayout();
        }

        // shortcut or pwa counts
        final int installedShortcut = shortcutUtils.getShortcutCounts();

        final int appsCount = activities.size();

        mAppsList = Collections.synchronizedList(new ArrayList<>(appsCount + installedShortcut));

        // get the most used apps
        // a list of app that are popular on f-droid and some of my apps
        List<String> oftenApps = Utils.getOftenAppsList();
        List<String> coloredAppsList = Utils.getColoredAppsList();

        String packageName, appName;
        int color, textSize;
        boolean hide;
        // iterate over each app and initialize app list
        for (ResolveInfo resolveInfo : activities) {

            packageName = resolveInfo.activityInfo.packageName;
            // activity name as com.example/com.example.MainActivity
            String activity = packageName + "/" + resolveInfo.activityInfo.name;
            /// save the app original name so that we can use this later e.g if user change
            /// the app name then we have the name in DB
            DbUtils.putAppOriginalName(activity, resolveInfo.loadLabel(pm).toString());
            // check whether user set the custom app name for eg. long name to small name
            appName = DbUtils.getAppName(activity, resolveInfo.loadLabel(pm).toString());
            // is app is hidden by user
            hide = DbUtils.isAppHidden(activity);
            // get the app text size
            textSize = DbUtils.getAppSize(activity);

            // check if text size is null then set the size to default size
            // size is null(-1) when user installed this app
            if (textSize == DbUtils.NULL_TEXT_SIZE) {
                if (oftenApps.contains(packageName)) {
                    textSize = Constants.DEFAULT_TEXT_SIZE_OFTEN_APPS;
                } else {
                    textSize = Constants.DEFAULT_TEXT_SIZE_NORMAL_APPS;
                }

                /// DbUtils.putAppSize(activity, textSize);
            }

            // get app color
            color = DbUtils.getAppColor(activity);

//            Drawable icon = null;
//            try {
//                icon = getPackageManager().getApplicationIcon(packageName);
//            } catch (PackageManager.NameNotFoundException e) {
//                e.printStackTrace();
//            }
//            color= Utils.getDominantColor(Utils.drawableToBitmap(icon));


            // check for default color : set default colors if random color is not set
            if (!DbUtils.isRandomColor() && color == DbUtils.NULL_TEXT_COLOR) {
                color = DbUtils.getAppsColorDefault();
                if (coloredAppsList.contains(packageName)) {
                    color = Utils.getColor();
                }

            }
            // whether app size is frozen
            boolean freeze = DbUtils.isAppFrozen(activity);

            // this is a separate implementation of ColorSniffer app
            // if User set the color from external app like ColorSniffer
            // then use that colors
            if (DbUtils.isRandomColor() && color == DbUtils.NULL_TEXT_COLOR) {
                color = Utils.generateColorFromString(appName);
            }

            int openingCounts = DbUtils.getOpeningCounts(activity);

            int updateTime;
            try {
                //ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
                long time = pm.getPackageInfo(packageName, 0).lastUpdateTime;
                time = time / 10000;
                updateTime = (int) time;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                updateTime = 0;
            }
            // save all and add this is to app list
            mAppsList.add(new Apps(packageName, false, activity, appName, getCustomView(), color, textSize, hide, freeze, openingCounts, updateTime));

        }

        // now adds Shortcut
        // shortcut are stored in DB, android doesn't store them

        ArrayList<Shortcut> shortcuts = shortcutUtils.getAllShortcuts();
        for (Shortcut s : shortcuts) {

            // shortcut only have URI
            String uri = s.getUri();
            // shortcut name
            String sName = s.getName();

            // this is the unique code for each uri
            // let store them in activity field app
            // As we have to store some uniquely identified info in Db
            // this be used as key as i have done for Each apps(see above)
            // Usually URI sting is too long and so it will take more memory and storage
            String sActivity = String.valueOf(Utils.hash(uri));

            // get color and size for this shortcut
            int sColor = DbUtils.getAppColor(sActivity);
            int sSize = DbUtils.getAppSize(sActivity);

            if (sSize == DbUtils.NULL_TEXT_SIZE) {
                sSize = Constants.DEFAULT_TEXT_SIZE_NORMAL_APPS;
            }

            if (sColor == DbUtils.NULL_TEXT_COLOR) {
                if (DbUtils.isRandomColor()) {
                    sColor = Utils.generateColorFromString(sName);
                } else {
                    sColor = DbUtils.getAppsColorDefault();
                }
            }

            boolean sFreeze = DbUtils.isAppFrozen(sActivity);
            int sOpeningCount = DbUtils.getOpeningCounts(sActivity);

            // add this shortcut to list
            // currently shortcut hide is disabled
            mAppsList.add(new Apps(null, true, uri, sName, getCustomView(), sColor, sSize, false, sFreeze, sOpeningCount, 0));
        }

        // now sort the app list
        // and display this
        sortApps(DbUtils.getSortsTypes());
    }

    /**
     * type sorting type
     */
    public void sortApps(final int type) {
        new SortTask().execute(type, DbUtils.getAppSortReverseOrder() ? 1 : 0);
    }

    // the text view and set the various parameters
    //TODO: new animated field for this (test randomly)
    private AppTextView getCustomView() {
        AppTextView textView = new AppTextView(this);
        textView.setBackgroundColor(Color.TRANSPARENT);
        textView.setOnClickListener(this);
        textView.setOnLongClickListener(this);
//        textView.setPadding(10, 0, 4, -2);
        textView.setPadding(16, 0, 16, 0);
        textView.setTypeface(mTypeface);
        return textView;
    }

    // app text is clicked
    // so launch the app
    @Override
    public void onClick(View view) {
        if (view instanceof AppTextView) {
            playAnim(view, () -> {
                String activity = (String) view.getTag();
                AppTextView appTextView = (AppTextView) view;

                if (searching) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mSearchBox.getWindowToken(), 0);
                }

                if (appTextView.isShortcut()) {
                    try {
                        Intent intent = Intent.parseUri(appTextView.getUri(), 0);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
//                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        appOpened(activity);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    //Notes to me:if view store package and component name then this could reduce this splits
                    String[] strings = activity.split("/");
                    try {
                        final Intent intent = new Intent(Intent.ACTION_MAIN, null);
                        intent.setClassName(strings[0], strings[1]);
                        intent.setComponent(new ComponentName(strings[0], strings[1]));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
//                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                        // tell the our db that app is opened
                        appOpened(activity);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 10f, 200);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (searching) {
            searching = false;
            imm.hideSoftInputFromWindow(mSearchBox.getWindowToken(), 0);
            mHomeLayout.setPadding(DbUtils.getPaddingLeft(), DbUtils.getPaddingTop(), DbUtils.getPaddingRight(), getPaddingBottomBaseOnSearchView());
            sortApps(DbUtils.getSortsTypes());
        }
        checkReadPolicy();
    }

    private void checkReadPolicy() {
        boolean isReadPolicy = SpUtils.Companion.getInstance().getBoolean(KEY_READ_POLICY, false);
        if (isReadPolicy) {
            if (!isDefaultLauncher(this)) {
                chooseLauncher(this, FakeLauncherActivity.class);
            }
        } else {
            PolicyDialog dialogs = new PolicyDialog(this, new PolicyDialog.OnClick() {
                @Override
                public void onYes() {
                    SpUtils.Companion.getInstance().putBoolean(KEY_READ_POLICY, true);
                    openBrowserPolicy(LauncherActivity.this);
                }

                @Override
                public void onNo() {
                    SpUtils.Companion.getInstance().putBoolean(KEY_READ_POLICY, false);
                }
            });
            dialogs.setCancelable(false);
            dialogs.setCanceledOnTouchOutside(false);
            dialogs.show();
            Window window = dialogs.getWindow();
            if (window != null) {
                window.setGravity(Gravity.CENTER);
                window.setBackgroundDrawableResource(android.R.color.transparent);
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (dialogs != null) {
            dialogs.dismiss();
            dialogs = null;
        }

        if (mSearchTask != null) {
            mSearchTask.cancel(true);
            mSearchTask = null;
        }
    }

    //show the option on long click
    @Override
    public boolean onLongClick(View view) {
        if (view instanceof AppTextView) {
            // show app setting
            dialogs = new AppSettingsDialog(this, this, (String) view.getTag(), (AppTextView) view);
            dialogs.show();

            Window window = dialogs.getWindow();
            if (window != null) {
                window.setGravity(Gravity.BOTTOM);
                window.setBackgroundDrawableResource(android.R.color.transparent);
                window.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            }
        } else if (view instanceof FlowLayout) {
            showGlobalSettingsDialog();
        }
        return true;
    }

    private void showGlobalSettingsDialog() {
        dialogs = new GlobalSettingsDialog(this, this);
        dialogs.show();

        Window window = dialogs.getWindow();
        if (window != null) {
            window.setGravity(Gravity.BOTTOM);
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        }
    }

    //  add a new app: generally called after reset
    public void addAppAfterReset(String activityName, boolean sortNeeded) {
        synchronized (mAppsList) {
            for (ListIterator<Apps> iterator = mAppsList.listIterator(); iterator.hasNext(); ) {
                Apps app = iterator.next();
                if (app.getActivityName() != null && app.getActivityName().equalsIgnoreCase(activityName)) {
                    iterator.remove();
                    //now add new App
                    int color;
                    if (DbUtils.isRandomColor()) {
                        color = Utils.generateColorFromString(activityName);
                    } else {
                        color = DbUtils.getAppsColorDefault();
                    }
                    String appOriginalName = DbUtils.getAppOriginalName(activityName, "");
                    String appName = DbUtils.getAppName(activityName, appOriginalName);
                    int openingCounts = DbUtils.getOpeningCounts(activityName);
                    boolean hide = app.isHidden();
                    boolean freezeSize = app.isSizeFrozen();
                    int appUpdateTime = app.getUpdateTime();
                    Apps newApp = new Apps(app.getPackageName(), app.isShortcut(), activityName, appName, getCustomView(), color, Constants.DEFAULT_TEXT_SIZE_NORMAL_APPS, hide, freezeSize, openingCounts, appUpdateTime);

                    //mAppsList.add(newApp);
                    iterator.add(newApp);
                    if (sortNeeded) sortApps(DbUtils.getSortsTypes());
                    break;
                }
            }
        }
    }

    // this is called by RenameInput.class Dialog when user set the name and sort the apps
    public void onAppRenamed(String activityName, String appNewName) {
        synchronized (mAppsList) {
            for (Apps app : mAppsList) {
                if (app.getActivityName() != null && app.getActivityName().equalsIgnoreCase(activityName)) {
                    app.setAppName(appNewName.trim());
                    if (Constants.SORT_BY_NAME == DbUtils.getSortsTypes()) {
                        sortApps(Constants.SORT_BY_NAME);
                    }
                    break;
                }
            }
        }
    }

    public Apps getApp(String activity) {
        Apps a = null;
        synchronized (mAppsList) {
            for (Apps apps : mAppsList) {
                if (apps.getActivityName() != null && apps.getActivityName().equalsIgnoreCase(activity)) {
                    a = apps;
                    break;
                }
            }
        }
        return a;
    }

    //TODO: multi thread check for memory leaks if any, or check any bad behaviour;
    private void appOpened(String activity) {
        synchronized (mAppsList) {
            for (Apps apps : mAppsList) {
                if (apps.getActivityName() != null && apps.getActivityName().equalsIgnoreCase(activity)) {
                    apps.increaseOpeningCounts();// save to Db that app is opened by user
                    recentlyUsedCounter++;
                    apps.setRecentUsedWeight(recentlyUsedCounter);

                    if (DbUtils.getSortsTypes() == Constants.SORT_BY_OPENING_COUNTS) {
                        int counter = apps.getOpeningCounts();
                        if (counter % 5 == 0) {
                            sortApps(Constants.SORT_BY_OPENING_COUNTS);
                        }
                    } else if (DbUtils.getSortsTypes() == Constants.SORT_BY_RECENT_OPEN) {
                        sortApps(Constants.SORT_BY_RECENT_OPEN);
                    }

                    // increase the app view size if not frozen
                    if (!DbUtils.isSizeFrozen() && !DbUtils.isAppFrozen(activity)) {
                        int size = DbUtils.getAppSize(activity);
                        size += 2;
                        if (size > 90) {
                            size = 90;
                        }
                        apps.setSize(size);
                        if (DbUtils.getSortsTypes() == Constants.SORT_BY_SIZE) {
                            sortApps(Constants.SORT_BY_SIZE);
                        }
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        toggleViewSearch();
//        if (searching) {
//            imm.hideSoftInputFromWindow(mSearchBox.getWindowToken(), 0);
//
//            searching = false;
//            if (mSearchTask != null) {
//                mSearchTask.cancel(true);
//                mSearchTask = null;
//            }
//            mHomeLayout.setPadding(DbUtils.getPaddingLeft(), DbUtils.getPaddingTop(), DbUtils.getPaddingRight(), getPaddingBottomBaseOnSearchView());
//            sortApps(DbUtils.getSortsTypes());
//        }
    }

    // register the receiver
    // when new app installed, app updated and app uninstalled launcher have to reflect it
    private void registerForReceivers() {
//        if (broadcastReceiverShortcutInstall != null){
//            unregisterReceiver(broadcastReceiverShortcutInstall);
//        }
//        if (broadcastReceiverAppInstall!=null){
//            unregisterReceiver(broadcastReceiverAppInstall);
//        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_PACKAGE_ADDED);
        intentFilter.addAction(ACTION_PACKAGE_CHANGED);
        intentFilter.addAction(ACTION_PACKAGE_REMOVED);
        intentFilter.addAction(ACTION_PACKAGE_REPLACED);
        intentFilter.addDataScheme("package");
        if (broadcastReceiverAppInstall == null) {
            broadcastReceiverAppInstall = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    loadApps();
                }
            };
            registerReceiver(broadcastReceiverAppInstall, intentFilter);
        }

        //shortcut install receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.android.launcher.action.INSTALL_SHORTCUT");
        filter.addAction("com.android.launcher.action.CREATE_SHORTCUT");

        if (broadcastReceiverShortcutInstall == null) {
            broadcastReceiverShortcutInstall = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Intent shortcutIntent = intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
                    String uri = shortcutIntent.toUri(0);
                    if (shortcutIntent.getAction() == null) {
                        shortcutIntent.setAction(Intent.ACTION_VIEW);

                    }
                    String name = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);

                    if (shortcutUtils.isShortcutToApp(uri)) {
                        return;
                    }

                    if (!shortcutUtils.isShortcutAlreadyAvailable(name)) {
                        addShortcut(uri, name);
                    }
                }
            };
            registerReceiver(broadcastReceiverShortcutInstall, filter);
        }

    }

    // unregister the receivers on destroy
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (dialogs != null) {
            dialogs.dismiss();
            dialogs = null;
        }

        if (mSearchTask != null) {
            mSearchTask.cancel(true);
            mSearchTask = null;
        }

        if (imm != null) {
            if (imm.isActive()) {
                imm.hideSoftInputFromWindow(mSearchBox.getWindowToken(), 0);
            }
            imm = null;
        }

        unregisterReceiver(broadcastReceiverAppInstall);
        unregisterReceiver(broadcastReceiverShortcutInstall);
        broadcastReceiverAppInstall = null;
        broadcastReceiverShortcutInstall = null;
        shortcutUtils.close();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) return;
        // restore request
        switch (requestCode) {

            case Constants.RESTORE_REQUEST:

                Uri uri = data.getData();
                ContentResolver cr = getContentResolver();
                try {
                    boolean b = DbUtils.loadDbFromFile(cr.openInputStream(uri));
                    if (b) {
                        recreate();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                break;

            case Constants.FONTS_REQUEST:

                try {

//               String[] projection = {MediaStore.Images.Media.DATA};
//                Cursor cursor = getContentResolver().query(data.getData(), projection, null, null, null);
//                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//                cursor.moveToFirst();
//                String path = cursor.getString(column_index);
//                cursor.close();

                    ///new
                    Uri uri1 = data.getData();
                    ContentResolver cr1 = getContentResolver();

                    File fontFile = new File(getFilesDir(), "font.ttf");
                    try (InputStream inputFontStream = cr1.openInputStream(uri1); OutputStream out = new FileOutputStream(fontFile)) {
                        byte[] buf = new byte[1024];
                        int len;
                        while ((len = inputFontStream.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }

                    String path = fontFile.getPath();
                    mTypeface = Typeface.createFromFile(path);
                    DbUtils.setFonts(path);
                    loadApps();
                } catch (Exception e) {
                    e.printStackTrace();
                    mTypeface = Typeface.createFromAsset(getAssets(), "fonts/raleway_bold.ttf");
                }
                break;

            case Constants.BACKUP_REQUEST:

                Uri uri2 = data.getData();
                ObjectOutputStream output = null;
                try {
                    ParcelFileDescriptor pfd = this.getContentResolver().openFileDescriptor(uri2, "w");
                    FileOutputStream fileOutputStream = new FileOutputStream(pfd.getFileDescriptor());
                    output = new ObjectOutputStream(fileOutputStream);
                    output.writeObject(DbUtils.getDBData());
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (output != null) {
                            output.flush();
                            output.close();
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                break;

            case Constants.COLOR_SNIFFER_REQUEST:
                colorSnifferCall(data.getBundleExtra("color_bundle"));

                break;
        }

    }

    //may be override of abstract class method to be called from color sniffer #3 types
    public void colorSnifferCall(Bundle bundle) {
        boolean defaultColorSet = false;// for change set
        final int DEFAULT_COLOR = bundle.getInt(Constants.DEFAULT_COLOR_FOR_APPS);//keys
        // not set by ColorSniffer
        if (DEFAULT_COLOR != DbUtils.NULL_TEXT_COLOR) { //NULL_TEXT_COLOR=-1
            defaultColorSet = true;// to save cpu cycle
        }

        // get each value as proposed by Color Sniffer App developer
        synchronized (mAppsList) {
            for (Apps apps : mAppsList) {
                TextView textView = apps.getTextView();
                String appPackage = apps.getActivityName();
                int color = bundle.getInt(appPackage);
                if (color != DbUtils.NULL_TEXT_COLOR) {
                    textView.setTextColor(color);
                    assert appPackage != null;
                    DbUtils.putAppColorExternalSource(appPackage, color);
                    // DbUtils.putAppColor(appPackage, color);
                } else if (defaultColorSet) {
                    //set default color
                    assert appPackage != null;
                    DbUtils.putAppColor(appPackage, DEFAULT_COLOR);
                    textView.setTextColor(DEFAULT_COLOR);
                }//else do nothing theme default color will apply
            }
        }
    }

//    private void setAppsColorFromClipboard(Map<String, Integer> colorsAndId) {
//        if (colorsAndId == null) return;
//        DbUtils.externalSourceColor(true);
//        synchronized (mAppsList) {
//            for (Apps apps : mAppsList) {
//                try {
//                    TextView textView = apps.getTextView();
//                    String s = apps.getActivityName();
//                    Integer newColor = colorsAndId.get(s);
//                    if (newColor == null) continue;
//                    textView.setTextColor(newColor);
//                    if (s != null) {
//                        DbUtils.putAppColorExternalSource(s, newColor);
//                    }
//                } catch (NullPointerException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }

    // show the hidden app dialog
    public void showHiddenApps() {
        HiddenAppsDialogs dialogs = new HiddenAppsDialogs(this, mAppsList);
        if (dialogs.updateHiddenList() != 0) {
            dialogs.show();

            Window window = dialogs.getWindow();
            if (window != null) {
                window.setGravity(Gravity.BOTTOM);
                window.setBackgroundDrawableResource(android.R.color.transparent);
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        } else {
            Toast.makeText(this, "No apps to show", Toast.LENGTH_SHORT).show();
        }
    }

    // show the frozen app dialog
    public void showFrozenApps() {
        FrozenAppsDialogs dialogs = new FrozenAppsDialogs(this, mAppsList);
        if (dialogs.updateFrozenList() != 0) {
            dialogs.show();

            Window window = dialogs.getWindow();
            if (window != null) {
                window.setGravity(Gravity.BOTTOM);
                window.setBackgroundDrawableResource(android.R.color.transparent);
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        } else {
            Toast.makeText(this, "No apps to show", Toast.LENGTH_SHORT).show();
        }
    }

    //set the flow layout alignment it is called from global settings
    public void setFlowLayoutAlignment(int gravity) {
        mHomeLayout.setGravity(gravity);
        DbUtils.setFlowLayoutAlignment(gravity);
    }

    public void setPadding() {
        dialogs = new PaddingDialog(this, mHomeLayout);
        dialogs.show();
        Window window = dialogs.getWindow();
        if (window != null) {
            window.setGravity(Gravity.BOTTOM);
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        }
    }

    public void setColorsAndSize() {
        dialogs = new GlobalColorSizeDialog(this, mAppsList);
        dialogs.show();
        Window window = dialogs.getWindow();
        if (window != null) {
            window.setGravity(Gravity.BOTTOM);
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        }
    }

    private void addShortcut(String uri, String appName) {
        if (mAppsList == null) return;
        mAppsList.add(new Apps(null, true, uri, appName, getCustomView(), DbUtils.NULL_TEXT_COLOR, Constants.DEFAULT_TEXT_SIZE_NORMAL_APPS, false, false, 0, (int) System.currentTimeMillis() / 1000));
        shortcutUtils.addShortcut(new Shortcut(appName, uri));
        // Log.d(TAG, "addShortcut: shortcut name==" + appName);
        sortApps(DbUtils.getSortsTypes());
    }

    private final Handler handlerOnSwipe = new Handler(Looper.myLooper());

    @Override
    public void onSwipe(Gestures.Direction direction) {
        if (direction == Gestures.Direction.SWIPE_RIGHT || direction == Gestures.Direction.SWIPE_LEFT) {
            handlerOnSwipe.removeCallbacksAndMessages(null);
            handlerOnSwipe.postDelayed(this::toggleViewSearch, 100);
        } else if (direction == Gestures.Direction.SWIPE_UP) {
            if (cvSearch.getVisibility() != View.GONE) {
                cvSearch.setVisibility(View.GONE);
            }
        } else if (direction == Gestures.Direction.SWIPE_DOWN) {
            if (cvSearch.getVisibility() != View.VISIBLE) {
                cvSearch.setVisibility(View.VISIBLE);
            }
        }
    }

    private void toggleViewSearch() {
        if (searching) {
            searching = false;
            mSearchBox.clearFocus();
            imm.hideSoftInputFromWindow(mSearchBox.getWindowToken(), 0);
            onResume();
        } else {
            searching = true;
            mSearchBox.requestFocus();
            imm.showSoftInput(mSearchBox, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    @Override
    public void onDoubleTap() {

    }

    private static Locale mLocale;

    static class SearchTask extends AsyncTask<CharSequence, Void, ArrayList<Apps>> {
        @Override
        protected void onPostExecute(ArrayList<Apps> filteredApps) {
            super.onPostExecute(filteredApps);
            showSearchResult(filteredApps);
        }

        @Override
        protected ArrayList<Apps> doInBackground(CharSequence... charSequences) {
            ArrayList<Apps> filteredApps = new ArrayList<>();
            synchronized (mAppsList) {
                CharSequence s = charSequences[0];
                for (Apps app : mAppsList) {
                    if (s.length() == 0) {
                        filteredApps.add(app);
                    } else if (Utils.simpleFuzzySearch(s, app.getAppName())) {
                        filteredApps.add(app);
                    } else {
                        // Support for searching non-ascii languages Apps using ascii characters.
                        boolean isMatch = false;
                        if ("zh".equals(mLocale.getLanguage())) {// In case of Chinese, PinYin Search is supported.
                            isMatch = PinYinSearchUtils.pinYinSimpleFuzzySearch(s, app.getAppName());
                        }
                        if (isMatch) filteredApps.add(app);
                    }
                }
            }
            return filteredApps;
        }
    }

    static class SortTask extends AsyncTask<Integer, Void, Void> {

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            mHomeLayout.removeAllViews();
            mHomeLayout.removeAllViewsInLayout();

            synchronized (mAppsList) {
                for (Apps app : mAppsList) {
                    AppTextView textView = app.getTextView();
                    if (textView.getParent() != null) {
                        ((ViewGroup) textView.getParent()).removeView(textView);
                    }
                    mHomeLayout.addView(textView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
                }
            }
        }

        /**
         * So this is where you sort your apps.
         * We modified this method so that when the first sorting condition fails, it can sort by the frequency of use, which makes it easier for users to find the app they want to use.
         * <p>
         * integers
         */
        @Override
        protected Void doInBackground(final Integer... integers) {
            final int type = integers[0];
            final boolean reverseOrder = integers[1] == 1;
            DbUtils.setAppsSortsType(type);

            synchronized (mAppsList) {
                //sort the apps alphabetically
                Collections.sort(mAppsList, (a, b) -> String.CASE_INSENSITIVE_ORDER.compare(a.getAppName(), b.getAppName()));

                switch (type) {
                    case Constants.SORT_BY_SIZE://descending
                        Collections.sort(mAppsList, (apps, t1) -> {
                            if (apps.getSize() != t1.getSize()) {
                                return t1.getSize() - apps.getSize();
                            } else {
                                return -t1.getRecentUsedWeight() + apps.getRecentUsedWeight();
                            }
                        });
                        break;
                    case Constants.SORT_BY_OPENING_COUNTS://descending
                        Collections.sort(mAppsList, (apps, t1) -> {
                            if (t1.getOpeningCounts() != apps.getOpeningCounts()) {
                                return t1.getOpeningCounts() - apps.getOpeningCounts();
                            } else {
                                return -t1.getRecentUsedWeight() + apps.getRecentUsedWeight();
                            }
                        });
                        break;
                    case Constants.SORT_BY_COLOR:
                        Collections.sort(mAppsList, (apps, t1) -> {
                            float[] hsv = new float[3];
                            Color.colorToHSV(apps.getColor(), hsv);
                            float[] another = new float[3];
                            Color.colorToHSV(t1.getColor(), another);
                            for (int i = 0; i < 3; i++) {
                                if (hsv[i] != another[i]) {
                                    return (hsv[i] < another[i]) ? -1 : 1;
                                }
                            }
                            return -t1.getRecentUsedWeight() + apps.getRecentUsedWeight();
                        });
                        break;
                    case Constants.SORT_BY_UPDATE_TIME://descending
                        Collections.sort(mAppsList, (apps, t1) -> {
                            if (t1.getUpdateTime() != apps.getUpdateTime()) {
                                return t1.getUpdateTime() - apps.getUpdateTime();
                            } else {
                                return -t1.getRecentUsedWeight() + apps.getRecentUsedWeight();
                            }
                        });
                        break;
                    case Constants.SORT_BY_RECENT_OPEN://descending
                        Collections.sort(mAppsList, (apps, t1) -> (t1.getRecentUsedWeight() - apps.getRecentUsedWeight()));
                        break;
                }

                if (reverseOrder) {
                    Collections.reverse(mAppsList);
                }
            }
            return null;
        }
    }
}

package be.virtualsushi.gaz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;
import be.virtualsushi.gaz.backend.DrawerAdapter;
import be.virtualsushi.gaz.backend.EventBusProvider;
import be.virtualsushi.gaz.backend.ImageManager;
import be.virtualsushi.gaz.backend.ImageManagerProvider;
import be.virtualsushi.gaz.backend.PublicKeyRequest;
import be.virtualsushi.gaz.backend.RequestQueueProvider;
import be.virtualsushi.gaz.backend.ShareActionProvider;
import be.virtualsushi.gaz.backend.Tick5Request;
import be.virtualsushi.gaz.elite.CustomTypefaceSpan;
import be.virtualsushi.gaz.fragments.AboutFragment;
import be.virtualsushi.gaz.fragments.PagerFragment;
import be.virtualsushi.gaz.fragments.SettingsFragment;
import be.virtualsushi.gaz.fragments.TickFragment.TickFragmentListener;
import be.virtualsushi.gaz.model.DrawerListItem;
import be.virtualsushi.gaz.model.Tick;
import be.virtualsushi.gaz.model.Tick5Response;
import be.virtualsushi.gaz.model.Tick5Response.ResponseStatuses;
import be.virtualsushi.gaz.elite.EliteTypefaceProvider;

import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;

import de.greenrobot.event.EventBus;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class MainActivity extends ActionBarActivity implements EliteTypefaceProvider, RequestQueueProvider, ErrorListener, ImageManagerProvider, TickFragmentListener, OnPageChangeListener,  OnItemClickListener, EventBusProvider {

	public static final String FILTER_NAME_EXTRA = "filter_name";
	public static final String TICKS_EXTRA = "ticks";
	public static final String LATEST_POSITION_EXTRA = "latest_position";
	public static final String LATEST_KEY_EXTRA = "latest_key";
	public static final String SCREEN_INDEX_EXTRA = "screen_index";

	private static final int LATEST_TICKS_INDEX = 0;
	private static final int OLDER_TICKS_INDEX = 1;
	private static final int ABOUT_SCREEN_INDEX = 2;

	private String mLatestKey;

	private Tick[] mTweets;
	private int mLatestPosition = -1;
	private int mScreenIndex;

	private ShareActionProvider mShareActionProvider;

	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_main);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);


		SpannableStringBuilder ssb = new SpannableStringBuilder(getString(R.string.app_name));
		ssb.setSpan(new CustomTypefaceSpan("", getEliteTypeface()), 0, 4, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
		actionBar.setTitle(ssb);


		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		mDrawerList.setAdapter(new DrawerAdapter(this, new DrawerListItem[]{new DrawerListItem(R.drawable.ic_news, R.string.latest), new DrawerListItem(R.drawable.ic_menu_older, R.string.older)
				, new DrawerListItem(R.drawable.ic_menu_about, R.string.about)}, this));
		mDrawerList.setOnItemClickListener(this);

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close);
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(TICKS_EXTRA)) {
				mTweets = Tick.fromJsonStringsArray(savedInstanceState.getStringArray(TICKS_EXTRA));
			}
			if (savedInstanceState.containsKey(LATEST_POSITION_EXTRA)) {
				mLatestPosition = savedInstanceState.getInt(LATEST_POSITION_EXTRA);
			}
			if (savedInstanceState.containsKey(LATEST_KEY_EXTRA)) {
				mLatestKey = savedInstanceState.getString(LATEST_KEY_EXTRA);
			}
			mScreenIndex = savedInstanceState.getInt(SCREEN_INDEX_EXTRA, 0);
		}
		if (mTweets == null) {
			String jsonString = getTick5Preferences().getString(Tick5Application.SAVED_TWEETS_PREFERENCE, null);
			if (jsonString != null) {
				mTweets = Tick.jsonToArray(jsonString);
			}
		}

		if (mLatestKey == null) {
			mLatestKey = getTick5Preferences().getString(Tick5Application.SAVED_KEY_PREFERENCE, null);
		}

		selectItem(mScreenIndex);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mTweets != null && mTweets.length > 0) {
			outState.putStringArray(TICKS_EXTRA, Tick.toJsonStringsArray(mTweets));
			outState.putInt(LATEST_POSITION_EXTRA, mLatestPosition);
			outState.putString(LATEST_KEY_EXTRA, mLatestKey);
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onResume() {
		super.onResume();
		//updateData(true);
	}

	@Override
	protected void onPause() {
		super.onPause();
		Editor editor = getTick5Preferences().edit();
		editor.putString(Tick5Application.SAVED_TWEETS_PREFERENCE, Tick.arrayToJson(mTweets));
		editor.putString(Tick5Application.SAVED_KEY_PREFERENCE, mLatestKey);
		editor.commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (mScreenIndex == LATEST_TICKS_INDEX || mScreenIndex == OLDER_TICKS_INDEX) {
			getMenuInflater().inflate(R.menu.menu_main, menu);
			mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menu.findItem(R.id.menu_share));
			updateShareIntent();
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		switch (item.getItemId()) {
			case R.id.menu_refresh:
				updateData(true);
				break;
		}
		return true;
	}

	/*@Override
	public void onBackPressed() {
		if (mScreenIndex != LATEST_TICKS_INDEX) {
			selectItem(LATEST_TICKS_INDEX);
		} else {
			super.onBackPressed();
		}
	}*/

	private void updateData(boolean fromServer) {
		setSupportProgressBarIndeterminateVisibility(true);
		if (fromServer) {
			getRequestQueue().add(new PublicKeyRequest(new Listener<String>() {

				@Override
				public void onResponse(String response) {
					setSupportProgressBarIndeterminateVisibility(false);
					Log.d("Tick5", "Key:" + response);
					//if (mLatestKey == null || !response.equals(mLatestKey) || mTweets == null) {
						mLatestKey = response;
						loadTicks(mLatestKey);
					//}
				}
			}, this));
		} else {
			setSupportProgressBarIndeterminateVisibility(false);
			loadTicks(mLatestKey);
		}
	}

	private Tick5Application getTick5Application() {
		return (Tick5Application) getApplication();
	}

	@Override
	public RequestQueue getRequestQueue() {
		return getTick5Application().getRequestQueue();
	}

	@Override
	public Typeface getEliteTypeface() {
		return getTick5Application().getEliteTypeface();
	}

	@Override
	public void onErrorResponse(VolleyError error) {
		Toast.makeText(this, R.string.load_error, Toast.LENGTH_LONG).show();
	}

	private void loadTicks(String key) {
		Log.d("Tick5", "Loading ticks for key: " + key);
		getRequestQueue().add(new Tick5Request(key, new Listener<Tick5Response>() {

			@Override
			public void onResponse(Tick5Response response) {
				if (ResponseStatuses.OK.equals(response.status)) {
					mTweets = response.tweets;
					updateTicksScreen(false);
					updateShareIntent();
				}
			}

		}, this));
	}

	@Override
	public ImageManager getImageManager() {
		return getTick5Application().getImageManager();
	}

	private SharedPreferences getTick5Preferences() {
		return getSharedPreferences(Tick5Application.TICK5_PREFERENCES, MODE_PRIVATE);
	}

	@Override
	public void onFilterChange() {
		updateTicksScreen(true);
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	@Override
	public void onPageSelected(int pageNum) {
		mLatestPosition = pageNum;
		updateShareIntent();
	}

	private void updateShareIntent() {
		if (mShareActionProvider == null) {
			return;
		}
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		if (mTweets != null) {
			Tick tweet;
			if (mLatestPosition >= 0) {
				tweet = mTweets[mLatestPosition % mTweets.length];
			} else {
				tweet = mTweets[0];
			}
			intent.putExtra(Intent.EXTRA_TEXT, tweet.tweet + getString(R.string.share_tweet_postfix, tweet.author));
		}
		mShareActionProvider.setShareIntent(intent);
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
		selectItem(position);
	}

	private void selectItem(int position) {
		mScreenIndex = position;
		Fragment fragment = null;
		switch (position) {
			case LATEST_TICKS_INDEX:
				updateData(true);
				break;
			case OLDER_TICKS_INDEX:
				getOlderKey();
				mTweets = null;
				updateTicksScreen(true);
				break;
			case ABOUT_SCREEN_INDEX:
				fragment = new AboutFragment();
				break;
		}
		if (fragment != null) {
			replaceContentFragment(fragment);
		}
		supportInvalidateOptionsMenu();
		mDrawerList.setItemChecked(position, true);
		mDrawerLayout.closeDrawer(mDrawerList);
	}

	private void getOlderKey() {
		//var previousKey = "2014_01_20_04_Q4";
		if (mLatestKey.indexOf("Q4") > 0) mLatestKey = mLatestKey.substring(0, 15) + "3";
		else if (mLatestKey.indexOf("Q3") > 0) mLatestKey = mLatestKey.substring(0, 15) + "2";
		else if (mLatestKey.indexOf("Q2") > 0) mLatestKey = mLatestKey.substring(0, 15) + "1";
		else if (mLatestKey.indexOf("Q1") > 0) {
			int hour = Integer.parseInt(mLatestKey.substring(11, 13));
			if (hour > 0 && hour < 11) mLatestKey = mLatestKey.substring(0, 11) + "0" + (hour - 1) + "_Q4";
			else if (hour > 10) mLatestKey = mLatestKey.substring(0, 11) + (hour - 1) + "_Q4";
			else {
				Calendar today = new GregorianCalendar();
				today.roll(Calendar.DAY_OF_YEAR, false);
				DateFormat sdf = new SimpleDateFormat("yyyy_mm_dd_");
				mLatestKey = sdf.format(today) + "23_Q4";
			}
		}
	}

	private void updateTicksScreen(boolean presetCurrentPosition) {
		if (mScreenIndex == ABOUT_SCREEN_INDEX) {
			return;
		}
		if (mTweets != null) {
			replaceContentFragment(presetCurrentPosition ? PagerFragment.getInstance(mTweets, mLatestPosition) : PagerFragment.getInstance(mTweets));
		}
		else {
			if (mScreenIndex == LATEST_TICKS_INDEX)
				updateData(true);
			else
				updateData(false);
		}
	}

	private void replaceContentFragment(Fragment fragment) {
		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
	}

	@Override
	public EventBus getEventBus() {
		return getTick5Application().getEventBus();
	}

}

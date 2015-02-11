package be.virtualsushi.gaz;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;
import android.widget.TextView;
import be.virtualsushi.gaz.backend.FiltersAdapter;
import be.virtualsushi.gaz.elite.EliteTypefaceProvider;

public class SettingsActivity extends ActionBarActivity implements EliteTypefaceProvider, OnItemSelectedListener {

	private TextView mTitle;
	private Spinner mSelector;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_settings);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setDisplayUseLogoEnabled(false);

		mTitle = (TextView) findViewById(R.id.title);
		mTitle.setTypeface(getEliteTypeface());

		mSelector = (Spinner) findViewById(R.id.selector);
		FiltersAdapter adapter = new FiltersAdapter(this);
		mSelector.setAdapter(adapter);
		mSelector.setOnItemSelectedListener(this);

		Bundle extras = getIntent().getExtras();
		if (extras != null && extras.containsKey(MainActivity.FILTER_NAME_EXTRA)) {
			String filterName = extras.getString(MainActivity.FILTER_NAME_EXTRA);

			for (int i = 0; i < adapter.getCount(); i++) {
				if (filterName.equals(adapter.getItem(i))) {
					mSelector.setSelection(i);
					break;
				}
			}
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_settings, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (R.id.menu_accept == item.getItemId() || android.R.id.home == item.getItemId()) {
			finish();
		}
		return true;
	}

	private Tick5Application getTick5Application() {
		return (Tick5Application) getApplication();
	}

	@Override
	public Typeface getEliteTypeface() {
		return getTick5Application().getEliteTypeface();
	}

	@Override
	public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
		setResult(RESULT_OK, new Intent().putExtra(MainActivity.FILTER_NAME_EXTRA, (String) adapterView.getItemAtPosition(position)));
	}

	@Override
	public void onNothingSelected(AdapterView<?> adapterView) {
		setResult(RESULT_CANCELED);
	}

}

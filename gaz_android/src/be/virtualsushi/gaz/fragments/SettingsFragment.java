package be.virtualsushi.gaz.fragments;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;
import android.widget.TextView;
import be.virtualsushi.gaz.MainActivity;
import be.virtualsushi.gaz.R;
import be.virtualsushi.gaz.Tick5Application;
import be.virtualsushi.gaz.backend.FiltersAdapter;
import be.virtualsushi.gaz.elite.EliteTypefaceProvider;

public class SettingsFragment extends Fragment implements EliteTypefaceProvider {

	private static final String FILTER_NAME_ARGUMENT = "filter_name";

	public static SettingsFragment newInstance(String filterName) {
		SettingsFragment fragment = new SettingsFragment();
		Bundle arguments = new Bundle();
		arguments.putString(FILTER_NAME_ARGUMENT, filterName);
		fragment.setArguments(arguments);
		return fragment;
	}

	private TextView mTitle;
	private Spinner mSelector;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View result = inflater.inflate(R.layout.activity_settings, container, false);

		mTitle = (TextView) result.findViewById(R.id.title);
		mTitle.setTypeface(getEliteTypeface());

		mSelector = (Spinner) result.findViewById(R.id.selector);
		FiltersAdapter adapter = new FiltersAdapter(getActivity());
		mSelector.setAdapter(adapter);

		Bundle arguments = getArguments();
		if (arguments != null && arguments.containsKey(MainActivity.FILTER_NAME_EXTRA)) {
			String filterName = arguments.getString(MainActivity.FILTER_NAME_EXTRA);

			for (int i = 0; i < adapter.getCount(); i++) {
				if (filterName.equals(adapter.getItem(i))) {
					mSelector.setSelection(i);
					break;
				}
			}
		}
		return result;
	}

	private Tick5Application getTick5Application() {
		return (Tick5Application) getActivity().getApplication();
	}

	@Override
	public Typeface getEliteTypeface() {
		return getTick5Application().getEliteTypeface();
	}

}

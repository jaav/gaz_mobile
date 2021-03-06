package be.virtualsushi.gaz.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import be.virtualsushi.gaz.R;
import be.virtualsushi.gaz.backend.TicksAdapter;
import be.virtualsushi.gaz.model.Tick;

public class PagerFragment extends Fragment {

	private static final String TICKS_ARGUMENT = "ticks";
	private static final String POSITION_ARGUMENT = "position";

	public static PagerFragment getInstance(Tick[] ticks) {
		PagerFragment fragment = new PagerFragment();
		Bundle arguments = new Bundle();
		arguments.putParcelableArray(TICKS_ARGUMENT, ticks);
		fragment.setArguments(arguments);
		return fragment;
	}

	public static PagerFragment getInstance(Tick[] ticks, int position) {
		PagerFragment fragment = getInstance(ticks);
		fragment.getArguments().putInt(POSITION_ARGUMENT, position);
		return fragment;
	}

	private ViewPager mPager;
	private OnPageChangeListener mOnPageChangeListener;

	private Tick[] mTicks;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mOnPageChangeListener = FragmentUtils.tryActivityCast(getActivity(), OnPageChangeListener.class, true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View result = inflater.inflate(R.layout.fragment_pager, container, false);

		mPager = (ViewPager) result.findViewById(R.id.pager);
		Parcelable[] parcelables = getArguments().getParcelableArray(TICKS_ARGUMENT);
		mTicks = new Tick[parcelables.length];
		int i = 0;
		for (Parcelable parcelable : parcelables) {
			mTicks[i] = (Tick) parcelable;
			i++;
		}
		mPager.setAdapter(new TicksAdapter(getChildFragmentManager(), mTicks));
		if (mOnPageChangeListener != null) {
			mPager.setOnPageChangeListener(mOnPageChangeListener);
		}

		return result;
	}

	@Override
	public void onResume() {
		super.onResume();
		Bundle arguments = getArguments();
		if (arguments.containsKey(POSITION_ARGUMENT)) {
			mPager.setCurrentItem(arguments.getInt(POSITION_ARGUMENT));
		} else {
			mPager.setCurrentItem(mTicks.length * TicksAdapter.LOOPS_COUNT / 2);
		}
	}

}

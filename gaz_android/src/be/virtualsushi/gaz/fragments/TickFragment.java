package be.virtualsushi.gaz.fragments;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import be.virtualsushi.gaz.R;
import be.virtualsushi.gaz.backend.ImageManager;
import be.virtualsushi.gaz.backend.ImageManagerProvider;
import be.virtualsushi.gaz.elite.EliteTypefaceProvider;
import be.virtualsushi.gaz.gestures.SwipeTouchDetector;
import be.virtualsushi.gaz.gestures.SwipeTouchDetector.SwipeDirection;
import be.virtualsushi.gaz.gestures.SwipeTouchDetector.SwipeListener;
import be.virtualsushi.gaz.model.Tick;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TickFragment extends Fragment{

	public interface TickFragmentListener {

		void onFilterChange();

	}

	public static final String TICK_ARGUMENT_NAME = "tick";

	private static final String TWITTER_HASHTAG_URL_PATTERN = "<a href=\"https://twitter.com/search?q=%23%s\">%s</a>";
	private static final String TWITTER_AUTHOR_URL_PATTERN = "<a href=\"https://twitter.com/#!/%s\">@%s</a>";

	private TextView mTweet;
	private LinearLayout mLinksContainer;
	private TextView mAuthor;
	private ImageView mImage;
	private RelativeLayout mContainer;

	private EliteTypefaceProvider mEliteTypefaceProvider;
	private ImageManager mImageManager;
	private TickFragmentListener mListener;

	private int mDummyImageResource;

	private Tick mTick;

	public static TickFragment getIntance(Tick tick) {
		TickFragment fragment = new TickFragment();
		Bundle arguments = new Bundle();
		arguments.putParcelable(TICK_ARGUMENT_NAME, tick);
		fragment.setArguments(arguments);
		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mEliteTypefaceProvider = FragmentUtils.tryActivityCast(getActivity(), EliteTypefaceProvider.class, false);
		mImageManager = FragmentUtils.tryActivityCast(getActivity(), ImageManagerProvider.class, false).getImageManager();
		mListener = FragmentUtils.tryActivityCast(getActivity(), TickFragmentListener.class, false);
		Resources resources = getResources();

		mDummyImageResource = resources.getIdentifier("no_image_squared", "drawable", activity.getPackageName());
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mImageManager = null;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View result = inflater.inflate(R.layout.fragment_tick, container, false);

		mTick = getArguments().getParcelable(TICK_ARGUMENT_NAME);

		mTweet = (TextView) result.findViewById(R.id.tweet);
		mTweet.setMovementMethod(LinkMovementMethod.getInstance());
		mTweet.setTypeface(mEliteTypefaceProvider.getEliteTypeface());
		String formatterTweet = mTick.tweet;
		if (mTick.hashtags != null) {
			for (String hashTag : mTick.hashtags) {
				String fullHashTagString = "#" + hashTag;
				formatterTweet = formatterTweet.replace(fullHashTagString, String.format(TWITTER_HASHTAG_URL_PATTERN, hashTag, fullHashTagString));
			}
		}
		Spanned formated = Html.fromHtml(formatterTweet);
		String test = formated.toString();
		mTweet.setText(formated);

		mAuthor = (TextView) result.findViewById(R.id.author);
		mAuthor.setText("@"+mTick.author);
		//mAuthor.setMovementMethod(LinkMovementMethodFABOUT.getInstance());
		mAuthor.setTypeface(mEliteTypefaceProvider.getEliteTypeface());

		Linkify.TransformFilter filter = new Linkify.TransformFilter() {
		    public final String transformUrl(final Matcher match, String url) {
		        return match.group();
		    }
		};

		Pattern mentionPattern = Pattern.compile("@([A-Za-z0-9_-]+)");
		String mentionScheme = "http://www.twitter.com/";
		Linkify.addLinks(mAuthor, mentionPattern, mentionScheme, null, filter);


		//mAuthor.setAutoLinkMask(Linkify.ALL);
		//mAuthor.setText(Html.fromHtml(String.format(TWITTER_AUTHOR_URL_PATTERN, mTick.author, mTick.author)));

		mImage = (ImageView) result.findViewById(R.id.image);
		mImage.setImageResource(mDummyImageResource);

		mContainer = (RelativeLayout) result.findViewById(R.id.container);

		mLinksContainer = (LinearLayout) result.findViewById(R.id.links_container);
		if (mTick.urls != null && mTick.urls.length > 0) {
			mLinksContainer.setVisibility(View.VISIBLE);
			for (int i = 0; i < mTick.urls.length; i++) {
				if(mTick.urls[i] != null){
					TextView linkView = (TextView) inflater.inflate(R.layout.view_url_text, null, false);
					linkView.setMovementMethod(LinkMovementMethod.getInstance());
					linkView.setText(Html.fromHtml(mTick.urls[i]));
					linkView.setTypeface(mEliteTypefaceProvider.getEliteTypeface());
					linkView.setTextSize(18.5f);
					mLinksContainer.addView(linkView);
				}
			}
		}

		loadImage(mTick);

		return result;
	}

	private void loadImage(Tick tick) {
		Bitmap cachedBitmap = mImageManager.loadFromCache(tick.image);
		if (cachedBitmap != null) {
			mImage.setImageBitmap(cachedBitmap);
		} else {
			mImageManager.loadImage(tick.image, new ImageListener() {

				@Override
				public void onErrorResponse(VolleyError error) {
					if (isVisible()) {
						mImage.setImageResource(mDummyImageResource);
					}
				}

				@Override
				public void onResponse(ImageContainer response, boolean isImmediate) {
					mImage.setImageBitmap(response.getBitmap());
				}
			}, mImage.getWidth(), mImage.getHeight());
		}
	}
}

package be.virtualsushi.gaz;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.annotation.SuppressLint;
import android.app.Application;
import android.graphics.Typeface;
import android.util.SparseArray;
import be.virtualsushi.gaz.backend.EventBusProvider;
import be.virtualsushi.gaz.backend.ImageManager;
import be.virtualsushi.gaz.backend.ImageManagerProvider;
import be.virtualsushi.gaz.backend.RequestQueueProvider;
import be.virtualsushi.gaz.elite.EliteTypefaceProvider;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import de.greenrobot.event.EventBus;

@ReportsCrashes(formKey = "", mode = ReportingInteractionMode.DIALOG, mailTo = "pavel@pavel.st", resDialogText = R.string.crash_dialog_text, resDialogIcon = android.R.drawable.ic_dialog_info, resDialogTitle = R.string.crash_dialog_title, resDialogCommentPrompt = R.string.crash_dialog_comment_prompt, resDialogOkToast = R.string.crash_dialog_ok_toast, customReportContent = {
		ReportField.USER_COMMENT, ReportField.ANDROID_VERSION, ReportField.PACKAGE_NAME, ReportField.STACK_TRACE, ReportField.APP_VERSION_NAME })
public class Tick5Application extends Application implements EliteTypefaceProvider, RequestQueueProvider, ImageManagerProvider, EventBusProvider {

	public static final String TICK5_PREFERENCES = "tick5_preferences";
	//public static final String DEFAULT_FILTER_NAME_PREFERENCE = "default_filter";
	public static final String SAVED_TWEETS_PREFERENCE = "tweets";
	public static final String SAVED_KEY_PREFERENCE = "key";


	private Typeface mEliteTypeface;
	private RequestQueue mRequestQueue;
	private ImageManager mImageManager;
	private EventBus mEventBus;

	@SuppressLint("SimpleDateFormat")
	@Override
	public void onCreate() {
		super.onCreate();
		ACRA.init(this);
		mRequestQueue = Volley.newRequestQueue(this);
		mImageManager = new ImageManager(this);
	}

	@Override
	public Typeface getEliteTypeface() {
		Typeface fromCache = mEliteTypeface;
		if (fromCache == null) {
			fromCache = Typeface.createFromAsset(getAssets(), "SpecialElite.ttf");
		}
		return fromCache;
	}

	@Override
	public RequestQueue getRequestQueue() {
		return mRequestQueue;
	}

	@Override
	public ImageManager getImageManager() {
		return mImageManager;
	}

	@Override
	public EventBus getEventBus() {
		if (mEventBus == null) {
			mEventBus = new EventBus();
		}
		return mEventBus;
	}

}

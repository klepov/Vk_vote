package youreindeer.vk_vote;

import android.app.Application;
import android.content.Context;

import com.vk.sdk.VKSdk;


/**
 * Created by sad klep.io on 16.03.16.
 */
public class OverrideApp extends Application {
    private static Context context;

    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        VKSdk.initialize(getApplicationContext());
    }

}

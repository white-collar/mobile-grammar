package jeston.org.mobilegrammar;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

/**
 * Back button handling: closes navigation drawer if it is open, otherwise goes back.
 * Since targetSdk 36 onBackPressed() is not called anymore, so callbacks are used instead.
 */
public abstract class DrawerBackHandler extends OnBackPressedCallback {

    private final AppCompatActivity activity;

    private DrawerBackHandler(AppCompatActivity activity) {
        super(true);
        this.activity = activity;
    }

    /**
     * Closes drawer on back, otherwise does default back action (closes activity)
     *
     * @param activity activity with (or without) drawer_layout
     */
    public static void install(AppCompatActivity activity) {
        install(activity, null);
    }

    /**
     * Own back action of activity, used when drawer is closed
     */
    public interface BackAction {
        /**
         * @return true if back was handled, false to do default back action
         */
        boolean onBack();
    }

    /**
     * Closes drawer on back, otherwise runs given action
     *
     * @param activity   activity with (or without) drawer_layout
     * @param backAction action before default back action, or null
     */
    public static void install(AppCompatActivity activity, final BackAction backAction) {
        activity.getOnBackPressedDispatcher().addCallback(activity, new DrawerBackHandler(activity) {
            @Override
            protected void onBackWithoutDrawer() {
                if (backAction == null || !backAction.onBack()) {
                    super.onBackWithoutDrawer();
                }
            }
        });
    }

    @Override
    public void handleOnBackPressed() {
        DrawerLayout drawer = (DrawerLayout) activity.findViewById(R.id.drawer_layout);
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            onBackWithoutDrawer();
        }
    }

    /**
     * Default back action: pass back event to the next handler (system closes activity)
     */
    protected void onBackWithoutDrawer() {
        setEnabled(false);
        activity.getOnBackPressedDispatcher().onBackPressed();
        setEnabled(true);
    }
}

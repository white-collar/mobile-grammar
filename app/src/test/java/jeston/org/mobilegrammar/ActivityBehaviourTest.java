package jeston.org.mobilegrammar;

import android.content.Intent;
import android.view.View;
import android.widget.ListView;

import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

/**
 * Checks of activities on targetSdk 36: back navigation (without onBackPressed) and edge-to-edge layout.
 */
@RunWith(AndroidJUnit4.class)
@Config(sdk = 36)
public class ActivityBehaviourTest {

    private static final int WIDTH = 1080;
    private static final int HEIGHT = 1920;
    private static final int STATUS_BAR = 60;
    private static final int NAVIGATION_BAR = 120;

    @Test
    public void mainScreenShowsAllLessons() {
        AllArticlesListViewActivity activity = Robolectric.setupActivity(AllArticlesListViewActivity.class);
        ListView list = (ListView) activity.findViewById(R.id.listViewArticles);
        assertEquals(130, list.getAdapter().getCount());
    }

    @Test
    public void backClosesOpenDrawer() {
        AllArticlesListViewActivity activity = Robolectric.setupActivity(AllArticlesListViewActivity.class);
        DrawerLayout drawer = (DrawerLayout) activity.findViewById(R.id.drawer_layout);
        drawer.openDrawer(GravityCompat.START, false);
        assertTrue(drawer.isDrawerOpen(GravityCompat.START));
        // Robolectric doesn't run the closing animation, so check that drawer starts to close
        final List<Integer> states = new ArrayList<>();
        drawer.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerStateChanged(int newState) {
                states.add(newState);
            }
        });

        activity.getOnBackPressedDispatcher().onBackPressed();

        assertTrue(states.contains(DrawerLayout.STATE_SETTLING));
        assertFalse(activity.isFinishing());
    }

    @Test
    public void backWithClosedDrawerLeavesScreen() {
        UserGroupLessonsActivity activity = Robolectric.setupActivity(UserGroupLessonsActivity.class);

        activity.getOnBackPressedDispatcher().onBackPressed();

        assertTrue(activity.isFinishing());
    }

    @Test
    public void backFromLessonsOfGroupOpensUserGroups() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), AllArticlesListViewActivity.class)
                .putExtra("group_id", GroupId.group1.getValue())
                .putExtra("group_name", "Group 1")
                .putExtra("status_what_show", ActivityArticlesStatusToShow.SHOW_ALL_GROUPS);
        AllArticlesListViewActivity activity =
                Robolectric.buildActivity(AllArticlesListViewActivity.class, intent).setup().get();

        activity.getOnBackPressedDispatcher().onBackPressed();

        Intent started = shadowOf(activity).getNextStartedActivity();
        assertEquals(UserGroupLessonsActivity.class.getName(), started.getComponent().getClassName());
    }

    @Test
    public void contentStaysClearOfSystemBars() {
        AllArticlesListViewActivity activity = Robolectric.setupActivity(AllArticlesListViewActivity.class);
        View decor = activity.getWindow().getDecorView();
        WindowInsetsCompat insets = new WindowInsetsCompat.Builder()
                .setInsets(WindowInsetsCompat.Type.statusBars(), Insets.of(0, STATUS_BAR, 0, 0))
                .setInsets(WindowInsetsCompat.Type.navigationBars(), Insets.of(0, 0, 0, NAVIGATION_BAR))
                .build();
        ViewCompat.dispatchApplyWindowInsets(decor, insets);
        decor.measure(View.MeasureSpec.makeMeasureSpec(WIDTH, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(HEIGHT, View.MeasureSpec.EXACTLY));
        decor.layout(0, 0, WIDTH, HEIGHT);

        assertTrue("toolbar under status bar", top(activity.findViewById(R.id.toolbar)) >= STATUS_BAR);
        assertTrue("list under navigation bar",
                bottom(activity.findViewById(R.id.listViewArticles)) <= HEIGHT - NAVIGATION_BAR);
        assertTrue("fab under navigation bar", bottom(activity.findViewById(R.id.fab)) <= HEIGHT - NAVIGATION_BAR);
    }

    private static int top(View view) {
        int[] location = new int[2];
        view.getLocationInWindow(location);
        return location[1];
    }

    private static int bottom(View view) {
        return top(view) + view.getHeight();
    }
}

package jeston.org.mobilegrammar;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Slides floating action button out of the screen when list is scrolled down
 * and back when list is scrolled up (as com.melnykov.fab library did).
 */
public final class FabScrollHelper {

    private FabScrollHelper() {
    }

    /**
     * Makes fab react on scrolling of listview. Visibility of fab is not changed.
     *
     * @param fab      floating action button
     * @param listView listview to be scrolled
     */
    public static void attachToListView(final FloatingActionButton fab, AbsListView listView) {
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            private int lastFirstVisibleItem;
            private int lastTop;
            private boolean visible = true;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (view.getChildCount() == 0) return;
                int top = view.getChildAt(0).getTop();
                if (firstVisibleItem == lastFirstVisibleItem) {
                    if (top < lastTop) {
                        setVisible(false);
                    } else if (top > lastTop) {
                        setVisible(true);
                    }
                } else {
                    setVisible(firstVisibleItem < lastFirstVisibleItem);
                }
                lastFirstVisibleItem = firstVisibleItem;
                lastTop = top;
            }

            private void setVisible(boolean visible) {
                if (this.visible == visible) return;
                this.visible = visible;
                fab.animate().translationY(visible ? 0 : hiddenTranslation(fab)).setDuration(200);
            }
        });
    }

    /**
     * Distance to move fab down to hide it below bottom edge of screen
     */
    private static int hiddenTranslation(View fab) {
        int marginBottom = 0;
        ViewGroup.LayoutParams params = fab.getLayoutParams();
        if (params instanceof ViewGroup.MarginLayoutParams) {
            marginBottom = ((ViewGroup.MarginLayoutParams) params).bottomMargin;
        }
        return fab.getHeight() + marginBottom;
    }
}

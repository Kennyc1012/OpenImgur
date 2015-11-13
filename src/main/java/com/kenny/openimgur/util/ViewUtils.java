package com.kenny.openimgur.util;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.TextView;

import com.kenny.openimgur.R;
import com.kenny.openimgur.ui.GridItemDecoration;
import com.kennyc.view.MultiStateView;

/**
 * Created by kcampagna on 7/27/14.
 */
public class ViewUtils {

    /**
     * Returns the height of the actionbar and status bar (4.4+) needed for the translucent style
     *
     * @param context
     * @return
     */
    public static int getHeightForTranslucentStyle(Context context) {
        return getActionBarHeight(context);
    }

    /**
     * Returns the height of the actionbar
     *
     * @param context
     * @return
     */
    public static int getActionBarHeight(Context context) {
        final TypedArray styledAttributes = context.getTheme().obtainStyledAttributes(
                new int[]{android.support.v7.appcompat.R.attr.actionBarSize});

        int abHeight = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();
        return abHeight;
    }

    /**
     * Returns an empty view to occupy the space present in the translucent style
     *
     * @param context
     * @param additionalHeight Additional height to be added to the view
     * @return
     */
    public static View getHeaderViewForTranslucentStyle(Context context, int additionalHeight) {
        View v = View.inflate(context, R.layout.empty_header, null);
        int height = getHeightForTranslucentStyle(context);
        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height + additionalHeight);
        v.setLayoutParams(lp);
        return v;
    }

    /**
     * Returns the height of the navigation bar
     *
     * @param context
     * @return
     */
    public static int getNavigationBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");

        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }

        return 0;
    }

    /**
     * Returns the height of the status bar
     *
     * @param context
     * @return
     */
    public static int getStatusBarHeight(Context context) {
        int height = 0;

        // On 4.4 + devices, we need to account for the status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                height = context.getResources().getDimensionPixelSize(resourceId);
            }
        }

        return height;
    }

    /**
     * Returns an empty view to occupy the space of the navigation bar for a translucent style for comments and messages fragment
     *
     * @param context
     * @return
     */
    public static View getFooterViewForComments(Context context) {
        View v = LayoutInflater.from(context).inflate(R.layout.profile_comment_item, null);
        int height = getNavigationBarHeight(context);
        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
        v.setLayoutParams(lp);
        return v;
    }

    /**
     * Pass in a runnable to run before the view starts drawing. Good for running code after
     * drawing has complete
     *
     * @param view
     * @param runnable
     */
    public static void onPreDraw(final View view, final Runnable runnable) {
        view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                view.getViewTreeObserver().removeOnPreDrawListener(this);
                view.post(runnable);
                //Dont draw this time, since we are posting the runnable
                return false;
            }
        });
    }

    /**
     * Sets the text for the Error View in a {@link MultiStateView}
     *
     * @param multiStateView The {@link MultiStateView}
     * @param textViewId     TextView id in the ErrorView
     * @param errorMessage   String resource of the error message
     */
    public static void setErrorText(MultiStateView multiStateView, @IdRes int textViewId, @StringRes int errorMessage) {
        if (multiStateView == null) return;

        View errorView = multiStateView.getView(MultiStateView.VIEW_STATE_ERROR);

        if (errorView == null) {
            throw new NullPointerException("Error view is null");
        }

        TextView errorTextView = (TextView) errorView.findViewById(textViewId);
        if (errorTextView != null) errorTextView.setText(errorMessage);
    }

    /**
     * Sets the text for the Error View in a {@link MultiStateView}
     *
     * @param multiStateView The {@link MultiStateView}
     * @param textViewId     TextView id in the ErrorView
     * @param errorMessage   String  of the error message
     */
    public static void setErrorText(MultiStateView multiStateView, @IdRes int textViewId, String errorMessage) {
        if (multiStateView == null) return;

        View errorView = multiStateView.getView(MultiStateView.VIEW_STATE_ERROR);

        if (errorView == null) {
            throw new NullPointerException("Error view is null");
        }

        TextView errorTextView = (TextView) errorView.findViewById(textViewId);
        if (errorTextView != null) errorTextView.setText(errorMessage);
    }

    /**
     * Sets the text for the empty view in a {@link MultiStateView}
     *
     * @param multiStateView The {@link MultiStateView}
     * @param textViewId     TextView id in the Empty View
     * @param emptyMessage   The empty message
     */
    public static void setEmptyText(MultiStateView multiStateView, @IdRes int textViewId, String emptyMessage) {
        if (multiStateView == null) return;

        View emptyView = multiStateView.getView(MultiStateView.VIEW_STATE_EMPTY);

        if (emptyView == null) {
            throw new NullPointerException("Empty view is null");
        }

        TextView emptyTextView = (TextView) emptyView.findViewById(textViewId);
        if (emptyTextView != null) emptyTextView.setText(emptyMessage);
    }

    /**
     * Sets the text for the empty view in a {@link MultiStateView}
     *
     * @param multiStateView The {@link MultiStateView}
     * @param textViewId     TextView id in the Empty View
     * @param emptyMessage   The empty message
     */
    public static void setEmptyText(MultiStateView multiStateView, @IdRes int textViewId, @StringRes int emptyMessage) {
        if (multiStateView == null) return;

        View emptyView = multiStateView.getView(MultiStateView.VIEW_STATE_EMPTY);

        if (emptyView == null) {
            throw new NullPointerException("Empty view is null");
        }

        TextView emptyTextView = (TextView) emptyView.findViewById(textViewId);
        if (emptyTextView != null) emptyTextView.setText(emptyMessage);
    }

    /**
     * Sets up a {@link RecyclerView} for a Grid style
     *
     * @param context
     * @param recyclerView
     */
    public static void setRecyclerViewGridDefaults(@NonNull Context context, @NonNull RecyclerView recyclerView) {
        Resources res = context.getResources();
        int gridSize = res.getInteger(R.integer.gallery_num_columns);
        recyclerView.setLayoutManager(new GridLayoutManager(context, gridSize));
        recyclerView.addItemDecoration(new GridItemDecoration(res.getDimensionPixelSize(R.dimen.grid_padding), gridSize));
    }
}

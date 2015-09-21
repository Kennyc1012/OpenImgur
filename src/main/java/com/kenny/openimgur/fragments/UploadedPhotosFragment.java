package com.kenny.openimgur.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.TextView;

import com.kenny.openimgur.R;
import com.kenny.openimgur.activities.FullScreenPhotoActivity;
import com.kenny.openimgur.activities.ViewActivity;
import com.kenny.openimgur.adapters.UploadAdapter;
import com.kenny.openimgur.api.ApiClient;
import com.kenny.openimgur.api.ImgurService;
import com.kenny.openimgur.api.responses.BasicResponse;
import com.kenny.openimgur.classes.FragmentListener;
import com.kenny.openimgur.classes.UploadedPhoto;
import com.kenny.openimgur.ui.HeaderGridView;
import com.kenny.openimgur.util.LogUtil;
import com.kenny.openimgur.util.ViewUtils;
import com.kenny.snackbar.SnackBar;
import com.kennyc.bottomsheet.BottomSheet;
import com.kennyc.view.MultiStateView;

import java.util.List;

import butterknife.Bind;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Kenny-PC on 1/14/2015.
 */
public class UploadedPhotosFragment extends BaseFragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, AbsListView.OnScrollListener {
    @Bind(R.id.multiView)
    public MultiStateView mMultiStateView;

    @Bind(R.id.grid)
    public HeaderGridView mGrid;

    @Bind(R.id.refreshLayout)
    protected SwipeRefreshLayout mRefreshLayout;

    private FragmentListener mListener;

    private UploadAdapter mAdapter;

    private int mPreviousItem = 0;

    public static Fragment createInstance() {
        return new UploadedPhotosFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof FragmentListener) {
            mListener = (FragmentListener) activity;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_uploads, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mListener != null)
            mListener.onUpdateActionBarTitle(getString(R.string.uploaded_photos_title));
        mGrid.setOnItemClickListener(this);
        mGrid.setOnItemLongClickListener(this);
        mGrid.setOnScrollListener(this);
        mRefreshLayout.setColorSchemeColors(getResources().getColor(theme.accentColor));
        int bgColor = theme.isDarkTheme ? R.color.background_material_dark : R.color.background_material_light;
        mRefreshLayout.setProgressBackgroundColorSchemeColor(getResources().getColor(bgColor));
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mRefreshLayout.setRefreshing(true);
                refresh();
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.topics, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                refresh();
                return true;
        }

        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdapter == null || mAdapter.isEmpty()) {
            refresh();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        int headerSize = mGrid.getNumColumns() * mGrid.getHeaderViewCount();
        int adapterPosition = position - headerSize;

        if (adapterPosition >= 0) {
            UploadedPhoto photo = mAdapter.getItem(adapterPosition);

            if (photo.isAlbum()) {
                startActivity(ViewActivity.createIntent(getActivity(), photo.getUrl(), true));
            } else {
                startActivity(FullScreenPhotoActivity.createIntent(getActivity(), photo.getUrl()));
            }
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        int headerSize = mGrid.getNumColumns() * mGrid.getHeaderViewCount();
        int adapterPosition = position - headerSize;

        if (adapterPosition >= 0) {
            final UploadedPhoto photo = mAdapter.getItem(adapterPosition);

            new AlertDialog.Builder(getActivity(), theme.getAlertDialogTheme())
                    .setItems(R.array.uploaded_photos_options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 0. Share 1. Copy Link 2. Delete

                            switch (which) {
                                case 0:
                                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                    shareIntent.setType("text/plain");
                                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share));
                                    shareIntent.putExtra(Intent.EXTRA_TEXT, photo.getUrl());
                                    BottomSheet shareDialog = BottomSheet.createShareBottomSheet(getActivity(), shareIntent, R.string.share);

                                    if (shareDialog != null) {
                                        shareDialog.show();
                                    } else {
                                        SnackBar.show(getActivity(), R.string.cant_launch_intent);
                                    }
                                    break;

                                case 1:
                                    ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                                    clipboard.setPrimaryClip(ClipData.newPlainText("link", photo.getUrl()));
                                    SnackBar.show(getActivity(), R.string.link_copied);
                                    break;

                                case 2:
                                    View deleteView = LayoutInflater.from(getActivity()).inflate(R.layout.upload_delete_confirm, null);
                                    final CheckBox cb = (CheckBox) deleteView.findViewById(R.id.imgurDelete);
                                    ((TextView) deleteView.findViewById(R.id.message)).setText(photo.isAlbum()
                                            ? R.string.uploaded_remove_album_message : R.string.uploaded_remove_photo_message);

                                    new AlertDialog.Builder(getActivity(), theme.getAlertDialogTheme())
                                            .setNegativeButton(R.string.cancel, null)
                                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    if (cb.isChecked()) deleteItem(photo);
                                                    app.getSql().deleteUploadedPhoto(photo);
                                                    mAdapter.removeItem(photo);

                                                    if (mAdapter.isEmpty()) {
                                                        mMultiStateView.setViewState(MultiStateView.VIEW_STATE_EMPTY);
                                                        if (mListener != null)
                                                            mListener.onUpdateActionBar(true);
                                                    }
                                                }
                                            })
                                            .setView(deleteView)
                                            .show();
                            }
                        }
                    }).show();
            return true;
        }
        return false;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // NOOP
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        // Hide the actionbar when scrolling down, show when scrolling up
        if (firstVisibleItem > mPreviousItem && mListener != null) {
            mListener.onUpdateActionBar(false);
        } else if (firstVisibleItem < mPreviousItem && mListener != null) {
            mListener.onUpdateActionBar(true);
        }

        mPreviousItem = firstVisibleItem;
    }

    private void deleteItem(@NonNull UploadedPhoto photo) {
        ImgurService apiService = ApiClient.getService();
        Callback<BasicResponse> cb = new Callback<BasicResponse>() {
            @Override
            public void success(BasicResponse basicResponse, Response response) {
                // We don't take any action on the responses
            }

            @Override
            public void failure(RetrofitError error) {
                LogUtil.e(TAG, "Unable to delete item", error);
            }
        };

        if (photo.isAlbum()) {
            apiService.deleteAlbum(photo.getDeleteHash(), cb);
        } else {
            apiService.deletePhoto(photo.getDeleteHash(), cb);
        }
    }

    private void refresh() {
        if (mAdapter != null) mAdapter.clear();
        mMultiStateView.setViewState(MultiStateView.VIEW_STATE_LOADING);
        List<UploadedPhoto> photos = app.getSql().getUploadedPhotos(true);

        if (!photos.isEmpty()) {
            if (mAdapter == null) {
                mAdapter = new UploadAdapter(getActivity(), photos);
                mGrid.addHeaderView(ViewUtils.getHeaderViewForTranslucentStyle(getActivity(), 0));
                mGrid.setAdapter(mAdapter);
            } else {
                mAdapter.addItems(photos);
            }

            mMultiStateView.setViewState(MultiStateView.VIEW_STATE_CONTENT);
        } else {
            mMultiStateView.setViewState(MultiStateView.VIEW_STATE_EMPTY);
            if (mListener != null) mListener.onUpdateActionBar(true);
        }

        mRefreshLayout.setRefreshing(false);
    }
}

package com.murraystudio.redditclient;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import java.util.List;

/**
 * Created by sushi_000 on 11/3/2016.
 */

public class HomePage extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = "RecyclerViewFragment";
    private static final String KEY_LAYOUT_MANAGER = "layoutManager";
    private static final int SPAN_COUNT = 2;
    private static final int DATASET_COUNT = 60;

    private String currentSubreddit = "all";

    private List<Post> postList;

    private enum LayoutManagerType {
        GRID_LAYOUT_MANAGER,
        LINEAR_LAYOUT_MANAGER
    }

    private SwipeRefreshLayout swipeRefreshLayout;

    protected LayoutManagerType mCurrentLayoutManagerType;

    protected RadioButton mLinearLayoutRadioButton;
    protected RadioButton mGridLayoutRadioButton;

    protected RecyclerView mRecyclerView;
    protected HomePageAdapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;
    //protected String[] mDataset;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.home_page, container, false);
        rootView.setTag(TAG);

        //swipe down to refresh data
        swipeRefreshLayout =  (SwipeRefreshLayout) rootView.findViewById(R.id.home_page_swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(this);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.home_page_recycler_view);

        // LinearLayoutManager is used here, this will layout the elements in a similar fashion
        // to the way ListView would layout elements. The RecyclerView.LayoutManager defines how
        // elements are laid out.
        mLayoutManager = new LinearLayoutManager(getActivity());

        mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;

        if (savedInstanceState != null) {
            // Restore saved layout manager type.
            mCurrentLayoutManagerType = (LayoutManagerType) savedInstanceState
                    .getSerializable(KEY_LAYOUT_MANAGER);
        }
        setRecyclerViewLayoutManager(mCurrentLayoutManagerType);




        if(postList == null || postList.size() == 0) {
            fetchPosts("all"); //upon start up, load posts from r/all
        }
        else{
            setAdapter(postList);
        }

        return rootView;
    }

    private void setAdapter(List<Post> postList){
        mAdapter = new HomePageAdapter(postList, getActivity());
        // Set HomePageAdapter as the adapter for RecyclerView.
        mRecyclerView.setAdapter(mAdapter);
        setRecyclerViewLayoutManager(LayoutManagerType.LINEAR_LAYOUT_MANAGER);
    }

    /**
     * Set RecyclerView's LayoutManager to the one given.
     *
     * @param layoutManagerType Type of layout manager to switch to.
     */
    public void setRecyclerViewLayoutManager(LayoutManagerType layoutManagerType) {
        int scrollPosition = 0;

        // If a layout manager has already been set, get current scroll position.
        if (mRecyclerView.getLayoutManager() != null) {
            scrollPosition = ((LinearLayoutManager) mRecyclerView.getLayoutManager())
                    .findFirstCompletelyVisibleItemPosition();
        }

        switch (layoutManagerType) {
            case GRID_LAYOUT_MANAGER:
                mLayoutManager = new GridLayoutManager(getActivity(), SPAN_COUNT);
                mCurrentLayoutManagerType = LayoutManagerType.GRID_LAYOUT_MANAGER;
                break;
            case LINEAR_LAYOUT_MANAGER:
                mLayoutManager = new LinearLayoutManager(getActivity());
                mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
                break;
            default:
                mLayoutManager = new LinearLayoutManager(getActivity());
                mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
        }

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.scrollToPosition(scrollPosition);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save currently selected layout manager.
        savedInstanceState.putSerializable(KEY_LAYOUT_MANAGER, mCurrentLayoutManagerType);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRefresh() {
        fetchPosts(currentSubreddit);
    }

    //fetch posts using RemoteData without the need for OAuth. Parameter allows for a specific subreddit to visit.
    public void fetchPosts(String subreddit) {
        new RemoteData(this).execute("https://www.reddit.com/r/" + subreddit + "/.json");
        currentSubreddit = subreddit;
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("r/" + subreddit);
    }

    //fetch posts from the frontpage using RemoteDataOAuth which requires OAuth parameter
    public void fetchPostsOAuth(){
        new RemoteDataOAuth(this).execute("https://oauth.reddit.com");
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Frontpage");
    }

    //refresh post list with new ones from network refresh
    public void onPostFetchComplete(List<Post> postList){
        this.postList = postList;
        swipeRefreshLayout.setRefreshing(false);
        if(mAdapter != null) {
            mAdapter.refresh(postList);
        }
        else{
            setAdapter(postList);
        }
    }
}

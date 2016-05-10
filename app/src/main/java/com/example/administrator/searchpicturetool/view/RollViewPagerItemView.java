package com.example.administrator.searchpicturetool.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.example.administrator.searchpicturetool.R;
import com.example.administrator.searchpicturetool.app.APP;
import com.example.administrator.searchpicturetool.model.BannerModel;
import com.example.administrator.searchpicturetool.model.bean.Banner;
import com.example.administrator.searchpicturetool.presenter.adapter.ImageLoopAdapter;
import com.example.administrator.searchpicturetool.presenter.adapter.RecommendAdapter;
import com.example.administrator.searchpicturetool.view.activity.SearchActivity;
import com.example.administrator.searchpicturetool.widght.BannerTextHintView;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;
import com.jude.easyrecyclerview.swipe.SwipeRefreshLayout;
import com.jude.rollviewpager.RollPagerView;
import com.jude.rollviewpager.hintview.TextHintView;
import com.jude.utils.JFileManager;
import com.jude.utils.JUtils;

import java.util.ArrayList;
import java.util.List;

import rx.Subscriber;

/**
 * Created by wenhuaijun on 2016/2/9 0009.
 */
public class RollViewPagerItemView implements RecyclerArrayAdapter.ItemView, View.OnClickListener {
    RollPagerView rollPagerView;
    ImageLoopAdapter adapter;
    SwipeRefreshLayout swipeRefreshLayout;
    Context context;
    List<Banner> banners;
    JFileManager.Folder folder;
    public RollViewPagerItemView(SwipeRefreshLayout swipeRefreshLayout) {
        this.swipeRefreshLayout =swipeRefreshLayout;
    }

    @Override
    public View onCreateView(ViewGroup parent) {
        adapter = new ImageLoopAdapter();
        context =parent.getContext();
        folder = JFileManager.getInstance().getFolder(APP.Dir.Object);
       View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.include_viewpager,parent,false);
        rollPagerView =(RollPagerView)view.findViewById(R.id.roll_view_pager);
        //解决viewPager和swipeRefreshLayout的冲突
        rollPagerView.getViewPager().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_MOVE:
                        swipeRefreshLayout.setEnabled(false);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        swipeRefreshLayout.setEnabled(true);
                        break;
                }
                return false;
            }
        });

        rollPagerView.setAdapter(adapter);
      //  rollPagerView.getViewPager().setOnClickListener(this);
        return view;
    }

    @Override
    public void onBindView(View headerView) {
        //加载缓存数据
        getBannerFromCache();
        BannerModel.getBanners(context, new Subscriber<List<Banner>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(final List<Banner> banners) {
                folder.writeObjectToFile(banners, "banner");
                RollViewPagerItemView.this.banners =banners;
                rollPagerView.setHintView(new BannerTextHintView(context, banners));
                adapter.setBanners(banners);
                adapter.notifyDataSetChanged();
                //rollPagerView.setHintView(new BannerTextHintView(context, banners));


    }

});
    }
    public void getBannerFromCache(){
        banners =(ArrayList<Banner>)folder.readObjectFromFile("banner");
        if(banners!=null&&banners.size()!=0){
            adapter.setBanners(banners);
            rollPagerView.setHintView(new BannerTextHintView(context, banners));
            adapter.notifyDataSetChanged();
        }
    }
    @Override
    public void onClick(View v) {
        JUtils.Log("onClick---banner");
        Bundle bundle = new Bundle();
        bundle.putString("search",banners.get(rollPagerView.getViewPager().getCurrentItem()).getSearchTip());
        Intent intent = new Intent();
        intent.putExtra("search", bundle);
        intent.setClass(context, SearchActivity.class);
        context.startActivity(intent);
    }
}
package com.ilmare.oschina.Fragment;

import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.ilmare.oschina.Adapter.NewsListViewAdapter;
import com.ilmare.oschina.Base.BaseListViewFragment;
import com.ilmare.oschina.Beans.News;
import com.ilmare.oschina.Beans.NewsList;
import com.ilmare.oschina.Net.OSChinaApi;
import com.ilmare.oschina.Utils.UIHelper;
import com.ilmare.oschina.Utils.XmlUtils;
import com.loopj.android.http.RequestParams;

import java.io.Serializable;


/**
 * ===============================
 * 作者: ilmare:
 * 创建时间：6/16/2016 12:38 PM
 * 版本号： 1.0
 * 版权所有(C) 6/16/2016
 * 描述：资讯界面
 * ===============================
 */
public class AllNewsFragment extends BaseListViewFragment implements AdapterView.OnItemClickListener {

    private NewsList newsList = new NewsList();
    private int mCatalog = 1;      //页分类
    private NewsListViewAdapter newsListViewAdapter;
    private static final String CACHE_KEY_PREFIX = "newslist_";
    private boolean isLoadMore=false;

    @Override
    protected void loadMoreFromServer() {

        if(!isLoadMore){
            isLoadMore=true;
            mCurrentPage++;
            loadFromServer();
        }
    }

    @Override
    protected String getCacheKeyPrefix() {
        return CACHE_KEY_PREFIX + mCatalog;
    }

    @Override
    protected void executeOnReadCacheSuccess(Serializable seri) {
        newsList = (NewsList) seri;
        if(isLoadMore){
            if(newsList.getList().size()==0){
                Toast.makeText(getActivity(), "没有数据", Toast.LENGTH_SHORT).show();
                mCurrentPage--;
            }else{
                newsListViewAdapter.addDatas(newsList);
                newsListViewAdapter.notifyDataSetChanged();
            }

            isLoadMore=false;
        }else{
            newsListViewAdapter = new NewsListViewAdapter(newsList, getActivity());
            listview.setAdapter(newsListViewAdapter);

        }

        listview.setOnItemClickListener(this);
    }


    @Override
    protected void loadFromServer() {
//      方式一：直接创建使用 简单粗暴
//      new AsyncHttpClient().post(getUrl(),getParams(),mHandler);

//      方式二：获取单列的直接使用
//      ApiHttpClient.getHttpClient().post(getUrl(),getParams(),mHandler);

//      方式三: 直接用封装了AsyncHttpClient的ApiHttpClient静态方法, 封装路径和拼接参数, 麻烦
//      ApiHttpClient.post("action/api/news_list", getParams(), mHandler);

//      方式四: (推荐)主线程 mCatalog = 1 资讯, mCatalog = 4 热点

        OSChinaApi.getNewsList(mCatalog, mCurrentPage, mHandler);
    }

    @Override
    protected void onLoadSuccess(String content) {
        newsList = XmlUtils.toBean(NewsList.class, content.getBytes());
        if(isLoadMore){
            if(newsList.getList().size()==0){
                Toast.makeText(getActivity(), "没有数据", Toast.LENGTH_SHORT).show();
                mCurrentPage--;
            }else{
                newsListViewAdapter.addDatas(newsList);
                newsListViewAdapter.notifyDataSetChanged();
            }

            isLoadMore=false;
        }else{
            newsListViewAdapter = new NewsListViewAdapter(newsList, getActivity());
            listview.setAdapter(newsListViewAdapter);
        }

        listview.setOnItemClickListener(this);

        //保存到本地
        saveLocal(newsList);
    }

    protected String getUrl() {
        return "http://www.oschina.net/action/api/news_list";
    }

    public RequestParams getParams() {
        RequestParams params = new RequestParams();
        params.put("pageIndex", "0");
        params.put("catalog", "1");
        params.put("pageSize", "20");
        return params;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        News news = (News) newsListViewAdapter.getItem(position);
        //Todo 跳转页面 加入已读列表
        if (news != null) {
            UIHelper.showNewsRedirect(view.getContext(), news);
            // 放入已读列表
//            saveToReadedList(view, NewsList.PREF_READED_NEWS_LIST, news.getId()
//                    + "");
        }
    }
}

package com.kalom.kalapp.fragments;


import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;

import com.kalom.kalapp.MainActivity;
import com.kalom.kalapp.R;
import com.kalom.kalapp.classes.Config;
import com.kalom.kalapp.classes.Duyuru;
import com.kalom.kalapp.classes.DuyuruAdapter;
import com.kalom.kalapp.classes.JSONParser;
import com.kalom.kalapp.classes.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class DuyuruFragment extends Fragment {

    private DuyuruAdapter adapter=null;
    private DuyuruInfo us;
    private ListView listemiz;
    private View list_footer_view;
    private CoordinatorLayout coordinatorLayout;
    private SwipeRefreshLayout swip;

    private SessionManager session;

    private int str=0;
    private int fnsh=Config.duyuru_load_one_time;
    public boolean refreshed=false;

    final List<Duyuru> duyurular= new ArrayList<>();

    public static DuyuruFragment newInstance() {
        return new DuyuruFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = new SessionManager(getContext());



    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

         final View rootView = inflater.inflate(R.layout.duyurufragment_layout,
                container, false);

          listemiz=rootView.findViewById(R.id.listView1);
          list_footer_view = ((LayoutInflater) getContext().getSystemService(MainActivity.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.list_footer, null, false);

            swip=rootView.findViewById(R.id.swiperefresh);

        listemiz.setSmoothScrollbarEnabled(true);

        adapter=new DuyuruAdapter(getActivity(),duyurular);
        listemiz.setAdapter(adapter);


        us=new DuyuruInfo();
        us.execute();




        swip.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        refresh();
                        swip.setRefreshing(false);

                    }
                }
        );




        listemiz.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {


                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE
                        && (listemiz.getLastVisiblePosition() - listemiz.getHeaderViewsCount() -
                        listemiz.getFooterViewsCount()) >= (adapter.getCount()-1)) {

                    us=new DuyuruInfo();
                    us.execute();

                }

                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE
                        &&listemiz.getFirstVisiblePosition() == 0 && refreshed) {

                    refresh();
                    listemiz.setSelection(0);
                    refreshed=false;
                }


            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });


        return rootView;

    }

    protected void showloader(){
        listemiz.addFooterView(list_footer_view);
        listemiz.setSelection(listemiz.getLastVisiblePosition());
        listemiz.setEnabled(false);
        swip.setEnabled(false);



    }

    protected void hideloader(){
        listemiz.removeFooterView(list_footer_view);
        listemiz.setEnabled(true);
        swip.setEnabled(true);


    }

    public void scrolltoTop(){

        listemiz.smoothScrollToPosition(0);

    }

    public void refresh(){
        duyurular.clear();
        adapter.notifyDataSetChanged();
        us=new DuyuruInfo();
        str=0;
        fnsh=Config.duyuru_load_one_time;
        us.execute();
    }



    @SuppressLint("StaticFieldLeak")
    private class DuyuruInfo extends AsyncTask<Void, String,String> {

        @Override
        protected void onPreExecute(){

            showloader();

        }

        @Override
        protected String doInBackground(Void... params) {


            // Simulate network access.
            JSONParser js=new JSONParser();

            try{



                String api_call= Config.api_server+"?action=duyuru&hash="+session.getToken()+"&s="+str +"&f="+fnsh;
                str=fnsh;
                fnsh+=Config.duyuru_load_one_time;
                return js.JsonString(api_call);



            }catch(IOException | JSONException e){
                e.getMessage();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result){
           hideloader();
            if(result==null){

                Toast.makeText(getContext(),"INTERNET YOK",Toast.LENGTH_LONG).show();

                return;
            }

            try {

                JSONArray ar = new JSONArray(result);


                for(int i = 0; i< ar.length(); ++i){
                    try {
                        JSONObject obj = (JSONObject) ar.get(i);
                        duyurular.add(new Duyuru(
                                Integer.parseInt(obj.get("id").toString()),
                                obj.get("yazar").toString(),
                                obj.get("title").toString(),
                                obj.get("content").toString(),
                                obj.get("img_url").toString()
                        ));


                    } catch (JSONException e) {
                        e.printStackTrace();

                        return;
                    }

                }

                adapter.notifyDataSetChanged();


            } catch (JSONException e) {
                e.printStackTrace();
            }



        }






    }

}
package com.example.newsreader;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.support.v7.widget.SearchView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int RESULT_CODE_PREFERENCES = 0;
    public static String NEW_URL = "newurl";
    public static String NEW_RRATE = "rrate";
    public static String NEW_ITEM_VALUE = "ivalue";
    private static final String TAG = "DEBUG";
    int currentItems, totalItems, scrollOutItems;
    Boolean isScrolling = false;
    public static ProgressBar progressBar;
    public static RecyclerView recyclerView;
    public static RSSAdapter adapter;
    public static RecyclerView.LayoutManager layoutManager;
    public static String urlLink = "https://www.nrk.no/toppsaker.rss";
    int refreshRate = 15;
    public static int numOfItems = 12;


    public static List<RSSObject> RSSObjectList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("RSS Reader");
        setSupportActionBar(toolbar);

        Button goToPreferences = findViewById(R.id.btnPrefAct);
        progressBar = findViewById(R.id.progress);
        recyclerView = findViewById(R.id.recyclerView);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getBaseContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);


        goToPreferences.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent startTransfer = new Intent(MainActivity.this, PreferencesActivity.class);
                startActivityForResult(startTransfer, RESULT_CODE_PREFERENCES);
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
                {
                    isScrolling = true;
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                currentItems = linearLayoutManager.getChildCount();
                totalItems = linearLayoutManager.getItemCount();
                totalItems = linearLayoutManager.findFirstCompletelyVisibleItemPosition();

                if (isScrolling && currentItems + scrollOutItems == totalItems)
                {
                    numOfItems = numOfItems +1;
                    isScrolling = false;
                }
            }
        });

        scheduleJob();

    }

    public void scheduleJob() {
        ComponentName componentName = new ComponentName(this, RSSFetchService.class);
        JobInfo info = new JobInfo.Builder(123, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                .setPersisted(true)
                .setPeriodic(refreshRate * 60000)
                .build();

        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        scheduler.schedule(info);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();


        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == RESULT_CODE_PREFERENCES) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(getBaseContext(),"Preferences Updated", Toast.LENGTH_SHORT).show();
                int NewRefreshRate = data.getIntExtra(NEW_RRATE,0);
                if (NewRefreshRate > 15) {
                    refreshRate = NewRefreshRate;
                }
                int NewItemSize = data.getIntExtra(NEW_ITEM_VALUE,0);
                if (NewItemSize != 0) {
                    numOfItems = NewItemSize;
                }
                String NewURL = data.getStringExtra(NEW_URL);
                if(!NewURL.isEmpty()){
                urlLink = NewURL;
                }
                scheduleJob();
            }
        }
    }
}
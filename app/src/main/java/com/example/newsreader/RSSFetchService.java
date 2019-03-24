package com.example.newsreader;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class RSSFetchService extends JobService {
    private static final String TAG = "DEBUG";
    private boolean jobCancelled = false;

    @Override
    public boolean onStartJob(JobParameters params) {
        doBackGroundWork(params);

        return true;
    }

    private void doBackGroundWork(final JobParameters params) {

        new loadRSS().execute((Void) null);

    }

    @Override
    public boolean onStopJob(JobParameters params) {
        jobCancelled = true;
        return false;
    }


    public List<RSSObject> parseFeed(InputStream inputStream) throws XmlPullParserException, IOException {
        String title = null;
        String link = null;
        String description = null;
        String imgURL = null;
        boolean isItem = false;
        List<RSSObject> items = new ArrayList<>();
        List<RSSObject> limitedItems = new ArrayList<>();

        try {
            XmlPullParser xmlPullParser = Xml.newPullParser();
            xmlPullParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            xmlPullParser.setInput(inputStream, null);

            xmlPullParser.nextTag();
            while (xmlPullParser.next() != XmlPullParser.END_DOCUMENT ) {
                int eventType = xmlPullParser.getEventType();

                String name = xmlPullParser.getName();
                if(name == null)
                    continue;

                if(eventType == XmlPullParser.END_TAG) {
                    if(name.equalsIgnoreCase("item")) {
                        isItem = false;
                    }
                    continue;
                }

                if (eventType == XmlPullParser.START_TAG) {
                    if(name.equalsIgnoreCase("item")) {
                        isItem = true;
                        continue;
                    }
                }

                String result = "";
                if (xmlPullParser.next() == XmlPullParser.TEXT) {
                    result = xmlPullParser.getText();
                    xmlPullParser.nextTag();
                }

                if (name.equalsIgnoreCase("title")) {
                    title = result;
                } else if (name.equalsIgnoreCase("link")) {
                    link = result;
                } else if (name.equalsIgnoreCase("description")) {
                    description = result;
                } else if (name.equalsIgnoreCase("enclosure")) {
                    imgURL = xmlPullParser.getAttributeValue(null,"url");
                }

                if (title != null && link != null && description != null && imgURL != null) {
                    if(isItem) {
                        RSSObject item = new RSSObject(title, link, description, imgURL);
                        items.add(item);
                    }
                    title = null;
                    link = null;
                    description = null;
                    imgURL = null;
                    isItem = false;
                }
            }
            if (MainActivity.numOfItems < items.size()) {
                for (int i = 0; i < MainActivity.numOfItems; i++) {
                    limitedItems.add(items.get(i));
                }

                return limitedItems;
            }   else {
                return  items;
            }

        } finally {
            inputStream.close();
        }
    }


    public class loadRSS extends AsyncTask<Void, Void, Boolean>{


        @Override
        protected void onPreExecute() {
           MainActivity.progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if (TextUtils.isEmpty(MainActivity.urlLink))
                return false;

            try {
                if(!MainActivity.urlLink.startsWith("http://") && !MainActivity.urlLink.startsWith("https://"))
                    MainActivity.urlLink = "http://" + MainActivity.urlLink;

                URL url = new URL(MainActivity.urlLink);
                InputStream inputStream = url.openConnection().getInputStream();
                MainActivity.RSSObjectList = parseFeed(inputStream);
                return true;
            } catch (IOException e) {
                Log.e(TAG, "Error", e);
            } catch (XmlPullParserException e) {
                Log.e(TAG, "Error", e);
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {

            if (success) {
                MainActivity.adapter = new RSSAdapter(MainActivity.RSSObjectList, getBaseContext());
                MainActivity.recyclerView.setAdapter(MainActivity.adapter);
            } else {
                Toast.makeText(getBaseContext(),
                        "Invalid RSS url, using default.",
                        Toast.LENGTH_LONG).show();
            }
            MainActivity.progressBar.setVisibility(View.GONE);

        }
    }

}

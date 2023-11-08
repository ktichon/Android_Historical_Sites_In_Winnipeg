package com.example.manitobahistoricalsites;

import androidx.annotation.RequiresApi;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.manitobahistoricalsites.Database.ManitobaHistoricalSite;
import com.example.manitobahistoricalsites.Database.SiteType;
import com.google.android.material.button.MaterialButton;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class HistoricalSiteDetailsFragment extends Fragment {

    private HistoricalSiteDetailsViewModel mViewModel;
    private LinearLayout llDetails;
    private LinearLayout llDisplayInfo;
    //private LinearLayout llPlaceInfo;

    private TextView tvName;

    /*private MaterialButton btnLong;
    private MaterialButton btnShort;
    private MaterialButton btnGoogle;*/
    private ImageButton btnDirections;
    /*private int activeBtnColour;
    private int restBtnColour;*/
    //private Location currentLocation;
    //private FusedLocationProviderClient fusedLocationProviderClient;
    LocationManager locationManager;
    //private RequestQueue queue;
    View mainView;
    WebView webView;
    LinearLayout llLoadingInfo;

    private LinearLayout llWebView;
    private GestureDetector mDetector;

    private ManitobaHistoricalSite currentSite;

    private final CompositeDisposable mDisposable = new CompositeDisposable();


    private static final String SITE_KEY = "current_historical_site_yehaw";

    //Allows historical site to be passed in away the allows back-button
    public static HistoricalSiteDetailsFragment newInstance(int site_id) {
        Bundle args = new Bundle();
        args.putInt(SITE_KEY, site_id);
        HistoricalSiteDetailsFragment fragment = new HistoricalSiteDetailsFragment();
        fragment.setArguments(args);
        return fragment;

    }



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.historical_site_details_fragment, container, false);
    }

    /*@Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(HistoricalSiteDetailsViewModel.class);
        currentSite = mViewModel.getCurrentSite().getValue();

    }*/

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainView = view;
        mViewModel = new ViewModelProvider(getActivity()).get(HistoricalSiteDetailsViewModel.class);

        //currentSite = mViewModel.getCurrentSite().getValue();
        int site_id  =  getArguments().getInt(SITE_KEY);
        /*if (mViewModel.getCurrentSite().getValue() != currentSite)
        {
            mViewModel.setCurrentSite(currentSite);
        }*/

        //queue = Volley.newRequestQueue(mainView.getContext());
        llDisplayInfo = mainView.findViewById(R.id.Details);
        llDisplayInfo.setVisibility(View.GONE);





        /*llPlaceInfo = mainView.findViewById(R.id.llPlaceInformation);
        llPlaceInfo.setVisibility(View.GONE);*/

       /* if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            locationManager = (LocationManager) mainView.getContext().getSystemService(LOCATION_SERVICE);*/


        //Set up Gesture listener

        mDetector = new GestureDetector(mainView.getContext(), new MyGestureListener());
        llDetails = mainView.findViewById(R.id.Details);
        llDetails.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                return mDetector.onTouchEvent(motionEvent);
            }
        });


        tvName = (TextView) mainView.findViewById(R.id.tvName);





        //Set button presses

        tvName.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                openWebPage(currentSite.getSite_url());
                return true;
            }
        });





        btnDirections = (ImageButton) mainView.findViewById(R.id.btnDirections);
        btnDirections.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //String origin = "origin=" + currentLocation.getLatitude()+","+ currentLocation.getLongitude();
                String destination = "destination=";

                destination += currentSite.getLocation().getLatitude() + "," + currentSite.getLocation().getLongitude();
                String alternatives = "alternatives=false";

                String departureTime = "departure_time=now";
                String mode = "mode=driving";
                String units = "units=metric";
                String directionUrl = "https://www.google.com/maps/dir/?api=1&" + destination + "&" + alternatives + "&" + departureTime + "&" + mode + "&" + units;
                //String directionUrl = "google.navigation:q=" + currentSite.location.getLatitude() + "," + currentSite.location.getLongitude();
                Uri gmmIntentUri = Uri.parse(directionUrl);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);

               /* FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fcvDetails, HistoricalSiteDirectionsFragment.class, null);
                ft.setReorderingAllowed(true)
                        .addToBackStack(null) // name can be null
                        .commit();*/
               //getDirectionsApi(currentSite);
            }
        });


        // Updates the "# away" textbox whenever the location changes
        mViewModel.getCurrentLocation().observe(getViewLifecycleOwner(), new Observer<Location>() {
            @Override
            public void onChanged(Location location) {
                displaySiteAddressAndDistance(location);
            }
        } );
        /*mViewModel.getCurrentSite().observe(getViewLifecycleOwner(), display -> {
            // Update the list UI
        });*/


        //setLlDisplayInfo(currentSite, mViewModel.getCurrentDisplayHeight().getValue());

        //Get Site info
        mDisposable.add(
                mViewModel.getHistoricalSiteDatabase().manitobaHistoricalSiteDao().getManitobaHistoricalSite(site_id)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe( manitobaHistoricalSites -> displayHistoricalSiteInfo( manitobaHistoricalSites),
                                throwable ->  Toast.makeText(getContext(), "Error retrieving site data", Toast.LENGTH_SHORT).show()
                        ));


        mDisposable.add(mViewModel.getHistoricalSiteDatabase().siteTypeDao().getAllSiteTypesForSite(site_id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe( siteTypes -> displaySiteType( siteTypes),
                        throwable ->  Toast.makeText(getContext(), "Error retrieving site types", Toast.LENGTH_SHORT).show()
                ));











    }


    //Gets and displays info for Manitoba Historical Site
    public void displayHistoricalSiteInfo(ManitobaHistoricalSite site)
    {
        try {
            currentSite = site;
            mViewModel.setCurrentSite(currentSite);
            llDisplayInfo.setVisibility(View.VISIBLE);
            tvName.setText(site.getName());
            displaySiteAddressAndDistance(mViewModel.getCurrentLocation().getValue());
            ((TextView) mainView.findViewById(R.id.tvMuni)).setText(site.getMunicipality() + ", " + site.getProvince());
            setSmall(mViewModel.getCurrentDisplayHeight().getValue());

            //Hopefully makes the description more readable
            String formattedDescription = site.getDescription().replace("\n", "\n\n");
            ((TextView) mainView.findViewById(R.id.tvDescription)).setText(formattedDescription);
        }
        catch (Exception e)
        {
            Log.e("Error", "displayHistoricalSiteInfo: Error displaying site info\n" + e.getMessage());
        }



    }

    //Gets and displays info for Manitoba Historical Site
    public void displaySiteType(List<SiteType> siteTypes)
    {
        try {
            if (siteTypes != null && siteTypes.size() > 0)
            {
                String allTypes = "";
                for (SiteType type: siteTypes) {
                    allTypes = allTypes  + type.getType().replace("%2f", " or ") + "/";
                }
                String displayTypes = allTypes.substring(0, allTypes.length() - 1);

                ((TextView) mainView.findViewById(R.id.tvTypes)).setText(displayTypes);


            }
        }
        catch (Exception e)
        {
            Log.e("Error", "displaySiteType: Error displaying site types\n" + e.getMessage());
        }


        
    }






    //Opens the web view activity and display the short or long link
    public void openWebPage(String url) {
        //url = "https://developer.android.com/reference/android/webkit/WebView";
        if (TextUtils.isEmpty(url)) {
            Toast.makeText(mainView.getContext(), "There is no addition information about the historic site " + currentSite.getName() + " in this app.", Toast.LENGTH_SHORT).show();
        } else {
            /*Uri webpage = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, webpage);

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);*/

            /*Intent intent = new Intent(getApplicationContext(), WebviewActivity.class);
            intent.putExtra(getString(R.string.webviewUrl), url);
            startActivity(intent);*/

            //Better and easier to pull up link in browser than in my own webview
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);


        }
    }

    /*public void setLlDisplayInfo(HistoricalSite site, DisplayHeight displayHeight) {
        //llPlaceInfo.setVisibility(View.GONE);
        llDisplayInfo.setVisibility(View.VISIBLE);

        //String buildDate = (TextUtils.isEmpty(site.getConstructionDate())? "": " (" + site.getConstructionDate() + ")");
        tvName.setText(site.getName());
        ((TextView) mainView.findViewById(R.id.tvAddress)).setText(site.getAddress());
        *//*if(displayHeight == DisplayHeight.SMALL)
        {
            ((LinearLayout) mainView.findViewById(R.id.llWebView)).setVisibility(View.GONE);
            ((LinearLayout) mainView.findViewById(R.id.llMoreInfo)).setVisibility(View.GONE);

        }
        else*//*


        setUpWebView();

        setSmall(displayHeight);







        llWebView = mainView.findViewById(R.id.llWebView);
        if(TextUtils.isEmpty(site.getShortUrl()))
        {
            llWebView.setVisibility(View.GONE);

            // To stop no urls from displaying blank space in the Relative Layout

            LinearLayout llLinkInfo = mainView.findViewById(R.id.llLinkInfo);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) llLinkInfo.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);

        }
        else
        {
            // (displayHeight);
            setWebViewContent(site.getShortUrl());
        }


    }*/

    //Sets text view data if it isn't null, else hide the textview
    private void setTextView(int viewId, String viewText)
    {
        TextView textView = mainView.findViewById(viewId);
        if (viewText != null && !viewText.trim().isEmpty())
        {
            textView.setVisibility(View.VISIBLE);
            textView.setText(viewText);
        }
        else
        {
            textView.setVisibility(View.GONE);
        }
    }

    //if the display is smoll, display no links
    private void setSmall(DisplayHeight displayHeight)
    {
//        Boolean result = false;
//       // ((LinearLayout) mainView.findViewById(R.id.llWebView)).setVisibility(View.VISIBLE);
//        ((LinearLayout) mainView.findViewById(R.id.llExtendedInfo)).setVisibility(View.VISIBLE);
//        if(displayHeight == DisplayHeight.SMALL)
//        {
//            //((LinearLayout) mainView.findViewById(R.id.llWebView)).setVisibility(View.GONE);
//            ((LinearLayout) mainView.findViewById(R.id.llExtendedInfo)).setVisibility(View.GONE);
//            result = true;
//        }
        NestedScrollView nsvMoreInfo =  (NestedScrollView) mainView.findViewById(R.id.nsvMoreInfo);
        nsvMoreInfo.setVisibility(displayHeight == DisplayHeight.SMALL? View.GONE: View.VISIBLE);
        TextView hasMoreInfo = (TextView) mainView.findViewById(R.id.tvHasMoreInfo);
        hasMoreInfo.setVisibility(displayHeight == DisplayHeight.SMALL? View.VISIBLE: View.GONE);




    }

    //Displays site address and its distance from the user location
    private void displaySiteAddressAndDistance (Location userLocation)
    {
        try{
            if (userLocation != null)
            {
                Float distance = currentSite.getLocation().distanceTo(userLocation) ;
                String distanceText = (distance >= 1000? String.format("%.2f",distance/1000) + " km": String.format("%.2f",distance) + " m");
                ((TextView) mainView.findViewById(R.id.tvAddress)).setText(currentSite.getAddress() + ", " + distanceText + " away");
            }

        } catch (Exception e)
        {
            Log.e("Error", "updateDistanceAway: Error updating user distance from the site\n" + e.getMessage());
        }
    }

    /*private void setWebViewHeight(DisplayHeight displayHeight)
    {
        int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
        Double maxHeightPercent = Double.parseDouble(displayHeight == DisplayHeight.MEDIUM? getString(R.string.medium_max_height_of_webview_percent): getString(R.string.full_max_height_of_webview_percent) );


        //int maxHeight = (int)(screenHeight * Double.parseDouble( getString(R.string.medium_max_height_of_webview_percent)));
        int maxHeight = (int)(screenHeight * maxHeightPercent);
        llWebView = mainView.findViewById(R.id.llWebView);
        ViewGroup.LayoutParams params = llWebView.getLayoutParams();
        params.height = maxHeight;
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        llWebView.setLayoutParams(params);
    }*/

   /* private void setWebViewContent(String siteURL)
    {
        String typeOfInfo = siteURL.contains("long.pdf")? "additional": "summary";
        String loadingMessage = "Loading " + typeOfInfo + " info ..." ;
        TextView tvLoading = (TextView) mainView.findViewById(R.id.tvLoadingUrl);
        tvLoading.setText(loadingMessage);


        //String pdf = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf";

        try {
            llWebView.setVisibility(View.VISIBLE);
            //webView.loadUrl("https://drive.google.com/viewerng/viewer?embedded=true&url=" + site.shortUrl);
            //   https://docs.google.com/gview?embedded=true&url=


            String cutFromURL = "https://";
            String processedURL = "https://docs.google.com/gview?embedded=true&url=" + siteURL.substring(siteURL.indexOf(cutFromURL) + cutFromURL.length());


            webView.loadUrl(processedURL);
            //webView.loadUrl(siteURL);


        } catch (Error e)
        {
            Toast.makeText(mainView.getContext(), "Error fetching " + typeOfInfo + " data:" + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("Error", "setWebViewContent: Error loading " + siteURL + " into webView \n" +  e.getMessage());
            llWebView.setVisibility(View.GONE);
        }
    }

    private void setUpWebView()
    {
        llWebView = mainView.findViewById(R.id.llWebView);
        webView = (WebView) mainView.findViewById(R.id.wvInfo);
        llLoadingInfo = (LinearLayout) mainView.findViewById(R.id.llLoadingInfo);

        try {
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setBuiltInZoomControls(true);
            webView.getSettings().setSupportZoom(true);
            webView.setInitialScale(200);








            webView.setWebViewClient(new WebViewClient() {
                boolean loadingFinished = true;
                boolean redirect = false;

                //If redirect, load url again
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    if (!loadingFinished) {
                        redirect = true;
                    }

                    loadingFinished = false;
                    webView.loadUrl(request.getUrl().toString());
                    return true;
                }

                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                    loadingFinished = false;
                    llLoadingInfo.setVisibility(View.VISIBLE);
                    webView.setVisibility(View.GONE);

                }

                public void onPageFinished(WebView view, String url) {
                    if (!redirect) {
                        loadingFinished = true;
                        llLoadingInfo.setVisibility(View.GONE);
                        webView.setVisibility(View.VISIBLE);
                    } else {
                        redirect = false;
                        Toast.makeText(mainView.getContext(), "There was error loading additional information (Redirect)", Toast.LENGTH_LONG).show();
                    }
                }

                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                    super.onReceivedError(view, request, error);

                    webView.setVisibility(View.GONE);
                    Toast.makeText(mainView.getContext(), "There was error loading additional information", Toast.LENGTH_LONG).show();

                    Log.e("Error", "setUpWebView: Error fetching  url:" + request.getUrl().toString() + "\n" +  error.getDescription());

                }
            });

        }
        catch (Exception e)
        {
            Log.e("Error", "setWebViewContent: Error fetching  url into webView \n" +  e.getMessage());
        }




    }*/


    @Override
    public void onStop() {
        super.onStop();
        mDisposable.clear();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (currentSite != null)
        {
            displayHistoricalSiteInfo(currentSite);
        }


    }

    @Override
    public void onPause() {
        try {
            super.onPause();

        } catch (Exception e)
        {
            Log.e("Error", "Display Details On Pause:" + e.getMessage());
            super.onPause();
        }

    }


    class MyGestureListener extends GestureDetector.SimpleOnGestureListener

    {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e) {



            return true;

        }
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Boolean result = true;
            DisplayHeight displayHeight = mViewModel.getCurrentDisplayHeight().getValue();
            try {
                float distanceX = e2.getX() - e1.getX();
                float distanceY = e2.getY() - e1.getY();
                if (Math.abs(distanceY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD &&  Math.abs(distanceY) > Math.abs(distanceX))
                {
                    //To make sure that the newHeight has a default value
                    DisplayHeight newHeight = displayHeight;
                    if (distanceY > 0) {
                        newHeight =  DisplayHeight.SMALL;
                    } else {
                        newHeight =  DisplayHeight.FULL;
                    }


                    if(displayHeight != newHeight)
                    {

                        mViewModel.setCurrentDisplayHeight(newHeight);
                        setSmall(newHeight);



                        /*if (newHeight != DisplayHeight.SMALL)
                            setWebViewHeight(newHeight);*/
                    }


                    //setLlDisplayInfo(currentSite,newHeight);
                    /*if (distanceY > 0) {
                        setLlDisplayInfo(currentSite,false);
                    } else {
                        setLlDisplayInfo(currentSite,true);
                    }*/



                }

            } catch (Exception e) {
                Log.e("Error", "MyGestureListener: Error when implementing gestures\n" + e.getMessage());
                result = false;
            }

            return result;
        }
    }


    //Gets theme colours.
    public int getThemeColour (int themeID ) {
        final TypedValue value = new TypedValue();
        getContext().getTheme ().resolveAttribute (themeID, value, true);
        return value.data;
    }
}
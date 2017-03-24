package com.catchoom.test;

import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.craftar.CraftARBoundingBox;
import com.craftar.CraftARCamera;
import com.craftar.CraftARError;
import com.craftar.CraftAROnDeviceIR;
import com.craftar.CraftARResult;
import com.craftar.CraftARSDK;
import com.craftar.CraftARSearchResponseHandler;
import com.craftar.CraftARActivity;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class CaptureActivity extends CraftARActivity implements CraftARSearchResponseHandler {

    // Singleton classes
    CraftAROnDeviceIR mOnDeviceIR;
    CraftARSDK mCraftARSDK;
    CraftARCamera mCamera;
    // Regular classes
    TrackingBox trackingBox;

    @Override
    public void onPostCreate() {
        View mainLayout = getLayoutInflater().inflate(R.layout.camera_overlay, null);
        setContentView(mainLayout);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initializeCraftAR();
        initializeTrackingBox();

        // Hide capture button
        final Button captureButton = (Button) findViewById(R.id.capture_button);
        captureButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startCapture();
            }
        });

        // Show restart button, attach functionality, and start
        final Button restartButton = (Button) findViewById(R.id.restart_button);
        restartButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                restartCapture();
            }
        });

    }

    private void initializeCraftAR() {
        //Obtain an instance of the CraftARSDK (which manages the camera interaction).
        //Note we already called CraftARSDK.init() in the Splash Screen, so we don't have to do it again
        mCraftARSDK = CraftARSDK.Instance();
        mCraftARSDK.startCapture(this);

        //Get the instance to the OnDeviceIR singleton (it has already been initialized in the SplashScreenActivity, and the collections are already loaded).
        mOnDeviceIR = CraftAROnDeviceIR.Instance();

        //Tell the SDK that the OnDeviceIR who manage the calls to singleShotSearch() and startFinding().
        //In this case, as we are using on-device-image-recognition, we will tell the SDK that the OnDeviceIR singleton will manage this calls.
        mCraftARSDK.setSearchController(mOnDeviceIR.getSearchController());

        //Tell the SDK that we want to receive the search responses in this class.
        mOnDeviceIR.setCraftARSearchResponseHandler(this);

        //Obtain the reference to the camera, to be able to restart the camera, trigger focus etc.
        //Note that if you use single-shot, you will always have to obtain the reference to the camera to restart it after you take the snapshot.
        mCamera = mCraftARSDK.getCamera();
    }

    public void initializeTrackingBox() {
        trackingBox = (TrackingBox) findViewById(R.id.tracking_box);
        trackingBox.setLayout((RelativeLayout) findViewById(R.id.camera_overlay));
        trackingBox.setHeader((TextView) findViewById(R.id.overlay_header));
        trackingBox.setBody((ImageView) findViewById(R.id.overlay_body));
        trackingBox.setDescription((TextView) findViewById(R.id.overlay_text));
        trackingBox.reset();
    }

    private void startCapture() {
        mCraftARSDK.singleShotSearch();
    }

    private void restartCapture() {
        trackingBox.reset();
        mCraftARSDK.getCamera().restartCapture();
    }

    public void searchResults(ArrayList<CraftARResult> results, long searchTimeMillis, int requestCode) {
        if(results.size() > 0){
            if(mCraftARSDK.isFinding()) {
                mCraftARSDK.stopFinder();
            }
            CraftARResult result = results.get(0); // Top result
            String name = result.getItem().getItemName();
            CraftARBoundingBox box = result.getBoundingBox();

            Log.d(TAG, "Found :" + name);
            trackingBox.setHeaderText(result.getItem().getItemName());
            trackingBox.assignPosition(box);
        }
        else {
            Log.e(TAG, "Nothing found");
            Toast.makeText(getApplicationContext(), "Nothing found ", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void searchFailed(CraftARError error, int requestCode) {
        Log.e(TAG, "Search failed( "+error.getErrorCode()+"):"+error.getErrorMessage());
        Toast.makeText(getApplicationContext(), "Search failed", Toast.LENGTH_SHORT).show();
        startCapture();
    }
    @Override
    public void onCameraOpenFailed(){
        Log.e(TAG, "Camera failed to open");
        Toast.makeText(getApplicationContext(), "Camera failed to open", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPreviewStarted(int i, int i1) {
        Log.d(TAG, "Preview started");
    }

}
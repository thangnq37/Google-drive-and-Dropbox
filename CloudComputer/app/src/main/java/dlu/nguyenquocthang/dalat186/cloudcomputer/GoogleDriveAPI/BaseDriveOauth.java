package dlu.nguyenquocthang.dalat186.cloudcomputer.GoogleDriveAPI;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;

import dlu.nguyenquocthang.dalat186.cloudcomputer.MainActivity;

/**
 * Created by nguyenquocthang on 07/09/2017.
 */

public abstract class BaseDriveOauth extends ActionBarActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {


    /**
     * Request code for auto Google Play Services error resolution.
     * Giá trị trả về khi lấy dữ liệu
     */
    protected static final int REQUEST_CODE = 1;


    /**
     * Google API client.
     */
    private GoogleApiClient googleApiClient;




    @Override
    protected void onStart() {
        super.onStart();

    }

    /**
     * thực hiện khi các lớp kế thừa lớp này thì người dùng
     * thực hiện kết nối thì nó sẽ kiểm tra xem googleApiClient
     * có kết nối hay chưa nếu thực hiện rồi thì nó kết nốt và
     * có thể gọi googleApiClient để gọi API từ Drive
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addScope(Drive.SCOPE_APPFOLDER)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        googleApiClient.connect();
    }

    /**
     * Handles resolution callbacks.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            googleApiClient.connect();
        }
    }

    /**
     * Called when activity gets invisible. Connection to Drive service needs to
     * be disconnected as soon as an activity is invisible.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (googleApiClient != null) {
            googleApiClient.disconnect();
        }

    }

    /**
     * Called when {@code mGoogleApiClient} is connected.
     */
    @Override
    public void onConnected(Bundle connectionHint) {


    }

    /**
     * Called when {@code mGoogleApiClient} is disconnected.
     */
    @Override
    public void onConnectionSuspended(int cause) {
        Log.i("Oauth", "GoogleApiClient connection suspended");
    }


    /**
     * Called when {@code mGoogleApiClient} is trying to connect but failed.
     * Handle {@code result.getResolution()} if there is a resolution is
     * available.
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {

        if (result.hasResolution()) {
            try {
                result.startResolutionForResult(this, REQUEST_CODE);
            } catch (IntentSender.SendIntentException e) {
                // Unable to resolve, message user appropriately
            }
        } else {
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this, 0).show();
        }
    }

    /**
     * Shows a toast message.
     */
    public void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }


    private void signInGoogle(){
        if(googleApiClient!=null){
            Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
            startActivityForResult(intent,REQUEST_CODE);
        }
    }

    public void signOutGoogle(){
        if(googleApiClient != null || googleApiClient.isConnected()){
            googleApiClient.clearDefaultAccountAndReconnect().setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                    googleApiClient.disconnect();
                    Intent ic1 = new Intent(getBaseContext(), MainActivity.class);
                    finish();
                    startActivity(ic1);
                }
            });
        }
    }


    public GoogleApiClient getGoogleApiClient() {
        return googleApiClient;
    }



}

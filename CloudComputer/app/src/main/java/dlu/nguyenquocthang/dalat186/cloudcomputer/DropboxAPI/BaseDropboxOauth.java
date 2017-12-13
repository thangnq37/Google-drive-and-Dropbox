package dlu.nguyenquocthang.dalat186.cloudcomputer.DropboxAPI;

import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;

import com.dropbox.client2.session.Session;
import com.dropbox.core.android.Auth;

/**
 * Created by nguyenquocthang on 30/10/2017.
 */

public abstract class BaseDropboxOauth extends ActionBarActivity {
    protected final static String DROPBOX_NAME="dropbox_prefs";
    protected final static String APP_KEY="0xew1mmepo51kx8";
    protected final static String APP_SECRET="9cmzz90nv3tombk";
    protected final static Session.AccessType ACCESS_TYPE = Session.AccessType.DROPBOX;

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = getSharedPreferences(DROPBOX_NAME, MODE_PRIVATE);
        String accessToken = prefs.getString("access-token", null);
        if (accessToken == null) {
            accessToken = Auth.getOAuth2Token();
            if (accessToken != null) {
                prefs.edit().putString("access-token", accessToken).apply();
                initAndLoadData(accessToken);
            }
        } else {
            initAndLoadData(accessToken);
        }
    //
        String uid = Auth.getUid();
        String storedUid = prefs.getString("user-id", null);
        if (uid != null && !uid.equals(storedUid)) {
            prefs.edit().putString("user-id", uid).apply();
        }
    }

    private void initAndLoadData(String accessToken) {
        DropboxClientFactory.init(accessToken);
        PicassoClient.init(getApplicationContext(), DropboxClientFactory.getClient());
        loadData();
    }

    protected abstract void loadData();

    protected boolean hasToken() {
        SharedPreferences prefs = getSharedPreferences("dropbox-sample", MODE_PRIVATE);
        String accessToken = prefs.getString("access-token", null);
        return accessToken != null;
    }

    /**
     * Shows a toast message.
     */
    public void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}

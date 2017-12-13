package dlu.nguyenquocthang.dalat186.cloudcomputer.DropboxAPI;

import android.os.AsyncTask;
import android.os.Environment;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by nguyenquocthang on 02/11/2017.
 */

public class OpenFileTask extends AsyncTask<FileMetadata, Void, File> {

    private final DbxClientV2 mDbxClient;
    private final Callback mCallback;
    private Exception mException;

    public interface Callback {
        void onDownloadComplete(File result);
        void onError(Exception e);
    }

    public OpenFileTask(DbxClientV2 dbxClient, Callback callback) {
        mDbxClient = dbxClient;
        mCallback = callback;
    }

    @Override
    protected void onPostExecute(File result) {
        super.onPostExecute(result);
        if (mException != null) {
            mCallback.onError(mException);
        } else {
            mCallback.onDownloadComplete(result);
        }
    }

    @Override
    protected File doInBackground(FileMetadata... params) {
        FileMetadata metadata = params[0];
        try {
            //File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File path = new File(Environment.getExternalStorageDirectory()+"/ThangAPI/test");
            if(!path.exists()){
                path.mkdirs();
            }
            File file = new File(path, metadata.getName());
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // Make sure the Downloads directory exists.
            if (!path.exists()) {
                if (!path.mkdirs()) {
                    mException = new RuntimeException("Unable to create directory: " + path);
                }
            } else if (!path.isDirectory()) {
                mException = new IllegalStateException("Download path is not a directory: " + path);
                return null;
            }

            // Download the file.
            try (
                    OutputStream outputStream = new FileOutputStream(file)) {
                        mDbxClient.files().download(metadata.getPathLower(), metadata.getRev())
                        .download(outputStream);
            }


            return file;
        } catch (DbxException | IOException e) {
            mException = e;
        }

        return null;
    }
}

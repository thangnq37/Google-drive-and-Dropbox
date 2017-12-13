package dlu.nguyenquocthang.dalat186.cloudcomputer.GoogleDriveAPI;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import dlu.nguyenquocthang.dalat186.cloudcomputer.MethodGlosbe.ClassGolsbe;
import dlu.nguyenquocthang.dalat186.cloudcomputer.R;

/**
 * Created by nguyenquocthang on 27/11/2017.
 */

public class DownloadFile extends AsyncTask<Void, Void, Boolean> {
    private DriveId driveId;
    private File filename;
    private boolean openFile;
    private GoogleApiClient bdo;
    private Context context;
    private DatabaseDriveLink database;


    public DownloadFile(final DriveId driveId, final File filename, final boolean openFile, Context context, DatabaseDriveLink datatbase, GoogleApiClient googleApiClient) {
        this.driveId = driveId;
        this.filename = filename;
        this.openFile = openFile;
        this.context = context;
        this.database = datatbase;
        this.bdo = googleApiClient;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (!filename.exists()) {
            try {
            filename.createNewFile();
            } catch (IOException e) {
            e.printStackTrace();
            }
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);

        if(openFile){
            try {
                ClassGolsbe.openFile(this.context,filename);
            } catch (IOException e) {
                e.printStackTrace();
            }
            database.QueryData("INSERT INTO LinksIDLocal(driveID, " +
            "URILocal,Uploaded) VALUES('"+driveId+"','" +
            filename.getPath()+"',0 )");
        }else{
            database.QueryData("INSERT INTO LinksIDLocal(driveID, " +
            "URILocal,Uploaded) VALUES('"+driveId+"','" +
            filename.getPath()+"',1 )");
            showAlertDialogOpenFile(driveId);
        }

    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            DriveFile file = Drive.DriveApi.getFile(bdo, driveId);
            file.getMetadata(bdo)
            .setResultCallback(metadataRetrievedCallback);
            DriveApi.DriveContentsResult driveContentsResult = file.open(
                    bdo,
            DriveFile.MODE_READ_WRITE, null).await();
            DriveContents driveContents = driveContentsResult
            .getDriveContents();

            ParcelFileDescriptor parcelFileDescriptor = driveContents.getParcelFileDescriptor();
            FileInputStream fileInputStream = new FileInputStream(parcelFileDescriptor.getFileDescriptor());

            FileOutputStream fileOutput = new FileOutputStream(filename);

            byte[] buffer = new byte[1024];
            int bufferLength = 0;
            while ((bufferLength = fileInputStream.read(buffer)) > 0) {
                fileOutput.write(buffer, 0, bufferLength);
            }
            fileOutput.close();
            fileInputStream.close();
        } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
        }
            return true;
    }

    private ResultCallback<DriveResource.MetadataResult> metadataRetrievedCallback = new ResultCallback<DriveResource.MetadataResult>() {
    @Override
    public void onResult(DriveResource.MetadataResult result) {
            if (!result.getStatus().isSuccess()) {
                return;
            }
        }
    };

    /**
     * thực hiện show alert dialog thông báo có thực hiện mở tập tin  vừa tải xuống local hay không
     * @param driveId
     */
    private void showAlertDialogOpenFile(final DriveId driveId){
        final Cursor data = database.GetData("SELECT * FROM LinksIDLocal WHERE driveID='"+driveId+"'");
        final AlertDialog.Builder alerBuilder = new AlertDialog.Builder(context);
        alerBuilder.setTitle("THÔNG BÁO");
        alerBuilder.setIcon(R.drawable.open);
        alerBuilder.setMessage("Bạn có muốn mở tập tin này không");
        alerBuilder.setPositiveButton("Có", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ClassGolsbe.openFile(context,data);
            }
        });
        alerBuilder.setNegativeButton("Không", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alerBuilder.show();
    }
}

package dlu.nguyenquocthang.dalat186.cloudcomputer.GoogleDriveAPI;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveApi.MetadataBufferResult;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveFolder.DriveFolderResult;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import dlu.nguyenquocthang.dalat186.cloudcomputer.MainActivity;
import dlu.nguyenquocthang.dalat186.cloudcomputer.MethodGlosbe.ClassGolsbe;
import dlu.nguyenquocthang.dalat186.cloudcomputer.R;
import ru.whalemare.sheetmenu.SheetMenu;

import static com.google.android.gms.drive.query.Filters.eq;


public class ListFilesInFolderActivity extends BaseDriveOauth  {

    //View
    private ListView lvListFolder,lvListFile;
    private ResultsAdapter mResultsAdapter,mResultsFileAdapter;
    private  TextView txtFolder,txtFile;
    // database
    private DatabaseDriveLink database;
    //==========================================
    private MetadataBufferResult metadataBufferResultListFolder;
    private MetadataBufferResult metadataBufferResultListFile;

    private int indexBackSave;
    //========================================================
    //các biến tĩnh.
    private static Uri uri;
    private static List<DriveId> listDriveId;
    private static String nameFolder;
    private static int dem;
    private static boolean editSetTrue;
    protected static final int REQUEST_CODE = 2;
    //    private Uri uri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CreateDatabase dataB= new CreateDatabase(this);
        database = dataB.createDatabase();
        editSetTrue = false;
        uri = null;
        listDriveId = new ArrayList<>();
        indexBackSave = 0;
        dem = 0;
        nameFolder = "";


        setContentView(R.layout.activity_list_files_in_folder);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().hide();


        //ánh xạ
        txtFile = (TextView)findViewById(R.id.textViewFile);
        txtFolder =(TextView)findViewById(R.id.textViewFolder);
        lvListFile = (ListView)findViewById(R.id.listViewFiles);
        lvListFolder = (ListView) findViewById(R.id.listViewResults);
        mResultsAdapter = new ResultsAdapter(ListFilesInFolderActivity.this,R.layout.activity_detail_listview);
        mResultsFileAdapter = new ResultsAdapter(ListFilesInFolderActivity.this,R.layout.activity_detail_listview);

        txtFile.setVisibility(View.GONE);
        txtFolder.setVisibility(View.GONE);


        lvListFolder.setAdapter(mResultsAdapter);
        lvListFile.setAdapter(mResultsFileAdapter);




        lvListFolder.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Metadata metadata = metadataBufferResultListFolder.getMetadataBuffer().get(position);
                setAccessFolderAndAccessOpenFiles(metadata);
            }
        });

        lvListFolder.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showPopupForFolder(position);
                return true;
            }
        });


        lvListFile.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Metadata metadata = metadataBufferResultListFile.getMetadataBuffer().get(position);
                openFile(metadata);
            }
        });

        lvListFile.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showMenuFile(position);
                return true;
            }
        });


    }
    private static Metadata metadata = null;
    /**
     * hiển thị menu cáo tác vụ của tập tin FIle
     * @param position
     */
    public void showMenuFile(final int position){

        SheetMenu.with(ListFilesInFolderActivity.this)
                .setTitle("Thực hiện tác vụ dưới:")
                .setMenu(R.menu.menu_lv_file)
                .setAutoCancel(true)
                .showIcons(true)
                .setClick(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        switch (item.getItemId()){
                            case R.id.menuDeleteFile:
                                deleteFile(position);
                                break;
                            case R.id.menuRenameFile:
                                showDialogRenameFile(position);
                                break;
                            case R.id.menuEditFile:
                                metadata = metadataBufferResultListFile.getMetadataBuffer().get(position);
                                //openEditFile(metadata);
                                //openEditFileNew(metadata);
                                openFileEditFile(metadata);
                                break;
                            case R.id.menuDownFile:
                                metadata = metadataBufferResultListFile.getMetadataBuffer().get(position);
                                Cursor checkID = database.GetData("SELECT * FROM LinksIDLocal WHERE driveID='"+metadata.getDriveId()+"'");
                                if(!checkID.moveToNext()) {
                                    downloadFileRootAndFileFolder(metadata,false);
                                }else{
                                    showAlertDialogOpenFile(metadata.getDriveId());
                                }
                                break;
                            case R.id.menuTest:
                                metadata = metadataBufferResultListFile.getMetadataBuffer().get(position);
                                Cursor data = database.GetData("SELECT * FROM  LinksIDLocal WHERE driveID='"+metadata.getDriveId()+"'");
                                //openFile(data);
                                break;
                        }
                        return false;
                    }
                }).show();
    }


    final private ResultCallback<DriveResource.MetadataResult> getLinkCreateFolder = new ResultCallback<DriveResource.MetadataResult>() {
        @Override
        public void onResult(@NonNull DriveResource.MetadataResult result) {
            dem++;
            nameFolder += "/"+result.getMetadata().getTitle();
            showMessage(nameFolder);
            if(dem==listDriveId.size()){

                File dir = new File(Environment.getExternalStorageDirectory() + nameFolder);
                if(!dir.exists()){
                    dir.mkdirs();
                }
                new DownloadFile(metadata.getDriveId(),new File(Environment.getExternalStorageDirectory() + nameFolder
                        ,metadata.getTitle()),editSetTrue,getBaseContext()).execute();
                editSetTrue = false;
            }
        }
    };

    /**
     * tải tập tin trong thu mục gốc và file trong thư mục
     * @param meta
     */
    private void downloadFileRootAndFileFolder(Metadata meta,boolean editTrue){
        nameFolder="/ThangAPI/googleApiThang";
        dem =0;
        int numberDriveId = listDriveId.size();
        dialog = new ProgressDialog(this);
        dialog.setMessage("Đang tải ...");
        dialog.show();
        if (numberDriveId > 0) {
            editSetTrue = true;
            List<DriveId> listIDFolder = listDriveId;
            if (numberDriveId > 0) {
                for (int i = 0; i < numberDriveId; i++) {
                    DriveFolder folder = Drive.DriveApi.getFolder(getGoogleApiClient(), listIDFolder.get(i));
                    folder.getMetadata(getGoogleApiClient()).setResultCallback(getLinkCreateFolder);
                }
            }
        } else {
            new DownloadFile(meta.getDriveId(), new File(Environment.getExternalStorageDirectory() + nameFolder
                    , meta.getTitle()), editTrue,this).execute();
        }
        dialog.dismiss();
    }

    /**
     * thực hiện show alert dialog thông báo có thực hiện mở tập tin  vừa tải xuống local hay không
     * @param driveId
     */
    private void showAlertDialogOpenFile(final DriveId driveId){
        final Cursor data = database.GetData("SELECT * FROM LinksIDLocal WHERE driveID='"+driveId+"'");
        final AlertDialog.Builder alerBuilder = new AlertDialog.Builder(this);
        alerBuilder.setTitle("THÔNG BÁO");
        alerBuilder.setIcon(R.drawable.open);
        alerBuilder.setMessage("Bạn có muốn mở tập tin này không");
        alerBuilder.setPositiveButton("Có", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ClassGolsbe.openFile(getBaseContext(),data);
            }
        });
        alerBuilder.setNegativeButton("Không", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alerBuilder.show();
    }


    /**
     * thực hiện tác vụ cập nhật lại các file khi bị thay đổi
     * lấy từ database các file thay đổi mà chưa được cập nhật lại trên google drive
     */
    private void getValueSetDataForFile(){
        Cursor data = database.GetData("SELECT * FROM LinksIDLocal WHERE Uploaded=0");
        String driveID = null,URILocal = null;
        if(!isDeviceOnline()){
            showMessage("Vui lòng kết nối mạng...");
            return;
        }

        while (data.moveToNext()){
            driveID = data.getString(0);
            URILocal = data.getString(1);
            final String finalURILocal = URILocal;
            database.QueryData("UPDATE LinksIDLocal SET Uploaded=1 WHERE driveID = '"+driveID+"'");
            DriveFile fileEdit = DriveId.decodeFromString(driveID).asDriveFile();
            final Uri dataUri = Uri.fromFile(new File(finalURILocal));
            fileEdit.open(getGoogleApiClient(),DriveFile.MODE_WRITE_ONLY,null).setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
               @Override
               public void onResult(@NonNull DriveApi.DriveContentsResult driveContentsResult) {
                   if (!driveContentsResult.getStatus().isSuccess()) {
                       // Handle error
                       return;
                   }
                   final DriveContents driveContents = driveContentsResult.getDriveContents();


                   new Thread() {
                       @Override
                       public void run() {

                           OutputStream outputStream = driveContents.getOutputStream();
                           try {
                               InputStream inputStream = getContentResolver().openInputStream(dataUri);

                               if (inputStream != null) {
                                   byte[] data = new byte[1024];
                                   while (inputStream.read(data) != -1) {
                                       outputStream.write(data);
                                   }
                                   inputStream.close();
                               }

                               outputStream.close();
                           } catch (IOException e) {
                           }
                           driveContents.commit(getGoogleApiClient(),null);

                       }
                   }.start();
               }
            });

            if(data.isLast()){
                showMessage("Đã cập nhật lên google drive !!!");
            }
        }


    }

    /**
     * kiểm tra hiện tại có mạng hay không
     * @return
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }



    /**
     * lấy giá trị tên file của tập tin
     * @param metadata
     * @return
     */
    private String getNameFile(Metadata metadata){
        String meta = metadata.getTitle();
        int index = -1;
        index =meta.lastIndexOf(".");
        String test="0";
        if(index>0)
            test = meta.substring(0,index);
        return test;
    }

    /**
     * đặc lại title co folder
     */
    private void setTitleInFolder(){
        int sizeListDriveID = listDriveId.size();
        if(sizeListDriveID<=0){
            setTitle("Thư mục gốc");
        }else{
            DriveFolder folder = Drive.DriveApi.getFolder(getGoogleApiClient(),getDriveIdFolderStatus());
            folder.getMetadata(getGoogleApiClient()).setResultCallback(getTitleFolderCallBack);
        }
    }

    final private  ResultCallback<DriveResource.MetadataResult> getTitleFolderCallBack = new ResultCallback<DriveResource.MetadataResult>() {
        @Override
        public void onResult(@NonNull DriveResource.MetadataResult result) {
            setTitle(result.getMetadata().getTitle());
        }
    };






    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().

            uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                dialog = new ProgressDialog(this);
                dialog.setMessage("Đang tải lên ...");
                dialog.show();
                Drive.DriveApi.newDriveContents(getGoogleApiClient()).setResultCallback(uploadCallback);

            }
        }
    }

    ResultCallback<DriveApi.DriveContentsResult> uploadCallback =new ResultCallback<DriveApi.DriveContentsResult>() {
        @Override
        public void onResult(@NonNull DriveApi.DriveContentsResult result) {
            if (!result.getStatus().isSuccess()) {
                showMessage("Error while trying to create new file contents");
                return;
            }



            final DriveContents driveContents = result.getDriveContents();
            final Uri finalUri = uri;
            final String wholeID = ClassGolsbe.getPath(getBaseContext(),uri);
            // Perform I/O off the UI thread.
            new Thread() {
                @Override
                public void run() {

                    OutputStream outputStream = driveContents.getOutputStream();
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(finalUri);

                        if (inputStream != null) {
                            byte[] data = new byte[1024];
                            while (inputStream.read(data) != -1) {
                                outputStream.write(data);
                            }
                            inputStream.close();
                        }

                        outputStream.close();
                    } catch (IOException e) {
                    }
                    String nameFile = getNameInUriPath(wholeID);
                    String mineTypeFile = ClassGolsbe.getTypeFile(nameFile);
                    MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                            .setTitle(nameFile)
                            .setMimeType(mineTypeFile)
                            .setStarred(true).build();

                    int indexRoot = listDriveId.size();
                    DriveId driveId;
                    if(indexRoot>0){

                        driveId = getDriveIdFolderStatus();

                    }else{
                        driveId = Drive.DriveApi.getRootFolder(getGoogleApiClient()).getDriveId();
                    }


                    //showMessage("driveid" + driveId.getResourceId());
                    final DriveFolder folder = driveId.asDriveFolder();

                    // create a file on root folder
                    folder.createFile(getGoogleApiClient(), changeSet, driveContents).setResultCallback(uploadCallbackResult);

                }
            }.start();
        }
    };


    final private ResultCallback<DriveFolder.DriveFileResult> uploadCallbackResult = new
            ResultCallback<DriveFolder.DriveFileResult>() {
                @Override
                public void onResult(DriveFolder.DriveFileResult result) {
                    if (!result.getStatus().isSuccess()) {
                        showMessage("Không thể upload");
                        return;
                    }
                    showMessage("Upload thành công");
                    reLoadListViewFilesInFolder();
                    dialog.dismiss();
                }
            };

    /**
     * lấy giá trị link file trong đường dẫn / uri hoặc url
     * @param path
     * @return
     */
    private String getNameInUriPath(String path){
        String[] arrayStr = path.split("/");
        return arrayStr[arrayStr.length-1];
    }



    private ResultCallback<DriveApi.DriveIdResult> EditFileCallback =
            new ResultCallback<DriveApi.DriveIdResult>() {
                @Override
                public void onResult(@NonNull DriveApi.DriveIdResult driveIdResult) {
                    if (!driveIdResult.getStatus().isSuccess()) {
                        showMessage("Cannot find DriveId. Are you authorized to view this file?");
                        return;
                    }
                    DriveId driveId = driveIdResult.getDriveId();
                    DriveFile file = driveId.asDriveFile();
                    file.open(getGoogleApiClient(), DriveFile.MODE_READ_ONLY, null)
                            .setResultCallback(OpenedEditFileCallback);
                }
            };

    ResultCallback<DriveApi.DriveContentsResult> OpenedEditFileCallback =
            new ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(DriveApi.DriveContentsResult result) {
                    if (!result.getStatus().isSuccess()) {
                        return;
                    }
                    // DriveContents object contains pointers
                    // to the actual byte stream
                    String contentsAsString = null;
                    DriveContents contents = result.getDriveContents();
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(contents.getInputStream()));
                        StringBuilder builder = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            builder.append(line);
                        }
                        contentsAsString = builder.toString();
                    }catch (IOException e){
                        e.printStackTrace();
                    }

                    showDialogOpenEditFile(contentsAsString,contents);

                }
            };

//==============================================================================
    /**
     * thực hiện mở file tập tin là file doc or txt docx Read only
     * @param metadata
     */
    private void openEditFile(Metadata metadata){
        String tailFile = getTailFile(metadata);
        if(tailFile.equals(".txt")||tailFile.equals(".doc")||tailFile.equals(".docx")){
            Drive.DriveApi.fetchDriveId(getGoogleApiClient(), metadata.getDriveId().getResourceId())
                    .setResultCallback(contentsOpenedEditFileCallback);
        }
    }

    private ResultCallback<DriveApi.DriveIdResult> contentsOpenedEditFileCallback =
            new ResultCallback<DriveApi.DriveIdResult>() {
                @Override
                public void onResult(@NonNull DriveApi.DriveIdResult driveIdResult) {
                    if (!driveIdResult.getStatus().isSuccess()) {
                        showMessage("Cannot find DriveId. Are you authorized to view this file?");
                        return;
                    }
                    new RetrieveDriveEditFileContentsAsyncTask(
                            ListFilesInFolderActivity.this).execute(driveIdResult.getDriveId());
                }
            };

    final private class RetrieveDriveEditFileContentsAsyncTask
            extends ApiClientAsyncTask<DriveId, String, String> {

        public RetrieveDriveEditFileContentsAsyncTask(Context context) {
            super(context);
        }



        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackgroundConnected(DriveId... params) {
            String contents = null;
            BufferedReader br = null;
            StringBuilder sb=null;

            DriveFile file = params[0].asDriveFile();
            DriveApi.DriveContentsResult driveContentsResult =
                    file.open(getGoogleApiClient(), DriveFile.MODE_READ_ONLY, null).await();

            if (!driveContentsResult.getStatus().isSuccess()) {
                return null;
            }
            DriveContents driveContents = driveContentsResult.getDriveContents();
            try {
                ParcelFileDescriptor parcelFileDescriptor = driveContents.getParcelFileDescriptor();
                FileInputStream fileInputStream = new FileInputStream(parcelFileDescriptor.getFileDescriptor());
                // Read to the end of the file.
                br = new BufferedReader(new InputStreamReader(fileInputStream,"utf-8"));
                sb = new StringBuilder();
                String line;
                while(( line = br.readLine()) != null ) {
                    sb.append( line );
                    sb.append( '\n' );
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(sb!=null){
                contents = sb.toString();
            }

            publishProgress(contents);
            return contents;

        }

        @Override
        protected void onProgressUpdate(String... values) {
            showMessage(values[0]);
            super.onProgressUpdate(values);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            showMessage(result);
            reLoadListViewFilesInFolder();

        }



    }



//========================================================================

    private static String resuleEditFile;

    /**
     * hiển thị Dialog chứa nội dung file tập tin FILE
     */
    private void showDialogOpenEditFile(final String result, final DriveContents contents){
        ImageButton btnBack;
        Button btnSave;
        final EditText edtEditFile;

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_edit_file);
        //ánh xạ
        btnBack = (ImageButton) dialog.findViewById(R.id.imageButtonClose);
        edtEditFile = (EditText) dialog.findViewById(R.id.editTextEditFile);
        btnSave = (Button) dialog.findViewById(R.id.buttonSave);
        edtEditFile.setText(result);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resuleEditFile = result;
                dialog.dismiss();
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resuleEditFile = edtEditFile.getText().toString();
                DriveId driveId = contents.getDriveId();
                DriveFile file = driveId.asDriveFile();
                file.open(getGoogleApiClient(), DriveFile.MODE_WRITE_ONLY, null)
                        .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                            @Override
                            public void onResult(@NonNull DriveApi.DriveContentsResult driveContentsResult) {
                                try {
                                    DriveContents driveContents = driveContentsResult.getDriveContents();
                                    OutputStream outputStream = driveContents.getOutputStream();
                                    outputStream.write(resuleEditFile.getBytes());
                                    driveContents.commit(getGoogleApiClient(),null);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                dialog.dismiss();
                                reLoadListViewFilesInFolder();

                            }
                        });

            }
        });
        dialog.show();
    }

    /**
     * mở tập tin thông qua chương trình có sẵn tại file local.
     * trước khi sửa tập tin tải về file cục bộ rồi thực hiện cập nhật file
     * @param metadata
     */
    private void openFileEditFile(Metadata metadata){
        Cursor data = database.GetData("SELECT * FROM LinksIDLocal WHERE driveID = '"+metadata.getDriveId()+"'");
        if(data!=null&&data.moveToFirst()){
            ClassGolsbe.openFile(getBaseContext(),data);
        }else{
            showAlertDialogDownloadFile(metadata);
        }
        database.QueryData("UPDATE LinksIDLocal SET Uploaded=0 WHERE driveID = '"+metadata.getDriveId()+"'");
    }



    private void showAlertDialogDownloadFile(final Metadata meta){

        final AlertDialog.Builder alerBuilder = new AlertDialog.Builder(this);
        alerBuilder.setTitle("THÔNG BÁO");
        alerBuilder.setIcon(R.drawable.download_d);
        alerBuilder.setMessage("Bạn có muốn tải về máy để chỉnh sửa file");

        alerBuilder.setPositiveButton("Có", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                downloadFileRootAndFileFolder(meta,true);
            }
        });
        alerBuilder.setNegativeButton("Không", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alerBuilder.show();
    }



    /**
     * thực hiện mở file tập tin là file doc or txt docx Read only
     * @param metadata
     */
    private void openFile(Metadata metadata){
        new FileContentsAsyncTask(this,metadata).execute(metadata.getDriveId());
    }

    final private class FileContentsAsyncTask
            extends ApiClientAsyncTask<DriveId, Boolean, File> {

        public FileContentsAsyncTask(Context context,Metadata metadata) {
            super(context);
            this.context = context;
            fileTest = new File(Environment.getExternalStorageDirectory(),metadata.getTitle());
            dialog = new ProgressDialog(context);
        }
        Context context;
        File fileTest;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                fileTest.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            dialog.setMessage("Đang đọc ....");
            dialog.show();
        }

        @Override
        protected File doInBackgroundConnected(DriveId... params) {
            DriveFile file = params[0].asDriveFile();
            DriveApi.DriveContentsResult driveContentsResult =
                    file.open(getGoogleApiClient(), DriveFile.MODE_READ_WRITE, null).await();

            if (!driveContentsResult.getStatus().isSuccess()) {
                return null;
            }
            DriveContents driveContents = driveContentsResult.getDriveContents();


            ParcelFileDescriptor parcelFileDescriptor = driveContents.getParcelFileDescriptor();
            FileInputStream fileInputStream = new FileInputStream(parcelFileDescriptor.getFileDescriptor());

            try {
                FileOutputStream fileOutput = new FileOutputStream(fileTest);
                byte[] buffer = new byte[1024];
                int bufferLength = 0;
                while ((bufferLength = fileInputStream.read(buffer)) > 0) {
                    fileOutput.write(buffer, 0, bufferLength);
                }
                fileOutput.close();
                fileInputStream.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return fileTest;
        }

        @Override
        protected void onPostExecute(File result) {
            super.onPostExecute(result);
            saveFile = result;
            dialog.dismiss();
            if (result == null || fileTest.getName().contains(".rar")) {
                showMessage("Tập tin không hổ trợ !!!");
                return;
            }
            try {
                ClassGolsbe.openFile(context,fileTest);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private File saveFile;

    /**
     * hiển thị Dialog chứa nội dung file tập tin FILE
     */
    private void showDialogOpenFile(String result){
        ImageButton btnBack;
        TextView txtContextFile;

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_content_file);
        //ánh xạ
        btnBack = (ImageButton) dialog.findViewById(R.id.imageButtonClose);
        txtContextFile = (TextView) dialog.findViewById(R.id.textViewContextFile);
        txtContextFile.setText(result);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }


    @Override
    public void onConnected(Bundle connectionHint) {
        super.onConnected(connectionHint);
        if(getGoogleApiClient().isConnected()&&txtFile.getVisibility()==View.GONE){
            txtFolder.setVisibility(View.VISIBLE);
            txtFile.setVisibility(View.VISIBLE);
            getSupportActionBar().show();
        }
        getValueSetDataForFile();
        reloadListView();
        reLoadListViewFilesInFolder();
        if(saveFile!=null){
            saveFile.delete();
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_header_left,menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * thực hiện các tác vụ của menu
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menuDerectoty:
                openFolder( Environment.getExternalStorageDirectory() +"/ThangAPI/googleApiThang");
                break;
            case R.id.menuLogout:
                signOutGoogle();
                break;
            case R.id.menuCreateFolder:
                showDialogCreateFolder();
                break;
            case R.id.menuCreateFile:

                openFileInLocal();

                break;
            case android.R.id.home:
                DriveId driveIdCheck =getDriveIdFolderBeforeFolder();
                if(driveIdCheck==null && indexBackSave==0){
                    retrieveNextPage();
                }else if(driveIdCheck==null && indexBackSave<0) {
                    Intent intent = new Intent(getBaseContext(),MainActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.translate_layout_left_right_main,R.anim.translate_layout_left_right1_main);
                }else
                {
                    setAccessFolderAndAccessOpenFiles(driveIdCheck);
                }

                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void openFileInLocal(){
        Intent intent1 = new Intent();
        intent1.setAction(Intent.ACTION_GET_CONTENT);
        intent1.setType("*/*");

        try {
            startActivityForResult(intent1, REQUEST_CODE);
        } catch (ActivityNotFoundException e) {

        }
    }

    /**
     * mở thư mục chứa drive
     * @param location
     */
    private void openFolder(String location)
    {
        // location = "/sdcard/my_folder";
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        Uri uri = Uri.parse(location);
        intent.setDataAndType(uri,"*/*");    // or use */*
        if (intent.resolveActivity(getPackageManager()) != null){
            startActivity(Intent.createChooser(intent, "/ThangAPI/googleApiThang"));
        }
        showMessage("Chú Ý: googleApiThang thư mục lưu trữ");
    }


    /**
     * thực hiện show popup để thực hiện các tác vụ xóa và đổi tên
     * @param position
     */
    private void showPopupForFolder(final int position){
        PopupMenu popupMenu = new PopupMenu(this,lvListFolder.getChildAt(position));//
        popupMenu.getMenuInflater().inflate(R.menu.menu_lv_folder,popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()){
                    case R.id.menuDeleteFolder:
                        deleteFolder(position);
                        break;
                    case R.id.menuRenameFolder:
                        showDialogRenameFolder(position);
                        break;
                }

                return false;
            }
        });
        popupMenu.show();
    }

    private ProgressDialog dialog;

    private class DownloadFile extends AsyncTask<Void, Void, Boolean> {
        private  DriveId driveId;
        private  File filename;
        private boolean openFile;


        public DownloadFile(final DriveId driveId, final File filename, final boolean openFile,Context context) {
            this.driveId = driveId;
            this.filename = filename;
            this.openFile = openFile;
            dialog = new ProgressDialog(context);
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
            dialog.setMessage("Đang tải ...");
//            dialog.show();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            Intent intent = new Intent();
            intent.setAction(android.content.Intent.ACTION_VIEW);

            if(openFile){
                try {
                    ClassGolsbe.openFile(getBaseContext(),filename);
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
                DriveFile file = Drive.DriveApi.getFile(
                        getGoogleApiClient(), driveId);
                file.getMetadata(getGoogleApiClient())
                        .setResultCallback(metadataRetrievedCallback);
                DriveApi.DriveContentsResult driveContentsResult = file.open(
                        getGoogleApiClient(),
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

    }

    private ResultCallback<DriveResource.MetadataResult> metadataRetrievedCallback = new ResultCallback<DriveResource.MetadataResult>() {
        @Override
        public void onResult(DriveResource.MetadataResult result) {
            if (!result.getStatus().isSuccess()) {
                showMessage("Unsuccess");
                return;
            }
        }
    };






    /**
     * thực hiện xóa thư mục hiện tại Folder
     */
    private void deleteFolder(int position){
        Metadata metadata = metadataBufferResultListFolder.getMetadataBuffer().get(position);
        String dirveIdStr = metadata.getDriveId().toString();
        if (dirveIdStr != null) {
            DriveFile driveFile = Drive.DriveApi.getFile(getGoogleApiClient(),
                    DriveId.decodeFromString(dirveIdStr));

            driveFile.delete(getGoogleApiClient()).setResultCallback(deleteCallback);

                Toast.makeText(getBaseContext(),"Xóa thành công",Toast.LENGTH_SHORT);

        }
    }

    /**
     * thực hiện xóa File hiện tại
     */
    private void deleteFile(int position){
        Metadata metadata = metadataBufferResultListFile.getMetadataBuffer().get(position);

        Cursor data = database.GetData("SELECT * FROM LinksIDLocal WHERE driveID='"+metadata.getDriveId()+"'");
        String filePath = "";
        while (data.moveToNext()){
            filePath = data.getString(1);
        }


        String dirveIdStr = metadata.getDriveId().toString();
        if (dirveIdStr != null) {
            DriveFile driveFile = Drive.DriveApi.getFile(getGoogleApiClient(),
                    DriveId.decodeFromString(dirveIdStr));
            File fileDelete = new File(filePath);
            boolean test = fileDelete.delete();

            driveFile.delete(getGoogleApiClient()).setResultCallback(deleteCallback);

            Toast.makeText(getBaseContext(),"Xóa thành công",Toast.LENGTH_SHORT);
            database.QueryData("DELETE FROM LinksIDLocal WHERE driveID='"+metadata.getDriveId()+"'");
        }
    }


    /**
     * hiển thị Dialog nhằm thay đổi tên folder
     */
    private void showDialogRenameFolder(final int position){
        final EditText edtNameFolder;
        final Button btnCreateFolder,btnBack;
        final TextView txtNameContext;

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_create_folder);


        edtNameFolder = (EditText) dialog.findViewById(R.id.editTextNameFolder);
        btnCreateFolder = (Button)dialog.findViewById(R.id.buttonCreateFolder);
        btnBack = (Button)dialog.findViewById(R.id.buttonBack);
        txtNameContext = (TextView)dialog.findViewById(R.id.textViewNameContext);


        txtNameContext.setText("THAY ĐỔI TÊN");
        btnCreateFolder.setText("lưu");

        edtNameFolder.setText(metadataBufferResultListFolder.getMetadataBuffer().get(position).getTitle());



        btnCreateFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String nameFolder = edtNameFolder.getText().toString();
                if((nameFolder != "" || nameFolder != null)){
                    //thực hiện load lại list view
                    renameForFolder(nameFolder,getDriveFolderInMetaResultList(position));
                    reloadListView();
                    dialog.dismiss();
                }else{
                    showMessage("Vui lòng nhập tên thay đổi");
                }
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();

    }


    /**
     * hiển thị Dialog nhằm thay đổi tên file
     */
    private void showDialogRenameFile(final int position){
        final EditText edtNameFile;
        final Button btnCreateFolder,btnBack;
        final TextView txtNameContext;

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_create_folder);


        edtNameFile = (EditText) dialog.findViewById(R.id.editTextNameFolder);
        btnCreateFolder = (Button)dialog.findViewById(R.id.buttonCreateFolder);
        btnBack = (Button)dialog.findViewById(R.id.buttonBack);
        txtNameContext = (TextView)dialog.findViewById(R.id.textViewNameContext);


        txtNameContext.setText("THAY ĐỔI TÊN");
        btnCreateFolder.setText("lưu");
        edtNameFile.setHint("Nhập tên file");

        Metadata metadataFile =    metadataBufferResultListFile.getMetadataBuffer().get(position);
        String nameFile = getNameFile(metadataFile);
        edtNameFile.setText(nameFile);

        btnCreateFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String nameFolder = edtNameFile.getText().toString();
                if((nameFolder != "" || nameFolder != null)){
                     nameFolder+=getTailFile(metadataBufferResultListFile.getMetadataBuffer().get(position));
                    //thực hiện load lại list view
                    renameForFile(nameFolder,getDriveIDFileInMetaResultList(position));
                    reLoadListViewFilesInFolder();
                    dialog.dismiss();
                }else{
                    showMessage("Vui lòng nhập tên thay đổi");
                }
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        dialog.show();


    }


    /**
     * lấy đuôi của tập tin để thực hiện gán hình cho file và thu mục
     * @param metadata
     * @return
     */
    private String getTailFile(Metadata metadata){
        String meta = metadata.getTitle();
        int index = -1;
        index =meta.lastIndexOf(".");
        String test="0";
        if(index>0)
            test = meta.substring(index,meta.length());
        return test;
    }

    /**
     * thực hiện tải lại listView Folder
     */
    private void reloadListView(){
        int indexRoot = listDriveId.size();
        if(indexRoot>0){
            //thực hiện load lại list view
            setAccessFolderAndAccessOpenFiles(listDriveId.get(listDriveId.size()-1));
        }else{
            retrieveNextPage();
        }
    }

    /**
     * Thực hiện tải lại listView File
     */
    private void reLoadListViewFilesInFolder(){
        int indexRoot = listDriveId.size();
        mResultsFileAdapter.clear();
        if(indexRoot>0){
            //thực hiện load lại list view
            setAccessFolderGetListFiles(listDriveId.get(listDriveId.size()-1));
        }else{
            getListFilesInFolderRoot();
        }
    }


    /**
     * hiển thị show Dialog nhằm tạo thư mục theo tên
     * nếu trùng tên trong thu mục hiện tại thì không được tạo
     */
    private void showDialogCreateFolder(){
        final EditText edtNameFolder;
        final Button btnCreateFolder,btnBack;

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_create_folder);

        edtNameFolder = (EditText) dialog.findViewById(R.id.editTextNameFolder);
        btnCreateFolder = (Button)dialog.findViewById(R.id.buttonCreateFolder);
        btnBack = (Button)dialog.findViewById(R.id.buttonBack);



        btnCreateFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nameFolder = edtNameFolder.getText().toString();
                MetadataBuffer metaData = metadataBufferResultListFolder.getMetadataBuffer();
                for(Metadata meta : metaData){
                    String name = meta.getTitle();
                    if(name.equals(nameFolder)){
                        showMessage("Thư mục này đã tồn tại thay đổi tên mới!!");
                        return;
                    }
                }
                int indexRoot = listDriveId.size();
                if((nameFolder != "" || nameFolder != null)&&indexRoot>0){
                    createFolder(nameFolder,getDriveIdFolderStatus());
                    //thực hiện load lại list view
                    setAccessFolderAndAccessOpenFiles(listDriveId.get(listDriveId.size()-1));
                }else{
                    createFolderRoot(nameFolder);
                    retrieveNextPage();
                }
                dialog.dismiss();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();


    }

    /**
     * trả về giá trị DriveID của folder hiện tại
     * @param position
     * @return
     */
    private DriveId getDriveFolderInMetaResultList(int position){
        DriveId driveId = metadataBufferResultListFolder.getMetadataBuffer().get(position).getDriveId();
        return driveId;
    }

    /**
     * trả về giá trị DriveID của file hiện tại
     * @param position
     * @return
     */
    private DriveId getDriveIDFileInMetaResultList(int position){
        DriveId driveId = metadataBufferResultListFile.getMetadataBuffer().get(position).getDriveId();
        return driveId;
    }

    /**
     * Trả về DriveID của thư mục hiện tại
     * @return
     */
    private DriveId getDriveIdFolderStatus(){
        return listDriveId.get(listDriveId.size()-1);
    }

    /**
     * lấy giá trị ID drive ở thư mục đứng trước nó để thực hiện trả về thư mục trước nó.
     * @return
     */
    private DriveId getDriveIdFolderBeforeFolder(){
        indexBackSave = listDriveId.size() - 1;
        if(indexBackSave ==0){
            listDriveId.remove(0);
            return null;
        }
        if(indexBackSave<0){
            return null;
        }
        listDriveId.remove(indexBackSave);
        return listDriveId.get(indexBackSave-1);
    }

    /**
     * thực hiện truy cập vào folder và mở tập tin
     * @param metadata
     */
    private void setAccessFolderAndAccessOpenFiles(Metadata metadata){
        if(metadata.isFolder()){
            Query query = new Query.Builder()
                    .addFilter(Filters.and(eq(SearchableField.MIME_TYPE, "application/vnd.google-apps.folder"))).build();
            DriveId driveIdFolder = metadata.getDriveId();
            mResultsFileAdapter.clear();
            DriveFolder folder = driveIdFolder.asDriveFolder();
            folder.queryChildren(getGoogleApiClient(),query)
                    .setResultCallback(metadataResultListFileInFolder);
            listDriveId.add(driveIdFolder);
            setTitleInFolder();
            //load file
            setAccessFolderGetListFiles(metadata.getDriveId());
            //==
        }
    }

    /**
     * thực hiện truy cập vào thư mục là lấy các giá trị của FILE
     * @param driveId
     */
    private void setAccessFolderGetListFiles(DriveId driveId){
        Query query = new Query.Builder().addFilter(Filters.or(
                Filters.eq(SearchableField.MIME_TYPE, "application/msword"),
                Filters.eq(SearchableField.MIME_TYPE, "image/png"),
                Filters.eq(SearchableField.MIME_TYPE, "image/jpeg"),
                Filters.eq(SearchableField.MIME_TYPE, "image/gif"),
                Filters.eq(SearchableField.MIME_TYPE, "image/bmp"),
                Filters.eq(SearchableField.MIME_TYPE, "audio/x-wav"),
                Filters.eq(SearchableField.MIME_TYPE, "text/xml"),
                Filters.eq(SearchableField.MIME_TYPE, "video/mp4"),
                Filters.eq(SearchableField.MIME_TYPE, "application/rar"),
                Filters.eq(SearchableField.MIME_TYPE, "application/zip"),
                Filters.eq(SearchableField.MIME_TYPE, "application/pdf"),
                Filters.eq(SearchableField.MIME_TYPE, "application/vnd.ms-powerpoint"),
                Filters.eq(SearchableField.MIME_TYPE, "application/vnd.openxmlformats-officedocument.presentationml.presentation"),
                Filters.eq(SearchableField.MIME_TYPE, "application/vnd.ms-excel"),
                Filters.eq(SearchableField.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
                Filters.eq(SearchableField.MIME_TYPE, "text/plain")))
                .build();
        DriveFolder folder = driveId.asDriveFolder();
        folder.queryChildren(getGoogleApiClient(),query).setResultCallback(metacallbackListFile);
    }

    private  ResultCallback<MetadataBufferResult> metacallbackListFile = new ResultCallback<MetadataBufferResult>() {
        @Override
        public void onResult(@NonNull MetadataBufferResult metadataBufferResult) {
            if(!metadataBufferResult.getStatus().isSuccess()){
                showMessage("HAVE || Problem");
                return;
            }
            mResultsFileAdapter.clear();
            metadataBufferResultListFile = metadataBufferResult;
            mResultsFileAdapter.append(metadataBufferResult.getMetadataBuffer());
            setHeightListView(false,lvListFile);
        }
    };

    /**
     * thực hiện truy cập trở lại thư mục cha vào folder và mở tập tin
     * @param driveId
     */
    private void setAccessFolderAndAccessOpenFiles(DriveId driveId){
        Query query = new Query.Builder()
                .addFilter(Filters.and(eq(SearchableField.MIME_TYPE, "application/vnd.google-apps.folder"))).build();
        DriveFolder folder = driveId.asDriveFolder();

        folder.queryChildren(getGoogleApiClient(),query)
                .setResultCallback(metadataResultListFileInFolder);
        setTitleInFolder();
        //load file
        setAccessFolderGetListFiles(driveId);

    }

    /**
     * tạo một thưc mục mới
     * @param nameFolder
     */
    private void createFolder(String nameFolder, DriveId driveIdFolder){
        DriveFolder folder = driveIdFolder.asDriveFolder();
        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(nameFolder).build();
        folder.createFolder(getGoogleApiClient(), changeSet)
                .setResultCallback(callbackCreateFolder);
        //load files
        setAccessFolderGetListFiles(driveIdFolder);
    }

    /**
     * Tạo tập tin tại thư mục hiện tại FILE
     * @param nameFile
     * @param driveIdFile
     */
    private void createFile(String nameFile, DriveId driveIdFile){
        DriveFolder folder = driveIdFile.asDriveFolder();
        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(nameFile)
                .setMimeType("text/plain").build();

        folder.createFile(getGoogleApiClient(), changeSet,null)
                .setResultCallback(callbackCreateFile);
        //load files
        setAccessFolderGetListFiles(driveIdFile);
    }

    /**
     * thực hiện trả về result của Create File
     */
    private ResultCallback<DriveFolder.DriveFileResult> callbackCreateFile = new ResultCallback<DriveFolder.DriveFileResult>() {
        @Override
        public void onResult(@NonNull DriveFolder.DriveFileResult driveFileResult) {
            if(!driveFileResult.getStatus().isSuccess()){
                showMessage("HAVE? Problem");
            }
            showMessage("Tạo tập tin thành công");
        }
    };

    /**
     * Tạo thư mục tại folder root.
     * @param nameFolder
     */
    private void createFolderRoot(String nameFolder){
        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(nameFolder).build();
        Drive.DriveApi.getRootFolder(getGoogleApiClient()).createFolder(
                getGoogleApiClient(), changeSet).setResultCallback(callbackCreateFolder);
    }



    /**
     * truy vấn và lấy lại tại thu mục gốc ROOT
     */
    private void retrieveNextPage(){

        Query query = new Query.Builder().addFilter(Filters.and(
                eq(SearchableField.MIME_TYPE, "application/vnd.google-apps.folder")))
                .build();

        //==
        setTitleInFolder();
        mResultsFileAdapter.clear();
        DriveFolder folder = Drive.DriveApi.getRootFolder(getGoogleApiClient());
        folder.queryChildren(getGoogleApiClient(),query).setResultCallback(metadataResult);
        //
        getListFilesInFolderRoot();
    }

    /**
     * thực hiện lấy danh sách các tập tin
     */
    private void getListFilesInFolderRoot(){
        Query query = new Query.Builder().addFilter(Filters.or(
                Filters.eq(SearchableField.MIME_TYPE, "application/msword"),
                Filters.eq(SearchableField.MIME_TYPE, "image/png"),
                Filters.eq(SearchableField.MIME_TYPE, "image/jpeg"),
                Filters.eq(SearchableField.MIME_TYPE, "image/gif"),
                Filters.eq(SearchableField.MIME_TYPE, "image/bmp"),
                Filters.eq(SearchableField.MIME_TYPE, "audio/x-wav"),
                Filters.eq(SearchableField.MIME_TYPE, "application/vnd.google-apps.audio"),
                Filters.eq(SearchableField.MIME_TYPE, "text/xml"),
                Filters.eq(SearchableField.MIME_TYPE, "video/mp4"),
                Filters.eq(SearchableField.MIME_TYPE, "application/rar"),
                Filters.eq(SearchableField.MIME_TYPE, "application/zip"),
                Filters.eq(SearchableField.MIME_TYPE, "application/pdf"),
                Filters.eq(SearchableField.MIME_TYPE, "application/vnd.ms-powerpoint"),
                Filters.eq(SearchableField.MIME_TYPE, "application/vnd.openxmlformats-officedocument.presentationml.presentation"),
                Filters.eq(SearchableField.MIME_TYPE, "application/vnd.ms-excel"),
                Filters.eq(SearchableField.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
                Filters.eq(SearchableField.MIME_TYPE, "text/plain")))
                .build();
        DriveFolder folder = Drive.DriveApi.getRootFolder(getGoogleApiClient());
        folder.queryChildren(getGoogleApiClient(),query).setResultCallback(metadataResultFiles);
    }

    /**
     * thực hiện gán giá trị cho Adapter list danh sach file
     */
    final private ResultCallback<MetadataBufferResult> metadataResultFiles = new
            ResultCallback<MetadataBufferResult>() {
                @Override
                public void onResult(MetadataBufferResult result) {
                    if (!result.getStatus().isSuccess()) {
                        showMessage("Problem while retrieving files");
                        return;
                    }

                    metadataBufferResultListFile = result;
                    mResultsFileAdapter.clear();
                    mResultsFileAdapter.append(result.getMetadataBuffer());

                    setHeightListView(false,lvListFile);

                }
            };

    /**
     * thực hiện trả về khi thực hiện delete
     */
    final private ResultCallback<Status> deleteCallback = new ResultCallback<Status>() {
        @Override
        public void onResult(Status result) {
            if (!result.isSuccess()) {
                showMessage("không thể xóa");
                return;
            }

            reloadListView();
            showMessage("Xóa thành công");
        }
    };



    /**
     * thực hien thay đôi tên
     * @param driveId
     */
    private void renameForFile(String name ,DriveId driveId){
        DriveFile file = driveId.asDriveFile();
        MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                .setStarred(true)
                .setTitle(name).build();
        file.updateMetadata(getGoogleApiClient(),metadataChangeSet).setResultCallback( metadataRenameCallback);
    }

    /**
     * thực hien thay đôi tên
     * @param driveId
     */
    private void renameForFolder(String name ,DriveId driveId){
        DriveFolder folder = driveId.asDriveFolder();
        MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                .setStarred(true)
                .setTitle(name).build();
        folder.updateMetadata(getGoogleApiClient(),metadataChangeSet).setResultCallback( metadataRenameCallback);
        //load file
        setAccessFolderGetListFiles(driveId);
    }

    final ResultCallback<DriveResource.MetadataResult> metadataRenameCallback = new ResultCallback<DriveResource.MetadataResult>() {
        @Override
        public void onResult(DriveResource.MetadataResult result) {
            if (!result.getStatus().isSuccess()) {
                showMessage("Problem while trying to update metadata");
                return;
            }
            showMessage("Thay đổi thành công");
        }
    };


    /**
     *  gán dữ liệu cho Adapter khi chọn vào thư mục hiển thị thị các file trong thu mục hiện tại
     */
    final private ResultCallback<MetadataBufferResult> metadataResultListFileInFolder = new
            ResultCallback<MetadataBufferResult>() {
                @Override
                public void onResult(MetadataBufferResult result) {
                    if (!result.getStatus().isSuccess()) {
                        showMessage("Problem while retrieving files");
                        return;
                    }
                    metadataBufferResultListFolder = result;
                    mResultsAdapter.clear();
                    mResultsAdapter.append(result.getMetadataBuffer());
                    setHeightListView(true,lvListFolder);
                }
            };


    private void setHeightListView(boolean isFolder,ListView listView){
        int count = listView.getCount();
        if(count <= 0){
            if(isFolder){
                txtFolder.setVisibility(View.GONE);
            }else{
                txtFile.setVisibility(View.GONE);
            }
        }else{
            if(isFolder){
                txtFolder.setVisibility(View.VISIBLE);
            }else{
                txtFile.setVisibility(View.VISIBLE);
            }
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = listView.getCount() * 205;
        listView.setLayoutParams(params);
    }

    /**
     * thực hiện gán giá trị cho Adapter list danh sach file
     */
    final private ResultCallback<MetadataBufferResult> metadataResult = new
            ResultCallback<MetadataBufferResult>() {
        @Override
        public void onResult(MetadataBufferResult result) {
            if (!result.getStatus().isSuccess()) {
                showMessage("Problem while retrieving files");
                return;
            }
            metadataBufferResultListFolder = result;
            mResultsAdapter.clear();
            mResultsAdapter.append(result.getMetadataBuffer());
            setHeightListView(true,lvListFolder);
        }
    };


    /**
     * xét new Folder có được thực hiện hay không và gán giá danh sách vào adapter
     */
    final ResultCallback<DriveFolderResult> callbackCreateFolder = new ResultCallback<DriveFolderResult>() {
        @Override
        public void onResult(DriveFolderResult result) {
            if (!result.getStatus().isSuccess()) {
                showMessage("Lỗi folder");
                return;
            }
            showMessage("Tạo thư mục thành công!!");
        }
    };



}

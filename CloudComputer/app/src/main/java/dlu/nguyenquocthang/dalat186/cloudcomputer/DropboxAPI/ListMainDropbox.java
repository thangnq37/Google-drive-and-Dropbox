package dlu.nguyenquocthang.dalat186.cloudcomputer.DropboxAPI;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.TokenPair;
import com.dropbox.core.DbxException;
import com.dropbox.core.android.Auth;
import com.dropbox.core.v2.files.CreateFolderErrorException;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import dlu.nguyenquocthang.dalat186.cloudcomputer.GoogleDriveAPI.DatabaseDriveLink;
import dlu.nguyenquocthang.dalat186.cloudcomputer.MainActivity;
import dlu.nguyenquocthang.dalat186.cloudcomputer.MethodGlosbe.ClassGolsbe;
import dlu.nguyenquocthang.dalat186.cloudcomputer.R;
import ru.whalemare.sheetmenu.SheetMenu;

/**
 * Created by nguyenquocthang on 30/10/2017.
 */

public class ListMainDropbox extends BaseDropboxOauth {

    private static DropboxAPI dropboxApi;
    private static boolean isUserLoggedIn;

    //View
    private ListView lvListFolder,lvListFile;
    private ResultsDropboxAdapter mResultsAdapter,mResultsFileAdapter;
    private TextView txtFolder,txtFile;
    // database
    private DatabaseDriveLink database;
    //==========================================
    private List<Metadata> metadataBufferResultListFolder;
    private List<Metadata> metadataBufferResultListFile;

    private int indexBackSave;
    //========================================================
    //các biến tĩnh.
    private static Uri uri;
    private static List<String> listPathDirectory;
    private static String nameFolder;
    private static int dem;
    private static boolean editSetTrue;
    protected static final int REQUEST_CODE = 2;
    private static final int REQUEST_CODE_UPDATE =3;
    private static File saveFileUpdate;
    private static FileMetadata saveFileUpdate2;


    public final static String EXTRA_PATH = "ListMainDropbox_Path";
    private String mPath;
    private File saveFile;

    //    private Uri uri;
    private SharedPreferences preferences;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String path = getIntent().getStringExtra(EXTRA_PATH);
        mPath = path == null ? "" : path;
        //==================== DROPBOX =================
        AppKeyPair appKeyPair =  new AppKeyPair(APP_KEY,APP_SECRET);
        AndroidAuthSession session = null;
        SharedPreferences prefs = getSharedPreferences(DROPBOX_NAME,0);
        String key = prefs.getString(APP_KEY,null);
        String secret = prefs.getString(APP_SECRET,null);
        if(key != null && secret != null){
            AccessTokenPair token = new AccessTokenPair(key,secret);
            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE,token);
        }else{
            session = new AndroidAuthSession(appKeyPair,ACCESS_TYPE);
        }

        dropboxApi = new DropboxAPI(session);
        preferences = getSharedPreferences("Thang",MODE_PRIVATE);


        //createDatabase();
        saveFile = null;
        saveFileUpdate = null;
        saveFileUpdate2 = null;
        editSetTrue = false;
        uri = null;
        listPathDirectory = new ArrayList<>();
        indexBackSave = 0;
        dem = 0;
        nameFolder = "";
        metadataBufferResultListFile = new ArrayList<>();
        metadataBufferResultListFolder = new ArrayList<>();


        setContentView(R.layout.activity_list_files_in_folder);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().hide();

        setTitleInFolder("Thư mục gốc");
        //ánh xạ
        txtFile = (TextView)findViewById(R.id.textViewFile);
        txtFolder =(TextView)findViewById(R.id.textViewFolder);
        lvListFile = (ListView)findViewById(R.id.listViewFiles);
        lvListFolder = (ListView) findViewById(R.id.listViewResults);
        mResultsAdapter = new ResultsDropboxAdapter(ListMainDropbox.this,R.layout.activity_detail_listview,metadataBufferResultListFolder);
        mResultsFileAdapter = new ResultsDropboxAdapter(ListMainDropbox.this,R.layout.activity_detail_listview,metadataBufferResultListFile);

        txtFile.setVisibility(View.GONE);
        txtFolder.setVisibility(View.GONE);


        lvListFolder.setAdapter(mResultsAdapter);
        lvListFile.setAdapter(mResultsFileAdapter);




        lvListFolder.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Metadata metadata = metadataBufferResultListFolder.get(position);
                setAccessFolderAndAccessOpenFiles(metadata);
                listPathDirectory.add(metadata.getPathDisplay());
                setTitleInFolder(metadata.getName());
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
                FileMetadata metadata = (FileMetadata) metadataBufferResultListFile.get(position);
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



    @Override
    protected void onResume(){
        super.onResume();

        if(!isUserLoggedIn){
            //((AndroidAuthSession)dropboxApi.getSession()).startOAuth2Authentication(getBaseContext());
            preferences = getSharedPreferences("Thang",0);
            if(preferences.getBoolean("dangNhap",false)){
                setAccessFolderAndAccessOpenFiles("");
            }else {
                Auth.startOAuth2Authentication(getBaseContext(),APP_KEY);
                preferences.edit().putBoolean("dangNhap",true).apply();
            }
            loggedIn(true);
        }
        AndroidAuthSession session = (AndroidAuthSession) dropboxApi.getSession();
        if(session.authenticationSuccessful()){
            try {
                session.finishAuthentication();

                TokenPair tokens = session.getAccessTokenPair();
                SharedPreferences prefs = getSharedPreferences(DROPBOX_NAME,MODE_PRIVATE);
                SharedPreferences.Editor editor= prefs.edit();
                if(tokens != null){
                    editor.putString(APP_KEY,tokens.key);
                    editor.putString(APP_SECRET,tokens.secret);
                    editor.commit();
                    loggedIn(true);
                }

            }catch (IllegalStateException e){
                Toast.makeText(this,"Lỗi đăng nhập",Toast.LENGTH_SHORT).show();
            }
        }
        if(saveFile != null){
            saveFile.delete();
        }


    }

    @Override
    protected void loadData() {

        if(isUserLoggedIn && listPathDirectory.size()<=0) {
            setTitleInFolder("Thư mục gốc");
            final ProgressDialog dialog = new ProgressDialog(this);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setCancelable(false);
            dialog.setMessage("Đang tải...");
            dialog.show();

            new ListFolderTask(DropboxClientFactory.getClient(), new ListFolderTask.Callback() {
                @Override
                public void onDataLoaded(ListFolderResult result) {
                    metadataBufferResultListFolder.clear();
                    metadataBufferResultListFile.clear();
                    for (Metadata data : result.getEntries()) {
                        Log.d("File1", data.getName());
                        Log.d("File2", data.getPathDisplay());
                        if (!getType(data.getName())) {
                            metadataBufferResultListFolder.add(data);
                        } else {
                            metadataBufferResultListFile.add(data);
                        }
                    }
                    //mFilesAdapter.setFiles(result.getEntries());
                    mResultsFileAdapter.notifyDataSetChanged();
                    mResultsAdapter.notifyDataSetChanged();

                    ClassGolsbe.setHeightListView(true, lvListFile, txtFolder, txtFile);
                    ClassGolsbe.setHeightListView(true, lvListFolder, txtFolder, txtFile);
                    dialog.dismiss();
                    titleFolderFile();
                }

                @Override
                public void onError(Exception e) {
                    dialog.dismiss();

                    Log.e("test", "Failed to list folder.", e);
                    Toast.makeText(ListMainDropbox.this,
                            "Không thể tải danh sách",
                            Toast.LENGTH_SHORT)
                            .show();
                }

            }).execute(mPath);

        }else if(isUserLoggedIn && listPathDirectory.size() > 0){
            setAccessFolderAndAccessOpenFiles(getPathOfFolderStatus());
        }
    }


    private void editFile(int position){
        FileMetadata file = (FileMetadata) metadataBufferResultListFile.get(position);
        saveFileUpdate2 =file;
        openFileEdit(file);

    }

    /**
     * hiển thị menu cáo tác vụ của tập tin FIle
     * @param position
     */
    public void showMenuFile(final int position){

        SheetMenu.with(ListMainDropbox.this)
            .setTitle("Thực hiện tác vụ dưới:")
            .setMenu(R.menu.menu_lv_file)
            .setAutoCancel(true)
            .showIcons(true)
            .setClick(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {

                    switch (item.getItemId()){
                        case R.id.menuDeleteFile:
                            String sFile = metadataBufferResultListFile.get(position).getPathDisplay();
                            new DeleteFolder().execute(sFile);
                            setAccessFolderAndAccessOpenFiles(getPathOfFolderStatus());
                            break;
                        case R.id.menuRenameFile:
                            showDialogRenameFile(position);
                            break;
                        case R.id.menuEditFile:
                            editFile(position);
                            break;
                        case R.id.menuDownFile:

                            FileMetadata meta = (FileMetadata) metadataBufferResultListFile.get(position);
                            downloadFile(meta);
                            break;
                        case R.id.menuTest:

                            break;
                    }
                    return false;
                }
            }).show();
    }

    /**
     * đặt lại tiêu đề cho file thư mục ...
     */
    private void titleFolderFile(){
        if(metadataBufferResultListFolder.size()<=0&&txtFolder.getVisibility()==View.VISIBLE){
            txtFolder.setVisibility(View.GONE);
        }else if(!(metadataBufferResultListFolder.size()<=0)&&txtFolder.getVisibility()==View.GONE){
            txtFolder.setVisibility(View.VISIBLE);
        }

        if(metadataBufferResultListFile.size()<=0&&txtFile.getVisibility()==View.VISIBLE){
            txtFile.setVisibility(View.GONE);
        }else if(!(metadataBufferResultListFile.size()<=0)&&txtFile.getVisibility()==View.GONE){
            txtFile.setVisibility(View.VISIBLE);
        }
    }

    /**
     * thực hiện mở file tập tin
     * @param file
     */
    private void openFile(FileMetadata file) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setMessage("Đang đọc...");
        dialog.show();

        new OpenFileTask(DropboxClientFactory.getClient(), new OpenFileTask.Callback() {
            @Override
            public void onDownloadComplete(File result) {
                dialog.dismiss();

                if (result != null) {
                    saveFile = result;
                    viewFileInExternalApp(result);
                }
            }

            @Override
            public void onError(Exception e) {
                dialog.dismiss();

                Log.e("Download", "Failed to download file.", e);
                Toast.makeText(ListMainDropbox.this,
                        "An error has occurred Openfile",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }).execute(file);

    }

    /**
     * thực hiện mở file tập tin
     * @param file
     */
    private void openFileEdit(FileMetadata file) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setMessage("Đang đọc và có thể chỉnh sửa...");
        dialog.show();

        new OpenFileTask( DropboxClientFactory.getClient(), new OpenFileTask.Callback() {
            @Override
            public void onDownloadComplete(File result) {
                dialog.dismiss();

                if (result != null) {
                    saveFileUpdate = result;
                    viewFileInExternalApp(result);
                }
            }

            @Override
            public void onError(Exception e) {
                dialog.dismiss();

                Log.e("Download", "Failed to download file.", e);
                Toast.makeText(ListMainDropbox.this,
                        "Không thể mở tệp này",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }).execute(file);

    }

    /**
     * tải file xuống
     * @param file
     */
    private void downloadFile(FileMetadata file) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setMessage("Tải xuống ...");
        dialog.show();

        new DownloadFileTask(ListMainDropbox.this, DropboxClientFactory.getClient(), new DownloadFileTask.Callback() {
            @Override
            public void onDownloadComplete(File result) {
                dialog.dismiss();

                if (result != null) {
                    showAlertDialogOpenFile(result);
                }
            }

            @Override
            public void onError(Exception e) {
                dialog.dismiss();

                Log.e("Download", "Failed to download file.", e);
                Toast.makeText(ListMainDropbox.this,
                        "An error has occurred Download",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }).execute(file);

    }

    /**
     * mở file ở local
     * @param result
     */
    private void viewFileInExternalApp(File result) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String ext = result.getName().substring(result.getName().indexOf(".") + 1);
        String type = mime.getMimeTypeFromExtension(ext);

        if(ext.equals("rar")){
            showMessage("Không đọc được");
            if(saveFileUpdate.exists()){
                saveFileUpdate.delete();
            }
            if(saveFile.exists()){
                saveFile.delete();
            }
        }

        intent.setDataAndType(Uri.fromFile(result), type);
//        saveFileUpdate = intent.getData().toString();

        // Check for a handler first to avoid a crash
        PackageManager manager = getPackageManager();
        List<ResolveInfo> resolveInfo = manager.queryIntentActivities(intent, 0);
        if (resolveInfo.size() > 0) {
            try {
                startActivityForResult(intent, REQUEST_CODE_UPDATE);
            } catch (ActivityNotFoundException e) {

            };
        }
    }

    /**
     * thực hiện show alert dialog thông báo có thực hiện mở tập tin  vừa tải xuống local hay không
     * @param file
     */
    private void showAlertDialogOpenFile(final File file){
        //final Cursor data = database.GetData("SELECT * FROM LinksIDLocal WHERE driveID='"+driveId+"'");
        final AlertDialog.Builder alerBuilder = new AlertDialog.Builder(this);
        alerBuilder.setTitle("THÔNG BÁO");
        alerBuilder.setIcon(R.drawable.open);
        alerBuilder.setMessage("Bạn có muốn mở tập tin này không");
        alerBuilder.setPositiveButton("Có", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                viewFileInExternalApp(file);
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

        Metadata metadataFile =    metadataBufferResultListFile.get(position);
        String nameFile = getNameFile(metadataFile);
        edtNameFile.setText(nameFile);

        btnCreateFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String nameFolder = edtNameFile.getText().toString();
                String sPathOld = metadataBufferResultListFile.get(position).getPathDisplay();
                if((nameFolder != "" || nameFolder != null)){
                    nameFolder+=getTailFile(metadataBufferResultListFile.get(position));
                    //thực hiện load lại list view

                    String sPathRename = (getPathOfFolderStatus()!=null?getPathOfFolderStatus():"")+"/"+nameFolder;
                    new RenameFolder().execute(sPathOld,sPathRename);
                    setAccessFolderAndAccessOpenFiles(getPathOfFolderStatus());
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
        String meta = metadata.getName();
        int index = -1;
        index =meta.lastIndexOf(".");
        String test="0";
        if(index>0)
            test = meta.substring(index,meta.length());
        return test;
    }
    /**
     * lấy giá trị tên file của tập tin
     * @param metadata
     * @return
     */
    private String getNameFile(Metadata metadata){
        String meta = metadata.getName();
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
    private void setTitleInFolder(String name){
        int sizeListDriveID = listPathDirectory.size();
        if(sizeListDriveID<=0){
            setTitle("Thư mục gốc");
        }else{
            setTitle(name);
        }
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
                        String sFolder = metadataBufferResultListFolder.get(position).getPathDisplay();
                        new DeleteFolder().execute(sFolder);
                        setAccessFolderAndAccessOpenFiles(getFolderBefore(sFolder));
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

        edtNameFolder.setText(metadataBufferResultListFolder.get(position).getName());



        btnCreateFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String nameFolder = edtNameFolder.getText().toString();
                String spathFolder = getPathOfFolderStatus()+"/"+nameFolder;
                String spathRename = metadataBufferResultListFolder.get(position).getPathDisplay();

                if((nameFolder != "" || nameFolder != null)){
                    //thực hiện load lại list view
                    new RenameFolder().execute(spathRename,spathFolder);
                    setAccessFolderAndAccessOpenFiles(getPathOfFolderStatus());
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
     * lấy đường dẫn trước link hiện tại
     * @param folderHere
     * @return
     */
    private String getFolderBefore(String folderHere){
        int index = folderHere.lastIndexOf("/");
        return folderHere.substring(0,index);
    }

    /**
     * Truy cập với đường dẫn của thư mục lấy file và thư mục
     * @param sPath
     */
    private void setAccessFolderAndAccessOpenFiles(String sPath) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setMessage("Đang tải...");
        dialog.show();
        new ListFolderTask(DropboxClientFactory.getClient(), new ListFolderTask.Callback() {
            @Override
            public void onDataLoaded(ListFolderResult result) {
                metadataBufferResultListFolder.clear();
                metadataBufferResultListFile.clear();
                for(Metadata data:result.getEntries()){
                    Log.d("File1",data.getName());
                    Log.d("File2",data.getPathDisplay());
                    if(!getType(data.getName())){
                        metadataBufferResultListFolder.add(data);
                    }else{
                        metadataBufferResultListFile.add(data);
                    }
                }
                //mFilesAdapter.setFiles(result.getEntries());
                mResultsFileAdapter.notifyDataSetChanged();
                mResultsAdapter.notifyDataSetChanged();
                titleFolderFile();
                dialog.dismiss();
                ClassGolsbe.setHeightListView(true,lvListFile,txtFolder,txtFile);
                ClassGolsbe.setHeightListView(true,lvListFolder,txtFolder,txtFile);
            }

            @Override
            public void onError(Exception e) {

                Log.e("Test", "Failed to list folder.", e);
                Toast.makeText(ListMainDropbox.this,
                        "An error has occurred listFile",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }).execute(sPath);
    }

    /**
     * truy cập vào folder với metadata
     * @param metadata
     */
    private void setAccessFolderAndAccessOpenFiles(Metadata metadata) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setMessage("Đang tải...");
        dialog.show();
        metadataBufferResultListFolder.clear();
        metadataBufferResultListFile.clear();
        new ListFolderTask(DropboxClientFactory.getClient(), new ListFolderTask.Callback() {
            @Override
            public void onDataLoaded(ListFolderResult result) {

                for(Metadata data:result.getEntries()){
                    Log.d("File1",data.getName());
                    Log.d("File2",data.getPathDisplay());
                    if(!getType(data.getName())){
                        metadataBufferResultListFolder.add(data);
                    }else{
                        metadataBufferResultListFile.add(data);
                    }
                }
                //mFilesAdapter.setFiles(result.getEntries());
                mResultsFileAdapter.notifyDataSetChanged();
                mResultsAdapter.notifyDataSetChanged();
                titleFolderFile();
                dialog.dismiss();
                ClassGolsbe.setHeightListView(true,lvListFile,txtFolder,txtFile);
                ClassGolsbe.setHeightListView(true,lvListFolder,txtFolder,txtFile);
            }

            @Override
            public void onError(Exception e) {

                Log.e("Test", "Failed to list folder.", e);
                Toast.makeText(ListMainDropbox.this,
                        "An error has occurred listFile meta",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }).execute(metadata.getPathDisplay());


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
                openFolder( Environment.getExternalStorageDirectory() +"/ThangAPI/DropboxAPIThang/");
                break;
            case R.id.menuLogout:
                loggedIn(loginAndLogout());
                break;
            case R.id.menuCreateFolder:
                showDialogCreateFolder();
                break;
            case R.id.menuCreateFile:

                Intent intent1 = new Intent();
                intent1.setAction(Intent.ACTION_GET_CONTENT);
                intent1.setType("*/*");

                try {
                    startActivityForResult(intent1, REQUEST_CODE);
                } catch (ActivityNotFoundException e) {

                }

                break;
            case android.R.id.home:

                String sPath =getPathOfFolderBeforeHere();
                 if(sPath==null) {
                    Intent intent = new Intent(getBaseContext(),MainActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.translate_layout_left_right_main,R.anim.translate_layout_left_right1_main);
                }else{
                    setAccessFolderAndAccessOpenFiles(sPath);
                }
                break;

        }
        return super.onOptionsItemSelected(item);
    }
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
                uploadFile(uri.toString(),getPathOfFolderStatus());

            }
        }else if(requestCode == REQUEST_CODE_UPDATE && saveFileUpdate!=null && saveFileUpdate2!=null){
            String sFile = null;
            if(saveFileUpdate2.getPathDisplay().lastIndexOf("/")!=saveFileUpdate2.getPathDisplay().indexOf("/")){
                sFile = saveFileUpdate2.getPathDisplay().substring(0,saveFileUpdate2.getPathDisplay().lastIndexOf("/"));
            }
            if(sFile==null){
                sFile = "";
            }
            showMessage(saveFileUpdate.toURI().toString());
            uploadFile(saveFileUpdate.toURI().toString(),sFile);
            saveFileUpdate2 = null;
        }
    }

    /**
     * mở thư mục chứa dropbox tải về
     * @param location
     */
    private void openFolder(String location)
    {
        // location = "/sdcard/my_folder";
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        Uri uri = Uri.parse(location);
        intent.setDataAndType(uri,"*/*");    // or use */*
        if (intent.resolveActivity(getPackageManager()) != null){
            startActivity(Intent.createChooser(intent, "/ThangAPI/DropboxAPIThang"));
        }
        showMessage("Chú Ý: /ThangAPI/DropboxAPIThang thư mục lưu trữ");
    }



    /**
     * tải file lên Dropbox
     * @param fileUri đường dẫn ở file local
     * @param sPath links tạo dropbox
     */
    private void uploadFile(String fileUri,String sPath) {
        new UploadFileTask(this, DropboxClientFactory.getClient(), new UploadFileTask.Callback() {
            @Override
            public void onUploadComplete(FileMetadata result) {

                if(saveFileUpdate!=null){
                    saveFileUpdate.delete();
                    saveFileUpdate=null;
                }
                // Reload the folder
                //setAccessFolderAndAccessOpenFiles(getPathOfFolderStatus());
            }

            @Override
            public void onError(Exception e) {
                //dialog.dismiss();

                Log.e("UPLOAD", "Failed to upload file.", e);
                Toast.makeText(ListMainDropbox.this,
                        "An error has occurred Upload ",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }).execute(fileUri, sPath);
    }
    /**
     * đổi tên thu mục
     */
    private class RenameFolder extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                DropboxClientFactory.getClient().files().move(params[0],params[1]);
                return true;
            } catch (CreateFolderErrorException e) {
                e.printStackTrace();
            } catch (DbxException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if(aBoolean){
                showMessage("Đổi thành công");

            }else{
                showMessage("Không đổi tên");
            }
        }
    }
    /**
     * xóa thu mục tập tin
     */
    private class DeleteFolder extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                DropboxClientFactory.getClient().files().delete(params[0]);
                return true;
            } catch (CreateFolderErrorException e) {
                e.printStackTrace();
            } catch (DbxException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if(aBoolean){
                showMessage("Xóa thành công");
            }else{
                showMessage("Không thể thực hiện");
            }
        }
    }

    /**
     * tạo thu mục
     */
    private class CreateFolder extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                DropboxClientFactory.getClient().files().createFolder(params[0]);
                return true;
            } catch (CreateFolderErrorException e) {
                e.printStackTrace();
            } catch (DbxException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if(aBoolean){
                showMessage("Tạo thư mục thành công");
            }else{
                showMessage("Tạo thư mục không thành công");
            }
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

                for(Metadata meta : metadataBufferResultListFolder){
                    String name = meta.getName();
                    if(name.equals(nameFolder)){
                        showMessage("Thư mục này đã tồn tại thay đổi tên mới!!");
                        return;
                    }
                }

                String spathFolder = getPathOfFolderStatus()+"/"+nameFolder;
                if((nameFolder != "" || nameFolder != null)){
                    new CreateFolder().execute(spathFolder);
                    //thực hiện load lại list view
                    setAccessFolderAndAccessOpenFiles(getPathOfFolderStatus());
                }
                dialog.dismiss();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                int indexRoot = listPathDirectory.size();
                showMessage(getPathOfFolderStatus()+"-"+indexRoot);
            }
        });
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();


    }


    /**
     * lấy thư mục trước đó và xóa đường dẫn đến thư mục hiện tại
     * @return
     */
    private String getPathOfFolderBeforeHere(){

        indexBackSave = listPathDirectory.size() - 1;
        if(listPathDirectory.size()>1){
            setTitleInFolder(getNameInPath(listPathDirectory.get(indexBackSave-1)));
        }else{
            setTitleInFolder("Thư mục gốc");
        }
        if(indexBackSave ==0){
            listPathDirectory.remove(0);
            return "";
        }
        if(indexBackSave < 0){
            return null;
        }

        listPathDirectory.remove(indexBackSave);

        return listPathDirectory.get(indexBackSave-1);
    }

    /**
     * lấy đường dẫn cuối cùng thư mục cuối hoặc file cuối
     * @param name
     * @return
     */
    private String getNameInPath(String name){
        int index = name.lastIndexOf("/");
        return name.substring(index+1,name.length());
    }

    /**
     * lấy đường dẫn thư mục hiện tại
     * @return
     */
    private String getPathOfFolderStatus(){
        if(listPathDirectory.size()>0){
            return listPathDirectory.get(listPathDirectory.size()-1);
        }else{
            return "";
        }

    }


    /**
     * lấy giá trị Kiểu dữ liệu sau dấu "."
     * @param name
     * @return
     */
    private boolean getType(String name){
        if(name.contains(".")){
            return true;
        }
        return false;
    }


    /**
     * xác nhận đã đăng nhập hay chưa
     * @param userLoggedIn
     */
    private void loggedIn(boolean userLoggedIn){
        isUserLoggedIn = userLoggedIn;
    }


    /**
     * Đăng nhập hoặc đăng xuất trả về giá trị true false
     * @return
     */
    private boolean loginAndLogout(){
        dropboxApi.getSession().unlink();
        loggedIn(false);
        preferences = getSharedPreferences("Thang",MODE_PRIVATE);
        preferences.edit().putBoolean("dangNhap",false).apply();
        dropboxApi = null;
        Intent ic1 = new Intent(getBaseContext(), MainActivity.class);
        finish();
        startActivity(ic1);
        return false;
    }



}

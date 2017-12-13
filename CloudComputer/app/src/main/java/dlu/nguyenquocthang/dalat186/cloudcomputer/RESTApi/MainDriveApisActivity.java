package dlu.nguyenquocthang.dalat186.cloudcomputer.RESTApi;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import com.google.api.services.drive.model.File;

import java.util.List;

import dlu.nguyenquocthang.dalat186.cloudcomputer.GoogleDriveAPI.DatabaseDriveLink;
import dlu.nguyenquocthang.dalat186.cloudcomputer.R;
import dlu.nguyenquocthang.dalat186.cloudcomputer.GoogleDriveAPI.ResultsAdapter;

public class MainDriveApisActivity extends DriveOauthREST {

    //View
    private ListView lvListFolder,lvListFile;
    private ResultsAdapter mResultsAdapter,mResultsFileAdapter;
    private TextView txtFolder,txtFile;
    // database
    private DatabaseDriveLink database;
    //==========================================
    private List<File> mListFile;
    private List<File> mListFolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_drive_apis);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //ánh xạ
        txtFile = (TextView)findViewById(R.id.textViewFile);
        txtFolder =(TextView)findViewById(R.id.textViewFolder);
        lvListFile = (ListView)findViewById(R.id.listViewFiles);
        lvListFolder = (ListView) findViewById(R.id.listViewResults);
        mResultsAdapter = new ResultsAdapter(this,R.layout.activity_detail_listview);
        mResultsFileAdapter = new ResultsAdapter(this,R.layout.activity_detail_listview);

        lvListFolder.setAdapter(mResultsAdapter);
        lvListFile.setAdapter(mResultsFileAdapter);

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

            case R.id.menuLogout:
                signOutGoogle();
                break;
            case R.id.menuCreateFolder:

                break;
            case R.id.menuCreateFile:

                Intent intent1 = new Intent();
                intent1.setAction(Intent.ACTION_GET_CONTENT);
                intent1.setType("*/*");

                try {
                    //startActivityForResult(intent1, REQUEST_CODE);
                } catch (ActivityNotFoundException e) {

                }

                break;
            case android.R.id.home:

                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }
}

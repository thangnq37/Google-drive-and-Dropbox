package dlu.nguyenquocthang.dalat186.cloudcomputer.GoogleDriveAPI;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Created by nguyenquocthang on 27/11/2017.
 */

public class CreateDatabase {

    private Context context;

    public CreateDatabase(Context context){
        this.context = context;
    }
    /**
     * =================================================
     * Database của dữ liệu
     */
    public DatabaseDriveLink createDatabase(){
        DatabaseDriveLink database = new DatabaseDriveLink(context,"idlocal.sqlite",null,1);
        database.QueryData("CREATE TABLE IF NOT EXISTS LinksIDLocal(driveID VARCHAR(50) PRIMARY KEY, " +
                "URILocal NVARCHAR(50),Uploaded BOOLEAN)");
        //database.QueryData("DELETE FROM LinksIDLocal");
        File dir = new File(Environment.getExternalStorageDirectory() + "/ThangAPI/googleApiThang/");
        if(!dir.exists()){
            dir.mkdirs();
        }
        return database;
    }
}

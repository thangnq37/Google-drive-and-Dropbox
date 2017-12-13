package dlu.nguyenquocthang.dalat186.cloudcomputer.MethodGlosbe;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

/**
 * Created by nguyenquocthang on 01/11/2017.
 */

public class ClassGolsbe {
    /**
     * thực hiện các gán chiều cao của listView
     * @param isFolder
     * @param listView
     * @param txtFolder
     * @param txtFile
     */
    public static void setHeightListView(boolean isFolder, ListView listView, TextView txtFolder, TextView txtFile){
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = listView.getCount() * 205;
        listView.setLayoutParams(params);
    }

    /**
     * Open file mở tập tin như .txt .doc ....
     * @param context
     * @param url
     * @throws IOException
     */
    public static void openFile(Context context, File url) throws IOException {
        // Create URI
        File file=url;
        Uri uri = Uri.fromFile(file);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        // Check what kind of file you are trying to open, by comparing the url with extensions.
        // When the if condition is matched, plugin sets the correct intent (mime) type,
        // so Android knew what application to use to open the file
        if (url.toString().contains(".doc") || url.toString().contains(".docx")) {
            // Word document
            intent.setDataAndType(uri, "application/msword");
        } else if(url.toString().contains(".pdf")) {
            // PDF file
            intent.setDataAndType(uri, "application/pdf");
        } else if(url.toString().contains(".ppt")) {
            // Powerpoint file
            intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
        }else if( url.toString().contains(".pptx")) {
            // Powerpoint file
            intent.setDataAndType(uri, "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        } else if(url.toString().contains(".xls")) {
            // Excel file
            intent.setDataAndType(uri, "application/vnd.ms-excel");
        }else if( url.toString().contains(".xlsx")) {
            // Excel file
            intent.setDataAndType(uri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        } else if(url.toString().contains(".zip")) {
            // WAV audio file
            intent.setDataAndType(uri, "application/zip");
        }else if( url.toString().contains(".rar")) {
            // WAV audio file
            intent.setDataAndType(uri, "application/rar");
        } else if(url.toString().contains(".rtf")) {
            // RTF file
            intent.setDataAndType(uri, "application/rtf");
        } else if(url.toString().contains(".wav") || url.toString().contains(".mp3")) {
            // WAV audio file
            intent.setDataAndType(uri, "audio/x-wav");
        } else if(url.toString().contains(".gif")) {
            // GIF file
            intent.setDataAndType(uri, "image/gif");
        } else if(url.toString().contains(".jpg") || url.toString().contains(".jpeg") || url.toString().contains(".png")) {
            // JPG file
            intent.setDataAndType(uri, "image/jpeg");
        } else if(url.toString().contains(".txt")) {
            // Text file
            intent.setDataAndType(uri, "text/plain");
        } else if(url.toString().contains(".3gp") || url.toString().contains(".mpg") || url.toString().contains(".mpeg")
                || url.toString().contains(".mpe") || url.toString().contains(".mp4") || url.toString().contains(".avi")) {
            // Video files
            intent.setDataAndType(uri, "video/*");
        } else {
            //if you want you can also define the intent type for any other file

            //additionally use else clause below, to manage other unknown extensions
            //in this case, Android will show all applications installed on the device
            //so you can choose which application to use
            intent.setDataAndType(uri, "*/*");
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * mở file tập tin local FILE
     * @param data
     */
    public static void openFile(Context context,Cursor data){
        String URILocal = null;
        if (data.moveToNext()){
            URILocal = data.getString(1);
        }else if(data.moveToFirst()){
            URILocal = data.getString(1);
        }
        if(data!=null){
            File file = new File(URILocal);
            try {
                openFile(context,file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Trả về kiểu của FILE của tập tin như .doc = "application/msword",....
     * @param path
     * @return
     */
    public static String getTypeFile(String path){
        String result ="";
        if (path.toString().contains(".doc") || path.toString().contains(".docx")) {
            // Word document
            result ="application/msword";
        } else if(path.toString().contains(".pdf")) {
            // PDF file
            result = "application/pdf";
        } else if(path.toString().contains(".ppt")) {
            // Powerpoint file
            result = "application/vnd.ms-powerpoint";
        } else if(path.toString().contains(".pptx")) {
            // Powerpoint file
            result = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
        } else if(path.toString().contains(".xls")) {
            // Excel file
            result = "application/vnd.ms-excel";
        }else if(path.toString().contains(".xlsx")) {
            // Excel file
            result = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        } else if(path.toString().contains(".zip")) {
            // WAV audio file
            result = "application/zip";
        }else if( path.toString().contains(".rar")) {
            // WAV audio file
            result = "application/rar";
        }else if(path.toString().contains(".rtf")) {
            // RTF file
            result = "application/rtf";
        } else if(path.toString().contains(".wav") || path.toString().contains(".mp3")) {
            // WAV audio file
            result = "audio/x-wav";
        } else if(path.toString().contains(".gif")) {
            // GIF file
            result = "image/gif";
        } else if(path.toString().contains(".jpg") || path.toString().contains(".jpeg") || path.toString().contains(".png")) {
            // JPG file
            result = "image/jpeg";
        } else if(path.toString().contains(".txt")) {
            // Text file
            result = "text/plain";
        } else if( path.toString().contains(".mp4")) {
            // Video files
            result = "video/mp4";
        }else if(path.toString().contains(".3gp")) {
            // Video files
            result = "video/3gpp2";
        }else if( path.toString().contains(".avi")) {
            // Video files
            result = "video/x-msvideo";
        }else if(path.toString().contains(".mpg") || path.toString().contains(".mpeg")
                || path.toString().contains(".mpe")) {
            // Video files
            result = "video/mp4";
        } else {
            result = "*/*";
        }
        return result;
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @author paulburke
     */
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}

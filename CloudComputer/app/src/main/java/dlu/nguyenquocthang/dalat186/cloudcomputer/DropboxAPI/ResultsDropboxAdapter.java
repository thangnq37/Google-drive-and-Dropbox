package dlu.nguyenquocthang.dalat186.cloudcomputer.DropboxAPI;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.Metadata;

import java.util.Date;
import java.util.List;

import dlu.nguyenquocthang.dalat186.cloudcomputer.R;

/**
 * Created by nguyenquocthang on 07/09/2017.
 */

public class ResultsDropboxAdapter extends BaseAdapter {

    private List<Metadata> listMetadata;
    private int layout;
    private Context context;

    public ResultsDropboxAdapter(Context context, int i,List<Metadata> list) {
        this.context = context;
        this.layout = i;
        this.listMetadata = list;
    }

    private class ViewHolder{
        TextView txtTitle,txtCreateDate;
        ImageView imgFile;
    }


    @Override
    public int getCount() {
        if(listMetadata != null)
            return listMetadata.size();
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            //convertView = inflater.inflate(layout,null);

            convertView = inflater.inflate(layout, parent, false);
//            View popupButton = convertView.findViewById(R.id.imageViewFiles);
//            popupButton.setTag(getItem(position));

            holder = new ViewHolder();
            holder.txtTitle = (TextView) convertView.findViewById(R.id.textViewTitle);
            holder.txtCreateDate = (TextView) convertView.findViewById(R.id.textViewCreateDate);
            holder.imgFile = (ImageView) convertView.findViewById(R.id.imageViewFiles);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        final Metadata entry = listMetadata.get(position);

        holder.txtTitle.setText(entry.getName());
//        Date date = metadata.
//        holder.txtCreateDate.setText(date.getDate()+"/"+
//                ((date.getMonth()+1)%12)+ "/"+
//                (date.getYear() + 1900));

        if(entry instanceof FileMetadata){
            Date day = ((FileMetadata) entry).getClientModified();
            int month= ((day.getMonth()+1)%12)==0?12:((day.getMonth()+1)%12);
            holder.txtCreateDate.setText(day.getDate()+"/"+
                    month+ "/"+
            (day.getYear() + 1900));
        }


        String sTailFile = getTailFile(entry);

        if(entry instanceof FolderMetadata){
            holder.txtCreateDate.setText("--/--/----");
            holder.imgFile.setImageResource(R.drawable.folderbig);
        }else if (sTailFile.compareTo(".txt")==0)
        {
            holder.imgFile.setImageResource(R.drawable.txtbig);
        }else if (sTailFile.compareTo(".doc")==0||sTailFile.compareTo(".docx")==0){
            holder.imgFile.setImageResource(R.drawable.wordbig);
        }else if (sTailFile.compareTo(".xlsx")==0||sTailFile.compareTo(".xls")==0){
            holder.imgFile.setImageResource(R.drawable.excelbig);
        }else if(sTailFile.compareTo(".pdf")==0){
            holder.imgFile.setImageResource(R.drawable.pdfbig);
        }else if (sTailFile.compareTo(".jpeg")==0||sTailFile.compareTo(".png")==0||sTailFile.compareTo(".jpg")==0){
            holder.imgFile.setImageResource(R.drawable.picture1);
        }else if (sTailFile.compareTo(".mp3")==0){
            holder.imgFile.setImageResource(R.drawable.musicbig);
        }else if (sTailFile.compareTo(".mp4")==0){
            holder.imgFile.setImageResource(R.drawable.videobig);
        }else if (sTailFile.compareTo(".rar")==0||sTailFile.compareTo(".zip")==0){
            holder.imgFile.setImageResource(R.drawable.rar);
        }else if (sTailFile.compareTo(".ppt")==0||sTailFile.compareTo(".pptx")==0){
            holder.imgFile.setImageResource(R.drawable.powerpoint);
        }else{
            holder.imgFile.setImageResource(R.drawable.helpbig );
        }

        //gán animation
        Animation animation = AnimationUtils.loadAnimation(context,R.anim.scale_list);
        convertView.startAnimation(animation);

        return convertView;
    }

    /**
     * lấy đuôi của tập tin để thực hiện gán hình cho file và thu mục
     * @param entry
     * @return
     */
    private String getTailFile(Metadata entry){
        String meta = entry.getName();
        int index = -1;
        index =meta.lastIndexOf(".");
        String test="0";
        if(index>0)
            test = meta.substring(index,meta.length());
        return test;
    }


}

package dlu.nguyenquocthang.dalat186.cloudcomputer.GoogleDriveAPI;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.widget.DataBufferAdapter;

import java.util.Date;

import dlu.nguyenquocthang.dalat186.cloudcomputer.R;

/**
 * Created by nguyenquocthang on 07/09/2017.
 */

public class ResultsAdapter extends DataBufferAdapter<Metadata> {

    private int layout;
    private Context context;

    public ResultsAdapter(Context context, int i) {
        super(context, i);
        this.context = context;
        this.layout = i;
    }

    private class ViewHolder{
        TextView txtTitle,txtCreateDate;
        ImageView imgFile;
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

        final Metadata metadata = getItem(position);

        holder.txtTitle.setText(metadata.getTitle());
        Date date = metadata.getModifiedDate();
        int month= ((date.getMonth()+1)%12)==0?12:((date.getMonth()+1)%12);
        holder.txtCreateDate.setText(date.getDate()+"/"+
                month + "/"+
                (date.getYear() + 1900));


        String sTailFile = getTailFile(metadata);

        if(metadata.isFolder()){
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


}

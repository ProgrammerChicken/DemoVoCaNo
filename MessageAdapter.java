package com.vovando212366211.trolling.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.vovando212366211.trolling.R;
import com.vovando212366211.trolling.activity.ChatAudio;
import com.vovando212366211.trolling.activity.HomeActivity;
import com.vovando212366211.trolling.libraries.ConvertMedia;
import com.vovando212366211.trolling.pojo.Chat;
import com.vovando212366211.trolling.until.Const;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Vo Van Do on 10/10/2015.
 */
public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessagesHolder> {
    private Context context;
    private ArrayList<ParseUser> arrayList;
    private LayoutInflater inflater;
    private ParseUser userCurrent = null;
    private byte[] byteBitmaps;

    public MessagesAdapter(Context context, ArrayList<ParseUser> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public MessagesHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.item_messages, parent, false);
        return new MessagesHolder(v);
    }

    @Override
    public void onBindViewHolder(final MessagesHolder holder, int position) {
        userCurrent = arrayList.get(position);
        ParseQuery<ParseObject> queryReceiver = ParseQuery.getQuery("Chat");
        queryReceiver.whereEqualTo("receiver", HomeActivity.userCurrent.getUsername());
        queryReceiver.whereEqualTo("sender", userCurrent.getUsername());
        queryReceiver.orderByDescending("createdAt");
        queryReceiver.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (list != null && list.size() > 0) {
                    int totalNewMessages = 0;
                    for (int i = 0; i < list.size(); i++) {
                        if (list.get(i).getInt("status") == Chat.STATUS_LISTENED) {
                            break;
                        } else {
                            totalNewMessages = totalNewMessages + 1;
                        }
                    }
                    if (totalNewMessages > 0) {
                        holder.tvTotalNewMessages.setVisibility(View.VISIBLE);
                        holder.tvTotalNewMessages.setText(String.valueOf(totalNewMessages));
                    }
                }
            }
        });
        holder.nickName.setText(userCurrent.getString("nickname"));

        if (userCurrent.getBoolean("online")){
            holder.ivStatus.setVisibility(View.VISIBLE);
        }else {
            holder.ivStatus.setVisibility(View.GONE);
        }

        byteBitmaps= ConvertMedia.getByteArrayAvatar(userCurrent);
        holder.ivAvatar.setImageBitmap(BitmapFactory.decodeByteArray(byteBitmaps, 0, byteBitmaps.length));
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    class MessagesHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView ivAvatar, ivStatus;
        TextView nickName, tvTotalNewMessages;
        LinearLayout llMessages;

        public MessagesHolder(View itemView) {
            super(itemView);
            ivAvatar = (ImageView) itemView.findViewById(R.id.avatarSend);
            ivStatus = (ImageView) itemView.findViewById(R.id.ivStatus);
            nickName = (TextView) itemView.findViewById(R.id.tvUsernameSend);
            tvTotalNewMessages = (TextView) itemView.findViewById(R.id.tvTotalNewMessage);
            llMessages = (LinearLayout) itemView.findViewById(R.id.llMessages);
            llMessages.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Intent it = new Intent(context, ChatAudio.class);
            it.putExtra(Const.EXTRA_DATA_USERNAME, arrayList.get(getAdapterPosition()).getUsername());
            try {
                it.putExtra(Const.EXTRA_DATA_AVATAR, arrayList.get(getAdapterPosition()).getParseFile("avatar").getData());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(arrayList.get(getAdapterPosition()).getDate("birthday"));
            String birthday = calendar.get(Calendar.DATE) + "/" + calendar.get(Calendar.MONTH) + "/" + calendar.get(Calendar.YEAR);
            it.putExtra("birthday", birthday);

            it.putExtra(Const.EXTRA_DATA_ONLINE, arrayList.get(getAdapterPosition()).getBoolean("online"));
            it.putExtra(Const.EXTRA_DATA_NICKNAME, arrayList.get(getAdapterPosition()).getString("nickname"));
            context.startActivity(it);
        }
    }
}

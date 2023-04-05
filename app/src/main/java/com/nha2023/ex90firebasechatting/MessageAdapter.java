package com.nha2023.ex90firebasechatting;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

//import com.nha2023.ex90firebasechatting.databinding.MyMessageboxBinding;
//import com.nha2023.ex90firebasechatting.databinding.OtherMessageboxBinding;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.VH> {

    Context context;
    ArrayList<MessageItem> messageItems;

    final int TYPE_MY = 0; //타입상수, 값이 불변했으면 좋겠다
    final int TYPE_OTHER = 1;

    public MessageAdapter(Context context, ArrayList<MessageItem> messageItems) {
        this.context = context;
        this.messageItems = messageItems;
    }

    //29. 리사이클러뷰의 항목뷰가 경우에 따라 다른 모양으로 보여야할때 사용하는 콜백메소드
    //이 메소드에서 해당 position 에 따른 식별값(ViewType번호)를 정하여 리턴하면
    //그 값이 onCreateViewHolder의 두번째 파라미터에 전달된다.
    //onCreateViewHolder() 메소드안에서 그 값에 따라 다른 xml문서를 inflate하면 됨.
    @Override
    public int getItemViewType(int position) {
        if(messageItems.get(position).nickName.equals(G.nickname)){
             return TYPE_MY; //맞으면 내꺼
        }else {//틀리면 니꺼
            return TYPE_OTHER;
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //28.홀더를 만드는데 무엇을 만들지 모르겠어... viewType을 설정해준다. viewType은 내가 정하는것이다.
        View itemview = null;

        //30.
        if(viewType==TYPE_MY) itemview = LayoutInflater.from(context).inflate(R.layout.my_messagebox,parent,false);
        else itemview=LayoutInflater.from(context).inflate(R.layout.other_messagebox,parent,false);

        return new VH(itemview);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        MessageItem item=messageItems.get(position);
        holder.tvName.setText(item.nickName);
        holder.tvMsg.setText(item.message);
        holder.tvTime.setText(item.time);
        Glide.with(context).load(item.profileUrl).into(holder.civ);
    }

    @Override
    public int getItemCount() {
        return messageItems.size();
    }

    class VH extends RecyclerView.ViewHolder{

        CircleImageView civ;
        TextView tvName;
        TextView tvMsg;
        TextView tvTime;
        public VH(@NonNull View itemView) {
            super(itemView);

            civ=itemView.findViewById(R.id.civ);
            tvName=itemView.findViewById(R.id.tv_name);
            tvMsg=itemView.findViewById(R.id.msg);
            tvTime=itemView.findViewById(R.id.tv_time);

        }

        //27
        //메세지 타입에 따라 뷰가 다르기에 바인딩클래스를 고정하지 못함. 바인딩이 두종류이다.
        //MyMessageboxBinding,OtherMessageboxBinding
        //뷰 홀더를 2개 만들면 onBind할때 분기처러가 필요하므로 다른방법으로 시도해보자 : 뷰바인딩 안쓰고 써보기!
        //안쓰는데 클래스가 있는게 싫어 xml의 Root에 tools:viewBindingIgnore="true"해주면 뷰바인딩 무시한다.


    }

}

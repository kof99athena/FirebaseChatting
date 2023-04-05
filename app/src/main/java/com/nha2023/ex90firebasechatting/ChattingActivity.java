package com.nha2023.ex90firebasechatting;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.nha2023.ex90firebasechatting.databinding.ActivityChattingBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class ChattingActivity extends AppCompatActivity {

    ActivityChattingBinding binding;


    //31.
    MessageAdapter adapter;

    //19.아예 찾아두자 많이쓸꺼니까
    FirebaseFirestore firestore; //아예 찾아두자 많이쓸꺼니까
    CollectionReference chatRef;
    String chatName = "앱개발자 취준생 준비방";

    //24. 리사이클러뷰에 대한 대량의 데이터 만들기
    ArrayList<MessageItem> messageItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_chatting);
        binding = ActivityChattingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //17. 채팅방이름
        getSupportActionBar().setTitle(chatName);
        getSupportActionBar().setSubtitle("상대방 이름"); //액션바 나중에 내가 만들어보기

        //32,
        adapter=new MessageAdapter(this,messageItems);
        binding.recycler.setAdapter(adapter);



        //20.FirebaseFirestore 관리객체 및 참조객체 소환
        firestore=FirebaseFirestore.getInstance();
        chatRef=firestore.collection(chatName); //레퍼런스에는 채팅방 이름을 준다.


        //25. 채팅방이름으로 된 컬렉션에 저장되어있는 데이터들을 읽어오기
        //chatRef.get(); 그 순간만 가져온다. 새로추가된거는 못가져온다. 일회용

        //이 chatRef가 변경될때마다 반응하는 리스너 추가 . 누가 글을 적던 반응한다
        chatRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                //변경된 도큐먼트만 찾아달라고 요청하자
                List<DocumentChange> documentChanges = value.getDocumentChanges();
                //처음시작할때가 문제이다. 어제쓴 메시지 10개가 있는데... 이걸 DocumentChange가 10개가 바뀌었다고 생각한다.
                for(DocumentChange documentChange : documentChanges){

                    DocumentSnapshot snapshot=documentChange.getDocument();//변경된 문서내역 중에서 데이터를 촬영한 SnapShot 얻어오기

                    //이제 Document에 있는 필드값 가져오기
                    Map<String,Object> msg = snapshot.getData(); //물론 우린다 String이지만 못 읽으니까 Object로 쓰자

                    String name = msg.get("nickName").toString();
                    String message = msg.get("message").toString();
                    String profileUrl = msg.get("profileUrl").toString();
                    String time = msg.get("time").toString();

                    //읽어온 메세지를 리스트에 추가
                    messageItems.add(new MessageItem(name,message,profileUrl,time));

                    //아답터에게 추가 되었다고 공지해줘야한다. 화면 갱신을 위해 !!!
                    //26 아답터와 xml만들기

                    //33. 데이터셋체인지는 한꺼번에 바뀔때
                    adapter.notifyItemInserted(messageItems.size()-1); //아이템하나가 바뀔때는 notifyItemInserted

                    //34. 이대로면 리사이클러뷰가 안보이게된다. 글이 밑으로 계속 생기니까
                    //리사이클러뷰의 스크롤 위치가 가장 아래로 이동해야한다.
                    binding.recycler.scrollToPosition(messageItems.size()-1);

                    //35 메니페스트로 가서 수정 - 다시봐야함


                }//for문

                Toast.makeText(ChattingActivity.this, ""+messageItems.size(), Toast.LENGTH_SHORT).show();
            }
        });


        //18. 이제 메세지입력하고 SEND했을때 채팅메세지들을 저장해보기 채팅방명 (컬렉션) - 시간 (도큐먼트) - 채팅정보 (사진,닉네임, 메세지, 시간 - 필드값)
        //센드버튼 누르면 파이어베이스에 저장하자
        binding.btn.setOnClickListener(view->clickSend());

    }
    void clickSend(){ //21.
        //firebase DB에 저장할 데이터(사진,닉네임,시간, 메세지)
        String nickName = G.nickname;
        String message = binding.et.getText().toString();
        String profileUrl = G.profileUrl;

        //시간은 간단하게 만들자 [시:분]
        Calendar calendar = Calendar.getInstance();
        String time = calendar.get(Calendar.HOUR_OF_DAY)+":"+calendar.get(Calendar.MINUTE); //데이터에 저장할 변수를 지역변수로 만들어줬다. HOUR_OF_DAY : 24시간이다

        //22. 필드값을 해쉬맵으로 만들지 말고 객체를 만들어서 객체를 통으로 넣으면 편하다. 리사이클러뷰 쓰기도 편하다.
        //필드에 넣을 값들을 아예 MessageItem 객체로 만들어서 한방에 입력하자. 해쉬맵은 여기밖에 못쓰잖아
        MessageItem item = new MessageItem(nickName,message,profileUrl,time);

        //23. 채팅방이름으로 된 컬렉션에 채팅메세지들을 저장하기 (채팅방이 여러개 있을수도있으니까!)
        //단 시간순으로 정렬되도록 도큐먼트에 이름은 현재날짜(1970년부터 카운트 된 ms)로 지정하자.
        chatRef.document("MSG_"+System.currentTimeMillis()).set(item);
        binding.et.setText("");  //다음 메세지 입력이 수월하도록 Edittext 글씨를 없애기
        //소프트 키보드를 안보이도록 키보드 숨기기
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);// 스머프내놔 getSystemService
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0); //소프트키보드에 대한 권한은 누가 갖고잇지? 그걸 토큰을 갖고있다한다. 토큰을 갖고있는 애한테 그걸 뺏어와야한다.
        //0은 바로종료


    }
}




















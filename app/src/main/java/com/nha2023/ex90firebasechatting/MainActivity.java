package com.nha2023.ex90firebasechatting;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nha2023.ex90firebasechatting.databinding.ActivityMainBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

//Member라는 컬렉션에 sam이라는 도큐먼트 만들고 걔가 갖고잇는 필드는 이미지경로 주소와 이름이 들어있다.
//프로필 사진 자체는 storage에 들어가있다.
public class MainActivity extends AppCompatActivity {

    //1.뷰바인딩
    ActivityMainBinding binding;

    //프로필  uri를 알고있어야한다.
    Uri imgUri = null; //자바는..! 코틀린은 자동 null아님

    //15
    Boolean isFirst = true;


   


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //setContentView(R.layout.activity_main);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot()); //우리로치면 렐러티브

        binding.civ.setOnClickListener(view-> clickImage());
        binding.btn.setOnClickListener(view-> clickBtn());

        //12. 디바이스에 저장되어있는 로그인정보(profile)가 있는지 확인
        //SharedPreference에 저장되어있는 닉네임, 프로필이미지가 있다면 읽어오라고 명령
        loadData();



        //14
        if(G.nickname!=null){
            binding.et.setText(G.nickname);
            Glide.with(this).load(G.profileUrl).into(binding.civ);

            isFirst= false; //닉네임 저장된게 있으면 isFirst는 false이다.
        }
    }

    void loadData(){ //13. 로드데이터의 함수를 만든다
        SharedPreferences pref = getSharedPreferences("profile",MODE_PRIVATE);
        G.nickname = pref.getString("nickName",null); //닉네임으로 주고 없으면 null
        G.profileUrl = pref.getString("profileUrl",null);
    }
    void clickImage(){

        //2. 이미지 바꿔주게 그 화면줘
        Intent intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
        resultLauncher.launch(intent);

    }//clickImage

    //3.
    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if(result.getResultCode()==RESULT_CANCELED) return;
        //이제 이미지의 Uri를 알고있어야한다. Uri를 전역변수에 만들어준다.
        imgUri = result.getData().getData(); //택배기사, 물건 순서대로 받아오자
        Glide.with(this).load(imgUri).into(binding.civ);

    });

    //액티비티에 대행사 등록하자
    //StartActivityForResult()면 액티비티에 관한 결과
    //permission이라면 퍼미션에 관한 결과를 대신해주기

    void clickBtn(){
        //4. 입장을 시작할때 프로필 이미지를 Firebase스토리지에 업로드하자
        //채팅화면 가기전에 프로필이미지와 닉네임을 서버에 저장해야한다.
        if(isFirst){
            saveData(); //saveData(); 라는 함수를 만들자
        }else{ //16
            startActivity(new Intent(this, ChattingActivity.class));
        }



    }//clickBtn 입장버튼

    void saveData(){
        //5. 서버에 연동하고 저장하는 함수를 만들자.
        //일단 이미지를 선택하지 않으면 ..! 채팅 불가
        if(imgUri==null) return; //이미지 없으면 리턴하자

        //먼저 닉네임부터 저장하자
        G.nickname = binding.et.getText().toString();
        //case1: application 안에 액티비티가 있다. 여기에 멤버변수를 만들면 언제든 불러서 쓸수있다. 귀찮음 ㅠ
        //case2: c에는 자바에는 전역변수가 없다. R클래스는 static으로 변수가 만들어짐. 그래서 우리가 new안하고 씀.
        //6. 전역변수를 만들자! 꼼수로 G.class에!
        //EditText에 있는 닉네임 가져와서 전역변수 역할의 G클래스에 대입- 하지만 이건 좋은 방법은 아니다.
        //그냥 쓰는데는 문제없음 - 다른 좋은 별도 라이브러리 클래스가 잇음

        //7. 이미지 업로드가 오래걸리기때문에 우선 파이어베이스스토리지에 먼저 업로드하자
        FirebaseStorage storage = FirebaseStorage.getInstance();//파이어베이스에게 겟인스턴스

        //참조위치명이 중복되지 않도록 날짜를 이용
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String fileName = "img_"+ sdf.format(new Date());
        StorageReference imgRef = storage.getReference("profileImage/"+fileName);
        //슬래쉬로 폴더명인걸 눈치챈다

        //8.이미지업로드
        imgRef.putFile(imgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(MainActivity.this, "성공", Toast.LENGTH_SHORT).show();
                //근데 https://firebasestorage.googleapis.com/v0/b/ex90firebasechattingathena.appspot.com/o/profileImage%2Fimg_20230317025726?alt=media&token=da44d281-7684-48d7-bdb9-6085b48b0afe
                //이 주소를 저장해야함...

                //9. 여기까지 오면 업로드가 성공 되었으니...
                //업로드된 파일의 [다운로드 Url주소]가 필요. 이 주소를 얻어오자
                //경로를 저장하는게 아니라 다운로드url을 저장하는거다
                imgRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        //업로드가 되면..! 그 주소가 uri안에 있다 G클래스
                        G.profileUrl= uri.toString();// 성공해서 얻어온 uri값은 G.profileUrl에 넣자
                        Toast.makeText(MainActivity.this, "프로필 이미지 저장 완료", Toast.LENGTH_SHORT).show();

                        //10. 서버의 Firestore DB에 닉네임과 이미지 url 저장
                        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                        //'profiles'라는 이름의 컬렉션 참조객체를 소환하자
                        CollectionReference profileRef = firestore.collection("profiles"); //없으면 만들고 있으면 여기에 넣는다
                        //닉네임을 도큐먼트명으로 정하고, 필드값은 다운로드 url을 저장하는 방식으로 한다.
                        //나중 확장성을 고려해서.. 필드틑 맵으로!
                        Map<String, Object> profile = new HashMap<>();
                        profile.put("profileUrl",G.profileUrl);
                        profileRef.document(G.nickname).set(profile); //닉네임은 중복될리가 없다.

                        //11. 앱을 처음 실행할때만 닉네임과 사진을 입력하도록 하기위해 디바이스에 영구적으로 데이터를 저장하자 [sharedPreference로 저장하기]
                        SharedPreferences pref = getSharedPreferences("profile" , MODE_PRIVATE);//액티비티야 프리퍼런스 내놔, 이 앱에서만 쓸수있도록 MODE_PRIVATE
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString("nickName",G.nickname);
                        editor.putString("profileUrl",G.profileUrl);
                        editor.commit(); //내부적으로 트렌젝션 상태이기 때문에 commmit으로 완료시켜야한다.
                        //여기는 내 디바이스에 저장하는 실행문이다.

                        //저장이 완료 되었으면 채팅 화면으로 이동하자...
                        Intent intent = new Intent(MainActivity.this, ChattingActivity.class);
                        startActivity(intent);

                        finish(); //현재액티비티를 끄려면 finishgo해준다.
                    }
                });

            }
        });

    }
}





















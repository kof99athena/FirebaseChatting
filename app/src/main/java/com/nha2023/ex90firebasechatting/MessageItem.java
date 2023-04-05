package com.nha2023.ex90firebasechatting;

public class MessageItem {

    //파이어베이스에서 사용하려면 반드시 public
    public String nickName;
    public String message;
    public String profileUrl; //이게 식별자 이름이된다.
    public String time;

        //파이어베이스에서는 생성자를 둘 다 만들어줘야한다.


    public MessageItem(String nickName, String message, String profileUrl, String time) {
        this.nickName = nickName;
        this.message = message;
        this.profileUrl = profileUrl;
        this.time = time;
    }

    public void setItem(String nickName, String message, String profileUrl, String time) {
        this.nickName = nickName;
        this.message = message;
        this.profileUrl = profileUrl;
        this.time = time;
    }

    public void show(){
        //println("")
    }

    public MessageItem() {
    }

}

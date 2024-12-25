package com.example.zegocloudlivestream;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

import org.json.JSONObject;

import java.util.ArrayList;

import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.callback.IZegoRoomLoginCallback;
import im.zego.zegoexpress.constants.ZegoStreamResourceMode;
import im.zego.zegoexpress.constants.ZegoUpdateType;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoPlayerConfig;
import im.zego.zegoexpress.entity.ZegoRoomConfig;
import im.zego.zegoexpress.entity.ZegoStream;
import im.zego.zegoexpress.entity.ZegoUser;

public class LiveStreamActivity extends AppCompatActivity {

    private String userID, userName, roomID;
    private boolean isHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_live_stream);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userID = getIntent().getStringExtra("userID");
        userName = getIntent().getStringExtra("userName");
        roomID = getIntent().getStringExtra("roomID");
        isHost = getIntent().getBooleanExtra("isHost", false);

        MaterialButton exitStream = findViewById(R.id.exitStream);
        exitStream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        startListenEvent();

        ZegoUser user = new ZegoUser(userID, userName);
        ZegoRoomConfig roomConfig = new ZegoRoomConfig();
        roomConfig.isUserStatusNotify = true;
        ZegoExpressEngine.getEngine().loginRoom(roomID, user, roomConfig, new IZegoRoomLoginCallback() {
            @Override
            public void onRoomLoginResult(int errorCode, JSONObject extendedData) {
                if (errorCode == 0) {
                    Toast.makeText(LiveStreamActivity.this, "Logged in into room successfully!", Toast.LENGTH_SHORT).show();

                    if (isHost) {
                        startPreview();
                        ZegoExpressEngine.getEngine().startPublishingStream(roomID + "_" + userID + "_call");
                    }
                } else {
                    Toast.makeText(LiveStreamActivity.this, "Login into room failed. Error: " + errorCode, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    void startListenEvent() {
        ZegoExpressEngine.getEngine().setEventHandler(new IZegoEventHandler() {
            @Override
            public void onRoomStreamUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoStream> streamList, JSONObject extendedData) {
                super.onRoomStreamUpdate(roomID, updateType, streamList, extendedData);
                if (updateType == ZegoUpdateType.ADD) {
                    startPlayStream(streamList.get(0).streamID);
                } else {
                    stopPlayStream(streamList.get(0).streamID);
                }
            }

            @Override
            public void onRoomUserUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoUser> userList) {
                super.onRoomUserUpdate(roomID, updateType, userList);
                if (updateType == ZegoUpdateType.ADD) {
                    for (ZegoUser user: userList) {
                        String text = user.userID + " joined the stream.";
                        Toast.makeText(LiveStreamActivity.this, text, Toast.LENGTH_SHORT).show();
                    }
                } else if (updateType == ZegoUpdateType.DELETE) {
                    for (ZegoUser user: userList) {
                        String text = user.userID + " left the stream.";
                        Toast.makeText(LiveStreamActivity.this, text, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    void startPlayStream(String streamID) {
        findViewById(R.id.hostView).setVisibility(View.VISIBLE);
        ZegoCanvas canvas = new ZegoCanvas(findViewById(R.id.hostView));
        ZegoPlayerConfig config = new ZegoPlayerConfig();
        config.resourceMode = ZegoStreamResourceMode.DEFAULT;
        ZegoExpressEngine.getEngine().startPlayingStream(streamID, canvas, config);
    }

    void stopPlayStream(String streamID) {
        ZegoExpressEngine.getEngine().stopPlayingStream(streamID);
        findViewById(R.id.hostView).setVisibility(View.GONE);
    }

    void startPreview() {
        ZegoCanvas canvas = new ZegoCanvas(findViewById(R.id.hostView));
        ZegoExpressEngine.getEngine().startPreview(canvas);
    }

    void stopPreview() {
        ZegoExpressEngine.getEngine().stopPreview();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isFinishing()) {
            stopListenEvent();
            ZegoExpressEngine.getEngine().logoutRoom();
        }
    }

    void stopListenEvent() {
        ZegoExpressEngine.getEngine().setEventHandler(null);
    }
}
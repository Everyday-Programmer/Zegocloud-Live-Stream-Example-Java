package com.example.zegocloudlivestream;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;
import java.util.Random;

import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.entity.ZegoEngineProfile;

public class MainActivity extends AppCompatActivity {

    private long appID = 2042370389;
    private String appSign = "9c8d45bb882873687585cd427a4b38f3a1b5f610cd0dcbd5b5121210db1f59b7";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        MaterialButton hostStream = findViewById(R.id.hostStream);
        MaterialButton joinStream = findViewById(R.id.joinStream);

        TextInputLayout roomIDLayout = findViewById(R.id.roomIDLayout);
        TextInputEditText roomIDET = findViewById(R.id.roomIDET);

        hostStream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    activityResultLauncher.launch(Manifest.permission.RECORD_AUDIO);
                } else if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    activityResultLauncher.launch(Manifest.permission.CAMERA);
                } else {
                    if (Objects.requireNonNull(roomIDET.getText()).toString().isEmpty()) {
                        roomIDLayout.setError("Please enter a room ID");
                    } else {
                        Intent intent = new Intent(MainActivity.this, LiveStreamActivity.class);
                        intent.putExtra("userID", generateRandomID());
                        intent.putExtra("userName", "user_" + generateRandomID());
                        intent.putExtra("roomID", roomIDET.getText().toString());
                        intent.putExtra("isHost", true);
                        startActivity(intent);
                    }
                }
            }
        });

        joinStream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    activityResultLauncher.launch(Manifest.permission.RECORD_AUDIO);
                } else if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    activityResultLauncher.launch(Manifest.permission.CAMERA);
                } else {
                    if (Objects.requireNonNull(roomIDET.getText()).toString().isEmpty()) {
                        roomIDLayout.setError("Please enter a room ID");
                    } else {
                        Intent intent = new Intent(MainActivity.this, LiveStreamActivity.class);
                        intent.putExtra("userID", generateRandomID());
                        intent.putExtra("userName", "user_" + generateRandomID());
                        intent.putExtra("roomID", roomIDET.getText().toString());
                        intent.putExtra("isHost", false);
                        startActivity(intent);
                    }
                }
            }
        });

        ZegoEngineProfile profile = new ZegoEngineProfile();
        profile.appID = appID;
        profile.appSign = appSign;
        profile.scenario = ZegoScenario.BROADCAST;
        profile.application = getApplication();
        ZegoExpressEngine.createEngine(profile, null);
    }

    private final ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean o) {

        }
    });

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ZegoExpressEngine.destroyEngine(null);
    }

    private String generateRandomID() {
        StringBuilder builder = new StringBuilder();
        Random random = new Random();
        while (builder.length() < 6) {
            int nextInt = random.nextInt(10);
            if (builder.length() == 0 && nextInt ==0) {
                continue;
            }
            builder.append(nextInt);
        }
        return builder.toString();
    }
}
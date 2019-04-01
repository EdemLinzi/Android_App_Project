package com.example.a2playerstankgame;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button btnNewGame;
    Button btnConnection;
    Button btnExitGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnNewGame = findViewById(R.id.btn_new_game);
        btnConnection = findViewById(R.id.btn_conncetion);
        btnExitGame = findViewById(R.id.btn_exit_game);

    }

    public void connection(View v){
        startActivity(new Intent(this,SetUpBluetooth.class));
    }

}

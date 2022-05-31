package com.example.tp2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.Console;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText bthCommand;
    private Button button;
    private Button nextActivity;
    private Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.presenter = new Presenter(this,new BthModel());

        this.bthCommand = findViewById(R.id.bthCommand);
        this.button = findViewById(R.id.button);
        this.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //System.out.print("pruebaaa");
                presenter.onButtonClick(bthCommand.getText().toString());
                Toast.makeText(getApplicationContext(),bthCommand.getText().toString(),Toast.LENGTH_LONG).show();
            }
        });
        this.nextActivity = findViewById(R.id.button2);
        this.nextActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(), FlashLightActivity.class));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
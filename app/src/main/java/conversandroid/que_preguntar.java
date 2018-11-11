package conversandroid;


import android.app.ActionBar;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

import conversandroid.chatbot.R;

/**
 * Esta actividad se usa para inicializar la actividad que muestra preguntas comunes y servira como
 * ayuda para el usuario
 */
public class que_preguntar  extends AppCompatActivity {

    Toolbar barraSuperior;

    int uiOptions;
    View decorView;
    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.que_preguntar);
        barraSuperior = (Toolbar) findViewById(R.id.toolbar);
        //barraSuperior.setBackgroundColor(getResources().getColor(R.color.lightBar));
    }

}
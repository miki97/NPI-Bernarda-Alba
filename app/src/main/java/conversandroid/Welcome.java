package conversandroid;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.shashank.sony.fancywalkthroughlib.FancyWalkthroughActivity;
import com.shashank.sony.fancywalkthroughlib.FancyWalkthroughCard;

import java.util.ArrayList;
import java.util.List;

import conversandroid.chatbot.R;

public class Welcome extends FancyWalkthroughActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FancyWalkthroughCard fancywalkthroughCard1 = new FancyWalkthroughCard("Bernarda alba", "Guía virtual para casa-museo de Bernarda Alba",R.drawable.conversandroid);
        FancyWalkthroughCard fancywalkthroughCard2 = new FancyWalkthroughCard("¡Agítame!", "Agítame o toca el botón de micrófono para poder hacerme una pregunta y podré ayudarte",R.drawable.icono_shake);
        FancyWalkthroughCard fancywalkthroughCard3 = new FancyWalkthroughCard("Versión Dia/Noche", "El tema de la aplicación se adapta a la luz del ambiente.",R.drawable.icono_daynight);
        FancyWalkthroughCard fancywalkthroughCard4 = new FancyWalkthroughCard("Usa la cámara", "Escanea los códigos QR y podrás obtener información sobre ese lugar.",R.drawable.ic_qr);
        FancyWalkthroughCard fancywalkthroughCard5 = new FancyWalkthroughCard("No uses sólo un dedo", "Haz doble click con dos dedos para volver a escuchar la respuesta, o arrastra dos dedos para ambos lados para navegar entre las últimas respuestas.",R.drawable.icono_multitouch);

        fancywalkthroughCard1.setBackgroundColor(R.color.white);
        fancywalkthroughCard1.setIconLayoutParams(400,400,0,0,0,0);
        fancywalkthroughCard2.setBackgroundColor(R.color.white);
        fancywalkthroughCard2.setIconLayoutParams(400,400,0,0,0,0);
        fancywalkthroughCard3.setBackgroundColor(R.color.white);
        fancywalkthroughCard3.setIconLayoutParams(400,400,0,0,0,0);
        fancywalkthroughCard4.setBackgroundColor(R.color.white);
        fancywalkthroughCard4.setIconLayoutParams(400,400,0,0,0,0);
        fancywalkthroughCard5.setBackgroundColor(R.color.white);
        fancywalkthroughCard5.setIconLayoutParams(600,600,0,0,0,0);
        List<FancyWalkthroughCard> pages = new ArrayList<>();

        pages.add(fancywalkthroughCard1);
        pages.add(fancywalkthroughCard2);
        pages.add(fancywalkthroughCard3);
        pages.add(fancywalkthroughCard4);
        pages.add(fancywalkthroughCard5);

        for (FancyWalkthroughCard page : pages) {
            page.setTitleColor(R.color.black);
            page.setDescriptionColor(R.color.black);
        }
        setFinishButtonTitle("Comenzar ahora");
        showNavigationControls(true);
        setColorBackground(R.color.fab_material_blue_900);
        //setImageBackground(R.drawable.restaurant);
        setInactiveIndicatorColor(R.color.grey_600);
        setActiveIndicatorColor(R.color.fab_material_blue_900);
        setOnboardPages(pages);

    }

    @Override
    public void onFinishButtonPressed() {
        Intent intent = new Intent(getBaseContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }
}

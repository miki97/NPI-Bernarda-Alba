package conversandroid;

import android.view.MotionEvent;
import android.view.ViewConfiguration;
import java.lang.Math.*;
import android.widget.Toast;

public abstract class Multitouch {
    private final int tiempo = ViewConfiguration.getDoubleTapTimeout() + 100;
    private long inicio = 0;
    private int numToques = 0;
    private float x1,x2=0;
    private float currentx1,currentx2 =0;
    private float UMBRAL = 500;

    private void reset(long time) {
        numToques = 0;
        inicio = time;
    }

    public boolean onTouchEvent(MotionEvent event) {
        switch(event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if(inicio == 0 || event.getEventTime() - inicio > tiempo)
                    reset(event.getDownTime());


                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if(event.getPointerCount() == 2) {

                    x1 = event.getX(0);
                    x2 = event.getX(1);

                }
                else{
                    x1 =x2 = currentx1 = currentx2 = 0;
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if(event.getPointerCount() == 2)
                    numToques++;
                else{
                    inicio = 0;
                    x1 =x2 = currentx1 = currentx2 = 0;
                }


                break;
            case MotionEvent.ACTION_UP:
                if(numToques == 2 && event.getEventTime() - inicio < tiempo) {
                    dosDedosDobleClick();
                    inicio = 0;
                    return true;
                }

                float diferencia1 = x1 - currentx1;
                float diferencia2 = x2 - currentx2;
                if( (( diferencia1 )> 0) && ((diferencia2)>0) && Math.abs((diferencia1 + diferencia2)) > UMBRAL ){
                    desplazamientoIzq();
                }
                else if( (( diferencia1 )< 0) && ((diferencia2)<0) && Math.abs((diferencia1 + diferencia2)) > UMBRAL){
                    desplazamientoDer();
                }
                x1 =x2 = currentx1 = currentx2 = 0;
                break;
            case MotionEvent.ACTION_MOVE:
                if(event.getPointerCount() == 2) {
                    currentx1 = event.getX(0);
                    currentx2 = event.getX(1);
                }
                else{
                    //x1 =x2 = currentx1 = currentx2 = 0;
                }

        }

        return false;
    }

    public abstract void dosDedosDobleClick();
    public abstract void desplazamientoIzq();
    public abstract void desplazamientoDer();
}


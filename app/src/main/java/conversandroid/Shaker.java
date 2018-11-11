package conversandroid;


import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;

public class Shaker {
    private SensorManager mgr = null;
    private long lastShakeTimestamp = 0;
    private double threshold = 1.0d;
    private long gap = 0;
    private Shaker.Callback cb = null;

    /**
     * Costructor para la clase Shaker que establece los limites para calcular si el movimiento detectado
     * es un shake. Para ello debemos establecer por parametros segun nuestro criterio
     * @param ctxt
     * @param threshold
     * @param gap
     * @param cb
     */
    public Shaker(Context ctxt, double threshold, long gap, Shaker.Callback cb) {
        this.threshold = threshold * threshold;
        this.threshold = this.threshold * SensorManager.GRAVITY_EARTH
                * SensorManager.GRAVITY_EARTH;
        this.gap = gap;
        this.cb = cb;

        mgr = (SensorManager) ctxt.getSystemService(Context.SENSOR_SERVICE);
        mgr.registerListener(listener,
                mgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_UI);
    }

    public void close() {
        mgr.unregisterListener(listener);
    }

    /**
     * Funcion que detecta si el movimiento detectado se corresponde con el movimiento shake.
     * Para ello se comprueba que  cumple con las condiciones establecidas. Ademas comprueba que no
     * se produzca varios en menos de 5 segundos para no sobrecargar la aplicacion en caso de movimiento
     * brusco
     */
    private void isShaking() {
        long now = SystemClock.uptimeMillis();
        System.out.println("no" + now);
        System.out.println("last" + lastShakeTimestamp);
        try {
            if (now-lastShakeTimestamp > 5000 || lastShakeTimestamp == 0) {
                lastShakeTimestamp = now;

                if (cb != null) {
                    cb.shakingStarted();
                }
            } else {
                lastShakeTimestamp = now;
            }
        } catch (NullPointerException e) {

        }
    }

    /**
     * Metodo para comprobar que se a dejado de agitar el telefono. En nuestro caso no nos aporta ninguna
     * funcionalidad
     */
    private void isNotShaking() {
//        long now = SystemClock.uptimeMillis();
//
//        if (lastShakeTimestamp > 0) {
//            if (now - lastShakeTimestamp > gap) {
//                lastShakeTimestamp = 0;
//
//                if (cb != null) {
//                    cb.shakingStopped();
//                }
//            }
//        }
    }

    /**
     * Interfaz que permite añadir funcionaldiad al usuario cuando se detecte el shake o se detecte
     * que el movimiento se interrumpe
     */
    public interface Callback {
        void shakingStarted();

        void shakingStopped();
    }

    /**
     * listener que estara analizando los valores que nos ofrece el acelerometro durante la ejecucion
     */
    private final SensorEventListener listener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent e) {
            if (e.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                double netForce = e.values[0] * e.values[0];

                netForce += e.values[1] * e.values[1];
                netForce += e.values[2] * e.values[2];

                if (threshold < netForce) {
                    isShaking();
                } else {
                    isNotShaking();
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // sin usar
        }
    };
}

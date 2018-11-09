/*
 *  Copyright 2016 Zoraida Callejas, Michael McTear and David Griol
 *
 *  This is AN UPDATE of the Conversandroid Toolkit, from the book:
 *  The Conversational Interface, Michael McTear, Zoraida Callejas and David Griol
 *  Springer 2016 <https://github.com/zoraidacallejas/>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.

 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package conversandroid;

/**
 * Example activity with speech input and output that connects to
 * a DialogFlow chatbot previously created.
 *
 * Note: it will not be functional until you do not insert your own
 * access token (see line 72)
 *
 * @author Zoraida Callejas, Michael McTear, David Griol
 * @version 4.0, 04/06/18
 */

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.SparseArray;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;
import android.widget.PopupMenu;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.app.Activity;
import android.widget.TextView;



//los que repito
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Random;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.File;
import java.io.FileNotFoundException;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;


//Check the dependencies necessary to make these imports in
//the build.gradle file
//See tutorial here: https://github.com/dialogflow/dialogflow-android-client
import ai.api.android.AIConfiguration; //<< be careful to use ai.api.android.AI... and not ai.api.AI...
import ai.api.AIDataService;
import ai.api.AIServiceException;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;



import conversandroid.chatbot.R;
public class MainActivity extends VoiceActivity implements Shaker.Callback {

    private static final String LOGTAG = "Bernarda Alba";
    private static final Integer ID_PROMPT_QUERY = 0;
    private static final Integer ID_PROMPT_INFO = 1;
    private String[] curiosidades = new String[4];

    private long startListeningTime = 0; // To skip errors (see processAsrError method)

    String textLIGHT_available, textLIGHT_reading;


    TextView respuesta;
    ImageButton botonGrabar;
    ImageButton boton_ayuda;
    Toolbar barraSuperior;
    ImageButton botonQR;
    ImageView bernarda;
    Shaker shaker;
    boolean isDark = false;
    SensorManager mySensorManager;
    Sensor LightSensor;

    //attributes for the qr reader
    private static final int PHOTO_REQUEST = 10;
    private BarcodeDetector detector;
    private Uri imageUri;
    private static final int REQUEST_WRITE_PERMISSION = 20;
    private static final String SAVED_INSTANCE_URI = "uri";
    private static final String SAVED_INSTANCE_RESULT = "result";

    //Connection to DialogFlow
    private AIDataService aiDataService=null;
    private final String ACCESS_TOKEN = "3b2ff8370cd040a985af74174f173b1c";
            // https://dialogflow.com/docs/reference/agent/#obtaining_access_tokens)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        shaker = new Shaker (getBaseContext (), 3.0d, 2, this);
        //Set layout
        setContentView(R.layout.activity_main);



        //Initialize the speech recognizer and synthesizer
        initSpeechInputOutput(this);
        inicializarSensorLuz();
        inicializarCuriosidades();

        //Set up the speech button
        setSpeakButton();

        boton_ayuda = (ImageButton) findViewById(R.id.help_button);
        barraSuperior = (Toolbar) findViewById(R.id.toolbar);
        respuesta = (TextView) findViewById(R.id.respuesta);
        respuesta.setMovementMethod(new ScrollingMovementMethod());
        botonGrabar = (ImageButton) findViewById(R.id.speech_btn);
        botonQR = (ImageButton) findViewById(R.id.qr_button);
        bernarda = (ImageView) findViewById(R.id.bernarda);

        initializeHelpButton();
        setActionBar(barraSuperior);

        //set th qr button
        setBotonQR();


        //configure de qrdetector
        if (savedInstanceState != null) {
            imageUri = Uri.parse(savedInstanceState.getString(SAVED_INSTANCE_URI));
            //scanResults.setText(savedInstanceState.getString(SAVED_INSTANCE_RESULT));
        }
        detector = new BarcodeDetector.Builder(getApplicationContext())
                .setBarcodeFormats(Barcode.DATA_MATRIX | Barcode.QR_CODE)
                .build();
        if (!detector.isOperational()) {
            Toast.makeText(getApplicationContext(),"No se pudo configurar el detector!", Toast.LENGTH_SHORT).show();
        }


        //Dialogflow configuration parameters
        final AIConfiguration config = new AIConfiguration(ACCESS_TOKEN,
                AIConfiguration.SupportedLanguages.Spanish,
                AIConfiguration.RecognitionEngine.System);

        aiDataService = new AIDataService(config);

        setClearTheme();

    }

    @Override
    protected void onPause() {
        super.onPause();
        System.out.println("Actividad en pausa");
        mySensorManager.unregisterListener(LightSensorListener);
        shutdown();
        shaker.close();
    }

    @Override
    protected void onStart() {
        super.onStart();
        initSpeechInputOutput(this);
        inicializarSensorLuz();
        inicializarCuriosidades();
        shaker = new Shaker (getBaseContext (), 3.0d, 2, this);
        System.out.println("Actividad start");
    }

    @Override
    protected void onResume() {
        super.onResume();
        //System.out.println("Actividad en resumen");
        //shaker = new Shaker (getBaseContext (), 3.0d, 2, this);
    }


    void inicializarCuriosidades(){
        curiosidades[0] = "Por razones políticas la obra no se representa en España hasta 1964, 28 años después de ser escrita, tal vez por aquello del grito final de Bernarda, ¡Silencio, silencio he dicho!";
        curiosidades[1] = "Uno de los temas de la obra es el sexo, todos y todas lo buscan pero solo está bien visto en el hombre";
        curiosidades[2] = "Cada nombre de las 5 hijas tiene un significado, pregúntame por cada uno de los nombres y te ayudaré";
        curiosidades[3] = "Toda la obra transcurre en la casa, un espacio cerrado comparable con un convento, un presidio o incluso un infierno";

    }


    private void initializeHelpButton(){
        boton_ayuda.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //Creating the instance of PopupMenu
                PopupMenu popup = new PopupMenu(MainActivity.this, boton_ayuda);
                //Inflating the Popup using xml file
                popup.getMenuInflater().inflate(R.menu.menu_main, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        Toast.makeText(MainActivity.this,"You Clicked : " + item.getTitle(), Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });

                popup.show();//showing popup menu
            }
        });//closing the setOnClickListener method
    }


    /**
     * Initializes the search button and its listener. When the button is pressed, a feedback is shown to the user
     * and the recognition starts
     */
    private void setSpeakButton() {
        // gain reference to speak button
        ImageButton speak = findViewById(R.id.speech_btn);
        speak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Ask the user to speak
                try {
                    changeButtonAppearanceToListening();
                    speak(getResources().getString(R.string.initial_prompt), "ES", ID_PROMPT_QUERY);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            //Toast.makeText(getApplicationContext(),"Grabando audio", Toast.LENGTH_SHORT).show();
                            startListening();
                        }
                    });
                } catch (Exception e) {
                    Log.e(LOGTAG, "TTS not accessible");
                }
            }
        });
    }

    private void inicializarSensorLuz(){

        mySensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        LightSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if(LightSensor != null){
            System.out.println("available");
            mySensorManager.registerListener(
                    LightSensorListener,
                    LightSensor,
                    SensorManager.SENSOR_DELAY_NORMAL);

        }else{
            System.out.println("not available");
        }
    }

    private final SensorEventListener LightSensorListener
            = new SensorEventListener(){

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            if(event.sensor.getType() == Sensor.TYPE_LIGHT){
                System.out.println(event.values[0]);
                if(event.values[0] < 8) {
                    setDarkTheme();
                    isDark = true;
                } else if(isDark) {
                    setClearTheme();
                    isDark = false;
                }
            }
        }

    };

    private void setDarkTheme() {
        findViewById(R.id.relativeLayout).setBackgroundColor(Color.rgb(50,50,50));
        respuesta.setTextColor(Color.WHITE);
        barraSuperior.setBackgroundColor(getResources().getColor(R.color.darkBar));
        bernarda.setImageResource(R.drawable.ic_bernarda_alba_white);
        botonGrabar.setBackgroundResource(R.drawable.round_botton_dark);
    }
    private void setClearTheme() {
        findViewById(R.id.relativeLayout).setBackgroundColor(Color.WHITE);
        respuesta.setTextColor(Color.BLACK);
        barraSuperior.setBackgroundColor(getResources().getColor(R.color.lightBar));
        bernarda.setImageResource(R.drawable.ic_bernarda_alba);
        botonGrabar.setBackgroundResource(R.drawable.round_botton);
    }

    /** Funcion que activa el boton de QR
    */
    private void setBotonQR(){
        botonQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(MainActivity.this, new
                        String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
            }
        });
    }

    /** Funcion para recibir la peticion de permisos de escritura, es decir vamos
        a leer un codigo qr
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_WRITE_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePicture();
                } else {
                    Toast.makeText(MainActivity.this, "Permiso denegado!", Toast.LENGTH_SHORT).show();
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {

            if (resultCode == RESULT_OK) {
                String contents = data.getStringExtra("SCAN_RESULT");
            }
            if(resultCode == RESULT_CANCELED){
                //handle cancel
            }
        }
        if (requestCode == PHOTO_REQUEST && resultCode == RESULT_OK) {
            launchMediaScanIntent();
            try {

                //Toast.makeText(MainActivity.this, "DENTRO TRY", Toast.LENGTH_SHORT).show();
                Bitmap bitmap = decodeBitmapUri(this,imageUri);

                //decodeBitmapUri(this, imageUri);
                if (detector.isOperational() && bitmap != null) {
                    //Toast.makeText(MainActivity.this, "DENTRO DEIF", Toast.LENGTH_SHORT).show();
                    Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                    SparseArray<Barcode> barcodes = detector.detect(frame);
                    for (int index = 0; index < barcodes.size(); index++) {
                        Barcode code = barcodes.valueAt(index);
                        String codigo = code.displayValue + "\n";
                        //Toast.makeText(MainActivity.this, codigo, Toast.LENGTH_SHORT).show();
                        sendMsgToChatBot(codigo);
                    }
                    if (barcodes.size() == 0) {
                        //oast.makeText(MainActivity.this, "No se ha identificado nada", Toast.LENGTH_SHORT).show();
                        sendMsgToChatBot("No he escuchado nada");

                    }
                } else {
                    Toast.makeText(MainActivity.this, "No se puede configurar el detector", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Failed to load Image", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }
    /**
     * Explain to the user why we need their permission to record audio on the device
     * See the checkASRPermission in the VoiceActivity class
     */
    public void showRecordPermissionExplanation() {
        Toast.makeText(getApplicationContext(), R.string.asr_permission, Toast.LENGTH_SHORT).show();
    }

    /**
     * If the user does not grant permission to record audio on the device, a message is shown and the app finishes
     */
    public void onRecordAudioPermissionDenied() {
        Toast.makeText(getApplicationContext(), R.string.asr_permission_notgranted, Toast.LENGTH_SHORT).show();
        System.exit(0);
    }

    /**
     * Starts listening for any user input.
     * When it recognizes something, the <code>processAsrResult</code> method is invoked.
     * If there is any error, the <code>onAsrError</code> method is invoked.
     */
    private void startListening() {
        if (deviceConnectedToInternet()) {
            try {

				/*Start listening, with the following default parameters:
					* Language = English
					* Recognition model = Free form,
					* Number of results = 1 (we will use the best result to perform the search)
					*/
                startListeningTime = System.currentTimeMillis();
                Locale espa = new Locale("spa","ESP");
                listen(espa, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM, 1); //Start listening
            } catch (Exception e) {
                this.runOnUiThread(new Runnable() {  //Toasts must be in the main thread
                    public void run() {
                        Toast.makeText(getApplicationContext(), R.string.asr_notstarted, Toast.LENGTH_SHORT).show();
                        changeButtonAppearanceToDefault();
                    }
                });

                Log.e(LOGTAG, "ASR could not be started");
                try {
                    speak(getResources().getString(R.string.asr_notstarted), "EN", ID_PROMPT_INFO);
                } catch (Exception ex) {
                    Log.e(LOGTAG, "TTS not accessible");
                }
            }
        } else {

            this.runOnUiThread(new Runnable() { //Toasts must be in the main thread
                public void run() {
                    Toast.makeText(getApplicationContext(), R.string.check_internet_connection, Toast.LENGTH_SHORT).show();
                    changeButtonAppearanceToDefault();
                }
            });
            try {
                speak(getResources().getString(R.string.check_internet_connection), "EN", ID_PROMPT_INFO);
            } catch (Exception ex) {
                Log.e(LOGTAG, "TTS not accessible");
            }
            Log.e(LOGTAG, "Device not connected to Internet");
        }
    }

    /**
     * Invoked when the ASR is ready to start listening. Provides feedback to the user to show that the app is listening:
     * * It changes the color and the message of the speech button
     */
    @Override
    public void processAsrReadyForSpeech() {
        changeButtonAppearanceToListening();
    }

    /**
     * Provides feedback to the user to show that the app is listening:
     * * It changes the color and the message of the speech button
     */
    private void changeButtonAppearanceToListening() {
        if(isDark){
            botonGrabar.setBackgroundResource(R.drawable.round_botton_dark_var);
        }else{
            botonGrabar.setBackgroundResource(R.drawable.round_botton_var);
        }
    }

    /**
     * Provides feedback to the user to show that the app is idle:
     * * It changes the color and the message of the speech button
     */
    private void changeButtonAppearanceToDefault() {
        if(isDark){
            botonGrabar.setBackgroundResource(R.drawable.round_botton_dark);
        }else{
            botonGrabar.setBackgroundResource(R.drawable.round_botton);

        }
    }

    /**
     * Provides feedback to the user (by means of a Toast and a synthesized message) when the ASR encounters an error
     */
    @Override
    public void processAsrError(int errorCode) {
        changeButtonAppearanceToDefault();

        //Possible bug in Android SpeechRecognizer: NO_MATCH errors even before the the ASR
        // has even tried to recognized. We have adopted the solution proposed in:
        // http://stackoverflow.com/questions/31071650/speechrecognizer-throws-onerror-on-the-first-listening
        long duration = System.currentTimeMillis() - startListeningTime;
        if (duration < 500 && errorCode == SpeechRecognizer.ERROR_NO_MATCH) {
            Log.e(LOGTAG, "Doesn't seem like the system tried to listen at all. duration = " + duration + "ms. Going to ignore the error");
            stopListening();
        } else {
            int errorMsg;
            switch (errorCode) {
                case SpeechRecognizer.ERROR_AUDIO:
                    errorMsg = R.string.asr_error_audio;
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    errorMsg = R.string.asr_error_client;
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    errorMsg = R.string.asr_error_permissions;
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    errorMsg = R.string.asr_error_network;
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    errorMsg = R.string.asr_error_networktimeout;
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    errorMsg = R.string.asr_error_nomatch;
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    errorMsg = R.string.asr_error_recognizerbusy;
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    errorMsg = R.string.asr_error_server;
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    errorMsg = R.string.asr_error_speechtimeout;
                    break;
                default:
                    errorMsg = R.string.asr_error; //Another frequent error that is not really due to the ASR, we will ignore it
                    break;
            }
            String msg = getResources().getString(errorMsg);
            this.runOnUiThread(new Runnable() { //Toasts must be in the main thread
                public void run() {
                    Toast.makeText(getApplicationContext(), R.string.asr_error, Toast.LENGTH_LONG).show();
                }
            });

            Log.e(LOGTAG, "Error when attempting to listen: " + msg);
            try {
                speak(msg, "EN", ID_PROMPT_INFO);
            } catch (Exception e) {
                Log.e(LOGTAG, "TTS not accessible");
            }
        }
    }

    /**
     * Synthesizes the best recognition result
     */
    @Override
    public void processAsrResults(ArrayList<String> nBestList, float[] nBestConfidences) {

        if(nBestList!=null){

            Log.d(LOGTAG, "ASR best result: " + nBestList.get(0));

            if(nBestList.size()>0){
                sendMsgToChatBot(nBestList.get(0)); //Send the best recognition hypothesis to the chatbot                botonGrabar.setBackgroundResource(R.drawable.round_botton_var);
                changeButtonAppearanceToDefault();
            }
        }
    }

    /**
     * Connects to DialogFlow sending the user input in text form
     * @param userInput recognized utterance
     */
    private void sendMsgToChatBot(String userInput) {

        //final AIRequest aiRequest = new AIRequest();
        //aiRequest.setQuery(userInput);

        new AsyncTask<String,Void,AIResponse>() {

            /**
             * Connects to the DialogFlow service
             * @param strings Contains the user request
             * @return language understanding result from DialogFlow
             */
            @Override
            protected AIResponse doInBackground(String... strings) {
                final String request = strings[0];
                Log.d(LOGTAG,"Request: "+strings[0]);
                try {
                    final AIRequest aiRequest = new AIRequest(request);
                    final AIResponse response = aiDataService.request(aiRequest);
                    Log.d(LOGTAG,"Request: "+aiRequest);
                    Log.d(LOGTAG,"Response: "+response);


                    return response;
                } catch (AIServiceException e) {
                    try {
                        speak("Could not retrieve a response from DialogFlow", "ES", ID_PROMPT_INFO);
                        Log.e(LOGTAG,"Problems retrieving a response");
                    } catch (Exception ex) {
                        Log.e(LOGTAG, "Spanish not available for TTS, default language used instead");
                    }
                }
                return null;
            }

            /**
             * The semantic parsing is decomposed and the text corresponding to the chatbot
             * response is synthesized
             * @param response parsing corresponding to the output of DialogFlow
             */
            @Override
            protected void onPostExecute(AIResponse response) {
                if (response != null) {
                    // process aiResponse here
                    // Mmore info for a more detailed parsing on the response: https://github.com/dialogflow/dialogflow-android-client/blob/master/apiAISampleApp/src/main/java/ai/api/sample/AIDialogSampleActivity.java

                    final Result result = response.getResult();
                    Log.d(LOGTAG,"Result: "+result.getResolvedQuery());
                    Log.d(LOGTAG,"Action: " + result.getAction());

                    final String chatbotResponse = result.getFulfillment().getSpeech();
                    respuesta.setText(chatbotResponse);
                    changeButtonAppearanceToDefault();
                    try {
                        speak(chatbotResponse, "ES", ID_PROMPT_QUERY); //It always starts listening after talking, it is neccessary to include a special "last_exchange" intent in dialogflow and process it here
                                    //so that the last system answer is synthesized using ID_PROMPT_INFO.
                    } catch (Exception e) { Log.e(LOGTAG, "TTS not accessible"); }

                }
            }
        }.execute(userInput);
    }

    /**
     * Checks whether the device is connected to Internet (returns true) or not (returns false)
     * From: http://developer.android.com/training/monitoring-device-state/connectivity-monitoring.html
     */
    public boolean deviceConnectedToInternet() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return (activeNetwork != null && activeNetwork.isConnectedOrConnecting());
    }

    /**
     * Shuts down the TTS engine when finished
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        shutdown();
    }

    /**
     * Invoked when the TTS has finished synthesizing.
     *
     * In this case, it starts recognizing if the message that has just been synthesized corresponds to a question (its id is ID_PROMPT_QUERY),
     * and does nothing otherwise.
     *
     * According to the documentation the speech recognizer must be invoked from the main thread. onTTSDone callback from TTS engine and thus
     * is not in the main thread. To solve the problem, we use Androids native function for forcing running code on the UI thread
     * (runOnUiThread).

     *
     * @param uttId identifier of the prompt that has just been synthesized (the id is indicated in the speak method when the text is sent
     * to the TTS engine)
     */

    @Override
    public void onTTSDone(String uttId) {
        botonGrabar.setBackgroundResource(R.drawable.round_botton);
        Toast.makeText(this, "aaaa", Toast.LENGTH_SHORT).show();
    }

    /**
     * Invoked when the TTS encounters an error.
     *
     * In this case it just writes in the log.
     */
    @Override
    public void onTTSError(String uttId) {
        Log.e(LOGTAG, "TTS error");
    }

    /**
     * Invoked when the TTS starts synthesizing
     *
     * In this case it just writes in the log.
     */
    @Override
    public void onTTSStart(String uttId) {
        Log.d(LOGTAG, "TTS starts speaking");
        //botonGrabar.setBackgroundResource(R.drawable.round_botton_var);
    }

    @Override
    public void shakingStarted() {
        //intent.setData(Uri.parse("http://www.google.es"));ç
        Random rand = new Random();
        int  n = rand.nextInt(curiosidades.length) ;
        final String chatbotResponse = curiosidades[n];
        respuesta.setText(chatbotResponse);
        try {
            speak(chatbotResponse, "ES", ID_PROMPT_QUERY); //It always starts listening after talking, it is neccessary to include a special "last_exchange" intent in dialogflow and process it here
            //so that the last system answer is synthesized using ID_PROMPT_INFO.
        } catch (Exception e) { Log.e(LOGTAG, "TTS not accessible"); }
    }

    @Override
    public void shakingStopped() {

    }

    //Funcion para tomar una foto para la funcion de lectura qr
    private void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photo = new File(Environment.getExternalStorageDirectory(), "picture.jpg");
        imageUri = FileProvider.getUriForFile(MainActivity.this,
                conversandroid.chatbot.BuildConfig.APPLICATION_ID+ ".provider", photo);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, PHOTO_REQUEST);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (imageUri != null) {
            outState.putString(SAVED_INSTANCE_URI, imageUri.toString());
            //outState.putString(SAVED_INSTANCE_RESULT, scanResults.getText().toString());
        }
        super.onSaveInstanceState(outState);
    }

    private void launchMediaScanIntent() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(imageUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private Bitmap decodeBitmapUri(Context ctx, Uri uri) throws FileNotFoundException {
        int targetW = 600;
        int targetH = 600;
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(ctx.getContentResolver().openInputStream(uri), null, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        return BitmapFactory.decodeStream(ctx.getContentResolver()
                .openInputStream(uri), null, bmOptions);
    }
}

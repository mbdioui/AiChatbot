package com.oab.orange.aichatbot;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.github.bassaer.chatmessageview.models.Message;
import com.github.bassaer.chatmessageview.models.User;
import com.github.bassaer.chatmessageview.views.ChatView;
import com.google.gson.JsonElement;

import java.util.Locale;
import java.util.Map;
import java.util.Random;

import ai.api.AIListener;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.model.Result;

public class MainActivity extends AppCompatActivity implements AIListener,TextToSpeech.OnInitListener {

    private AIService aiService;
    private AlertDialog.Builder builder;
    private AlertDialog dialog;
    private ChatView mChatView;
    private  User me;
    private User you;
    private TextToSpeech tts;
    private ConnectivityManager conMgr;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        conMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        AIserviceinit();
        tts = new TextToSpeech(this, this);
        tts.setSpeechRate(1f);
        mChatView = (ChatView) findViewById(R.id.chat_view);

        iniChatInterface();

    }
    private void AIserviceinit() {

        final AIConfiguration config = new AIConfiguration(getResources().getString(R.string.clientkeyai),
                AIConfiguration.SupportedLanguages.French,
                AIConfiguration.RecognitionEngine.System);
        aiService = AIService.getService(this, config);
        aiService.setListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void iniChatInterface() {
        //User id
        int myId = 0;
        //User icon
        Bitmap myIcon = BitmapFactory.decodeResource(getResources(), R.drawable.face_2);
        //User name
        String myName = "salouh";

        int yourId = 1;
        Bitmap yourIcon = BitmapFactory.decodeResource(getResources(), R.drawable.chatbot);
        String yourName = "chatbot";

        me = new User(myId, myName, myIcon);
        you = new User(yourId, yourName, yourIcon);

        mChatView.setRightBubbleColor(ContextCompat.getColor(this, R.color.colorPrimary));
        mChatView.setLeftBubbleColor(ContextCompat.getColor(this, R.color.gray200));
        mChatView.setSendIcon(R.drawable.ic_action_mic);
        mChatView.setSendButtonColor(R.color.colorLight);
        mChatView.setAutoHidingKeyboard(true);
        mChatView.setOptionIcon(R.drawable.ic_action_info);
        mChatView.setOptionButtonColor(R.color.colorLight);
        mChatView.setOnBubbleClickListener(new Message.OnBubbleClickListener() {
            @Override
            public void onClick(Message message) {
                Toast.makeText(MainActivity.this,"onbubbleclick success",Toast.LENGTH_SHORT).show();
            }
        });
        mChatView.addInputChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(mChatView.getInputText().isEmpty())
                {
                    mChatView.setSendIcon(R.drawable.ic_action_mic);
                    mChatView.setEnableSendButton(true);
                }
                else
                {
                    mChatView.setSendIcon(R.drawable.ic_action_send);
                }
            }
        });
        mChatView.setOnClickSendButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M)
                {
                if(mChatView.getInputText().isEmpty())
                    {if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.RECORD_AUDIO)
                        == PackageManager.PERMISSION_GRANTED) {
                        if (conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED
                                || conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED  )
                            aiService.startListening();
                        else
                            showtoast("vous devez avoir accès à internet");
                    } else {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 100);
                        }
                    }
                    else
                    {

                        String msg=mChatView.getInputText();
                        sendmessage(msg);
                        speakOut(msg);
                    }
                }
                else
                    if(mChatView.getInputText().isEmpty())
                        if (conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED
                                || conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED  )
                            aiService.startListening();
                        else
                            showtoast("vous devez avoir accès à internet");

                    else
                    {
                        String msg=mChatView.getInputText();
                        sendmessage(msg);
                        speakOut(msg);
                    }
            }
        });
    }

    private void sendmessage(String inputText) {
        Message message = new Message.Builder()
                .setUser(me)
                .setRightMessage(true)
                .setMessageText(inputText)
                .hideIcon(true)
                .build();
        //Set to chat view
        mChatView.send(message);
        mChatView.setInputText("");
        mChatView.clearFocus();
    }

    private void receivemessage(String InputText)
    {
        final Message receivedMessage = new Message.Builder()
                .setUser(you)
                .setRightMessage(false)
                .setMessageText(InputText)
                .build();
        mChatView.receive(receivedMessage);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 100: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    aiService.startListening();

                } else {
                    Toast.makeText(MainActivity.this,"vous n'avez pas autorisé l'appareil à faire un enregistrement vocal",
                            Toast.LENGTH_LONG).show();
                }
                return;
            }

        }
    }

    @Override
    public void onResult(AIResponse response) {
        final Result result = response.getResult();

        // Get parameters
        String parameterString = "";
        if (result.getParameters() != null && !result.getParameters().isEmpty()) {
            for (final Map.Entry<String, JsonElement> entry : result.getParameters().entrySet()) {
                parameterString += "(" + entry.getKey() + ", " + entry.getValue() + ") ";
            }
        }
        sendmessage(result.getResolvedQuery());
        //Reset edit text
        mChatView.setInputText("");

        int sendDelay = (new Random().nextInt(4) + 1) * 1000;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                String fullfillment =result.getFulfillment().getSpeech().toString();
                receivemessage(fullfillment);
                speakOut(fullfillment);
            }
        }, sendDelay);

        switch(result.getAction())
        {
            case "reservation.salle":
                Toast.makeText(this,"activité de réservation lancée",Toast.LENGTH_LONG).show();
                break;
            default:
//                Toast.makeText(this,result.getResolvedQuery(),Toast.LENGTH_LONG).show();
        }


    }

    @Override
    public void onError(AIError error) {
        Log.d("AI:",error.getMessage()) ;
    }

    @Override
    public void onAudioLevel(float level) {
        float positiveLevel = Math.abs(level);

        if (positiveLevel < 10) {
//            Toast.makeText(this, "votre voix est trop faible", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onListeningStarted() {
        builder = new AlertDialog.Builder(this);
        builder.setMessage("Enregistrement en cours ...");
        dialog= builder.create();
        dialog.show();

    }
    private void showtoast(String msg)
    {
        Toast.makeText(this,msg,Toast.LENGTH_LONG).show();
    }

    @Override
    public void onListeningCanceled() {
        if(dialog.isShowing())
            dialog.dismiss();
    }

    @Override
    public void onListeningFinished() {
        if(dialog.isShowing())
            dialog.dismiss();
    }

    @Override
    public void onDestroy() {
        // Don't forget to shutdown tts!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
    @Override
    public void onInit(int status) {

        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.FRANCE);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "ce language n'est pas supporté");
            } else {
            }

        } else {
            Log.e("TTS", "echec d'intialisation!");
        }

    }

    private void speakOut(String texttospeech) {
        if (texttospeech!="")
            tts.speak(texttospeech, TextToSpeech.QUEUE_FLUSH, null);
    }
}

package r.com.testingdot;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    TextView tv = null,tvVal = null;
    String sentID= null, location = "";
    Double currentLat = 0.0;
    Double currentLong = 0.0;
    RadioGroup radioGroup = null;
    EditText editText = null;
    Button btnAceptar = null, btnDenegar=null, btnVal=null, btnSendFeed=null;
    public static final String FCM_MESSAGE_URL = "https://fcm.googleapis.com/fcm/send";
    OkHttpClient mClient = new OkHttpClient();
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static String testToken="";
    String token = null;
    String thisAppToken="";
    String UID = "";
    int value = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tabbed_activity);
        tabHostinit();
        inicializar();
        processExtraData();
        thisAppToken=FirebaseInstanceId.getInstance().getToken().toString();
        writeDB(thisAppToken);
        dbReadLocation();
        getCityLocation();
        Log.d("TAG", location);
    }

    private void tabHostinit() {
        TabHost tabHost = findViewById(R.id.tabHost);
        tabHost.setup();
        TabHost.TabSpec spec = tabHost.newTabSpec("TabMen");

        spec.setContent(R.id.Mensajería);
        spec.setIndicator("TabMen");
        tabHost.addTab(spec);

        spec = tabHost.newTabSpec("TabVal");
        spec.setContent(R.id.Valoracion);
        spec.setIndicator("TabVal");
        tabHost.addTab(spec);
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        processExtraData();
        dbReadOnFeedback();
    }

    private void processExtraData() {
        Bundle b = getIntent().getExtras();
        if (b != null) {
            findViewById(R.id.imgLogo).setVisibility(View.INVISIBLE);
            btnDenegar.setVisibility(View.VISIBLE);
            btnAceptar.setVisibility(View.VISIBLE);
            String sMensaje = b.getString("theMessage");
            sentID = b.getString("UID");
            tv.setText(sMensaje);

        }
    }

    private void inicializar(){

        //TEXTVIEW
        tv = (TextView)findViewById(R.id.tvMensaje);
        tvVal = (TextView)findViewById(R.id.tvVal);

        //RADIOGROUP
        radioGroup = (RadioGroup)findViewById(R.id.rgPunt);

        //EDITTEXT
        editText = (EditText)findViewById(R.id.etFeed);

        //BUTTONS
        btnAceptar = (Button)findViewById(R.id.btnRecieve);
        btnDenegar = (Button)findViewById(R.id.btnDeny);
        btnSendFeed = (Button)findViewById(R.id.btnFeed);
        btnVal = (Button)findViewById(R.id.btnConfirm);

       //VISIBILITIES
        editText.setVisibility(View.INVISIBLE);
        btnSendFeed.setVisibility(View.INVISIBLE);
        radioGroup.setVisibility(View.INVISIBLE);
        btnDenegar.setVisibility(View.INVISIBLE);
        btnAceptar.setVisibility(View.INVISIBLE);
        btnVal.setEnabled(false);
    }

    public void recieve (View v){
        sendMessage(v,"Package can be recieved");
    }

    public void notRecieve(View v){
        sendMessage(v,"Package cannot be recieved");
        tv.setText("EL PAQUETE LLEGARA EL SIGUIENTE DÍA LABORABLE");
    }

    public void sendMessage(View view, String message){
        String id = generateMessageId();
        //HashMap<String,String> dataValues = new HashMap<String,String>();
        //dataValues.put("elMensaje", "Tu paquete esta cerca");
        JSONArray jsonArray = new JSONArray();
        //variable token para devolver mensaje en caso de ser el gerente leer de BBDD el UID sobre el mail y el
        //token sobre el UID
        jsonArray.put(token);
        sendFinal(jsonArray,"FEEDBACK NOTIFICATION", "FEEDBACK", null,message);
    }

    private String generateMessageId() {
        String id=null;
        id=UUID.randomUUID().toString();
        return id;
    }

    public void mostrarToken(View view) {
        String token = FirebaseInstanceId.getInstance().getToken();
        Toast t = Toast.makeText(this,token,Toast.LENGTH_LONG);
        t.show();
    }

    public void sendFinal(final JSONArray recipients, final String title, final String body, final String icon, final String message) {

        new AsyncTask<String, String, String>() {
            @Override
            protected String doInBackground(String... params) {
                try {
                    JSONObject root = new JSONObject();
                    JSONObject notification = new JSONObject();
                    notification.put("body", body);
                    notification.put("title", title);
                    notification.put("icon", icon);

                    JSONObject data = new JSONObject();
                    data.put("message", message);
                    data.put("UID", FirebaseAuth.getInstance().getUid());
                    root.put("notification", notification);
                    root.put("data", data);
                    root.put("registration_ids", recipients);

                    String result = postToFCM(root.toString());
                    String TAG2="RESULTADO";
                    Log.d(TAG2, "Result: " + result);
                    return result;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                try {
                    JSONObject resultJson = new JSONObject(result);
                    int success, failure;
                    success = resultJson.getInt("success");
                    failure = resultJson.getInt("failure");
                    Toast.makeText(getApplicationContext(), "Message Success: " + success + "Message Failed: " + failure, Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Message Failed, Unknown error occurred.", Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }

    String postToFCM(String bodyString) throws IOException {
        RequestBody body = RequestBody.create(JSON,bodyString);
        Request request = new Request.Builder()
                .url(FCM_MESSAGE_URL)
                .post(body)
                .addHeader("Authorization", "key= AAAA4HnJLjE:APA91bHaw3U-E2L2qtT4jQcEDV0_CiZK4VXIG5jEhlzSPp_a62mg1UO29oJUK5_KRGcqDBrpW-jrZl1ETp_OdrvEu9Ifjmcdt0bs6-TA42D-1NUQpuDTMVDudPgIkq0f-dweNJr0lVxM")
                .build();
        Response response = mClient.newCall(request).execute();
        return response.body().string();
    }

    public void dbReadOnFeedback() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Token");
        ref.child(sentID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String sentTK = dataSnapshot.getValue(String.class);
                token=sentTK;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    public void startVal(View v){
        tvVal.setVisibility(View.VISIBLE);
        radioGroup.setVisibility(View.VISIBLE);
        editText.setVisibility(View.VISIBLE);
        btnSendFeed.setVisibility(View.VISIBLE);
    }

    public void sendFeedback(View v){
        String valoration = "";
        switch(radioGroup.getCheckedRadioButtonId()){
            case R.id.rb1:{
                value = 1;
                break;
            }
            case R.id.rb2:{
                value = 2;
                break;
            }
            case R.id.rb3:{
                value = 3;
                break;
            }
            case R.id.rb4:{
                value = 4;
                break;
            }
            case R.id.rb5:{
                value = 5;
                break;
            }
        }
        if(!editText.getText().toString().equals("")){
            valoration = editText.getText().toString();
        }

        JSONArray recipient = new JSONArray();
        recipient.put(token);
        createValorationTask(recipient,"Valoration","You have recieved a new validation", null, valoration);
    }

    public void createValorationTask(final JSONArray recipients, final String title, final String body, final String icon, final String message){
        new AsyncTask<String, String, String>() {
            @Override
            protected String doInBackground(String... params) {
                try {
                    JSONObject root = new JSONObject();
                    JSONObject notification = new JSONObject();
                    notification.put("body", body);
                    notification.put("title", title);
                    notification.put("icon", icon);

                    JSONObject data = new JSONObject();
                    data.put("Valoration", message);
                    data.put("Puntuation",value);
                    data.put("UID", FirebaseAuth.getInstance().getUid());
                    root.put("notification", notification);
                    root.put("data", data);
                    root.put("registration_ids", recipients);

                    String result = postToFCM(root.toString());
                    String TAG2="result";
                    Log.d(TAG2, "Result: " + result);
                    return result;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                try {
                    JSONObject resultJson = new JSONObject(result);
                    int success, failure;
                    success = resultJson.getInt("success");
                    failure = resultJson.getInt("failure");
                    Toast.makeText(getApplicationContext(), "Message Success: " + success + "Message Failed: " + failure, Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Message Failed, Unknown error occurred.", Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }

    public void getCityLocation(){
        System.out.println(currentLong);
        Geocoder gcd = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            List<Address> addresses = gcd.getFromLocation(currentLat,currentLong, 1);
            if (addresses.size() > 0) {
                location = addresses.get(0).getLocality();
            } else {
            }
        }catch(IOException ioe){
            Log.d("error",ioe.getMessage().toString());
        }
        tv.setText(tv.getText().toString() + location);
    }
    public void dbReadLocation(){
        dbReadLatitude();
        dbReadLongitude();
    }

    private void dbReadLongitude() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("locations");
        ref.child("uGhsJjEC1CS44vwdoo1PNpR4akY2").child("currentLongitude").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Double longi = (Double)dataSnapshot.getValue(Double.class);
                currentLong = longi;
                System.out.println(currentLong);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void dbReadLatitude() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("locations");
        ref.child("uGhsJjEC1CS44vwdoo1PNpR4akY2").child("currentLatitude").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Double lat = (Double)dataSnapshot.getValue(Double.class);
                currentLat = lat;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    public void sendTest(View v){
        String message = "Espero que funciones por dios";
        String id = generateMessageId();
        //HashMap<String,String> dataValues = new HashMap<String,String>();
        //dataValues.put("elMensaje", "Tu paquete esta cerca");
        JSONArray jsonArray = new JSONArray();
        //variable token para devolver mensaje en caso de ser el gerente leer de BBDD el UID sobre el mail y el
        //token sobre el UID
        dbReadUID();
        dbReadToken();
        Log.d("debug", "Token" + testToken);
        jsonArray.put(testToken);
        sendFinal(jsonArray,"FEEDBACK NOTIFICATION", "FEEDBACK", null,message);
    }

    private void dbReadUID() {
        String ID = "nashexpi@gmail.com";
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("email");
        reference.child(TransformMail.transformMail(ID)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String readUID = dataSnapshot.getValue(String.class);
                UID = readUID;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

    }
    private void dbReadToken(){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Token");
        Log.d("debug","UID: " + UID);
        String id = UID.toString();
        System.out.println(id);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> it = dataSnapshot.getChildren();
                for (DataSnapshot child: it) {
                    if(child.getKey().equalsIgnoreCase(UID)){
                        testToken = (String)child.getValue();
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }
    public void writeDB(String token) {
        String ID = FirebaseAuth.getInstance().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Token");
        ref.child(ID).setValue(token);
    }
}

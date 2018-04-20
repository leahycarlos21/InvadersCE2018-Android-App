package datos1.tec.com.tcpclienteandroid;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {
    SensorManager sensorManager;
    Sensor sensor;
    SensorEventListener sensorEventListener;


    /**
     * Controles
     */
    private Button button;
    private Context context = this;
    private TextView nivelText;
    private String posicion = "Centro";

    /**
     * Puerto
     */
    private static final int SERVERPORT = 5000;
    /**
     * HOST
     */
    private static final String ADDRESS = "192.168.43.46";
public void disparoClick(View view){
    MyATaskCliente myATaskYW1 = new MyATaskCliente();
    onPause();
    myATaskYW1.execute("Disparo");
}
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = ((Button) findViewById(R.id.button));
        nivelText = ((TextView) findViewById(R.id.nivelID));

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (sensor == null)//ve si existe el sensor
            finish();
        sensorEventListener = new SensorEventListener() {


            @Override
            public void onSensorChanged(SensorEvent event) {
                float x = event.values[1];
                if (x < -3) {
                    posicion = "Izquierda";
                    button.setText("Izquierda");
                    stop();
                    MyATaskCliente myATaskYW = new MyATaskCliente();

                    myATaskYW.execute(posicion);

                } else if (x > 3) {
                    stop();
                    button.setText("Derecha");
                    posicion = "Derecha";
                    MyATaskCliente myATaskYW = new MyATaskCliente();

                    myATaskYW.execute(posicion);


                } else if (x < 3 && x > -3) {
                    start();
                    button.setText("Centro");
                    posicion = "Centro";
                    MyATaskCliente myATaskYW = new MyATaskCliente();

                    myATaskYW.execute(posicion);
                }

            }


            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
        start();
    }//end:onCreate

    private void start() {
        sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void stop() {
        sensorManager.unregisterListener(sensorEventListener);
    }



    @Override
    protected void onResume() {
        start();
        super.onResume();
    }


    /**
     * Interactuar con el serverSocket
     */
    class MyATaskCliente extends AsyncTask<String, Void, String> {


        ProgressDialog progressDialog;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(context);
            progressDialog.setCanceledOnTouchOutside(false);

            //progressDialog.show();
        }

        /**
         * Se conecta al servidor y obtiene el dato
         */
        @Override
        protected String doInBackground(String... values) {

            try {
                //Se conecta al servidor
                InetAddress serverAddr = InetAddress.getByName(ADDRESS);
                Socket socket = new Socket(serverAddr, SERVERPORT);

                //envia peticion de cliente

                PrintStream output = new PrintStream(socket.getOutputStream());
                String request = values[0];
                output.println(request);

                //recibe respuesta del servidor y formatea a String
                InputStream stream = socket.getInputStream();

                byte[] lenBytes = new byte[256];


                stream.read(lenBytes, 0, 256);

                String received = new String(lenBytes, "UTF-8").trim();

                Log.i("I/TCP Client", "Received " + received.toCharArray()[1]);
                Log.i("I/TCP Client", "");
                //cierra conexion
                socket.close();
                return received;
            } catch (UnknownHostException ex) {
                Log.e("E/TCP C  lient", "" + ex.getMessage());
                return null;

            } catch (IOException ex) {
                Log.e("E/TCP Client", "" + ex.getMessage());
                return null;
            }
        }


        @Override
        protected void onPostExecute(String value) {
            progressDialog.dismiss();
            nivelText.setText(value);
            onResume();
        }
    }
}
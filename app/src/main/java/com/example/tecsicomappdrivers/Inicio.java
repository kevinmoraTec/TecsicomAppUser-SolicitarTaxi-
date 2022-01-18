package com.example.tecsicomappdrivers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonObject;
import com.shashank.sony.fancygifdialoglib.FancyGifDialog;
import com.shashank.sony.fancygifdialoglib.FancyGifDialogListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Inicio extends AppCompatActivity implements OnMapReadyCallback {

    TextView direccionTotal, direccionFinal;
    Button ubicacion, cerrarSesion,mostrar,myAsignacion,btnacalificar;
    Dialog dialogCalificar;
    ImageView imageView ;
    Double latitud,longitud;

    private FirebaseAuth mAuth;
    private GoogleMap mMap;
    private DatabaseReference mDatabaseReference;
    private RequestQueue requestQueue;
    private static final String url="http://192.168.1.5:4000/allasignaciones";
    private static final String urlmypeticion="http://192.168.1.5:4000/selectViajeManual";
    private static final String urlDriveraddActivo="http://192.168.1.5:4000/addDriverActivo";





    // private static int AUTOCOMPLETE_REQUEST_CODE = 1;
    //private static int TO_REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);
        requestQueue= Volley.newRequestQueue(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseReference= FirebaseDatabase.getInstance().getReference();

        //Places.initialize(getApplicationContext(), getString(R.string.places));
        direccionTotal=findViewById(R.id.tvdireccionTotal);
        ubicacion = findViewById(R.id.butonUbicacion);
        cerrarSesion = findViewById(R.id.butonCerrarSecion);
        mostrar=findViewById(R.id.bottonEnviarPeticiones);
        myAsignacion=findViewById(R.id.botonMYpetciones);
        dialogCalificar=new Dialog(Inicio.this);
        btnacalificar=findViewById(R.id.buttonComentarios);
        imageView =dialogCalificar.findViewById(R.id.imageViewClose);



        // Bienvenido User
        welcomoUserInfo();

        //Asignar los datod A mysql DriverActivo
        asignarDriverAMysqlDriverActivo();

        ubicacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocalizacion();

            }
        });
//
        cerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                startActivity(new Intent(Inicio.this, MainActivity.class));
                finish();
                //https://www.youtube.com/watch?v=cZWSpWwToas
                //9.51
            }
        });

        mostrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //openDialog(v);


                Custom_Dialog dialog = new Custom_Dialog();

                Bundle bundle = new Bundle();
                bundle.putString("latitud", String.valueOf(latitud));
                bundle.putString("body", "Body");
                dialog.setArguments(bundle);
                dialog.show(getSupportFragmentManager(), "DialogFragmentWithSetter");

            }
        });


        //Para mapas >>> https://www.youtube.com/watch?v=cZWSpWwToas



        myAsignacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cargarmyPeticion();
            }
        });



    }





    private void getLocalizacion() {
        int permiso = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (permiso == PackageManager.PERMISSION_DENIED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);

        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        LocationManager locationManager = (LocationManager) Inicio.this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                LatLng miUbicacion = new LatLng(location.getLatitude(), location.getLongitude());
                direccionTotal.setText("LT= "+location.getLatitude()+" "+"LO= "+location.getLongitude());
                    latitud=location.getLatitude();
                enviarLatidtud(latitud);

                mMap.addMarker(new MarkerOptions().position(miUbicacion).title("ubicacion actual"));
                //                mMap.addMarker(new MarkerOptions().position(miUbicacion).icon(BitmapDescriptorFactory.fromResource(R.drawable.taximarker)).anchor(0.0f,1.0f).position(miUbicacion));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(miUbicacion));
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(miUbicacion)
                        .zoom(14)
                        .bearing(90)
                        .tilt(45)
                        .build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);


    }

    public void welcomoUserInfo(){
        String id=mAuth.getCurrentUser().getUid();
        mDatabaseReference.child("Drivers").child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String name =snapshot.child("NameDriver").getValue().toString();
                    Toast.makeText(Inicio.this,"Welcome :"+name,Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            Log.e("error Welcome User ",error.getMessage());
            }
        });


    }
// Envia los datos que mi user esta activo A ka bd
    public void asignarDriverAMysqlDriverActivo(){
        String id=mAuth.getCurrentUser().getUid();
        mDatabaseReference.child("Drivers").child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String name =snapshot.child("NameDriver").getValue().toString();
                    String placaDriver =snapshot.child("PlacaDriver").getValue().toString();

                    Toast.makeText(Inicio.this,"Welcome Placa:"+placaDriver,Toast.LENGTH_SHORT).show();

                    Toast.makeText(Inicio.this,"Welcome Name:"+name,Toast.LENGTH_SHORT).show();
                    eviarAbdDrivaersActivos(id,name,placaDriver);
                }else {
                    Toast.makeText(Inicio.this,"No Existe Driver",Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
    public  void traerSolicitudes(){
        StringRequest request=new StringRequest(
                Request.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {


                        JSONObject jsonObject = null;
                        try {
                            jsonObject = new JSONObject(response);
                            String startDireccion=jsonObject.getString("StartDirection").toString();
                            //Toast.makeText(Inicio.this,"-> "+startDireccion,Toast.LENGTH_SHORT).show();
                           // Toast.makeText(Inicio.this,"-> "+jsonObject.getString("FinalDirection").toString(),Toast.LENGTH_SHORT).show();
                            cargarAsignaciones(startDireccion,jsonObject.getString("FinalDirection").toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //Toast.makeText(Inicio.this,response,Toast.LENGTH_SHORT).show();





                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Error Volelly",error.getMessage());
                    }
                }
        );
        requestQueue.add(request);
    }
// Motod para cargar una peticion si la Asignacion es para mi ID
    public  void cargarmyPeticion(){
        // Validamos que la Asignacion sea para mi id
        String id=mAuth.getCurrentUser().getUid();
        mDatabaseReference.child("Drivers").child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    //Toast.makeText(Inicio.this,id,Toast.LENGTH_SHORT).show();

                    StringRequest request=new StringRequest(
                            Request.Method.GET,
                            urlmypeticion,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {

                                    JSONObject jsonObject = null;
                                    try {
                                        jsonObject = new JSONObject(response);
                                        String idAsignaciondriver=jsonObject.getString("idDriverActivo").toString();
                                        Toast.makeText(Inicio.this,idAsignaciondriver,Toast.LENGTH_SHORT).show();
                                        Toast.makeText(Inicio.this,">>"+id,Toast.LENGTH_SHORT).show();

                                        if (id.equals(idAsignaciondriver)){
                                            jsonObject = new JSONObject(response);
                                            String idRequqestCaragarDatos=jsonObject.getString("idRequqest").toString();
                                            Toast.makeText(Inicio.this,idRequqestCaragarDatos,Toast.LENGTH_SHORT).show();
                                            int idRequesCargasDatos=Integer.parseInt(idRequqestCaragarDatos);

                                            //Toast.makeText(Inicio.this,"-> "+startDireccion,Toast.LENGTH_SHORT).show();
                                            // Toast.makeText(Inicio.this,"-> "+jsonObject.getString("FinalDirection").toString(),Toast.LENGTH_SHORT).show();
                                            mostarDatosPeticionAsigada(idRequesCargasDatos);
                                        }else {
                                            Toast.makeText(Inicio.this,"No tienes Asignaciones DEL ADministrados",Toast.LENGTH_SHORT).show();

                                        }

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    //Toast.makeText(Inicio.this,response,Toast.LENGTH_SHORT).show();

                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("Error Volelly",error.getMessage());
                        }
                    }
                    );
                    requestQueue.add(request);

                }else{
                    Toast.makeText(Inicio.this,"No Existe User",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void eviarAbdDrivaersActivos(String id,String name,String placaDriver){
        Toast.makeText(Inicio.this,"Heyy >>"+id+" : "+placaDriver,Toast.LENGTH_SHORT).show();

        StringRequest postRequest = new StringRequest(Request.Method.POST, urlDriveraddActivo, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                ////////////
                ///////////  Respusta del servidor///////////
                ////////////
                Toast.makeText(Inicio.this,"[]"+response,Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Error Volelly",error.getMessage());
            }
        }){
            protected Map<String,String>  getParams(){
                Map<String,String> params = new HashMap<>();
                params.put("idDriverActivo",id);
                params.put("Nombre",name);
                params.put("placa",placaDriver);
                return params;
            }
        };
        Volley.newRequestQueue(this).add(postRequest);

    }

    public  void mostarDatosPeticionAsigada(int idRequesColsulta){
        Toast.makeText(Inicio.this,"[Lllego al final Ok Ok]",Toast.LENGTH_SHORT).show();

        String urlMostrarDatosAgisnacion="http://192.168.1.5:4000/consultaDatosRequesAsignado/"+idRequesColsulta;

        StringRequest getRequest = new StringRequest(Request.Method.GET, urlMostrarDatosAgisnacion, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    int idRequest=0;
                    String nameUser="";
                    String startDirection="";
                    String finalDirection="";
                    String descriptions="";
                    int estado=0;
                    String nombreAdministrador="";
                    String dateOrden="";
                    JSONObject  jsonObject = new JSONObject(response);
                    idRequest=jsonObject.getInt("idRequest");
                    nameUser=jsonObject.getString("nameUser");
                    startDirection=jsonObject.getString("StartDirection");
                    finalDirection=jsonObject.getString("FinalDirection");
                    descriptions=jsonObject.getString("Descriptions");
                    estado=jsonObject.getInt("Estado");
                    nombreAdministrador=jsonObject.getString("Admistrador");
                    dateOrden=jsonObject.getString("DateOrden");

                    cargarAsignacionesDatosDelRequest(idRequest,nameUser,descriptions,startDirection,finalDirection,estado,nombreAdministrador,dateOrden);



                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        Volley.newRequestQueue(this).add(getRequest);

    }


    public void  editarStadoDelRequestAsignado(int  idRequesColsultaIdUpdate){
        Toast.makeText(Inicio.this,"[Lllego al final Ok Ok]",Toast.LENGTH_SHORT).show();

        String urlUpdateDatosAgisnacion="http://192.168.1.5:4000/editarEstadoRequest/"+idRequesColsultaIdUpdate;

        StringRequest getRequest = new StringRequest(Request.Method.GET, urlUpdateDatosAgisnacion, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                ////////////
                ///////////  Respusta del servidor///////////
                ////////////
                Toast.makeText(Inicio.this,"[Estado Request ]"+response,Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Error Volelly",error.getMessage());
            }
        });
        Volley.newRequestQueue(this).add(getRequest);
    }


    public void cargarAsignaciones(String starD,String finishD){
        new FancyGifDialog.Builder(Inicio.this)
                .setTitle("Direcccion :"+starD) // You can also send title like R.string.from_resources
                .setMessage("Direccon Final :"+finishD) // or pass like R.string.description_from_resources
                .setTitleTextColor(R.color.titleText)
                .setDescriptionTextColor(R.color.descriptionText)
                .setNegativeBtnText("Cancel") // or pass it like android.R.string.cancel
                .setPositiveBtnBackground(R.color.positiveButton)
                .setPositiveBtnText("Ok") // or pass it like android.R.string.ok
                .setNegativeBtnBackground(R.color.negativeButton)
                .setGifResource(R.drawable.giftdireccion)   //Pass your Gif here
                .isCancellable(true)
                .OnPositiveClicked(new FancyGifDialogListener() {
                    @Override
                    public void OnClick() {
                        Toast.makeText(Inicio.this,"Ok",Toast.LENGTH_SHORT).show();
                    }
                })
                .OnNegativeClicked(new FancyGifDialogListener() {
                    @Override
                    public void OnClick() {
                        Toast.makeText(Inicio.this,"Cancel",Toast.LENGTH_SHORT).show();
                    }
                })
                .build();
    }

    public void cargarAsignacionesDatosDelRequest(int idRequest,String nameUserString,String description,String startDireccion,String finishD, int estad,String nombreAdministrado,String dateorden){
        new FancyGifDialog.Builder(Inicio.this)
                .setTitle("Direcccion : "+startDireccion) // You can also send title like R.string.from_resources
                .setMessage("Direccon Final : "+finishD+""+"\n"+""
                        +"Sector/Referencia : "+description+"\n"+""
                        +"Usuario : "+nameUserString+"\n"+""
                        +"Administrador : "+nombreAdministrado+"\n"+""
                        +"Fecha : "+dateorden+"\n"+""
                        +"Estado : "+estad) // or pass like R.string.description_from_resources
                .setTitleTextColor(R.color.titleText)
                .setDescriptionTextColor(R.color.descriptionText)
                .setNegativeBtnText("Cancel") // or pass it like android.R.string.cancel
                .setPositiveBtnBackground(R.color.positiveButton)
                .setPositiveBtnText("Ok") // or pass it like android.R.string.ok
                .setNegativeBtnBackground(R.color.negativeButton)
                .setGifResource(R.drawable.giftdireccion)   //Pass your Gif here
                .isCancellable(true)
                .OnPositiveClicked(new FancyGifDialogListener() {
                    @Override
                    public void OnClick() {
                        Toast.makeText(Inicio.this,"Ok",Toast.LENGTH_SHORT).show();
                        editarStadoDelRequestAsignado(idRequest);
                        try {
                                    openingWinDialog();
                        }catch (Error error){
                            Toast.makeText(Inicio.this, (CharSequence) error, Toast.LENGTH_SHORT).show();
                        }
                        openingWinDialog();

                    }
                })
                .OnNegativeClicked(new FancyGifDialogListener() {
                    @Override
                    public void OnClick() {
                        Toast.makeText(Inicio.this,"Cancel",Toast.LENGTH_SHORT).show();
                    }
                })
                .build();
    }
    private  void  openingWinDialog(){
        dialogCalificar.setContentView(R.layout.win_layout);
        dialogCalificar.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
      /*
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogCalificar.dismiss();
                Toast.makeText(Inicio.this,"Dialog Close",Toast.LENGTH_SHORT).show();

            }
        });
        */
        
        dialogCalificar.show();
    }

    public void openDialog(View view){
        Custom_Dialog custom_dialog = new Custom_Dialog();
        custom_dialog.show(getSupportFragmentManager(),"Ingresa Una Referencia De tu Ubicacion");
    }

    public Double enviarLatidtud(Double latitud){
        return latitud;
    }


}
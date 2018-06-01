package edu.np.ece.wetrack;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import edu.np.ece.wetrack.api.RetrofitUtils;
import edu.np.ece.wetrack.api.ServerAPI;
import edu.np.ece.wetrack.model.BeaconInfo;
import edu.np.ece.wetrack.model.Relative;
import edu.np.ece.wetrack.model.Resident;
import edu.np.ece.wetrack.tasks.ImageLoadTask;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static edu.np.ece.wetrack.BeaconScanActivation.patientList;

public class ResidentDetailActivity extends AppCompatActivity {

    @BindView(R.id.name)
    TextView name;

    @BindView(R.id.avatar)
    ImageView avt;

    @BindView(R.id.status)
    TextView status;

    @BindView(R.id.nric)
    TextView nric;

    @BindView(R.id.dob)
    TextView dob;

    @BindView(R.id.beaconList)
    TextView tvBeaconList;

    @BindView(R.id.reportedAt)
    TextView reportedAt;

    @BindView(R.id.lastSeen)
    TextView lastSeenBeacon;

    @BindView(R.id.lastLocation)
    TextView lastLocation;

    @BindView(R.id.remark)
    TextView remark;
////new
    @BindView(R.id.tvDectectInfo)
    TextView tvDectectedInfo;
    @BindView(R.id.tvBDetectInfo)
    TextView tvBDectectInfo;
    @BindView(R.id.tvBLocationInfo)
    TextView tvBLocationInfo;
    @BindView(R.id.tvBBelongInfo)
    TextView tvBBelongInfo;
/////
    private Handler handler;

    private ServerAPI serverAPI;

    @BindView(R.id.srlUsers)
    SwipeRefreshLayout srlUser;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.statusArea)
    LinearLayout statusArea;

    @BindView(R.id.mySwitch)
    ToggleButton toggleButton;

    @BindView(R.id.tvRemind)
    TextView remind;

    String uri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resident_detail);
        ButterKnife.bind(this);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String token = sharedPref.getString("userToken-WeTrack", "");//retrieving user token

        final Resident patient = getIntent().getParcelableExtra("patient");//retrieving patient from intent

        if (token.equals("")) {//when there is no token received
            //go to loginActivity
            Intent intent = new Intent(getBaseContext(), LoginActivity.class);
            intent.putExtra("whatParent", "yyy");////?
            startActivity(intent);
        } else {//condition when there is a token
            final ProgressDialog dialog = ProgressDialog.show(this, "We Track",
                    "Loading...Please wait...", true, false);

            serverAPI = RetrofitUtils.get().create(ServerAPI.class);

            displayDetail(patient, dialog);//display the resident's info

            //srlUser is the swipe refresh layout
            srlUser.setDistanceToTriggerSync(400);
            srlUser.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

                @Override
                public void onRefresh() {//pull to refresh
                    dialog.show();

                    displayDetail(patient, dialog);

                    srlUser.setRefreshing(false);

                }
//                }, 100);
//            }
            });
        }


        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


        toggleButton.setVisibility(View.GONE);
        statusArea.setVisibility(View.GONE);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {//for the 3 vertical dot menu
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();//invoke the function of back button press
                break;
        }

        return super.onOptionsItemSelected(item);
    }
////////////////////////////////////////////start of method//////////////////////////////
    //method to display patients info
    private void displayDetail(final Resident patient, final ProgressDialog dialog) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        //below: finalize and get the token from shared preference
        final String token = sharedPref.getString("userToken-WeTrack", "");
        //calling api to get Patient list from the server
        serverAPI.getPatientList("Bearer " + token).enqueue(new Callback<List<Resident>>() {
            @Override
            public void onResponse(Call<List<Resident>> call, Response<List<Resident>> response) {
                try {
                    patientList = response.body();//retrieve the list from the response

                    Gson gson = new Gson();
                    String jsonPatients = gson.toJson(patientList);
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("patientList-WeTrack", jsonPatients);
                    editor.commit();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<List<Resident>> call, Throwable t) {
                t.printStackTrace();
                Gson gson = new Gson();
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                String jsonPatients = sharedPref.getString("patientList-WeTrack", "");
                //get new token
                Type type = new TypeToken<List<Resident>>() {
                }.getType();
                patientList = gson.fromJson(jsonPatients, type);
            }
        });


        handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //below: when there is patient
                if (patient != null) {
                    //below: condition when there is patient
                    if (patientList != null && !patientList.equals("") && patientList.size() > 0) {
                        //below: for the patient in correspondence to the patient in the list from the
                        for (final Resident aPatient : patientList) {
                           //below: condition if the patient id is identical to the patientID from the Intent
                            if (aPatient.getId() == patient.getId()) {

                                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());//12/04
                                final String userRole = sharedPref.getString("userRole-WeTrack", "");//12/04
                                if(userRole.equals("5")){//new
                                    name.setText("********");//set the name
                                    tvDectectedInfo.setVisibility(View.GONE);
                                    tvBDectectInfo.setVisibility(View.GONE);
                                    tvBLocationInfo.setVisibility(View.GONE);
                                    tvBBelongInfo.setVisibility(View.GONE);
                                }
                                else{//new
                                    name.setText(aPatient.getFullname());//set the name
                                }
                                //name.setText(aPatient.getFullname());//set the name
                                //below: condition when there is no profile pic
                                if (aPatient.getThumbnailPath() == null || aPatient.getThumbnailPath().equals("")) {
                                    avt.setImageResource(R.drawable.default_avt);//set to default pic
                                }
                                else {//condition when there is profile pic
                                    if(userRole.equals("5")){//new
                                        avt.setImageResource(R.drawable.default_avt);//set to default pic
                                    }
                                    else {//new
                                        new ImageLoadTask("http://128.199.93.67/WeTrack/backend/web/" + aPatient.getThumbnailPath().replace("thumbnail_",""), avt, getBaseContext()).execute();

                                    }
                                    //below: load the image
                                    //new ImageLoadTask("http://128.199.93.67/WeTrack/backend/web/" + aPatient.getThumbnailPath().replace("thumbnail_",""), avt, getBaseContext()).execute();
//                                    avt.setMaxHeight(150);
//                                    avt.setMaxWidth(150);
                                }

                                if (aPatient.getRemark().equals("")) {//condition when there is no remarks
                                    remark.setText("None");//set textview to no remarks to none

                                } else {//condition when there is remark
                                    remark.setText(aPatient.getRemark());//put in the remarks on textview

                                }
                                if(userRole.equals("5")){
                                    nric.setVisibility(View.GONE);//new
                                }
                                else {
                                    nric.setText(aPatient.getNric());//set the nric onn textview //new

                                }
                                //nric.setText(aPatient.getNric());//set the nric onn textview
                                String tmp = "";
                                if (aPatient.getStatus() == 1) {//condition for missing status
                                    tmp = "Missing";//indicate missing
                                    //TODO
                                    toggleButton.setChecked(true);
                                } else {//condition when the patient is not missing
                                    tmp = "Available";
                                    //TODO
                                    toggleButton.setChecked(false);
                                }

                                status.setText(tmp);//set the missing/available on the status textview

                                final SharedPreferences sharedPref2 = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                                String userID = sharedPref2.getString("userID-WeTrack", "");//get userID
                                if (userID.equals("0")) {//when the user is annonymous
                                    toggleButton.setVisibility(View.GONE);//remove the change status button
                                    statusArea.setVisibility(View.GONE);// and the status area
                                } else {//condition when the user is is registered user
                                    if (!userID.equals("")) {//condition when the user have an id
                                        //for  all the relative in correspondence to the relatives of the patient
                                        for (Relative aRelative : aPatient.getRelatives()) {
                                            if (String.valueOf(aRelative.getId()).equals(userID)) {//when this id is the relative of the patient
                                                toggleButton.setVisibility(View.VISIBLE);//enablee button
                                                statusArea.setVisibility(View.VISIBLE);

                                                toggleButton.setOnClickListener(new View.OnClickListener() {//listener for the toggle button for the status
                                                    @Override
                                                    public void onClick(View v) {
                                                        //below: get user token
                                                        final String token = sharedPref2.getString("userToken-WeTrack", "");

                                                        final Gson gson = new GsonBuilder()
                                                                .setLenient()
                                                                .create();

                                                        final EditText input = new EditText(ResidentDetailActivity.this);
                                                        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);

//                                                        ContextThemeWrapper ctw = new ContextThemeWrapper(this, AlertDialog.);
                                                        AlertDialog alertDialog = new AlertDialog.Builder(ResidentDetailActivity.this).create();
                                                        alertDialog.setTitle("Remark");

                                                        alertDialog.setView(input);
                                                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                                                                new DialogInterface.OnClickListener() {
                                                                    public void onClick(DialogInterface dialog, int which) {

//                                                                        Date aDate = new Date();
//                                                                        SimpleDateFormat curFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                                                                        String dateObj = curFormatter.format(aDate);
//                                                                        reportedAt.setText(dateObj);

                                                                        if(input.getText().toString().equals(""))//if there is no remark
                                                                        {
                                                                            remark.setText("Unknown");

                                                                        }else{//when there is remark
                                                                            remark.setText(input.getText().toString());

                                                                        }

                                                                        aPatient.setRemark(input.getText().toString());//set to Resident class
                                                                        JsonObject obj = gson.toJsonTree(aPatient).getAsJsonObject();
                                                                        //call the api to send the changes of the patient status to the server
                                                                        serverAPI.changeStatus("Bearer " + token, "application/json", obj).enqueue(new Callback<Resident>() {
                                                                            @Override
                                                                            public void onResponse(Call<Resident> call, Response<Resident> response) {
                                                                                //below: if the status of the resident is missing
                                                                                if (status.getText().equals("Missing")) {
                                                                                    status.setText("Available");///// not missing yet
                                                                                    remind.setVisibility(View.GONE);
                                                                                    if(userRole.equals("5")){
                                                                                        reportedAt.setVisibility(View.GONE);
                                                                                        lastSeenBeacon.setVisibility(View.GONE);
                                                                                        lastLocation.setVisibility(View.GONE);
                                                                                    }
                                                                                } else {//if the status of the resident is missing
                                                                                    status.setText("Missing");
                                                                                    if(userRole.equals("5")){
                                                                                        reportedAt.setVisibility(View.GONE);
                                                                                        lastSeenBeacon.setVisibility(View.GONE);
                                                                                        lastLocation.setVisibility(View.GONE);
                                                                                    }
                                                                                    else{
                                                                                        reportedAt.setText("Unknown");
                                                                                        lastSeenBeacon.setText("Unknown");
                                                                                        lastLocation.setText("Unknown");
                                                                                    }
                                                                                   // reportedAt.setText("Unknown");
                                                                                    //lastSeenBeacon.setText("Unknown");
                                                                                    //lastLocation.setText("Unknown");
                                                                                    remind.setVisibility(View.VISIBLE);
                                                                                }

                                                                            }

                                                                            @Override
                                                                            public void onFailure(Call<Resident> call, Throwable t) {

                                                                            }
                                                                        });
                                                                    }
                                                                });

                                                        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL",
                                                                new DialogInterface.OnClickListener() {
                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                        if (status.getText().equals("Missing")) {
                                                                            toggleButton.setChecked(true);//toggle button allows to change status of patient
                                                                            remind.setVisibility(View.VISIBLE);
                                                                        } else {
                                                                            toggleButton.setChecked(false);
                                                                            remind.setVisibility(View.GONE);
                                                                        }
                                                                        dialog.dismiss();
                                                                    }
                                                                });


                                                        if (status.getText().equals("Available")) {
                                                            alertDialog.show();
                                                        } else {

                                                            final JsonObject obj = gson.toJsonTree(aPatient).getAsJsonObject();

                                                            serverAPI.changeStatus("Bearer " + token, "application/json", obj).enqueue(new Callback<Resident>() {
                                                                @Override
                                                                public void onResponse(Call<Resident> call, Response<Resident> response) {
                                                                    if (status.getText().equals("Missing")) {
                                                                        status.setText("Available");
                                                                        remind.setVisibility(View.GONE);
                                                                    } else {
                                                                        status.setText("Missing");
                                                                        remind.setVisibility(View.VISIBLE);
                                                                    }

                                                                }

                                                                @Override
                                                                public void onFailure(Call<Resident> call, Throwable t) {

                                                                }
                                                            });
                                                        }


                                                    }
                                                });
                                            }
                                        }


                                    }
                                }

                                dob.setText(aPatient.getDob());

                                String beacons = "";

                                if (aPatient.getBeacons() != null && aPatient.getBeacons().size() > 0) {//when there is beacon
                                    for (BeaconInfo temp : aPatient.getBeacons()) {//for a beaconInfo in correspondence to the patient beacon
                                        //get info
                                        beacons += "\t► ID: " + temp.getId() + " ☼ Major: " + temp.getMajor() + " | Minor: " + temp.getMinor() + "\n";
                                    }
                                }
                                if(userRole.equals("5")){
                                    tvBeaconList.setVisibility(View.GONE);
                                }
                                else{
                                    tvBeaconList.setText(beacons);

                                }
                                //tvBeaconList.setText(beacons);

//                                reportedAt.setText(aPatient.getCreatedAt());


                              /*  if (aPatient.getLatestLocation().size() != 0) {//when there is last location
                                    if (aPatient.getLatestLocation() != null && aPatient.getLatestLocation().size() > 0) {
                                        reportedAt.setText(aPatient.getLatestLocation().get(0).getCreatedAt());

                                        //get the beacon  info
                                        String beaconDetected= "Unknown";
                                        if (aPatient.getBeacons() != null && aPatient.getBeacons().size() > 0) {
                                            for (BeaconInfo temp : aPatient.getBeacons()) {
                                                if(aPatient.getLatestLocation().get(0).getBeaconId() == temp.getId()) {
                                                    beaconDetected = "\t► ID: " + temp.getId() + " ☼ Major: " + temp.getMajor() + " | Minor: " + temp.getMinor() + "\n";
                                                }
                                            }
                                        }

                                        if(userRole.equals("5")){
                                            reportedAt.setVisibility(View.GONE);
                                            lastSeenBeacon.setVisibility(View.GONE);
                                            lastLocation.setVisibility(View.GONE);
                                        }
                                        else {
                                            lastSeenBeacon.setText(beaconDetected);
                                            lastLocation.setText(aPatient.getLatestLocation().get(0).getAddress());
                                        }
                                        //lastSeenBeacon.setText(beaconDetected);
                                       // lastLocation.setText(aPatient.getLatestLocation().get(0).getAddress());
                                    }

                                    uri = "http://maps.google.com/maps?q=loc:" + aPatient.getLatestLocation().get(0).getLatitude() + "," + aPatient.getLatestLocation().get(0).getLongitude() + " (" + aPatient.getFullname() + ")";

                                } else {
                                    if(userRole.equals("5")){
                                        reportedAt.setVisibility(View.GONE);
                                        lastSeenBeacon.setVisibility(View.GONE);
                                        lastLocation.setVisibility(View.GONE);
                                    }
                                    else {
                                        reportedAt.setText("Unknown");
                                        lastSeenBeacon.setText("Unknown");
                                        lastLocation.setText("Unknown");
                                    }
                                    //reportedAt.setText("Unknown");
                                    //lastSeenBeacon.setText("Unknown");
                                    //lastLocation.setText("Unknown");
                                }*/ //25/04
                            }

                        }

                    }

                    dialog.dismiss();

                }
            }
        }, 2000);


    }
/////////////////////////////End of method///////////////////////////////////////////////////////////
//    @Override
//    protected void onNewIntent(Intent intent) {
//        final Resident patient = getIntent().getExtras().getParcelable("patient");
//        Log.i("longgggggggggggg", patient.getFullname());
//        super.onNewIntent(intent);
//    }

    @Override
    public void onBackPressed() {

        Intent detailIntent = getIntent();

        Intent intent = new Intent(this, MainActivity.class);//go back to main activity

        Bundle c = new Bundle();

        if (detailIntent != null) {
            try {
                String tmp = detailIntent.getStringExtra("fromWhat");
                if (tmp.equals("home")) {
                    c.putString("whatParent", "home");
                    intent.putExtras(c);

                }
                if (tmp.equals("detectedList")) {
                    c.putString("whatParent", "detectedList");
                    intent.putExtras(c);

                }

                if (tmp.equals("relativeList")) {
                    c.putString("whatParent", "relativeList");
                    intent.putExtras(c);

                }

                startActivity(intent);
            } catch (Exception e) {
                c.putString("isFromDetailActivity", "false");
                intent.putExtras(c);
                startActivity(intent);

            }

        }
        finish();


    }

    @OnClick(R.id.openMap)//for the map
    public void onUpdateClick() {
        if (uri != null && !uri.equals("") && !lastLocation.getText().equals("Unknown") ) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("whatParent", "xxx");
            getBaseContext().startActivity(intent);
        }

    }

}

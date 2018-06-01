package edu.np.ece.wetrack;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import edu.np.ece.wetrack.api.RetrofitUtils;
import edu.np.ece.wetrack.api.ServerAPI;
import edu.np.ece.wetrack.model.BeaconInfo;
import edu.np.ece.wetrack.model.Resident;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static edu.np.ece.wetrack.BeaconScanActivation.patientList;
//import static edu.np.ece.wetrack.MainActivity.homeAdapter;

/**
 * Created by hoanglong on 06-Dec-16.
 */
//Fragments are used for tabs
public class HomeFragment extends Fragment {
    @BindView(R.id.rvMissingResident)
    RecyclerView rvResident;
    private List<Resident> missingPatientList = new ArrayList<>();

    private HomeAdapter homeAdapter;


    public static HomeFragment newInstance(String title) {
        Bundle args = new Bundle();
        args.putString("title", title);
        HomeFragment fragment = new HomeFragment();
        fragment.setArguments(args);

        return fragment;
    }

    private Handler handler;
    @BindView(R.id.srlUsers)
    SwipeRefreshLayout srlUser;


    private ServerAPI serverAPI;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
//        tvTitle = (TextView) rootView.findViewById(R.id.tvTitle);
        //BInd Views
        ButterKnife.bind(this, rootView);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rvResident.getContext(),
                DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.divider_line));
        rvResident.addItemDecoration(dividerItemDecoration);

//        rvResident.addItemDecoration(
//                new DividerItemDecoration(getActivity(), R.drawable.divider_line));

        rvResident.setLayoutManager(new LinearLayoutManager(getActivity()));

//        MainActivity.homeAdapter = new HomeAdapter(missingPatientList);
//        rvResident.setAdapter(MainActivity.homeAdapter);
        onActivityCreated(savedInstanceState);

        serverAPI = RetrofitUtils.get().create(ServerAPI.class);

        handler = new Handler();


        srlUser.setDistanceToTriggerSync(550);//pull to refresh
        srlUser.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (getContext() != null) {
                            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
                            String token = sharedPref.getString("userToken-WeTrack", "");//retrieving tokens
                            //below: call th api to get the patient list
                            serverAPI.getPatientList("Bearer " + token).enqueue(new Callback<List<Resident>>() {
                                //below: return the list
                                @Override
                                public void onResponse(Call<List<Resident>> call, Response<List<Resident>> response) {
                                    try {
                                        patientList = response.body();

                                        Gson gson = new Gson();
                                        String jsonPatients = gson.toJson(patientList);
                                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
                                        SharedPreferences.Editor editor = sharedPref.edit();
                                        editor.putString("patientList-WeTrack", jsonPatients);//Refresh patient list in shared preference
                                        editor.commit();

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onFailure(Call<List<Resident>> call, Throwable t) {
                                    t.printStackTrace();
                                    Gson gson = new Gson();
                                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
                                    String jsonPatients = sharedPref.getString("patientList-WeTrack", "");
                                    Type type = new TypeToken<List<Resident>>() {
                                    }.getType();
                                    patientList = gson.fromJson(jsonPatients, type);
                                }
                            });

//                            MainActivity.homeAdapter = new HomeAdapter(missingPatientList);

                            homeAdapter = new HomeAdapter(missingPatientList);//for the missing patient list on the adapter
                            //below: when there is patient on the patient list
                            if (patientList != null && !patientList.equals("") && patientList.size() > 0) {
                                //for the patient in correspondence to the patient in the patient list from the server
                                for (Resident aPatient : patientList) {
                                    //below: for the beacon info that correspond with the the beacon info from the server
                                    for (BeaconInfo aBeacon : aPatient.getBeacons()) {
                                        //if the patient is reported missing
                                        if (aPatient.getStatus() == 1 && aBeacon.getStatus() == 1 && aPatient.getBeacons() != null && aPatient.getBeacons().size() > 0) {
                                            //below: condition when the missing patient is not on the list
                                            if (!missingPatientList.contains(aPatient)) {
                                                missingPatientList.add(aPatient);//add the patient to the list
                                            } else {//when ther missing patient is already on the list
                                                missingPatientList.set(missingPatientList.indexOf(aPatient), aPatient);
                                            }

                                        } else {//if the patient is not missing
                                            //below: the missing patient is still on the list (maybe found)
                                            if (missingPatientList.contains(aPatient)) {
                                                //remove the patient from the list
                                                missingPatientList.remove(aPatient);
//                                            forDisplay.logToDisplay2();
                                            }
                                        }
                                    }

                                }
                            }

//                            rvResident.setAdapter(MainActivity.homeAdapter);
                            rvResident.setAdapter(homeAdapter);// set the listview
                            srlUser.setRefreshing(false);
                        }


                    }
                }, 2000);


                if (getActivity() != null) {
//                    getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
//                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                }

            }
        });

//        mHandler = new Handler();
//        startRepeatingTask();


        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        String token = sharedPref.getString("userToken-WeTrack", "");//retrieve user token
        //call the api to get the patient list from the server
        serverAPI.getPatientList("Bearer " + token).enqueue(new Callback<List<Resident>>() {
            @Override
            public void onResponse(Call<List<Resident>> call, Response<List<Resident>> response) {
                try {
                    patientList = response.body();

                    Gson gson = new Gson();
                    String jsonPatients = gson.toJson(patientList);
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
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
                try {
                    Gson gson = new Gson();
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
                    String jsonPatients = sharedPref.getString("patientList-WeTrack", "");
                    Type type = new TypeToken<List<Resident>>() {
                    }.getType();
                    patientList = gson.fromJson(jsonPatients, type);
                } catch (Exception e) {
                    e.printStackTrace();

                }
            }
        });


//        if (patientList != null && !patientList.equals("") && patientList.size() > 0) {
//            for (Resident aPatient : patientList) {
//                for (BeaconInfo aBeacon : aPatient.getBeacons()) {
//                    if (aPatient.getStatus() == 1 && aBeacon.getStatus() == 1 && aPatient.getBeacons() != null && aPatient.getBeacons().size() > 0) {
//
//                        if (!missingPatientList.contains(aPatient)) {
//                            missingPatientList.add(aPatient);
//                        } else {
//                            missingPatientList.set(missingPatientList.indexOf(aPatient), aPatient);
//                        }
//
//                    } else {
//                        if (missingPatientList.contains(aPatient)) {
//                            missingPatientList.remove(aPatient);
//                        }
//                    }
//                }
//
//            }
//        }


        handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    //if patient List is present
                                    if (patientList != null && !patientList.equals("") && patientList.size() > 0) {
                                        //For all the patient correspond to the patient List
                                        for (Resident aPatient : patientList) {
                                            for (BeaconInfo aBeacon : aPatient.getBeacons()) {

                                                if (aPatient.getStatus() == 1 && aBeacon.getStatus() == 1 && aPatient.getBeacons() != null && aPatient.getBeacons().size() > 0) {

                                                    if (!missingPatientList.contains(aPatient)) {
                                                        missingPatientList.add(aPatient);
                                                    } else {
                                                        missingPatientList.set(missingPatientList.indexOf(aPatient), aPatient);
                                                    }

                                                } else {
                                                    if (missingPatientList.contains(aPatient)) {
                                                        missingPatientList.remove(aPatient);
                                                    }
                                                }
                                            }

                                        }
                                    }

                                    homeAdapter = new HomeAdapter(missingPatientList);
                                    rvResident.setAdapter(homeAdapter);

                                }

                            }, 300
        );


        return rootView;
    }


//    private Handler mHandler;
//    private int mInterval = 3000;
//    Runnable mStatusChecker = new Runnable() {
//        @Override
//        public void run() {
//            if (getActivity() != null && !srlUser.isRefreshing()) {
//                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
//            }
//
//
//            mHandler.postDelayed(mStatusChecker, mInterval);
//        }
//    };


//    private int mInterval2 = 10000;
//    Runnable mRefreshlist = new Runnable() {
//        @Override
//        public void run() {
//            serverAPI.getPatientList().enqueue(new Callback<List<Resident>>() {
//                @Override
//                public void onResponse(Call<List<Resident>> call, Response<List<Resident>> response) {
//                    try {
//                        patientList = response.body();
//
//                        Gson gson = new Gson();
//                        String jsonPatients = gson.toJson(patientList);
//                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
//                        SharedPreferences.Editor editor = sharedPref.edit();
//                        editor.putString("patientList-WeTrack", jsonPatients);
//                        editor.commit();
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                @Override
//                public void onFailure(Call<List<Resident>> call, Throwable t) {
//                    t.printStackTrace();
//                    try {
//                        Gson gson = new Gson();
//                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
//                        String jsonPatients = sharedPref.getString("patientList-WeTrack", "");
//                        Type type = new TypeToken<List<Resident>>() {
//                        }.getType();
//                        patientList = gson.fromJson(jsonPatients, type);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//
//                    }
//                }
//            });
//
//            homeAdapter = new HomeAdapter(missingPatientList);
//
//            if (patientList != null && !patientList.equals("") && patientList.size() > 0) {
//                for (Resident aPatient : patientList) {
//                    for (BeaconInfo aBeacon : aPatient.getBeacons()) {
//                        if (aPatient.getStatus() == 1 && aBeacon.getStatus() == 1 && aPatient.getBeacons() != null && aPatient.getBeacons().size() > 0) {
//
//                            if (!missingPatientList.contains(aPatient)) {
//                                missingPatientList.add(aPatient);
//                            } else {
//                                missingPatientList.set(missingPatientList.indexOf(aPatient), aPatient);
//                            }
//
//                        } else {
//                            if (missingPatientList.contains(aPatient)) {
//                                missingPatientList.remove(aPatient);
////                                            forDisplay.logToDisplay2();
//                            }
//                        }
//                    }
//
//                }
//            }
//
//            rvResident.setAdapter(homeAdapter);
//
//
//            mHandler.postDelayed(mRefreshlist, mInterval2);
//        }
//    };


//    void startRepeatingTask() {
//        mStatusChecker.run();
//        mRefreshlist.run();
//    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        String title = getArguments().getString("title");
//        tvTitle.setText(title);
    }

    @Override
    public void onStart() {
        super.onStart();


    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
//        EventBus.getDefault().register(this);

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public void onPause() {
        super.onPause();
//        EventBus.getDefault().unregister(this);

    }

    final int EDIT_USER = 69;

//    @Subscribe
//    public void onEvent(HomeAdapter.OpenEvent event) {
////        EventBus.getDefault().unregister(this);
//        Intent intent = new Intent(getActivity(), ResidentDetailActivity.class);
//        intent.putExtra("patient", event.patient);
//        intent.putExtra("position", event.position);
//        intent.putExtra("fromWhat", "home");
//        startActivityForResult(intent, EDIT_USER);
//        getActivity().finish();
//    }
}

package com.zero.shareby.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.navigation.NavigationView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.zero.shareby.R;
import com.zero.shareby.activities.AddressActivity;
import com.zero.shareby.models.Post;
import com.zero.shareby.models.UserDetails;
import com.zero.shareby.adapters.DashboardAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class DashboardFragment extends Fragment  {
    public static final String MAP_KEY="map_key";
    private static final String TAG="DashboardFragment";

    private static DashboardFragment fragment;

    private FirebaseAuth mAuth;
    //private FirebaseAuth.AuthStateListener mAuthListener;
    SharedPreferences preferences;
    ArrayList<Post> data;
    DashboardAdapter dashboardAdapter;
    SwipeRefreshLayout swipeRefreshLayout;
    ListView listView;

   // private OnFragmentInteractionListener mListener;

    public DashboardFragment() {
        // Required empty public constructor
        //fake data
        
    }

    public static DashboardFragment getInstance() {
        if (fragment==null)fragment = new DashboardFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth=FirebaseAuth.getInstance();
        /*mAuthListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser()==null){
                    startActivity(new Intent(getActivity(),LoginActivity.class));
                    onDestroyView();
                    onDetach();
                    onDestroy();
                }
            }
        };*/

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        preferences= PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        if(preferences.getBoolean(MAP_KEY,true) && FirebaseAuth.getInstance().getCurrentUser()!=null){
            DatabaseReference dbReference= FirebaseDatabase.getInstance().getReference().child("UserDetails").child(mAuth.getCurrentUser().getUid());
            dbReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.d(TAG,dataSnapshot.toString());
                    UserDetails userDetails=dataSnapshot.getValue(UserDetails.class);
                    try{
                        if (userDetails.getLatitude()==0){
                            startActivity(new Intent(getActivity(), AddressActivity.class));
                            SharedPreferences.Editor editor=preferences.edit().putBoolean(MAP_KEY,false);
                            editor.apply();
                        }
                    }catch (NullPointerException e){
                        e.printStackTrace();
                        startActivity(new Intent(getActivity(), AddressActivity.class));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }
        // Inflate the layout for this fragment
        View rootView=inflater.inflate(R.layout.fragment_dashboard, container, false);
        data=new ArrayList<>();
        /*data.add(new DashboardData("Jay","hammer","zero",1));
        data.add(new DashboardData("Jay","hammer","zero",1));
        data.add(new DashboardData("Jay","hammer","zero",1));
        data.add(new DashboardData("Jay","hammer","zero",1));
        data.add(new DashboardData("Jay","hammer","zero",1));
        data.add(new DashboardData("Jay","hammer","zero",1));
        data.add(new DashboardData("Jay","hammer","zero",1));*/
        swipeRefreshLayout=rootView.findViewById(R.id.main_dashboard_refresh);
        dashboardAdapter=new DashboardAdapter(getActivity(),data);
        listView=rootView.findViewById(R.id.main_dashboard_list_view);
        listView.setAdapter(dashboardAdapter);
        return rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
   /* public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }*/

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        FragmentManager fm=getFragmentManager();
        final MapFragment mapFragment = new MapFragment();
        fm.beginTransaction().replace(R.id.map_container,mapFragment).commit();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                data.clear();
                dashboardAdapter.clear();
                updateDashboard();

            }
        },2000);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                data.clear();
                dashboardAdapter.clear();
                updateDashboard();
                mapFragment.onResume();
            }
        });
        super.onViewCreated(view, savedInstanceState);
//        AdView adView = view.findViewById(R.id.main_dashboard_ad);
//        AdRequest adRequest = new AdRequest.Builder().build();
//        adView.loadAd(adRequest);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
      /*  if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
    }

    @Override
    public void onResume() {
        super.onResume();
        NavigationView nav=getActivity().findViewById(R.id.nav_view);
        nav.setCheckedItem(R.id.nav_home);
        if(!isConnected(getActivity())) buildDialog(getActivity()).show();
    }

    @Override
    public void onPause() {
        super.onPause();
        /*if(mAuth!=null)
            mAuth.removeAuthStateListener(mAuthListener);*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
     //   mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
   /* public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }*/


    public boolean isConnected(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netinfo = cm.getActiveNetworkInfo();

        if (netinfo != null && netinfo.isConnectedOrConnecting()) {
            android.net.NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            android.net.NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if((mobile != null && mobile.isConnectedOrConnecting()) || (wifi != null && wifi.isConnectedOrConnecting()))
                return true;
            else
                return false;
        } else
            return false;
    }





    public AlertDialog.Builder buildDialog(Context c) {

        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("No Internet Connection");
        builder.setMessage("You need to have Mobile Data or wifi to access this. Press OK to Exit");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                getActivity().finish();
            }
        });

        return builder;
    }



    private void updateDashboard() {
        if (this.isAdded()) {
            Log.d(TAG, preferences.getString(getString(R.string.pref_key1), "nnn"));
            if (FirebaseAuth.getInstance().getCurrentUser() != null && !preferences.getString(getString(R.string.pref_key1), "nope").equals("nope")) {
                swipeRefreshLayout.setRefreshing(true);
                Log.d(TAG, "inside of upadateDash");
                String country = preferences.getString(getString(R.string.pref_country), "null");
                String pin = preferences.getString(getString(R.string.pref_pin), "null");
                String key1 = preferences.getString(getString(R.string.pref_key1), "null");
                String key2 = preferences.getString(getString(R.string.pref_key2), "null");

                DatabaseReference getMyPosts = FirebaseDatabase.getInstance().getReference().child("Groups").child(country).child(pin)
                        .child(key1).child(key2).child("posts");

                Query query = getMyPosts.orderByChild("timestamp");
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.d(TAG, dataSnapshot.toString() + "\n");
                        for (DataSnapshot posts : dataSnapshot.getChildren()) {
                            if (posts.hasChild("sharedUid")
                                    || posts.child("priority").getValue(Integer.class) == 0) {
                                data.add(posts.getValue(Post.class));
                            }
                        }
                        Collections.sort(data, Collections.reverseOrder(new Comparator<Post>() {
                            @Override
                            public int compare(Post o1, Post o2) {
                                return Long.compare(o1.getTimestamp(), o2.getTimestamp());
                            }
                        }));
                        dashboardAdapter.notifyDataSetChanged();
                        if (swipeRefreshLayout.isRefreshing())
                            swipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }
        }
    }

}

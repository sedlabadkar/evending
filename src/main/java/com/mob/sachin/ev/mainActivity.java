package com.mob.sachin.ev;


import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import java.util.ArrayList;

public class mainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.OnConnectionFailedListener, AdapterView.OnItemClickListener {

    private final static int REQUEST_ENABLE_BT = 1, MY_PERMISSIONS_REQUEST_ACCESS_COARSE = 2;
    private static final long SCAN_PERIOD = 10000, BLUETOOTH_VISIBILITY_PERIOD = 120000;
    GoogleApiClient mGoogleApiClient;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler = new Handler();
    private ArrayList<BluetoothDevice> mLeDevices = new ArrayList<BluetoothDevice>();
    private ArrayList<String> mLeDevicesURL = new ArrayList<String>();
    private ScanCallback mScanCallback;
    private ProgressDialog mProgressDialog;
    SwipeRefreshLayout swipeRefresh;
    ArrayAdapter<String> beaconsAdapter;
    ListView listView;

    private static final ParcelUuid EDDYSTONE_SERVICE_UUID =
            ParcelUuid.fromString("0000FEAA-0000-1000-8000-00805F9B34FB");

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        break;
                    case BluetoothAdapter.STATE_ON:
                        scanLeDevice(true);
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();


        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
            Toast.makeText(this, R.string.bl_not_supported, Toast.LENGTH_SHORT).show();
            //finish();
        } else {
            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                Toast.makeText(this, R.string.bl_not_enabled, Toast.LENGTH_SHORT).show();
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else if (mBluetoothAdapter.isEnabled()) {
                scanLeDevice(true);
            }
        }

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);

        //Signout Code
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestId()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this )
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addApi(AppIndex.API).build();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        swipeRefresh.setOnRefreshListener(
            new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    scanLeDevice(true);
                }
            }
        );

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        beaconsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mLeDevicesURL);
        listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(this);
        listView.setAdapter(beaconsAdapter);
    }

    private void scanLeDevice(final boolean enable) {
        showProgressDialog();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_COARSE);
            }
        }

        System.out.println("Scanning bluetooth");
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        mScanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {

                super.onScanResult(callbackType, result);
                if (result == null) {
                    System.out.println ("Scan result is NULL");
                    //We should probably do something here.
                }
                ScanRecord scanRecord = result.getScanRecord();
                byte[] rawData = result.getScanRecord().getBytes();
                if (!mLeDevices.contains(result.getDevice())){
                    byte[] beaconServiceData = scanRecord.getServiceData(EDDYSTONE_SERVICE_UUID);
                    if (beaconServiceData == null)
                        System.out.println ("Service Data is null");
                    else if (beaconServiceData[0] == 0x10) {
                        System.out.println("URL Type");
                        System.out.println("####DEVICE FOUND######");
                        if (swipeRefresh.isRefreshing())
                            swipeRefresh.setRefreshing(false);
                        hideProgressDialog();
                        String deviceAddress = result.getDevice().getAddress();
                        Beacon beacon;
                        beacon = new Beacon(deviceAddress, result.getRssi());
                        //deviceToBeaconMap.put(deviceAddress, beacon);
                        beaconUtils.validateServiceData(deviceAddress, beaconServiceData, beacon);
                        mLeDevicesURL.add(beacon.getDecodedURL());
                        beaconsAdapter.notifyDataSetChanged();
                        mLeDevices.add(result.getDevice());
                    }
                } else {
                    System.out.println ("Already Exists");
                }
            }
        };

        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    hideProgressDialog();
                    mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
                }
            }, SCAN_PERIOD);
            System.out.println("SEARCHING DEVICE");
            mScanning = true;
            mBluetoothAdapter.getBluetoothLeScanner().startScan(mScanCallback);
        } else {
            mScanning = false;
            hideProgressDialog();
            mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                //mBluetoothAdapter.enable();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println ("Disabling Bluetooth");
                        Toast.makeText(getApplicationContext(),"Disabling Bluetooth", Toast.LENGTH_SHORT).show();
                        mBluetoothAdapter.disable();
                    }
                }, BLUETOOTH_VISIBILITY_PERIOD);
                Toast.makeText(getApplicationContext(),"Bluetooth enabled for 120 Seconds", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        //Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.menu_refresh) {
            swipeRefresh.setRefreshing(true);
            scanLeDevice(true);
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            //TODO: user profile
        } else if (id == R.id.nav_settings) {
            //TODO: settings activity
            Intent homepage = new Intent(mainActivity.this, SettingsActivity.class);
            startActivity(homepage);
        } else if (id == R.id.nav_transaction) {
            //TODO: transaction activity
        } else if (id == R.id.nav_help) {
            //TODO: redirect to help activity
        } else if (id == R.id.nav_signout) {
            if (mGoogleApiClient.isConnected()) {
                System.out.println("Sign Out");

                Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                        new ResultCallback<Status>() {
                            @Override
                            public void onResult(Status status) {
                                System.err.println("Success");
                                Intent homepage = new Intent(mainActivity.this, StartActivity.class);
                                startActivity(homepage);
                            }
                        });
                //What/When to use this?
                /*Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                        new ResultCallback<Status>() {
                            @Override
                            public void onResult(Status status) {
                                Intent homepage = new Intent(mainActivity.this, StartActivity.class);
                                startActivity(homepage);
                            }
                        });
                */
            }
        } else if (id == R.id.nav_send) {
            //TODO: Not used
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        mGoogleApiClient.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.mob.sachin.ev/http/host/path")
        );
        AppIndex.AppIndexApi.start(mGoogleApiClient, viewAction);
    }

    @Override
    public void onStop() {
        System.out.println("onStop");
        super.onStop();
        mLeDevices.clear();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.mob.sachin.ev/http/host/path")
        );
        AppIndex.AppIndexApi.end(mGoogleApiClient, viewAction);
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mLeDevices.clear();
        unregisterReceiver(mReceiver);
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.scanningForMachines));
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (swipeRefresh.isRefreshing())
            swipeRefresh.setRefreshing(false);
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(this,listView.getItemAtPosition(position).toString(), Toast.LENGTH_SHORT).show();
        Intent i = new Intent(getApplicationContext(), AllProductsActivity.class);
        i.putExtra("url", listView.getItemAtPosition(position).toString());
        startActivity(i);
    }
}

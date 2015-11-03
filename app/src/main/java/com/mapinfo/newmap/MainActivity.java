package com.mapinfo.newmap;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.mapinfo.newmap.Imle.MyLocationListener;
import com.mapinfo.newmap.Imle.MyOrientationListener;
import com.mapinfo.newmap.View.RoutinNaviActivity;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    MapView mapView = null;
    Toolbar toolbar;
    FloatingActionButton fab;
    BaiduMap map;
    LocationClient locationClient;
    MyLocationListener locationListener;
    MyLocationConfiguration.LocationMode currentMode;
    MyOrientationListener orientationListener;
    float direction;
    BitmapDescriptor bd, info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        initView();
        map = mapView.getMap();
        setSupportActionBar(toolbar);

        locationListener = new MyLocationListener(mapView, this);
        //地图初始化
        map.setMyLocationEnabled(true);
        locationClient = new LocationClient(this);
        locationClient.registerLocationListener(locationListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);
        option.setScanSpan(5000);
        option.setCoorType("bd09ll");
        option.setAddrType("all");

        locationClient.setLocOption(option);
        locationClient.start();
        map.setMapStatus(MapStatusUpdateFactory.zoomTo(16));//设置初始缩放等级
        initOrientationListener();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                centerToCurrentPlace();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //长按添加一个Marker
        map.setOnMapLongClickListener(new BaiduMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                OverlayOptions oo = new MarkerOptions().icon(bd).position(latLng);
                map.addOverlay(oo);
            }
        });

        map.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                map.hideInfoWindow();
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                return false;
            }
        });
        map.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {
                Toast.makeText(getApplicationContext(), "点击了" + marker.getPosition().toString(), Toast.LENGTH_SHORT)
                        .show();
                Button button = new Button(getApplicationContext());

                button.setBackgroundResource(R.drawable.popup);
                button.setText("deleteThis");
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        marker.remove();
                        map.hideInfoWindow();

                    }
                });
                InfoWindow info=new InfoWindow(button,marker.getPosition(),-100);
                map.showInfoWindow(info);
                return true;
            }
        });
    }

    //定位并移动
    private void centerToCurrentPlace() {
        MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(locationListener.getLatLng());
        map.animateMapStatus(msu);
    }

    private void initOrientationListener() {
        orientationListener = new MyOrientationListener(this);

        orientationListener.setOrientationListener(new MyOrientationListener.onOrientationListener() {
            @Override
            public void onOrientationChanged(float x) {
                direction = x;

                if (mapView == null) return;
                MyLocationData data = new MyLocationData.Builder()
                        .accuracy(locationListener.getRadius())
                        .direction(direction)
                        .latitude(locationListener.getLatLng().latitude)
                        .longitude(locationListener.getLatLng().longitude)
                        .build();
                map.setMyLocationData(data);
            }
        });
    }

    void initView() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mapView = (MapView) findViewById(R.id.mapview);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        bd = BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding);
        info = BitmapDescriptorFactory.fromResource(R.drawable.popup);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.normalMode) {
            currentMode = MyLocationConfiguration.LocationMode.NORMAL;
            map.setMyLocationConfigeration(new MyLocationConfiguration(currentMode,
                    true, null));
        } else if (id == R.id.followMode) {

            currentMode = MyLocationConfiguration.LocationMode.FOLLOWING;
            map.setMyLocationConfigeration(new MyLocationConfiguration(currentMode, true, null));
        } else if (id == R.id.compassMode) {

            currentMode = MyLocationConfiguration.LocationMode.COMPASS;
            map.setMyLocationConfigeration(new MyLocationConfiguration(currentMode
                    , true, null));

        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camara) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {
            Intent intent=new Intent(MainActivity.this, RoutinNaviActivity.class);
            startActivity(intent);
            item.setChecked(false);
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onDestroy() {
        locationClient.stop();
        map.setMyLocationEnabled(false);
        mapView.onDestroy();
        mapView = null;
        super.onDestroy();
    }

    @Override
    protected void onResume() {

        mapView.onResume();
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        orientationListener.start();
    }

    @Override
    protected void onStop() {
        orientationListener.stop();
        super.onStop();

    }


}

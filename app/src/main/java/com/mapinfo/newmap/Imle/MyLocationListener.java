package com.mapinfo.newmap.Imle;

import android.content.Context;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

/**
 * 定位监听
 */
public class MyLocationListener implements BDLocationListener {
    private static boolean isFirst = true;
    private LatLng latLng;
    private String currentAddr;//当前的地址。
    private Context context;
    private MapView mapView;
    private float currentRadius;


    public MyLocationListener(MapView mapView, Context context) {
        this.context = context;
        this.mapView = mapView;

    }

    /**
     *
     * @return 当前的经纬度
     */
    public LatLng getLatLng() {
        return latLng;
    }

    public String getCurrentAddr() {
        return currentAddr;
    }

    public float getRadius(){
        return currentRadius;
    }
    @Override
    public void onReceiveLocation(BDLocation bdLocation) {

        if (bdLocation == null || mapView == null) return;
        MyLocationData data = new MyLocationData.Builder()
                .accuracy(bdLocation.getRadius())
                .direction(100)
                .latitude(bdLocation.getLatitude())
                .longitude(bdLocation.getLongitude())
                .build();

        latLng = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());

        MyLocationConfiguration configuration=new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL,true,null);
        mapView.getMap().setMyLocationData(data);
        mapView.getMap().setMyLocationConfigeration(configuration);

        currentRadius=bdLocation.getRadius();
        currentAddr = bdLocation.getAddrStr();
        if (isFirst) {
            isFirst = false;
            latLng = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
            MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);
            mapView.getMap().animateMapStatus(msu);
            Toast.makeText(context, "成功" + currentAddr, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onReceivePoi(BDLocation bdLocation) {

    }


}

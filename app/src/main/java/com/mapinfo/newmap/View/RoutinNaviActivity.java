package com.mapinfo.newmap.View;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.OverlayManager;
import com.baidu.mapapi.overlayutil.WalkingRouteOverlay;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteLine;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.mapinfo.newmap.R;

public class RoutinNaviActivity extends AppCompatActivity implements BaiduMap.OnMapClickListener
        , OnGetRoutePlanResultListener {

    MapView mapView;
    BaiduMap map;
    Toolbar toolbar;
    Button btnPre, btnNext;
    int nodeIndex = -1;//节点索引，用来浏览节点使用
    OverlayManager routeOverlay = null;
    TextView popText = null;
    RoutePlanSearch rps = null;//搜索模块
    RouteLine line;
    EditText start, end;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_routin_navi);
        initView();
        setSupportActionBar(toolbar);
        toolbar.setTitle("路径规划");
        toolbar.setNavigationIcon(android.R.drawable.ic_menu_myplaces);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        map.setOnMapClickListener(this);
        //注册监听事件
        rps = RoutePlanSearch.newInstance();
        rps.setOnGetRoutePlanResultListener(this);

        /*
        * 这个当通过这个方法来返回Mainactivity的时候，会导致罗盘监听器无效化，相当于走了一遍Oncreate方法。
        * */
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


    }

    /**
     * 初始化变量
     */
    private void initView() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mapView = (MapView) findViewById(R.id.routinView);
        map = mapView.getMap();
        mapView.showZoomControls(false);
        btnNext = (Button) findViewById(R.id.next);
        btnPre = (Button) findViewById(R.id.pre);
        start = (EditText) findViewById(R.id.start);
        end = (EditText) findViewById(R.id.end);
        btnNext.setVisibility(View.INVISIBLE);
        btnPre.setVisibility(View.INVISIBLE);
    }

    /**
     * 发起路径规划的操作
     */
    public void searchButtonProcess(View v) {
        //清空当前地图上的信息
        line = null;
        btnNext.setVisibility(View.INVISIBLE);
        btnPre.setVisibility(View.INVISIBLE);
        map.clear();
        nodeIndex = -1;
        //起点和终点，对于Transit来说，城市名没有意义
        PlanNode stNode = PlanNode.withCityNameAndPlaceName("长沙", start.getText().toString());
        PlanNode enNode = PlanNode.withCityNameAndPlaceName("长沙", end.getText().toString());

        rps.walkingSearch(new WalkingRoutePlanOption()
                .from(stNode).to(enNode));
    }

    /**
     * 节点浏览
     *
     * @param v
     */
    public void nodeClick(View v) {
//没有路径的情况下
        if (line == null || line.getAllStep() == null) return;
        if (nodeIndex == -1 && v.getId() == R.id.pre) return;
        //设置节点索引
        if (v.getId() == R.id.next) {
            if (nodeIndex < line.getAllStep().size() - 1)
                nodeIndex++;
            else
                return;
        } else if (v.getId() == R.id.pre) {
            if (nodeIndex > 0)
                nodeIndex--;
            else
                return;
        }
        //获取节点结果信息
        LatLng nodeLocation;
        String nodeTitle;
        /*
        官方源码设置成Obeject对象的原因是，通过instanceof语句判断出是公交，汽车还是步行三个类的实例，
                 此处只需要步行，所以不需要做此判断。
        * */
        WalkingRouteLine.WalkingStep step = (WalkingRouteLine.WalkingStep) line.getAllStep().get(nodeIndex);
        nodeLocation = step.getEntrace().getLocation();
        nodeTitle = step.getEntraceInstructions();

        if (nodeLocation == null || nodeTitle == null) return;
        //移动节点至屏幕中心并显示popwindow;
        MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(nodeLocation);
        map.animateMapStatus(msu);
        popText = new TextView(RoutinNaviActivity.this);
        popText.setBackgroundResource(R.drawable.popup);
        popText.setTextColor(0xFF000000);
        popText.setText(nodeTitle);
        InfoWindow info = new InfoWindow(popText, nodeLocation, 0);
        map.showInfoWindow(info);
    }


    /**
     * @param walkingRouteResult 行走路径结果
     */
    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {

        if (walkingRouteResult == null || walkingRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(RoutinNaviActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        }
        if (walkingRouteResult.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            //起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            //result.getSuggestAddrInfo()
            return;
        }
        if (walkingRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
            nodeIndex = -1;
            btnNext.setVisibility(View.VISIBLE);
            btnPre.setVisibility(View.VISIBLE);
            line = walkingRouteResult.getRouteLines().get(0);
            WalkingRouteOverlay ol = new MyWalkingRoutinOverlay(map);
            routeOverlay = ol;
            map.setOnMarkerClickListener(ol);//?
            ol.setData(walkingRouteResult.getRouteLines().get(0));
            ol.addToMap();
            ol.zoomToSpan();
        }
    }

    @Override
    public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {

    }

    @Override
    public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {

    }

    @Override
    public void onMapClick(LatLng latLng) {
        map.hideInfoWindow();
    }

    @Override
    public boolean onMapPoiClick(MapPoi mapPoi) {
        return false;
    }

    //自定义起点终点的覆盖物
    class MyWalkingRoutinOverlay extends WalkingRouteOverlay {

        public MyWalkingRoutinOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
        }
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        mapView.onResume();
        rps.destroy();
        super.onDestroy();
    }
}

package hk.edu.hkmu.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import hk.edu.hkmu.myapplication.adapter.BusStopAdapter;
import hk.edu.hkmu.myapplication.api.BusApiClient;
import hk.edu.hkmu.myapplication.model.BusRoute;
import hk.edu.hkmu.myapplication.model.BusStop;
import hk.edu.hkmu.myapplication.model.RouteEta;
import hk.edu.hkmu.myapplication.utils.FavoriteManager;
import hk.edu.hkmu.myapplication.utils.FavoriteUtil;

/**
 * 顯示巴士路线的所有站點
 */
public class RouteStopsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BusStopAdapter stopAdapter;
    private ProgressBar progressBar;
    private TextView noStopsText;
    private SwipeRefreshLayout swipeRefreshLayout;
    private BusApiClient busApiClient;
    private boolean isEnglish = false;
    private FavoriteManager favoriteManager;
    private Menu optionsMenu;
    private boolean isFavorite = false;

    // 路线信息
    private String routeId;
    private String direction;
    private String serviceType;
    private String originTC;
    private String destTC;
    private String originEN;
    private String destEN;

    /**
     * 启动活动的静态方法
     */
    public static void start(Context context, BusRoute route) {
        if (route == null) {
            return;
        }

        Intent intent = new Intent(context, RouteStopsActivity.class);
        intent.putExtra("routeId", route.getRouteId());
        intent.putExtra("direction", route.getDirection());
        intent.putExtra("serviceType", route.getServiceType());
        intent.putExtra("originTC", route.getOriginTC());
        intent.putExtra("destTC", route.getDestinationTC());
        intent.putExtra("originEN", route.getOriginEN());
        intent.putExtra("destEN", route.getDestinationEN());

        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_stops);

        // 初始化API客户端和收藏管理器
        busApiClient = new BusApiClient();
        favoriteManager = FavoriteManager.getInstance(this);

        // 获取Intent中的数据
        Intent intent = getIntent();
        routeId = intent.getStringExtra("routeId");
        direction = intent.getStringExtra("direction");
        serviceType = intent.getStringExtra("serviceType");
        originTC = intent.getStringExtra("originTC");
        destTC = intent.getStringExtra("destTC");
        originEN = intent.getStringExtra("originEN");
        destEN = intent.getStringExtra("destEN");

        // 检查路线是否已收藏
        checkFavoriteStatus();

        // 检查当前语言设置
        checkCurrentLanguage();

        // 设置工具栏
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

            // 设置标题：路线号 起点-终点
            String title = routeId + " " + (isEnglish ? originEN : originTC) + " - " + (isEnglish ? destEN : destTC);
            getSupportActionBar().setTitle(title);
        }

        // 初始化视图
        progressBar = findViewById(R.id.progress_bar);
        noStopsText = findViewById(R.id.tv_no_stops);
        recyclerView = findViewById(R.id.rv_stops);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 创建适配器
        stopAdapter = new BusStopAdapter(routeId);
        recyclerView.setAdapter(stopAdapter);

        // 设置下拉刷新
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this::loadRouteStops);
        swipeRefreshLayout.setColorSchemeResources(R.color.bus_red, R.color.bus_blue);

        // 加载路线站点
        loadRouteStops();
    }

    private void checkFavoriteStatus() {
        String uniqueId = FavoriteUtil.standardizeRouteId(routeId, direction, serviceType);
        favoriteManager.refresh();
        isFavorite = favoriteManager.isFavorite(uniqueId);
        if (optionsMenu != null) {
            updateFavoriteIcon();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.route_stops_menu, menu);
        this.optionsMenu = menu;
        updateFavoriteIcon();
        return true;
    }

    private void updateFavoriteIcon() {
        if (optionsMenu != null) {
            MenuItem favoriteItem = optionsMenu.findItem(R.id.action_favorite);
            if (favoriteItem != null) {
                favoriteItem.setIcon(isFavorite ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);
            }
        }
    }

    private void checkCurrentLanguage() {
        Locale currentLocale = getResources().getConfiguration().getLocales().get(0);
        isEnglish = !currentLocale.getLanguage().equals(Locale.CHINESE.getLanguage());
        if (stopAdapter != null) {
            stopAdapter.updateLanguageSetting(isEnglish);
        }
    }

    private void loadRouteStops() {
        showLoading(true);
        busApiClient.getRouteStopList(routeId, direction, serviceType, new BusApiClient.ApiCallback<List<BusStop>>() {
            @Override
            public List<RouteEta> onSuccess(List<BusStop> result) {
                showLoading(false);
                if (result.isEmpty()) {
                    showNoStops(true);
                } else {
                    showNoStops(false);
                    List<RouteEta> allEtas = new ArrayList<>();
                    CountDownLatch latch = new CountDownLatch(result.size()); // Create a latch for the number of stops

                    // Load ETA for each stop
                    for (BusStop bus : result) {
                        loadEtaForStop(bus.getStopId(), allEtas, latch);
                    }

                    // Wait for all ETA loads to finish
                    new Thread(() -> {
                        try {
                            latch.await(); // Wait until all ETA requests are done
                            runOnUiThread(() -> stopAdapter.updateData(result, allEtas)); // Update UI on the main thread
                        } catch (InterruptedException e) {
                            Log.e("RouteStopsActivity", "Error waiting for ETA loading", e);
                        }
                    }).start();
                }//bb
                return null;
            }

            @Override
            public void onError(String errorMessage) {
                showLoading(false);
                Toast.makeText(RouteStopsActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadEtaForStop(String stopId, List<RouteEta> allEtas, CountDownLatch latch) {
        busApiClient.getRouteEta(stopId, routeId, serviceType, new BusApiClient.ApiCallback<List<RouteEta>>() {
            @Override
            public List<RouteEta> onSuccess(List<RouteEta> etaList) {
                if (etaList != null && !etaList.isEmpty()) {
                    Log.d("RouteStopsActivity", "Received " + etaList.size() + " ETAs for stopId: " + stopId);
                    allEtas.addAll(etaList);  // Collect all ETA data
                } else {
                    Log.d("RouteStopsActivity", "No ETA data for stopId: " + stopId);
                }
                latch.countDown(); // Signal that this ETA load is complete
                return etaList;
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("RouteStopsActivity", "Error loading ETA for stopId: " + stopId + ", error: " + errorMessage);
                latch.countDown(); // Ensure latch is decremented even on error
            }
        });
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        swipeRefreshLayout.setRefreshing(isLoading);
    }

    private void showNoStops(boolean show) {
        noStopsText.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkCurrentLanguage();
        checkFavoriteStatus();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_favorite) {
            toggleFavorite();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toggleFavorite() {
        String uniqueId = FavoriteUtil.standardizeRouteId(routeId, direction, serviceType);
        isFavorite = favoriteManager.toggleFavorite(uniqueId);
        updateFavoriteIcon();
        Toast.makeText(this, isFavorite ? "Added to favorites" : "Removed from favorites", Toast.LENGTH_SHORT).show();
    }
}
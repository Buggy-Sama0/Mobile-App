package hk.edu.hkmu.myapplication.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import hk.edu.hkmu.myapplication.R;
import hk.edu.hkmu.myapplication.api.BusApiClient;
import hk.edu.hkmu.myapplication.model.BusStop;
import hk.edu.hkmu.myapplication.model.RouteEta;
// import hk.edu.hkmu.myapplication.model.StopEta;

public class BusStopAdapter extends RecyclerView.Adapter<BusStopAdapter.ViewHolder> {
    private static final String TAG = "BusStopAdapter";

    private List<BusStop> stopList = new ArrayList<>();
    private Map<String, List<RouteEta>> etaMap = new HashMap<>();
    private boolean isEnglish = false;
    private BusApiClient busApiClient;
    private String routeId;

    public BusStopAdapter(String routeId) {
        this.busApiClient = new BusApiClient();
        this.routeId = routeId;
        Locale currentLocale = Locale.getDefault();
        this.isEnglish = !currentLocale.getLanguage().equals(Locale.CHINESE.getLanguage());
    }

    public void updateData(List<BusStop> newStops, List<RouteEta> etaList) {
        this.stopList = newStops;
        populateEtaMap(etaList);  // Populate the ETA map with the incoming data
        Log.d(TAG, "Updated adapter with " + newStops.size() + " stops and " + etaList.size() + " ETAs.");
        notifyDataSetChanged();
    }

    private void populateEtaMap(List<RouteEta> etaList) {
        etaMap.clear();
        for (RouteEta eta : etaList) {
            String stopId = eta.getStopId();
            if (!etaMap.containsKey(stopId)) {
                etaMap.put(stopId, new ArrayList<>());
            }
            etaMap.get(stopId).add(eta);
        }
        Log.d(TAG, "Populated etaMap with " + etaMap.size() + " entries.");
    }


    public void updateLanguageSetting(boolean isEnglish) {
        this.isEnglish = isEnglish;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bus_stop, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BusStop stop = stopList.get(position);
        Context context = holder.itemView.getContext();

        // Set stop name (with number)
        String stopName = (position + 1) + ". " + (isEnglish ? stop.getNameEN() : stop.getNameTC());
        holder.stopName.setText(stopName);

        // Set top and bottom connecting lines visibility
        holder.stopLineTop.setVisibility(position == 0 ? View.INVISIBLE : View.VISIBLE);
        holder.stopLineBottom.setVisibility(position == getItemCount() - 1 ? View.INVISIBLE : View.VISIBLE);

        // Set stop icon color
        holder.stopIcon.setBackgroundResource(R.drawable.circle_background_red);

        // Initially hide ETA
        holder.eta.setVisibility(View.GONE);

        // Set click listener to toggle ETA visibility and load data
        holder.itemView.setOnClickListener(v -> {
            boolean shouldShow = holder.eta.getVisibility() != View.VISIBLE;
            holder.eta.setVisibility(shouldShow ? View.VISIBLE : View.GONE);

            if (shouldShow) {
                // loadEtaData(holder, stop.getStopId(), context, stop.getServiceType());
                displayEta(holder, stop.getStopId());
            }
        });
    }

    /*
    private void loadEtaData(ViewHolder holder, String stopId, Context context, String serviceType) {
        // Show loading message
        holder.eta.setText(isEnglish ? "Loading..." : "加載中...");
        holder.eta.setVisibility(View.VISIBLE);


        // Check if we have cached ETA data for this stop
        if (etaMap.containsKey(stopId)) {
            displayEta(holder, etaMap.get(stopId), context);
            return;
        }

        // Fetch ETA data from API
        /*
        busApiClient.getStopEta(routeId, stopId, new BusApiClient.ApiCallback<List<StopEta>>() {
            @Override
            public void onSuccess(List<StopEta> etaList) {
                if (etaList == null) {
                    Log.e(TAG, "ETA list is null");
                    holder.eta.setText(isEnglish ? "No ETA data" : "無到站資料");
                    return;
                }

                Log.d(TAG, "Received " + etaList.size() + " ETAs for stop " + stopId);

                for (StopEta eta : etaList) {
                    Log.d(TAG, "ETA: " + eta.getEtaTime() + " - " + eta.getEtaTime() + " min");
                    holder.eta.setText(isEnglish ? eta.getEtaTime() : "暫無班次");
                }

                if (etaList.isEmpty()) {
                    Log.d(TAG, "Empty ETA list for stop " + stopId + " "+ etaList );
                    holder.eta.setText(isEnglish ? "No schedules" : "暫無班次");
                    return;
                }

                // Cache the ETA data
                //etaMap.put(stopId, etaList);

                // Display the ETA
                //displayEta(holder, etaList, context);
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error loading ETA: " + errorMessage);
                holder.eta.setText(isEnglish ? "Load failed" : "加載失敗");
            }
        }); */

        /*
        busApiClient.getRouteEta(stopId, routeId, serviceType, new BusApiClient.ApiCallback<List<RouteEta>>() {
            @Override
            public void onSuccess(List<RouteEta> etaList) {
                if (etaList == null || etaList.isEmpty() ) {
                    Log.e(TAG, "ETA list is null or Empty");
                    Log.d(TAG, "Received " + etaList.size() + " ETAs for stop " + stopId);
                    holder.eta.setText(isEnglish ? "No ETA data" : "無到站資料");
                    return;
                }

                Log.d(TAG, "Received " + etaList.size() + " ETAs for stop " + stopId);

                for (RouteEta eta : etaList) {
                    Log.d(TAG, "ETA: " + eta.getEtaTime() + " - " + eta.getEtaTime() + " min");
                    holder.eta.setText(isEnglish ? eta.getEtaTime() : "暫無班次");
                }

                // Cache the ETA data
                // etaMap.put(stopId, etaList);

                // Display the ETA
                // displayEta(holder, etaList, context);
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error loading ETA: " + errorMessage);
                holder.eta.setText(isEnglish ? "Load failed" : "加載失敗");
            }
        });

    }  */

    private void displayEta(ViewHolder holder, String stopId) {
        List<RouteEta> etaList = etaMap.get(stopId);
        if (etaList != null && !etaList.isEmpty()) {
            Log.d(TAG, "Displaying ETA for stopId: " + stopId + ", ETAs found: " + etaList.size());
            RouteEta earliestEta = getEarliestEta(etaList);
            if (earliestEta != null) {
                if (earliestEta.getMinutesRemaining() <= 0) {
                    holder.eta.setText(isEnglish ? "Arriving" : "即將到站");
                } else {
                    String text = isEnglish ? earliestEta.getMinutesRemaining() + " min" : earliestEta.getMinutesRemaining() + " 分鐘";
                    holder.eta.setText(text);
                }
            } else {
                holder.eta.setText(isEnglish ? "No buses" : "無班次");
            }
        } else {
            Log.d(TAG, "No ETA data found for stopId: " + stopId);
            holder.eta.setText(isEnglish ? "No ETA data" : "無到站資料");
        }
    }


    private RouteEta getEarliestEta(List<RouteEta> etaList) {
        RouteEta earliestEta = null;
        for (RouteEta eta : etaList) {
            if (eta.getMinutesRemaining() >= 0) {
                if (earliestEta == null || eta.getMinutesRemaining() < earliestEta.getMinutesRemaining()) {
                    earliestEta = eta;
                }
            }
        }
        return earliestEta;
    }

    @Override
    public int getItemCount() {
        return stopList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View stopLineTop;
        View stopLineBottom;
        ImageView stopIcon;
        TextView stopName;
        TextView eta;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            stopLineTop = itemView.findViewById(R.id.stop_line_top);
            stopLineBottom = itemView.findViewById(R.id.stop_line_bottom);
            stopIcon = itemView.findViewById(R.id.stop_icon);
            stopName = itemView.findViewById(R.id.tv_stop_name);
            eta = itemView.findViewById(R.id.tv_eta);
        }
    }
}
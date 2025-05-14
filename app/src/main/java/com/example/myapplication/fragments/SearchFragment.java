package com.example.myapplication.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Activity.SearchActivity;
import com.example.myapplication.Adapter.CafeAdapter;
import com.example.myapplication.DAO.CafeSearchDAO;
import com.example.myapplication.DAO.FavoriteCafeDAO;
import com.example.myapplication.Dialogs.DistanceFilterBottomSheet;
import com.example.myapplication.Dialogs.PriceFilterBottomSheet;
import com.example.myapplication.Model.CafeModel;
import com.example.myapplication.R;
import com.example.myapplication.Session.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class SearchFragment extends Fragment implements LocationListener {

    private LocationManager locationManager;
    private double userLatitude;
    private double userLongitude;
    private RecyclerView recyclerView;
    private CafeAdapter cafeAdapter;
    private List<CafeModel> cafeList;
    private EditText searchEditText;
    private CafeSearchDAO cafeSearchDAO;
    private FavoriteCafeDAO cafeFavoriteDAO;
    private boolean wifiSelected;
    private boolean workspaceSelected;
    private boolean openNowSelected;
    private double currentDistanceFilter;
    private double currentPriceFilter;
    private TextView txtFilterCount;
    private static final int REQUEST_SEARCH = 1001;
    SessionManager sessionManager;

    @SuppressLint("MissingPermission")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        currentDistanceFilter = -1;
        currentPriceFilter = -1;
        workspaceSelected = false;
        openNowSelected = false;
        wifiSelected = false;
        cafeList = new ArrayList<>();
        sessionManager = new SessionManager(requireContext());

        View view = inflater.inflate(R.layout.search_fragment, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        txtFilterCount = view.findViewById(R.id.txtFilterCount);
        updateFilterCount();

        TextInputLayout searchLayout = view.findViewById(R.id.searchText);
        searchEditText = searchLayout.getEditText();

        if (searchEditText != null) {
            searchEditText.setFocusable(false);
            searchEditText.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), SearchActivity.class);
                String currentKeyword = searchEditText.getText().toString().trim();
                intent.putExtra("CURRENT_KEYWORD", currentKeyword);
                startActivityForResult(intent, REQUEST_SEARCH);
            });
        }

        MaterialButton btnWifi = view.findViewById(R.id.btn_wifi);
        MaterialButton btnWorkspace = view.findViewById(R.id.btn_workspace);
        MaterialButton btnOpenNow = view.findViewById(R.id.btn_open_now);
        MaterialButton btnSortDistance = view.findViewById(R.id.btn_sort_distance);
        MaterialButton btnSortPrice = view.findViewById(R.id.btn_sort_price);

        cafeSearchDAO = new CafeSearchDAO(requireContext());
        cafeFavoriteDAO = new FavoriteCafeDAO(requireContext());
        cafeAdapter = new CafeAdapter(cafeList, requireContext());
        recyclerView.setAdapter(cafeAdapter);

        cafeAdapter.setOnCafeClickListener(cafe -> {
            int cafeId = (int) cafe.getId();
            int userId = sessionManager.getUserId();

            SharedPreferences prefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            prefs.edit()
                    .putInt("selected_cafe_id", cafeId)
                    .putInt("id_user", userId)
                    .apply();

            FeedFragment feedFragment = new FeedFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("cafe_id", cafeId);
            bundle.putString("cafe_name", cafe.getName());
            feedFragment.setArguments(bundle);

            View currentView = getView();
            if (currentView != null && currentView.getParent() instanceof ViewGroup) {
                int containerId = ((ViewGroup) currentView.getParent()).getId();
                if (containerId != View.NO_ID) {
                    FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                    transaction.replace(containerId, feedFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                } else {
                    Log.e("SearchFragment", "ViewGroup parent has NO_ID – cần đặt id trong layout.");
                }
            } else {
                Log.e("SearchFragment", "getView() hoặc parent null.");
            }
        });

        locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastLocation != null) {
                userLatitude = lastLocation.getLatitude();
                userLongitude = lastLocation.getLongitude();
                loadFilteredCafes();
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
        }

        btnWifi.setOnClickListener(v -> {
            wifiSelected = !wifiSelected;
            updateButtonState(btnWifi, wifiSelected);
            updateFilterCount();
            loadFilteredCafes();
        });

        btnWorkspace.setOnClickListener(v -> {
            workspaceSelected = !workspaceSelected;
            updateButtonState(btnWorkspace, workspaceSelected);
            updateFilterCount();
            loadFilteredCafes();
        });

        btnOpenNow.setOnClickListener(v -> {
            openNowSelected = !openNowSelected;
            updateButtonState(btnOpenNow, openNowSelected);
            updateFilterCount();
            loadFilteredCafes();
        });

        btnSortDistance.setOnClickListener(v -> {
            DistanceFilterBottomSheet sheet = new DistanceFilterBottomSheet();
            sheet.setInitialDistance(currentDistanceFilter);
            sheet.setOnDistanceSelectedListener(distance -> {
                currentDistanceFilter = distance;
                updateDistanceButton(btnSortDistance);
                updateFilterCount();
                loadFilteredCafes();
            });
            sheet.show(getChildFragmentManager(), sheet.getTag());
        });

        btnSortPrice.setOnClickListener(v -> {
            PriceFilterBottomSheet sheet = new PriceFilterBottomSheet();
            sheet.setInitialPrice(currentPriceFilter);
            sheet.setOnPriceSelectedListener(price -> {
                currentPriceFilter = price;
                updatePriceButton(btnSortPrice);
                updateFilterCount();
                loadFilteredCafes();
            });
            sheet.show(getChildFragmentManager(), sheet.getTag());
        });

        cafeAdapter.setOnFavoriteClickListener((cafe, isFavoriteNow) -> {
            long cafeId = cafe.getId();
            if (isFavoriteNow) {
                cafeFavoriteDAO.addFavorite(cafeId);
            } else {
                cafeFavoriteDAO.removeFavorite(cafeId);
            }
        });

        cafeFavoriteDAO.getFavoritesByUserId(new FavoriteCafeDAO.FavoriteListCallback() {
            @Override
            public void onSuccess(Set<Long> favoriteIds) {
                cafeAdapter.setFavoriteCafes(favoriteIds);
                cafeAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("Favorite", "Lỗi khi lấy danh sách yêu thích", e);
            }
        });

        return view;
    }

    private void loadFilteredCafes() {
        String keyword = searchEditText != null ? searchEditText.getText().toString().trim() : "";

        cafeSearchDAO.getAllCafes(new CafeSearchDAO.CafeListCallback() {
            @Override
            public void onResult(List<CafeModel> allCafes) {
                List<CafeModel> filtered = new ArrayList<>();
                for (CafeModel cafe : allCafes) {
                    if (!keyword.isEmpty() && !cafe.getName().toLowerCase().contains(keyword.toLowerCase())) continue;
                    if (wifiSelected && !cafe.isWifiAvailable()) continue;
                    if (workspaceSelected && !cafe.isWorkSpace()) continue;
                    if (openNowSelected && !cafe.isOpen()) continue;
                    if (currentDistanceFilter > 0 && cafe.getDistance() > currentDistanceFilter) continue;
                    if (currentPriceFilter > 0 && cafe.getMinPrice() > currentPriceFilter * 1000) continue;
                    filtered.add(cafe);
                }
                filtered.sort(Comparator.comparingDouble(CafeModel::getDistance));
                cafeList.clear();
                cafeList.addAll(filtered);
                cafeAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                Log.e("SearchFragment", "Lỗi khi lấy danh sách cafe: " + e.getMessage());
            }
        });
    }

    private void updatePriceButton(MaterialButton btnSortPrice) {
        if (currentPriceFilter > 0) {
            btnSortPrice.setText("0K - " + (int) currentPriceFilter + "K");
            btnSortPrice.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.filter_selected_bg));
            btnSortPrice.setTextColor(ContextCompat.getColor(requireContext(), R.color.filter_selected_text));
        } else {
            btnSortPrice.setText("Giá tiền");
            btnSortPrice.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.white));
            btnSortPrice.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
        }
    }

    private void updateDistanceButton(MaterialButton btnSortDistance) {
        if (currentDistanceFilter > 0) {
            btnSortDistance.setText("0km - " + (int) currentDistanceFilter + " km");
            btnSortDistance.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.filter_selected_bg));
            btnSortDistance.setTextColor(ContextCompat.getColor(requireContext(), R.color.filter_selected_text));
        } else {
            btnSortDistance.setText("Khoảng cách");
            btnSortDistance.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.white));
            btnSortDistance.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
        }
    }

    private void updateButtonState(MaterialButton button, boolean selected) {
        button.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), selected ? R.color.filter_selected_bg : R.color.white));
        button.setTextColor(ContextCompat.getColor(requireContext(), selected ? R.color.filter_selected_text : R.color.black));
    }

    private void updateFilterCount() {
        int count = 0;
        if (wifiSelected) count++;
        if (workspaceSelected) count++;
        if (openNowSelected) count++;
        if (currentDistanceFilter > 0) count++;
        if (currentPriceFilter > 0) count++;

        if (txtFilterCount != null) {
            if (count > 0) {
                txtFilterCount.setText(String.valueOf(count));
                txtFilterCount.setVisibility(View.VISIBLE);
            } else {
                txtFilterCount.setVisibility(View.GONE);
            }
        }
    }

    @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
    @Override public void onProviderEnabled(@NonNull String provider) {}
    @Override public void onProviderDisabled(@NonNull String provider) {}

    @Override
    public void onLocationChanged(@NonNull Location location) {
        userLatitude = location.getLatitude();
        userLongitude = location.getLongitude();
        sessionManager.saveUserLocation(userLatitude, userLongitude);
        loadFilteredCafes();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SEARCH && resultCode == Activity.RESULT_OK && data != null) {
            String keyword = data.getStringExtra("SEARCH_KEYWORD");
            if (searchEditText != null && keyword != null) {
                searchEditText.setText(keyword);
                loadFilteredCafes();
            }
        }
    }
}

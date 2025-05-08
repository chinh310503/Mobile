package com.example.myapplication.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Adapter.CafeAdapter;
import com.example.myapplication.DAO.CafeSearchDAO;
import com.example.myapplication.DAO.FavoriteCafeDAO;
import com.example.myapplication.Model.CafeModel;
import com.example.myapplication.R;
import com.example.myapplication.Session.SessionManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FavoriteTabFragment extends Fragment {

    private RecyclerView recyclerView;
    private CafeAdapter cafeAdapter;
    private List<CafeModel> favoriteCafes = new ArrayList<>();
    private FavoriteCafeDAO favoriteCafeDAO;
    private CafeSearchDAO cafeSearchDAO;
    private SessionManager sessionManager;
    private FavoriteCafeDAO cafeFavoriteDAO;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite_tab, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewFavorites);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        cafeAdapter = new CafeAdapter(favoriteCafes, requireContext());
        recyclerView.setAdapter(cafeAdapter);

        sessionManager = new SessionManager(requireContext());
        favoriteCafeDAO = new FavoriteCafeDAO(requireContext());
        cafeSearchDAO = new CafeSearchDAO(requireContext());
        cafeFavoriteDAO = new FavoriteCafeDAO(requireContext());

        int userId = sessionManager.getUserId();
        if (userId != -1) {
            favoriteCafeDAO.getFavoritesByUserId(new FavoriteCafeDAO.FavoriteListCallback() {
                @Override
                public void onSuccess(Set<Long> favoriteIds) {
                    loadFavoriteCafes(favoriteIds);
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e("FavoriteTab", "Lỗi khi lấy danh sách yêu thích", e);
                }
            });
        }

        cafeAdapter.setOnFavoriteClickListener((cafe, isFavoriteNow) -> {
            long cafeId = cafe.getId();
            cafeFavoriteDAO.removeFavorite(cafeId);
            favoriteCafes.removeIf(c -> c.getId() == cafeId);
            cafeAdapter.setFavoriteCafes(getCurrentFavoriteIds());
            cafeAdapter.notifyDataSetChanged();
        });

        return view;
    }

    private void loadFavoriteCafes(Set<Long> favoriteIds) {
        if (favoriteIds == null || favoriteIds.isEmpty()) return;

        cafeSearchDAO.getAllCafes(new CafeSearchDAO.CafeListCallback() {
            @Override
            public void onResult(List<CafeModel> allCafes) {
                favoriteCafes.clear();
                for (CafeModel cafe : allCafes) {
                    if (favoriteIds.contains(cafe.getId())) {
                        favoriteCafes.add(cafe);
                    }
                }
                cafeAdapter.setFavoriteCafes(favoriteIds);
                cafeAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                Log.e("FavoriteTab", "Lỗi khi lấy quán cafe", e);
            }
        });
    }

    private Set<Long> getCurrentFavoriteIds() {
        Set<Long> ids = new HashSet<>();
        for (CafeModel cafe : favoriteCafes) {
            ids.add(cafe.getId());
        }
        return ids;
    }

}

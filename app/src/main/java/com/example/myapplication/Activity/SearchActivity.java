package com.example.myapplication.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Adapter.CafeSuggestionAdapter;
import com.example.myapplication.Adapter.SearchHistoryAdapter;
import com.example.myapplication.DAO.CafeSearchDAO;
import com.example.myapplication.Model.CafeModel;
import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private EditText edtSearch;
    private ImageView btnClear;
    private RecyclerView recyclerHistory, recyclerSuggestions;
    private SearchHistoryAdapter historyAdapter;
    private CafeSuggestionAdapter suggestionAdapter;

    private List<String> searchHistory;
    private List<CafeModel> cafeSuggestions;

    private CafeSearchDAO searchDAO;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        edtSearch = findViewById(R.id.edtSearch);
        btnClear = findViewById(R.id.btnClear);
        recyclerHistory = findViewById(R.id.recyclerHistory);
        recyclerSuggestions = findViewById(R.id.recyclerSuggestion);

        searchHistory = new ArrayList<>();
        cafeSuggestions = new ArrayList<>();

        searchDAO = new CafeSearchDAO(this);

        // Set up adapters
        historyAdapter = new SearchHistoryAdapter(searchHistory, this::returnResult);
        suggestionAdapter = new CafeSuggestionAdapter(cafeSuggestions, this::returnResult);

        recyclerHistory.setLayoutManager(new LinearLayoutManager(this));
        recyclerHistory.setAdapter(historyAdapter);

        recyclerSuggestions.setLayoutManager(new LinearLayoutManager(this));
        recyclerSuggestions.setAdapter(suggestionAdapter);

        // Nhận keyword từ fragment
        String keywordFromFragment = getIntent().getStringExtra("CURRENT_KEYWORD");
        if (keywordFromFragment != null && !keywordFromFragment.isEmpty()) {
            edtSearch.setText(keywordFromFragment);
            edtSearch.setSelection(keywordFromFragment.length());
        }
        filterSuggestions(keywordFromFragment != null ? keywordFromFragment : "");

        // Sự kiện nút xoá
        btnClear.setOnClickListener(v -> edtSearch.setText(""));

        // Icon back trong ô tìm kiếm
        edtSearch.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                int leftDrawableWidth = edtSearch.getCompoundDrawables()[0] != null
                        ? edtSearch.getCompoundDrawables()[0].getBounds().width()
                        : 0;
                if (event.getX() <= leftDrawableWidth + edtSearch.getPaddingStart()) {
                    returnResult(edtSearch.getText().toString().trim());
                    return true;
                }
            }
            return false;
        });

        TextView btnClearHistory = findViewById(R.id.btnClearHistory);
        btnClearHistory.setOnClickListener(v -> {
            searchDAO.clearSearchHistory(new CafeSearchDAO.SearchHistoryCallback() {
                @Override
                public void onResult(List<String> history) {
                    searchHistory.clear();
                    historyAdapter.notifyDataSetChanged();
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(SearchActivity.this, "Lỗi khi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Gợi ý realtime khi gõ
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterSuggestions(s.toString().trim());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Nhấn enter để tìm
        edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String keyword = edtSearch.getText().toString().trim();
                if (!keyword.isEmpty() && !searchHistory.contains(keyword)) {
                    searchDAO.saveSearchHistory(keyword);
                    searchHistory.add(keyword);
                }
                returnResult(keyword);
                return true;
            }
            return false;
        });

        loadSearchHistory();
    }

    private void filterSuggestions(String keyword) {
        searchDAO.getAllCafes(0.0, 0.0, new CafeSearchDAO.CafeListCallback() {
            @Override
            public void onResult(List<CafeModel> allCafes) {
                List<CafeModel> result = new ArrayList<>();

                if (keyword.isEmpty()) {
                    allCafes.sort(Comparator.comparingDouble(CafeModel::getDistance));
                    for (int i = 0; i < Math.min(3, allCafes.size()); i++) {
                        result.add(allCafes.get(i));
                    }
                } else {
                    int count = 0;
                    for (CafeModel cafe : allCafes) {
                        if (cafe.getName().toLowerCase().contains(keyword.toLowerCase())) {
                            result.add(cafe);
                            count++;
                            if (count >= 3) break;
                        }
                    }
                }

                suggestionAdapter.updateSuggestions(result);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(SearchActivity.this, "Lỗi tải gợi ý: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadSearchHistory() {
        searchDAO.getSearchHistory(new CafeSearchDAO.SearchHistoryCallback() {
            @Override
            public void onResult(List<String> history) {
                searchHistory.clear();
                searchHistory.addAll(history);
                historyAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(SearchActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void returnResult(CafeModel cafe) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("SEARCH_KEYWORD", cafe.getName());
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    // Overload cũ vẫn dùng cho searchHistoryAdapter
    private void returnResult(String keyword) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("SEARCH_KEYWORD", keyword);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}

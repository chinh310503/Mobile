package com.example.myapplication.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Adapter.SearchHistoryAdapter;
import com.example.myapplication.DAO.CafeSearchDAO;
import com.example.myapplication.R;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private EditText edtSearch;
    private RecyclerView recyclerHistory;
    private SearchHistoryAdapter adapter;
    private List<String> searchHistory;

    private CafeSearchDAO searchDAO;
    private int userId;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        edtSearch = findViewById(R.id.edtSearch);
        recyclerHistory = findViewById(R.id.recyclerHistory);

        String keywordFromFragment = getIntent().getStringExtra("CURRENT_KEYWORD");
        if (keywordFromFragment != null && !keywordFromFragment.isEmpty()) {
            edtSearch.setText(keywordFromFragment);
            edtSearch.setSelection(keywordFromFragment.length()); // Đặt con trỏ ở cuối text
        }

        ImageView btnClear = findViewById(R.id.btnClear);
        EditText edtSearch = findViewById(R.id.edtSearch);

        btnClear.setOnClickListener(v -> edtSearch.setText(""));
        edtSearch.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                int leftDrawableWidth = edtSearch.getCompoundDrawables()[0] != null
                        ? edtSearch.getCompoundDrawables()[0].getBounds().width()
                        : 0;
                if (event.getX() <= leftDrawableWidth + edtSearch.getPaddingStart()) {
                    // Người dùng nhấn vào icon back
                    String keyword = edtSearch.getText().toString().trim();
                    returnResult(keyword);
                    return true;
                }
            }
            return false;
        });

        searchHistory = new ArrayList<>();
        adapter = new SearchHistoryAdapter(searchHistory, this::onSearchItemSelected);
        recyclerHistory.setLayoutManager(new LinearLayoutManager(this));
        recyclerHistory.setAdapter(adapter);

        searchDAO = new CafeSearchDAO(this);
        loadSearchHistory();

        edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String keyword = edtSearch.getText().toString().trim();
                if (!keyword.isEmpty()) {
                    saveHistoryIfNew(keyword);
                }
                returnResult(keyword);
                return true;
            }
            return false;
        });
    }



    private void onSearchItemSelected(String keyword) {
        returnResult(keyword);
    }

    private void returnResult(String keyword) {
        // Trả kết quả về SearchFragment
        Intent resultIntent = new Intent();
        resultIntent.putExtra("SEARCH_KEYWORD", keyword);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    private void loadSearchHistory() {
        searchDAO.getSearchHistory(new CafeSearchDAO.SearchHistoryCallback() {
            @Override
            public void onResult(List<String> history) {
                searchHistory.clear();
                searchHistory.addAll(history);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(SearchActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveHistoryIfNew(String keyword) {
        if (searchHistory == null || !searchHistory.contains(keyword)) {
            searchDAO.saveSearchHistory(keyword);
        }
    }
}

package com.example.josep.recipesapp;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by josep on 4/12/2018.
 */

public class IngredientView extends FrameLayout {
    //https://stackoverflow.com/questions/4328838/create-a-custom-view-by-inflating-a-layout?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
    public IngredientView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    public IngredientView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public IngredientView(Context context) {
        super(context);
        initView();
    }
    private void initView() {
        View view = inflate(getContext(), R.layout.ingredient, null);
        addView(view);
    }
}

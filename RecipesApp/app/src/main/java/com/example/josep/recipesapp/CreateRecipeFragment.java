package com.example.josep.recipesapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;


public class CreateRecipeFragment extends Fragment {


    private OnFragmentInteractionListener mListener;

    public CreateRecipeFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //https://stackoverflow.com/questions/12659747/call-an-activity-method-from-a-fragment?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_create_recipe, container, false);
        try{
            JSONObject recipe = new JSONObject(getArguments().getString("recipe"));
            if(recipe.has("image")){
                //https://stackoverflow.com/questions/10631715/how-to-split-a-comma-separated-string/10631738?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
                //https://stackoverflow.com/questions/23179301/base64-image-on-android-image-view?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
                String base64Image = recipe.getString("image").split(",")[1];
                byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                ImageView imageView = view.findViewById(R.id.imageView2);
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageBitmap(decodedByte);
            }
            TextView heading = view.findViewById(R.id.textView4);
            heading.setText("Edit Recipe");
            Button submit = view.findViewById(R.id.button2);
            submit.setText("Edit Recipe");
            submit.setTag(recipe.getJSONObject("_id"));
            TextView title = view.findViewById(R.id.editTitle);
            TextView directions = view.findViewById(R.id.editDirections);
            title.setText(recipe.getString("title"));
            directions.setText(recipe.getString("directions"));
            LinearLayoutCompat ingredients =  view.findViewById(R.id.ingredients);
            ingredients.removeAllViews();
            JSONArray jsonIngredients = recipe.getJSONArray("ingredients");
            for(int i=0;i<jsonIngredients.length();i++){
                JSONObject ingredient = jsonIngredients.getJSONObject(i);
                IngredientView ingredientView = new IngredientView(getActivity());
                EditText editName = ingredientView.findViewById(R.id.editName);
                EditText editAmount = ingredientView.findViewById(R.id.editAmount);
                editName.setText(ingredient.getString("title"));
                editAmount.setText(ingredient.getString("amount"));
                ingredients.addView(ingredientView);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return view;


    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}

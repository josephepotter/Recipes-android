package com.example.josep.recipesapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;


public class ViewRecipeFragment extends Fragment {
    private OnFragmentInteractionListener mListener;

    public ViewRecipeFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_recipe, container, false);
        //https://stackoverflow.com/questions/26389938/nullpointerexception-when-accessing-a-fragments-textview-in-an-activity?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
        //https://stackoverflow.com/questions/12739909/send-data-from-activity-to-fragment-in-android?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
        String jsonObject = getArguments().getString("JSONObject");
        JSONObject recipe = null;
        try{
            recipe = new JSONObject(jsonObject);
            TextView title = view.findViewById(R.id.Title);
            TextView directions = view.findViewById(R.id.Directions);
            LinearLayout right =  view.findViewById(R.id.right);
            LinearLayout left = view.findViewById(R.id.left);
            //https://stackoverflow.com/questions/17487205/how-to-check-if-a-json-key-exists?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
            if(recipe.has("image")){
                //https://stackoverflow.com/questions/10631715/how-to-split-a-comma-separated-string/10631738?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
                //https://stackoverflow.com/questions/23179301/base64-image-on-android-image-view?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
                String base64Image = recipe.getString("image").split(",")[1];
                byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                ImageView imageView = view.findViewById(R.id.imageView);
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageBitmap(decodedByte);
            }
            title.setText(recipe.getString("title"));
            directions.setText(recipe.getString("directions"));
            Log.d("json",recipe.toString());
            JSONObject id = recipe.getJSONObject("_id");
            String oid = id.getString("$oid");
            Button delete = view.findViewById(R.id.delete);
            delete.setTag(oid);
            Button edit = view.findViewById(R.id.edit);
            edit.setTag(jsonObject);
            JSONArray ingredients =  recipe.getJSONArray("ingredients");
            for (int i=0;i<ingredients.length();i++){
                TextView ingredient = new TextView(getActivity());
                JSONObject jsonIngredient = (ingredients.getJSONObject(i));
                String text = jsonIngredient.getString("amount") + " " + jsonIngredient.getString("title");
                ingredient.setText(text);
                if (i % 2 == 0){
                    left.addView(ingredient);
                } else {
                    right.addView(ingredient);
                }
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

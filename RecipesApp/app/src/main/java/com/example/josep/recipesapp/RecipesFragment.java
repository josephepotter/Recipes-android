package com.example.josep.recipesapp;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;


public class RecipesFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    public RecipesFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipes, container, false);
        //https://stackoverflow.com/questions/26389938/nullpointerexception-when-accessing-a-fragments-textview-in-an-activity?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
        //https://stackoverflow.com/questions/12739909/send-data-from-activity-to-fragment-in-android?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
        //https://stackoverflow.com/questions/12659747/call-an-activity-method-from-a-fragment?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
        String jsonArray = ((MainActivity)getActivity()).getJson();
        JSONArray recipes = null;
        try{
            recipes = new JSONArray(jsonArray);
        } catch(Exception e) {
            //do nothing
        }
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        MainAdapter adapter = new MainAdapter(getActivity(), recipes);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
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

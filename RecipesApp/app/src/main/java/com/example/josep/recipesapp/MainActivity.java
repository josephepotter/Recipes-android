package com.example.josep.recipesapp;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.FragmentManager;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;


public class MainActivity extends AppCompatActivity implements LoginFragment.OnFragmentInteractionListener, CreateRecipeFragment.OnFragmentInteractionListener, RecipesFragment.OnFragmentInteractionListener, ViewRecipeFragment.OnFragmentInteractionListener, HomeFragment.OnFragmentInteractionListener {
    private String mUrl = "https://polar-cliffs-45386.herokuapp.com/";
    //for Shared Preferences: https://stackoverflow.com/questions/11688689/save-variables-after-quitting-application?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
    private SharedPreferences mPrefs;
    private SharedPreferences.Editor mEditor;
    public String mMostRecentJson = null;
    private boolean mNeedsUpdate = true;
    private TalkToApiTask mTalkToApiTask;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTalkToApiTask = new TalkToApiTask(this);
        mPrefs = this.getSharedPreferences("label", 0);
        mEditor =  mPrefs.edit();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        createHomeView();
    }
    //http://abhiandroid.com/programming/onstart-method-in-android.html
    @Override
    protected void onStart() {
        super.onStart();
        mMostRecentJson = mPrefs.getString("mostRecentJson", null);
        mNeedsUpdate = mPrefs.getBoolean("needsUpdate", true);
        mEditor.putString("mostRecentJson", null).commit();
        mEditor.putBoolean("needsUpdate", true).commit();
    }
    @Override
    protected void onStop() {
        super.onStop();
        mEditor.putString("mostRecentJson", mMostRecentJson).commit();
        mEditor.putBoolean("needsUpdate", mNeedsUpdate).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //https://stackoverflow.com/questions/7821284/how-to-stop-asynctask-thread-in-android?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
        //cancel backgound task if one is running
        mTalkToApiTask.cancel(true);
        int id = item.getItemId();
        if (id == R.id.login) {
            createLoginView(false);
        } else if (id == R.id.create_recipe){
           createCreateRecipeView();
        } else if (id == R.id.my_recipes){
           createMyRecipesView();
        } else if (id == R.id.create_account){
            createLoginView(true);
        } else if(id == R.id.logout){
            signout(true);
        } else if(id == R.id.home){
            createHomeView();
        }
        return super.onOptionsItemSelected(item);
    }
    //https://stackoverflow.com/questions/24777985/how-to-implement-onfragmentinteractionlistener?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
    @Override
    public void onFragmentInteraction(Uri uri){
        //you can leave it empty
    }
    //---- Navigation Functions ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    public void createHomeView(){
        Fragment fragment = new HomeFragment();
        //https://stackoverflow.com/questions/30339524/obj-fragment-wrong-2nd-argument-type-found-android-support-v4-app-fragment-re?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.frameLayout, fragment).commit();
    }


    public void callLogin(View view) {
        mTalkToApiTask.cancel(true);
        createLoginView(false);
    }
    public void callCreateAccount(View view) {
        mTalkToApiTask.cancel(true);
        createLoginView(true);
    }
    public void callMyRecipes(View view) {
        mTalkToApiTask.cancel(true);
        createMyRecipesView();
    }
    public void callCreateRecipe(View view) {
        mTalkToApiTask.cancel(true);
        createCreateRecipeView();
    }
    public void createLoginView(Boolean isCreate){
        //https://stackoverflow.com/questions/36100187/how-to-start-fragment-from-an-activity?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
        if(isConnected()){
            Bundle bundle = new Bundle();
            bundle.putBoolean("isCreate", isCreate);
            Fragment fragment = new LoginFragment();
            fragment.setArguments(bundle);
            //https://stackoverflow.com/questions/30339524/obj-fragment-wrong-2nd-argument-type-found-android-support-v4-app-fragment-re?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                    .replace(R.id.frameLayout, fragment).commit();
        } else {
            displayNotConnectedError();
        }


    }
    public void createMyRecipesView(){
        String token = mPrefs.getString("userToken", "No User Found");
        if ((!token.equals("No User Found"))) {
            mTalkToApiTask = new TalkToApiTask(this);
            String url = "recipes/user/" + mPrefs.getString("userToken", "No User Found");
            mTalkToApiTask.execute("", url, "GET");
            if(mNeedsUpdate) {
                if(isConnected()){
                    Snackbar snackbar = Snackbar.make(findViewById(R.id.main_layout), "We are retrieving your recipes. This may take a few seconds", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }

            }
        } else {
            Snackbar snackbar = Snackbar.make(findViewById(R.id.main_layout), "You must be logged in to complete that action.", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }
    public void createCreateRecipeView(){
        String token = mPrefs.getString("userToken", "No User Found");
        if ((!token.equals("No User Found")) && isConnected()) {
            Fragment mFragment = new CreateRecipeFragment();
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                    .replace(R.id.frameLayout, mFragment).commit();
        } else {
            if (token.equals("No User Found")){
                Snackbar snackbar = Snackbar.make(findViewById(R.id.main_layout), "You must be logged in to complete that action.", Snackbar.LENGTH_LONG);
                snackbar.show();
            } else {
                displayNotConnectedError();
            }
        }
    }
    public void createRecipeView(View view){
        String token = mPrefs.getString("userToken", "No User Found");
        if ((!token.equals("No User Found"))) {
            Bundle bundle = new Bundle();
            bundle.putString("JSONObject", view.findViewById(R.id.view).getTag().toString());
            Fragment fragment = new ViewRecipeFragment();
            fragment.setArguments(bundle);
            FragmentManager fragmentManager = getFragmentManager();
            //https://stackoverflow.com/questions/4932462/animate-the-transition-between-fragments?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
            fragmentManager.beginTransaction().setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                    .replace(R.id.frameLayout, fragment).commit();
        } else {
            Snackbar snackbar = Snackbar.make(findViewById(R.id.main_layout), "You must be logged in to complete that action.", Snackbar.LENGTH_LONG);
            snackbar.show();

        }
    }

    public void createEditRecipe(View view){
        mTalkToApiTask.cancel(true);
        String token = mPrefs.getString("userToken", "No User Found");
        Log.d(token,token);
        if ((!token.equals("No User Found")) && isConnected()) {
            Bundle bundle = new Bundle();
            bundle.putString("recipe", view.getTag().toString());
            Fragment fragment = new CreateRecipeFragment();
            fragment.setArguments(bundle);
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                    .replace(R.id.frameLayout, fragment).commit();
        } else {
            if (token.equals("No User Found")){
                Snackbar snackbar = Snackbar.make(findViewById(R.id.main_layout), "You must be logged in to complete that action.", Snackbar.LENGTH_LONG);
                snackbar.show();
            } else {
                displayNotConnectedError();
            }

        }
    }

    //---- Database Functions --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    public void signIn(View view){
        mTalkToApiTask.cancel(true);
        if(isConnected()){
            //https://stackoverflow.com/questions/8498880/retrieving-data-from-edittext-in-android?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
            EditText usernameView = findViewById(R.id.username);
            EditText passwordView = findViewById(R.id.password);

            String username = usernameView.getText().toString();
            String password = passwordView.getText().toString();

            JSONObject user = new JSONObject();
            JSONObject apiJSON = new JSONObject();

            try{
                user.put("username", username);
                user.put("password", password);
                apiJSON.put("user",user.toString());
            } catch(Exception e){
                e.printStackTrace();
            }
            //https://stackoverflow.com/questions/26224016/android-os-networkonmainthreadexception-in-asynctasks-doinbackground
            mTalkToApiTask = new TalkToApiTask(this);
            String token = mPrefs.getString("userToken", "No User Found");
            if ((boolean) view.getTag()){
                if ((!token.equals("No User Found"))) {
                    signout(false);
                }
                mTalkToApiTask.execute(apiJSON.toString(), "users/", "POST");
            } else {
                if ((!token.equals("No User Found"))) {
                    signout(false);
                }
                mTalkToApiTask.execute(apiJSON.toString(), "login/", "POST");
            }
            Snackbar snackbar = Snackbar.make(findViewById(R.id.main_layout), "We are attempting to sign you in.", Snackbar.LENGTH_SHORT);
            snackbar.show();
        } else {
            displayNotConnectedError();
        }


    }

    public void createRecipe(View view){
        mTalkToApiTask.cancel(true);
        String token = mPrefs.getString("userToken", "No User Found");
        if ((!token.equals("No User Found")) && isConnected()) {
            mNeedsUpdate = true;
            String type = "POST";
            JSONObject APIJSON = new JSONObject();
            JSONObject recipe = new JSONObject();
            JSONArray ingredientArray = new JSONArray();
            String title = ((EditText) findViewById(R.id.editTitle)).getText().toString();
            String directions = ((EditText) findViewById(R.id.editDirections)).getText().toString();
            try {
                recipe.put("user", mPrefs.getString("userToken", "No User Found"));
                recipe.put("title", title);
                recipe.put("directions", directions);
                ImageView imageView = findViewById(R.id.imageView2);
                //https://stackoverflow.com/questions/3791607/how-can-i-check-if-a-view-is-visible-or-not-in-android
                if (imageView.getVisibility() == View.VISIBLE) {
                    //https://stackoverflow.com/questions/26865787/get-bitmap-from-imageview-in-android-l/27030439
                    Log.d("hello", "htere");
                    BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
                    Bitmap bitmap = drawable.getBitmap();
                    int scale = 300;//scale down image so we don't use so much data
                    double height = bitmap.getHeight();
                    double width = bitmap.getWidth();
                    Log.d("width: ", Double.toString(width));
                    Log.d("height: ", Double.toString(height));
                    Double ratio = height / width;
                    Log.d("ratio",Double.toString(ratio));
                    if(ratio > 1){
                        height = scale;
                        width = scale/ratio;
                    } else {
                        height = ratio * scale;
                        width = scale;
                    }
                    bitmap = Bitmap.createScaledBitmap(bitmap, (int) width, (int) height, false);
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                    //https://stackoverflow.com/questions/9224056/android-bitmap-to-base64-string?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                    byte[] byteArray = byteArrayOutputStream.toByteArray();
                    String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
                    encoded = "data:image/png;base64," + encoded;
                    Log.d("data", Integer.toString(encoded.length()));
                    recipe.put("image", encoded);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            //https://stackoverflow.com/questions/4809834/how-to-iterate-through-a-views-elements?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
            LinearLayoutCompat ingredients = findViewById(R.id.ingredients);
            for (int i = 0; i < ingredients.getChildCount(); i++) {
                JSONObject ingredientObject = new JSONObject();
                IngredientView ingredient = (IngredientView) ingredients.getChildAt(i);
                ConstraintLayout constraintLayout = (ConstraintLayout) ingredient.getChildAt(0);
                EditText editName = (EditText) constraintLayout.getChildAt(3);
                EditText editAmount = (EditText) constraintLayout.getChildAt(5);
                try {
                    ingredientObject.put("title", editName.getText().toString());
                    ingredientObject.put("amount", editAmount.getText().toString());
                    ingredientObject.put("id", i);
                    ingredientArray.put(ingredientObject);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                if (view.getTag() != null) {
                    JSONObject tag = (JSONObject) view.getTag();
                    recipe.put("_id", tag);
                    type = "PUT";
                }
                recipe.put("ingredients", ingredientArray);
                APIJSON.put("recipe", recipe.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }

            //https://stackoverflow.com/questions/28601318/android-check-if-a-view-has-a-tag?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
            mTalkToApiTask = new TalkToApiTask(this);
            mTalkToApiTask.execute(APIJSON.toString(), "recipes/", type);
            Snackbar snackbar = Snackbar.make(findViewById(R.id.main_layout), "Updating Database. This may take a few seconds.", Snackbar.LENGTH_LONG);
            snackbar.show();
        } else {
            if (token.equals("No User Found")){
                Snackbar snackbar = Snackbar.make(findViewById(R.id.main_layout), "You must be logged in to complete that action.", Snackbar.LENGTH_LONG);
                snackbar.show();
            } else {
                displayNotConnectedError();
            }
        }
    }
    public void syncRecipes(View view){
        mTalkToApiTask.cancel(true);
        if(isConnected()){
            mNeedsUpdate = true;
            createMyRecipesView();
        } else {
            displayNotConnectedError();
        }
    }

    public void deleteRecipe(View view){
        mTalkToApiTask.cancel(true);
        if(isConnected()){
            mTalkToApiTask = new TalkToApiTask(this);
            //https://stackoverflow.com/questions/5127407/how-to-implement-a-confirmation-yes-no-dialogpreference?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
            new AlertDialog.Builder(this)
                    .setTitle("Delete Recipe")
                    .setMessage("Are you sure you want to delete this recipe?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            mNeedsUpdate = true;
                            String oid = (String) findViewById(R.id.delete).getTag();
                            mTalkToApiTask.execute("", "recipes/id/" + oid + "/" + mPrefs.getString("userToken", "No User Found"), "DELETE");
                            Snackbar snackbar = Snackbar.make(findViewById(R.id.main_layout), "Recipe is being deleted.", Snackbar.LENGTH_SHORT);
                            snackbar.show();
                        }})
                    .setNegativeButton(android.R.string.no, null).show();
        } else {
            displayNotConnectedError();
        }
    }

    //---- Helper Functions ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    public void signout(Boolean talkToUser){
        String token = mPrefs.getString("userToken", "No User Found");
        mMostRecentJson = null;
        mNeedsUpdate = true;
        if (!token.equals("No User Found")){
            //https://stackoverflow.com/questions/3687315/deleting-shared-preferences
            mEditor.clear().commit();
            if (talkToUser){
                Snackbar snackbar = Snackbar.make(findViewById(R.id.main_layout), "You have successfully logged out.", Snackbar.LENGTH_SHORT);
                snackbar.show();
            }
        } else {
            if (talkToUser) {
                Snackbar snackbar = Snackbar.make(findViewById(R.id.main_layout), "You are not signed in", Snackbar.LENGTH_SHORT);
                snackbar.show();
            }
        }
        if (talkToUser){
            createHomeView();
        }
    }

    public String getJson(){
        return mMostRecentJson;
    }
    public boolean getNeedsUpdate(){
        return mNeedsUpdate;
    }
    public String getUrl(){
        return mUrl;
    }
    public void displayNotConnectedError(){
        Snackbar snackbar = Snackbar.make(findViewById(R.id.main_layout), "You must be connected to the internet in to complete that action.", Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    public void addImage(View view){
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto , 1);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            try {
                Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                ImageView imageView = findViewById(R.id.imageView2);
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageBitmap(imageBitmap);
            }
            catch(Exception e) {
                Log.d("image failed: ", e.toString());
            }
        }
    }

    public void addIngredient(View view){
        LinearLayoutCompat ingredients = findViewById(R.id.ingredients);
        //https://stackoverflow.com/questions/6661261/adding-content-to-a-linear-layout-dynamically?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
        IngredientView ingredient = new IngredientView(this);
        ingredients.addView(ingredient);
    }
    public void removeIngredient(View view){
        //https://stackoverflow.com/questions/17879743/get-parents-view-from-a-layout?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
        //https://stackoverflow.com/questions/8740801/android-how-to-remove-the-view-of-linear-layout?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
        View ingredient = (ViewGroup)(view.getParent()).getParent();
        LinearLayoutCompat ingredients = findViewById(R.id.ingredients);
        ingredients.removeView(ingredient);
    }


    private void useAPIResult(String[] result){
        if(result[0].equals("failure")){
            Snackbar snackbar = Snackbar.make(findViewById(R.id.main_layout), "We're having trouble talking to the network.", Snackbar.LENGTH_LONG);
            snackbar.show();
        } else if (result[1].equals("login/") || result[1].equals("users/") ){
            //https://stackoverflow.com/questions/5245840/how-to-convert-string-to-jsonobject-in-java?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
            JSONObject jsonObj = null;
            try{
                jsonObj = new JSONObject(result[0]);
                //https://stackoverflow.com/questions/15918861/how-to-get-data-from-json-object?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
                String message = jsonObj.getString("message");
                Boolean success = jsonObj.getBoolean("success");
                if(!success){
                    TextView errorMessage = findViewById(R.id.error_message);
                    errorMessage.setText(message);
                    errorMessage.setVisibility(View.VISIBLE);
                } else {
                    mEditor.putString("userToken", message).commit();
                    Snackbar snackbar = Snackbar.make(findViewById(R.id.main_layout), "Successfully Logged in.", Snackbar.LENGTH_SHORT);
                    snackbar.show();
                    createMyRecipesView();
                }
            } catch(Exception e) {
                e.printStackTrace();
            }

        } else if (result[1].equals("recipes/")){
            createMyRecipesView();
        } else if(result[1].equals("recipes/user/" + mPrefs.getString("userToken", "No User Found"))){
            mMostRecentJson = result[0];
            mNeedsUpdate = false;
            Fragment fragment = new RecipesFragment();
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                    .replace(R.id.frameLayout, fragment).commit();

        } else if(result[1].startsWith("recipes/id/")){
            createMyRecipesView();
        }
    }
    public Boolean isConnected(){
        //https://developer.android.com/training/monitoring-device-state/connectivity-monitoring.html
        ConnectivityManager cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        try{
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        } catch(Exception e){
            return false;
        }

    }

    //---- Classes -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //https://stackoverflow.com/questions/7635902/passing-nothing-to-an-asynctask-in-android
    private static class TalkToApiTask extends AsyncTask<String, Context, String[] > {
        MainActivity context;

        public TalkToApiTask(Context context) {
            this.context = (MainActivity) context;
        }
        protected String[] doInBackground(String... params) {
            try {
                if(params[1].startsWith("recipes/user/") ){
                    if((!context.getNeedsUpdate())|| (context.getJson() != null && !context.isConnected())){
                        return new String[] { context.getJson(), params[1] };
                    }
                }
                //https://stackoverflow.com/questions/3324717/sending-http-post-request-in-java?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
                URL url = new URL(context.getUrl() + params[1]);
                URLConnection con = url.openConnection();
                HttpURLConnection http = (HttpURLConnection)con;
                http.setRequestMethod(params[2]); // PUT is another valid option


                byte[] out = params[0].getBytes(StandardCharsets.UTF_8);
                int length = out.length;
                if(params[2].equals("PUT") || params[2].equals("POST")){
                    http.setDoOutput(true);
                    http.setFixedLengthStreamingMode(length);
                }
                http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                http.connect();

                if(params[2].equals("PUT") || params[2].equals("POST")){
                    try(OutputStream os = http.getOutputStream()) {
                        os.write(out);
                    }
                }

                //https://stackoverflow.com/questions/309424/read-convert-an-inputstream-to-a-string?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
                String stringReturned = "";
                try{
                    java.util.Scanner s = new java.util.Scanner(http.getInputStream()).useDelimiter("\\A");
                    stringReturned = s.hasNext() ? s.next() : "";
                } catch (Exception e){
                    //https://stackoverflow.com/questions/5379247/filenotfoundexception-while-getting-the-inputstream-object-from-httpurlconnectio?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
                    java.util.Scanner s = new java.util.Scanner(http.getErrorStream()).useDelimiter("\\A");
                    stringReturned = s.hasNext() ? s.next() : "";
                }

                return new String[] { stringReturned, params[1] };
            }
            catch (Exception e){
                e.printStackTrace();
                Log.d("error",e.toString());
                return new String[]{"failure"};
            }
        }
        protected void onPostExecute(String[] result) {
            context.useAPIResult(result);
        }
    }
}

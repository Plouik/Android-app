package fr.plouik.apprendre;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.AutoCompleteTextView;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Toast;


public class MainActivity extends Activity implements OnItemClickListener{
    /*
        Déroulement :
            Affichage du layout MainActivity et choix de la destination
                Autocompletion à l'aide de l'API google ( https://maps.googleapis.com/maps/api/place/autocomplete/json )
                    Choix des paramétres dans la fonction autocomplete
            l'origine de l'itinéraire est donné par la position gps @MapsActivity

            OnClik du button ploup lance l'activité maps avec un bundle contenant la destination uniquement

             A faire :
         écriture de la google map @location/actualisation => MapsActivity

    */
        private static final String LOG_TAG = "Google Places Autocomp";
        private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
        private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
        private static final String OUT_JSON = "/json";
        private static final String API_KEY = "AIzaSyBmhskS8vv0WgcI51ie4oFt2d2wvyoguio";

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            Button btn_get=  findViewById(R.id.button_get);
            btn_get.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent maps = new Intent(MainActivity.this, MapsActivity.class);
                    AutoCompleteTextView arriver = findViewById(R.id.autoCompleteTextView_arriver);
                    // On rajoute un extra
                    maps.putExtra("destination",arriver.getText().toString());

                    // Puis on lance l'intent !
                    startActivity(maps);
                }
            });
            AutoCompleteTextView autoCompView = findViewById(R.id.autoCompleteTextView_arriver);

            autoCompView.setAdapter(new GooglePlacesAutocompleteAdapter(this, R.layout.list_item));
            autoCompView.setOnItemClickListener(this);
        }

        public void onItemClick(AdapterView adapterView, View view, int position, long id) {
            String str = (String) adapterView.getItemAtPosition(position);
            Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
        }

        public  ArrayList autocomplete(String input) {
            ArrayList resultList = null;

            HttpURLConnection conn = null;
            StringBuilder jsonResults = new StringBuilder();
            try {
                StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
                sb.append("?key=" + API_KEY);
                sb.append("&components=country:fr");
                sb.append("&input=" + URLEncoder.encode(input, "utf8"));

                URL url = new URL(sb.toString());
                conn = (HttpURLConnection) url.openConnection();
                InputStreamReader in = new InputStreamReader(conn.getInputStream());

                // Load the results into a StringBuilder
                int read;
                char[] buff = new char[1024];
                while ((read = in.read(buff)) != -1) {
                    jsonResults.append(buff, 0, read);
                }
            } catch (MalformedURLException e) {
                Log.e(LOG_TAG, "Error processing Places API URL", e);
                return resultList;
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error connecting to Places API", e);
                return resultList;
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }

            try {
                // Create a JSON object hierarchy from the results
                JSONObject jsonObj = new JSONObject(jsonResults.toString());
                JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

                // Extract the Place descriptions from the results
                resultList = new ArrayList(predsJsonArray.length());
                for (int i = 0; i < predsJsonArray.length(); i++) {
                    System.out.println(predsJsonArray.getJSONObject(i).getString("description"));
                    System.out.println("============================================================");
                    resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
                }
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Cannot process JSON results", e);
            }
/* drtujk*/
            return resultList;
        }

        class GooglePlacesAutocompleteAdapter extends ArrayAdapter implements Filterable {
            private ArrayList resultList;

            public GooglePlacesAutocompleteAdapter(Context context, int textViewResourceId) {
                super(context, textViewResourceId);
            }

            @Override
            public int getCount() {
                return resultList.size();
            }

            @Override
            public String getItem(int index) {
                return (String) resultList.get(index);
            }

            @Override
            public Filter getFilter() {
                Filter filter = new Filter() {
                    @Override
                    protected FilterResults performFiltering(CharSequence constraint) {
                        FilterResults filterResults = new FilterResults();
                        if (constraint != null) {
                            // Retrieve the autocomplete results.
                            resultList = autocomplete(constraint.toString());

                            // Assign the data to the FilterResults
                            filterResults.values = resultList;
                            filterResults.count = resultList.size();
                        }
                        return filterResults;
                    }

                    @Override
                    protected void publishResults(CharSequence constraint, FilterResults results) {
                        if (results != null && results.count > 0) {
                            notifyDataSetChanged();
                        } else {
                            notifyDataSetInvalidated();
                        }
                    }
                };
                return filter;
            }
        }
    }


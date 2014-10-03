package se.joelpet.android.reddit.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import se.joelpet.android.reddit.R;
import se.joelpet.android.reddit.activities.SubredditActivity;

public class SubredditListingFragment extends Fragment {

    private TextView textView1;

    public SubredditListingFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_subreddit, container, false);
        textView1 = (TextView) rootView.findViewById(R.id.textView1);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        // TODO: Create model according to https://github.com/reddit/reddit/wiki/JSON
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        JSONObject jsonObject = new JSONObject();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                "http://www.reddit.com/top.json", jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                System.out.print(jsonObject);
                try {
                    JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("children");
                    String title = ((JSONObject) jsonArray.get(0)).getJSONObject("data").getString("title");
                    textView1.setText(title);
                    Log.d(SubredditActivity.class.getSimpleName(),
                            String.format("Fetched top post and updated title to '%s'", title));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                System.out.print(volleyError.toString());
            }
        });

        queue.add(jsonObjectRequest);
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}

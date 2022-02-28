package com.my_project.telescopecontrol;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.JsonReader;
import android.util.JsonToken;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.my_project.telescopecontrol.NamesRecyclerAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import static android.view.HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING;
import static android.view.HapticFeedbackConstants.VIRTUAL_KEY;
import static android.view.View.FOCUS_UP;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class NearestStarSuggestionFragment extends Fragment implements NamesRecyclerAdapter.OnNameListener {
    private MaterialButton close_help;
    private MaterialButton close_help_corner;
    SharedPreferences.Editor editor;
    private ConstraintLayout frame_select_ok;
    private InputStream is_hip;
    private String jsonString_hip;
    private ConstraintLayout nearest_star_help_frame;
    private NestedScrollView nestedScrollview;
    private ProgressBar progressBar;
    RecyclerView recyclerView;
    private TextView selected_object;
    SharedPreferences sharedPreferences;
    private MaterialButton show_help;
    private ArrayList<star> starArrayList;
    StarSeeker starSeeker;
    private TextView target_star_info;
    private SharedViewModel viewModel;
    private HashMap<String, hipObject> hipHashMap = new HashMap<>();

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((BottomNavigationView) getActivity().findViewById(R.id.bottom_navigation)).getMenu().getItem(0).setChecked(true);
        viewModel = (SharedViewModel) new ViewModelProvider(getActivity()).get(SharedViewModel.class);

        sharedPreferences = getContext().getSharedPreferences(MainActivity.SHARED_PREFS, 0);
        editor = sharedPreferences.edit();
        final View v = inflater.inflate(R.layout.nearest_star_suggestion, container, false);
        starSeeker = new StarSeeker(getContext(), viewModel);
        target_star_info = v.findViewById(R.id.text_target_star_info);
        Button button_ok = v.findViewById(R.id.button_ok);
        frame_select_ok = (ConstraintLayout) v.findViewById(R.id.frame_select_ok);
        selected_object = v.findViewById(R.id.text_selected_object);
        show_help = v.findViewById(R.id.button_show_nearest_help);
        close_help = v.findViewById(R.id.button_hide_nearest_star_help);
        close_help_corner = v.findViewById(R.id.button_close_nearest_star_help);
        nearest_star_help_frame = (ConstraintLayout) v.findViewById(R.id.frame_nearest_star_info);
        frame_select_ok.setVisibility(GONE);
        progressBar = v.findViewById(R.id.progressBar_nearest_star);
        recyclerView = v.findViewById(R.id.recyclerview);
        if (viewModel.getStar_object().getValue() != null) {
            TextView textView = target_star_info;
            textView.setText("The list contains the nearest stars to : \n" + viewModel.getStar_object().getValue().getName_ascii() + "  .\nTo improve the pointing accuracy near this object , add as an extra alignment point , a star from the list .\nFor more details , press the information button on the top-right corner .\n");
        }
        if (viewModel.get_HipHashMap().getValue() != null) {
            show_list();
        }
        if (viewModel.get_HipHashMap().getValue() == null) {
            progressBar.setVisibility(VISIBLE);

            viewModel.get_hip_is_loading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean is_loading) {
                    if (!is_loading) {

                        progressBar.setVisibility(GONE);
                        show_list();

                    }
                }
            });

            Handler main_handler = new Handler();
            Runnable runnable_load_hip = new Runnable() {
                @Override
                public void run() {
                    /*if the hashmap containining the hipparcos data is not created,read the json file and store the date to the hashmap.
                    if the hashmap is already created do not repeate its costly creation : */
                    is_hip = getResources().openRawResource(R.raw.hip);
                    JsonReader reader = new JsonReader(new InputStreamReader(is_hip));
                    JsonToken token;
                    try {
                        // read array
                        reader.beginArray();
                        reader.setLenient(true);
                        String name = "";
                        String hip_no = "";
                        Double RAj2000 = 0.0;
                        Double DEj2000 = 0.0;
                        Double Vmag = 0.0;

                        while (reader.peek() != JsonToken.END_DOCUMENT) {
                            token = reader.peek();

                            switch (token) {
                                case NAME:
                                    name = reader.nextName();
                                    break;
                                case NUMBER:
                                    if (name.equals("_RA_icrs")) {
                                        RAj2000 = reader.nextDouble();
                                        reader.nextName();
                                        DEj2000 = reader.nextDouble();
                                    }
                                    if (name.equals("HIP")) {
                                        hip_no = String.valueOf(reader.nextInt());
                                    }
                                    if (name.equals("Hpmag")) {
                                        Vmag = reader.nextDouble();
                                        hipObject hip_o = new hipObject(Vmag, RAj2000, DEj2000);
                                        hipHashMap.put(hip_no, hip_o);
                                    }
                                    break;
                                case END_OBJECT:
                                    reader.endObject();
                                    break;
                                case END_ARRAY:
                                    reader.endArray();
                                    break;
                                case BEGIN_OBJECT:
                                    reader.beginObject();
                                    break;
                                case BEGIN_ARRAY:
                                    reader.beginArray();
                                    break;
                                case STRING:
                                    reader.nextString();
                                    break;
                                default:
                                    reader.skipValue();

                            }

                        }


                        reader.close();
                        is_hip = null;

                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    main_handler.post(new Runnable() {
                        @Override
                        public void run() {
                            viewModel.set_hip_is_loading(false);
                            viewModel.setHipHashMap(hipHashMap);
                            hipHashMap = null;
                            progressBar.setVisibility(GONE);
                            show_list();


                        }
                    });
                }


            };

            if (!viewModel.get_hip_is_loading().getValue()) {
                viewModel.set_hip_is_loading(true);
                Thread newThread = new Thread(runnable_load_hip);
                newThread.start();

            }
        }

        show_help.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                v.performHapticFeedback(VIRTUAL_KEY, FLAG_IGNORE_GLOBAL_SETTING);
                nearest_star_help_frame.setVisibility(VISIBLE);
                recyclerView.setVisibility(GONE);
            }
        });
        close_help.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                v.performHapticFeedback(VIRTUAL_KEY, FLAG_IGNORE_GLOBAL_SETTING);
                recyclerView.setVisibility(VISIBLE);
                nearest_star_help_frame.setVisibility(GONE);
            }
        });
        close_help_corner.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                v.performHapticFeedback(VIRTUAL_KEY, FLAG_IGNORE_GLOBAL_SETTING);
                recyclerView.setVisibility(VISIBLE);
                nearest_star_help_frame.setVisibility(GONE);
            }
        });
        button_ok.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                v.performHapticFeedback(VIRTUAL_KEY, FLAG_IGNORE_GLOBAL_SETTING);
                MainActivity.handler.obtainMessage(10, -1, -1).sendToTarget();
            }
        });
        return v;
    }

    public void onNameClick(int position) {
        NestedScrollView nestedScrollView = (NestedScrollView) (getActivity().findViewById(R.id.relative)).getRootView().findViewById(R.id.scrollview);
        nestedScrollView.fullScroll(FOCUS_UP);
        String input = starArrayList.get(position).getName_ascii();
        if (input.contains("    ❂")) {
            starArrayList.get(position).setName_ascii(input.replace("❂   ", ""));
            input = starArrayList.get(position).getName_ascii();
        }
        String input2 = input.split("\n", 2)[0];
        starArrayList.get(position).setName_ascii(input2);
        Toast.makeText(getContext(), "selected : " + input2, Toast.LENGTH_SHORT).show();
        selected_object.setText(input2);
        frame_select_ok.setVisibility(VISIBLE);
        viewModel.setStar_object(starArrayList.get(position));
    }

    public void show_list() {
        if (viewModel.get_HipHashMap().getValue() != null) {
            starArrayList = starSeeker.get_nearest_star();
            recyclerView.setAdapter(new NamesRecyclerAdapter(starArrayList, this));
        }
    }
}

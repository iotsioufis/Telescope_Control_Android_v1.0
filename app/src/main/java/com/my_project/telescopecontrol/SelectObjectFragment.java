package com.my_project.telescopecontrol;


import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.JsonReader;
import android.util.JsonToken;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

import static android.view.View.FOCUS_UP;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class SelectObjectFragment extends Fragment implements NamesRecyclerAdapter.OnNameListener {
    private SharedViewModel viewModel;
    private double ra_to_go;
    private double dec_to_go;
    RecyclerView recyclerView;
    private ArrayList<star> starArrayList;
    private HashMap<String, ngcObject> ngcHashMap = new HashMap<>();
    private HashMap<String, hipObject> hipHashMap = new HashMap<>();
    private InputStream is_ngc;
    private InputStream is_hip;
    // String jsonString_names = "";
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    private ConstraintLayout frame_select_ok;
    private TextView selected_object;
    private EditText EnterNGC;
    private MaterialTextView textViewNGC;
    private MaterialButton buttonOK_NGC;
    private MaterialTextView textViewHIP;
    private EditText EnterHIP;
    private MaterialButton buttonOK_HIP;
    private ContentLoadingProgressBar progressBar;
    private boolean show_hip_button = false;

    public star starobject = new star("", "", "", "", "", "", "", 0, 0.0d, "", "0", 0, 0.0d, 0.0d, "", "");

    private SolarCalcs solar_ob = new SolarCalcs();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        /* make bottomNav visible again:*/
        BottomNavigationView bottomNav = (BottomNavigationView) getActivity().findViewById(R.id.bottom_navigation);
        bottomNav.setVisibility(View.VISIBLE);
        bottomNav.getMenu().getItem(1).setChecked(true);

        View v = inflater.inflate(R.layout.fragment_select_object, container, false);
        viewModel = (SharedViewModel) new ViewModelProvider(getActivity()).get(SharedViewModel.class);
        radioGroup = v.findViewById(R.id.radio_group);
        Button button_ok = v.findViewById(R.id.button_ok);
        frame_select_ok = v.findViewById(R.id.frame_select_ok);
        selected_object = v.findViewById(R.id.text_selected_object);
        textViewNGC = v.findViewById(R.id.textViewNGC);
        EnterNGC = v.findViewById(R.id.editTextNGC);
        buttonOK_NGC = v.findViewById(R.id.button_ok_NGC);
        textViewHIP = v.findViewById(R.id.textViewHIP);
        EnterHIP = v.findViewById(R.id.editTextHIP);
        buttonOK_HIP = v.findViewById(R.id.button_ok_HIP);
        progressBar = v.findViewById(R.id.progressBar2);
        frame_select_ok.setVisibility(GONE);
        recyclerView = v.findViewById(R.id.recyclerview);
        SearchView searchview = v.findViewById(R.id.search);
        int id_search_close_button = searchview.getResources().getIdentifier("android:id/search_close_btn", null, null);
        ImageView button_close_search = searchview.findViewById(id_search_close_button);
        int id = searchview.getResources().getIdentifier("android:id/search_src_text", null, null);
        EditText editText = searchview.findViewById(id);
        TextInputLayout textInputlayoutHIP = v.findViewById(R.id.materialtextField);
        TextInputLayout textInputlayoutNGC = v.findViewById(R.id.materialtextFieldNGC);
        /*relativeLayout belongs to to the layout of the main activity.it is used here to get the view of the ui of the mainActivity.
        Then the scrollable view is used to scroll all the way up to the top (on onQueryTextSubmit) :*/
        RelativeLayout relativeLayout = getActivity().findViewById(R.id.relative);
        View v2 = relativeLayout.getRootView();
        NestedScrollView nestedScrollview = v2.findViewById(R.id.scrollview);

        /*read from star_names.json and create the jsonString_names.
        if already, done the costly creation of  jsonString_names is not repeated :*/
        if (viewModel.get_jsonString_names().getValue() == "") {
            InputStream is = getResources().openRawResource(R.raw.star_names);
            viewModel.set_jsonString_names(new Scanner(is).useDelimiter("\\A").next());
        }
        Type listType = new TypeToken<ArrayList<star>>() {
        }.getType();
        starArrayList = new Gson().fromJson(viewModel.get_jsonString_names().getValue(), listType);

        NamesRecyclerAdapter namesRecyclerAdapter = new NamesRecyclerAdapter(starArrayList, this);
        recyclerView.setAdapter(namesRecyclerAdapter);
        searchview.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                nestedScrollview.fullScroll(FOCUS_UP); //return to the top of the view.
                List<star> single_result = namesRecyclerAdapter.getSingle_result();
                if (single_result.size() == 1) {

                    ra_to_go = single_result.get(0).getRaj2000();
                    dec_to_go = single_result.get(0).getDecj2000();
                    String input = single_result.get(0).getName_ascii();
                    Toast.makeText(getContext(), "selected : " + input, Toast.LENGTH_SHORT).show();
                    selected_object.setText(input);
                    frame_select_ok.setVisibility(View.VISIBLE);
                    if (input.equals("Mercury") || input.equals("Mars") || input.equals("Neptune") || input.equals("Venus")
                            || input.equals("Jupiter") || input.equals("Saturn") || input.equals("Uranus") || input.equals("Pluto")) {
                        double Planet_coordinates[] = solar_ob.GetPlanetCoords(input, viewModel, 0);
                        single_result.get(0).setRaj2000(Planet_coordinates[0]);
                        single_result.get(0).setDecj2000(Planet_coordinates[1]);
                    }
                    if (input.equals("Moon")) {
                        double Moon_coordinates[] = solar_ob.GetMoonCoords(viewModel, 0);
                        single_result.get(0).setRaj2000(Moon_coordinates[0]);
                        single_result.get(0).setDecj2000(Moon_coordinates[1]);
                    }

                    viewModel.setStar_object(single_result.get(0));

                    //   Toast.makeText(v.getContext(), single_result.get(0).toString(), Toast.LENGTH_SHORT).show();
                }

                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);

                return false;
            }


            @Override
            public boolean onQueryTextChange(String newText) {
                namesRecyclerAdapter.getFilter().filter((newText));

                final Rect rect = new Rect(0, 200, v.getWidth(), v.getHeight());
                v.requestRectangleOnScreen(rect, true);
                return false;
            }
        });


        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        button_close_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);   //hide the keyboard
                editText.setText("");
            }
        });


        button_ok.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                getActivity().onBackPressed();
            }
        });

        buttonOK_NGC.setOnClickListener(new View.OnClickListener() {
            Integer tempInt;

            @Override
            public void onClick(View view) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
                String selectedNGC = EnterNGC.getText().toString();
                if (selectedNGC.equals("")) {
                    tempInt = -1;
                }
                if (!selectedNGC.equals("")) {
                    tempInt = Integer.parseInt(selectedNGC);
                }

                if (tempInt < 1 || tempInt > 7840) {
                    Toast.makeText(getContext(), "Invalid NGC number !", Toast.LENGTH_SHORT).show();

                } else if (tempInt >= 1 || tempInt <= 7840) {
                    ngcObject testob = ngcHashMap.get(selectedNGC);
                    if (ngcHashMap.get(selectedNGC) != null) {
                        starobject.setRaj2000(testob.getRaj2000() * 15);
                        starobject.setDecj2000(testob.getDecj2000());
                        starobject.setMmag(testob.getMmag());
                        starobject.setCon(testob.getCon());
                        starobject.setName_ascii("NGC : " + selectedNGC);
                        viewModel.setStar_object(starobject);
                        getActivity().onBackPressed();
                    }
                    if (ngcHashMap.get(selectedNGC) == null) {
                        Toast.makeText(getContext(), "NGC OBJECT NOT FOUND", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });

        buttonOK_HIP.setOnClickListener(new View.OnClickListener() {
            Integer tempInt2;

            @Override
            public void onClick(View view) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
                String selectedHIP = EnterHIP.getText().toString();
                if (selectedHIP.equals("")) {
                    tempInt2 = -1;
                }
                if (!selectedHIP.equals("")) {
                    tempInt2 = Integer.parseInt(selectedHIP);
                }


                if (tempInt2 < 1 || tempInt2 > 120404) {
                    Toast.makeText(getContext(), "Invalid HIP number !", Toast.LENGTH_SHORT).show();
                } else if (tempInt2 >= 1 || tempInt2 <= 120404) {
                    hipObject testob = viewModel.get_HipHashMap().getValue().get(selectedHIP);

                    if (viewModel.get_HipHashMap().getValue().get(selectedHIP) != null) {
                        starobject.setRaj2000(testob.getRaj2000() * 15);
                        starobject.setDecj2000(testob.getDecj2000());
                        starobject.setName_ascii("HIP : " + selectedHIP);
                        starobject.setMmag(testob.getMmag());
                        starobject.setCon("unavailable");
                        viewModel.setStar_object(starobject);
                        getActivity().onBackPressed();
                    }
                    if (viewModel.get_HipHashMap().getValue().get(selectedHIP) == null) {
                        Toast.makeText(getContext(), "HIP OBJECT NOT FOUND", Toast.LENGTH_SHORT).show();

                    }

                }

            }
        });


        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int radioId = radioGroup.getCheckedRadioButtonId();
                radioButton = v.findViewById(radioId);
                String radioText = radioButton.getText().toString();

                switch (radioText) {
                    case "Name":
                        progressBar.setVisibility(GONE);
                        show_hip_button = false;
                        editText.setText("");
                        recyclerView.setVisibility(VISIBLE);
                        button_ok.setVisibility(VISIBLE);
                        searchview.setVisibility(VISIBLE);
                        textViewNGC.setVisibility(GONE);
                        EnterNGC.setVisibility(GONE);
                        buttonOK_NGC.setVisibility(GONE);
                        textViewHIP.setVisibility(GONE);
                        EnterHIP.setVisibility(GONE);
                        buttonOK_HIP.setVisibility(GONE);
                        textInputlayoutHIP.setEndIconVisible(false);
                        textInputlayoutNGC.setEndIconVisible(false);


                        break;
                    case "HIP number":
                        viewModel.get_hip_is_loading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
                            @Override
                            public void onChanged(Boolean is_loading) {
                                if (!is_loading && show_hip_button) {
                                    textViewHIP.setText("Enter HIP number (1 to 120404) : ");
                                    progressBar.setVisibility(GONE);
                                    textInputlayoutHIP.setVisibility(VISIBLE);
                                    buttonOK_HIP.setVisibility(VISIBLE);
                                }
                            }
                        });
                        show_hip_button = true;
                        EnterHIP.setText("");
                        recyclerView.setVisibility(GONE);
                        button_ok.setVisibility(GONE);
                        searchview.setVisibility(GONE);
                        frame_select_ok.setVisibility(GONE);

                        if (viewModel.get_HipHashMap().getValue() != null) {
                            textViewHIP.setText("Enter HIP number (1 to 120404) : ");
                            progressBar.setVisibility(GONE);
                            textInputlayoutHIP.setVisibility(VISIBLE);
                            buttonOK_HIP.setVisibility(VISIBLE);
                        } else if (viewModel.get_HipHashMap().getValue() == null) {
                            textViewHIP.setText("Loading Hipparcos Catalogue ...");
                            progressBar.setVisibility(VISIBLE);
                            buttonOK_HIP.setVisibility(GONE);
                            textInputlayoutHIP.setVisibility(GONE);


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
                                            viewModel.setHipHashMap(hipHashMap);
                                            viewModel.set_hip_is_loading(false);
                                            hipHashMap = null;
                                            textViewHIP.setText("Enter HIP number (1 to 120404) : ");

                                            if (show_hip_button) {
                                                progressBar.setVisibility(GONE);
                                                textInputlayoutHIP.setVisibility(VISIBLE);
                                                buttonOK_HIP.setVisibility(VISIBLE);
                                            }


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


                        textViewHIP.setVisibility(VISIBLE);
                        EnterHIP.setVisibility(VISIBLE);
                        textInputlayoutHIP.setEndIconMode(TextInputLayout.END_ICON_CLEAR_TEXT);

                        //buttonOK_HIP.setVisibility(View.VISIBLE);
                        textViewNGC.setVisibility(GONE);
                        EnterNGC.setVisibility(GONE);
                        buttonOK_NGC.setVisibility(GONE);
                        textInputlayoutNGC.setEndIconVisible(false);


                        break;
                    case "NGC number":
                        show_hip_button = false;
                        progressBar.setVisibility(GONE);
                        EnterNGC.setText("");
                        recyclerView.setVisibility(GONE);
                        button_ok.setVisibility(GONE);
                        searchview.setVisibility(GONE);
                        frame_select_ok.setVisibility(GONE);


                        if (ngcHashMap.size() > 0) {
                            textViewNGC.setText("Enter NGC number (1 to 7840) : ");
                        } else if (ngcHashMap.size() == 0) {
                            textViewNGC.setText("Loading New General Catalogue ...");
                            progressBar.setVisibility(VISIBLE);

                          /*delay the costly creation of the hashmap by 100 millisecs.
                          This is done to provide time for the progressBar and the loading message to appear in the UI before the hashmap creation.
                          So the user is informed about the data loading and the UI is not appearing frozen: */
                            //final Handler handler2 = new Handler();
                           /* handler2.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                 /*if the hashmap containining the NGC data is not created,read the json file and store the date to the hashmap.
                                 if the hashmap is already created do not repeate its costly creation : */
                             /*       is_ngc = getResources().openRawResource(R.raw.ngc);
                                    jsonString_ngc = new Scanner(is_ngc).useDelimiter("\\A").next();
                                    Type listType_ngc = new TypeToken<HashMap<String, ngcObject>>() {
                                    }.getType();
                                    ngcHashMap = new Gson().fromJson(jsonString_ngc, listType_ngc);
                                    jsonString_ngc = "ngc_loaded";
                                    progressBar.setVisibility(GONE);
                                    textViewNGC.setText("Enter NGC number (1 to 7840) : ");

                                }
                            }, 100);


                            */

                                    /*if the hashmap containining the hipparcos data is not created,read the json file and store the date to the hashmap.
                                 if the hashmap is already created do not repeate its costly creation : */
                            final Handler handler2 = new Handler();
                            handler2.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    is_ngc = getResources().openRawResource(R.raw.ngc);
                                    JsonReader reader = new JsonReader(new InputStreamReader(is_ngc));
                                    JsonToken token;
                                    try {
                                        // read array
                                        reader.beginArray();
                                        reader.setLenient(true);
                                        String name = "";
                                        String ngc_no = "";
                                        Double RAB2000 = 0.0;
                                        Double DEB2000 = 0.0;
                                        String Const = "";
                                        Double mag = 0.0;

                                        while (reader.peek() != JsonToken.END_DOCUMENT) {
                                            token = reader.peek();

                                            switch (token) {
                                                case NAME:
                                                    name = reader.nextName();
                                                    break;
                                                case NUMBER:
                                                    if (name.equals("Name")) {
                                                        ngc_no = String.valueOf(reader.nextInt());
                                                    }
                                                    if (name.equals("RAB2000")) {
                                                        RAB2000 = reader.nextDouble();
                                                        reader.nextName();
                                                        DEB2000 = reader.nextDouble();
                                                    }

                                                    if (name.equals("mag")) {
                                                        mag = reader.nextDouble();
                                                        ngcObject ngc_ob = new ngcObject(Const, mag, RAB2000, DEB2000);
                                                        ngcHashMap.put(ngc_no, ngc_ob);
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
                                                    if (name.equals("Const")) {
                                                        Const = reader.nextString();
                                                    }
                                                    break;
                                                default:
                                                    reader.skipValue();

                                            }

                                        }


                                        reader.close();
                                        is_ngc = null;

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    progressBar.setVisibility(GONE);
                                    textViewNGC.setText("Enter NGC number (1 to 7840) : ");
                                }
                            }, 100);


                        }


                        textViewNGC.setVisibility(VISIBLE);
                        EnterNGC.setVisibility(VISIBLE);
                        textInputlayoutNGC.setEndIconMode(TextInputLayout.END_ICON_CLEAR_TEXT);
                        buttonOK_NGC.setVisibility(VISIBLE);
                        textViewHIP.setVisibility(GONE);
                        EnterHIP.setVisibility(GONE);
                        buttonOK_HIP.setVisibility(GONE);
                        textInputlayoutHIP.setEndIconVisible(false);

                        break;

                }

            }
        });


        return v;
    }


    @Override

    public void onNameClick(int position) {

        NestedScrollView nestedScrollView = (NestedScrollView) (getActivity().findViewById(R.id.relative)).getRootView().findViewById(R.id.scrollview);
        nestedScrollView.fullScroll(FOCUS_UP);
//pass data to home fragmnet using the setStar_object()

        String input = starArrayList.get(position).getName_ascii();
        Toast.makeText(getContext(), "selected : " + input, Toast.LENGTH_SHORT).show();
        selected_object.setText(input);
        frame_select_ok.setVisibility(View.VISIBLE);
        if (input.equals("Mercury") || input.equals("Mars") || input.equals("Neptune") || input.equals("Venus")
                || input.equals("Jupiter") || input.equals("Saturn") || input.equals("Uranus") || input.equals("Pluto")) {
            double Planet_coordinates[] = solar_ob.GetPlanetCoords(input, viewModel, 0);
            starArrayList.get(position).setRaj2000(Planet_coordinates[0]);
            starArrayList.get(position).setDecj2000(Planet_coordinates[1]);
        }

        if (input.equals("Moon")) {
            double Moon_coordinates[] = solar_ob.GetMoonCoords(viewModel, 0);
            starArrayList.get(position).setRaj2000(Moon_coordinates[0]);
            starArrayList.get(position).setDecj2000(Moon_coordinates[1]);

        }
        viewModel.setStar_object(starArrayList.get(position));

        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);   //hide the keyboard when a name is selected in the RecyclerView

    }


}

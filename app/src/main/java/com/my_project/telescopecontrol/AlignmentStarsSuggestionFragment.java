package com.my_project.telescopecontrol;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.my_project.telescopecontrol.MainActivity;
import com.my_project.telescopecontrol.NamesRecyclerAdapter;
import com.my_project.telescopecontrol.R;
import com.my_project.telescopecontrol.SharedViewModel;
import com.my_project.telescopecontrol.StarSeeker;
import com.my_project.telescopecontrol.star;

import java.util.ArrayList;

public class AlignmentStarsSuggestionFragment extends Fragment implements NamesRecyclerAdapter.OnNameListener {
    private ConstraintLayout frame_select_ok;
    private RadioButton radioButton;
    private RadioGroup radioGroup;
    RecyclerView recyclerView;
    private TextView selected_object;
    private ArrayList<star> starArrayList;
    private SharedViewModel viewModel;
    Button button_ok;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((BottomNavigationView) getActivity().findViewById(R.id.bottom_navigation)).getMenu().getItem(3).setChecked(true);
        final View v = inflater.inflate(R.layout.alignment_stars_suggestion, container, false);
        viewModel = (SharedViewModel) new ViewModelProvider(getActivity()).get(SharedViewModel.class);
        radioGroup = (RadioGroup) v.findViewById(R.id.radio_group_east_west);
        frame_select_ok = (ConstraintLayout) v.findViewById(R.id.frame_select_ok);
        selected_object = (TextView) v.findViewById(R.id.text_selected_object);
        frame_select_ok.setVisibility(View.GONE);
        recyclerView = (RecyclerView) v.findViewById(R.id.recyclerview);
        button_ok = v.findViewById(R.id.button_ok);
        final StarSeeker starSeeker = new StarSeeker(getContext(), viewModel);
        starSeeker.find_brightest_stars();
        starArrayList = starSeeker.get_western_stars();
        NamesRecyclerAdapter namesRecyclerAdapter_west = new NamesRecyclerAdapter(starArrayList, this);
        recyclerView.setAdapter(namesRecyclerAdapter_west);
        final NamesRecyclerAdapter namesRecyclerAdapter_east = new NamesRecyclerAdapter(starSeeker.get_eastern_stars(), this);
        button_ok.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                v.performHapticFeedback(1, 2);
                MainActivity.handler.obtainMessage(10, -1, -1).sendToTarget();
            }
        });
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                char c;
                int radioId = radioGroup.getCheckedRadioButtonId();
                radioButton = (RadioButton) v.findViewById(radioId);
                String radioText = radioButton.getText().toString();

                if (radioText.equals("East")) {
                    starSeeker.find_brightest_stars();
                    starArrayList = starSeeker.get_eastern_stars();
                    recyclerView.setAdapter(namesRecyclerAdapter_east);
                    frame_select_ok.setVisibility(View.GONE);
                }

                if (radioText.equals("West")) {
                    starSeeker.find_brightest_stars();
                    starArrayList = starSeeker.get_western_stars();
                    recyclerView.setAdapter(namesRecyclerAdapter_west);
                    frame_select_ok.setVisibility(View.GONE);
                }


            }
        });
        return v;
    }

    public void onNameClick(int position) {
        String input = starArrayList.get(position).getName_ascii();
        if (input.contains("❂ ")) {
            starArrayList.get(position).setName_ascii(input.replace("❂ ", ""));
            input = starArrayList.get(position).getName_ascii();
        }
        Context context = getContext();
        Toast.makeText(context, "selected : " + input, Toast.LENGTH_SHORT).show();
        selected_object.setText(input);
        frame_select_ok.setVisibility(View.VISIBLE);
        viewModel.setStar_object(starArrayList.get(position));
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}

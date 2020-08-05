package com.example.eventplanner.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.eventplanner.R;
import com.google.android.material.snackbar.Snackbar;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link OrderDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OrderDialogFragment extends DialogFragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_ORDER = "order";

    public enum Order {
        BY_CREATION_DATE,
        BY_SCHEDULED_DATE
    }
    Order order;

    RadioGroup radioOrderGroup;
    Button btnDone;

    private OnDonePressedListener listener;
    public interface OnDonePressedListener {
        public void sendSelectedRadioButton(Order order);
    }

    public OrderDialogFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment OrderDialogFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static OrderDialogFragment newInstance(int order) {
        OrderDialogFragment fragment = new OrderDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ORDER, order);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            int orderInt = getArguments().getInt(ARG_ORDER);
            if (orderInt == 1) {
                order = Order.BY_SCHEDULED_DATE;
            } else {
                order = Order.BY_CREATION_DATE;
            }
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnDonePressedListener) {
            listener = (OnDonePressedListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement MyListFragment.OnItemSelectedListener");
        }
    }

    public void onSomeClick(View v) {
        listener.sendSelectedRadioButton(order);
    }

    /**
     * For setting the size of info dialog
     */
    @Override
    public void onResume() {
        getDialog().getWindow().setLayout(700, 630);
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_order_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        radioOrderGroup = view.findViewById(R.id.radio_order_group);
        // initially check radio buttons based off user settings
        RadioButton btnCreation = view.findViewById(R.id.radio_creation_date);
        RadioButton btnScheduled = view.findViewById(R.id.radio_scheduled_date);
        if (order == Order.BY_CREATION_DATE) {
            btnCreation.setChecked(true);
            btnScheduled.setChecked(false);
        } else if (order == Order.BY_SCHEDULED_DATE) {
            btnCreation.setChecked(false);
            btnScheduled.setChecked(true);
        }

        btnDone = view.findViewById(R.id.btnDone);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateUserOrderSettingsAndExit(view);
            }
        });
        radioOrderGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch(i) {
                    case R.id.radio_creation_date:
                        order = Order.BY_CREATION_DATE;
                        break;
                    case R.id.radio_scheduled_date:
                        order = Order.BY_SCHEDULED_DATE;
                        break;
                    default:
                        break;
                }
            }
        });

    }

    private void updateUserOrderSettingsAndExit(View view) {
        ParseUser currentUser = ParseUser.getCurrentUser();
        boolean isOrderedByCreationDate = true;
        if (order != Order.BY_CREATION_DATE) {
            isOrderedByCreationDate = false;
        }
        currentUser.put("orderByCreationDate", isOrderedByCreationDate);
        currentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Toast.makeText(getContext(), "Failed to save settings", Toast.LENGTH_SHORT).show();
                }
                onSomeClick(view);
                dismiss();
            }
        });
    }
}
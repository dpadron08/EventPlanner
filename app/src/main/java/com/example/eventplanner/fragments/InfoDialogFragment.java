package com.example.eventplanner.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.example.eventplanner.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link InfoDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InfoDialogFragment extends DialogFragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_TITLE = "title";
    private static final String ARG_BODY = "body";
    public static final String ARG_WIDTH = "width";
    public static final String ARG_HEIGHT = "height";

    // TODO: Rename and change types of parameters
    private String dialogTitle;
    private String dialogBody;
    private int dialogWidth;
    private int dialogHeight;

    private TextView tvTitle;
    private TextView tvBody;


    public InfoDialogFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param title Parameter 1.
     * @param body Parameter 2.
     * @return A new instance of fragment InfoDialogFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static InfoDialogFragment newInstance(String title, String body, int width, int height) {
        InfoDialogFragment fragment = new InfoDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_BODY, body);
        args.putInt(ARG_WIDTH, width);
        args.putInt(ARG_HEIGHT, height);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            dialogTitle = getArguments().getString(ARG_TITLE);
            dialogBody = getArguments().getString(ARG_BODY);
            dialogWidth = getArguments().getInt(ARG_WIDTH);
            dialogHeight = getArguments().getInt(ARG_HEIGHT);
        }

    }

    /*
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_info_dialog, container, false);
    }
    */

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        String title = dialogTitle;
        String body = dialogBody;
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setMessage(body);
        alertDialogBuilder.setPositiveButton("OK",  new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // on success
                dismiss();
            }
        });
        //return super.onCreateDialog(savedInstanceState);
        return alertDialogBuilder.create();
    }

    /**
     * For setting the size of info dialog
     */
    @Override
    public void onResume() {
        getDialog().getWindow().setLayout(dialogWidth, dialogHeight);
        super.onResume();
    }

    /*
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvTitle = view.findViewById(R.id.tvTitle);
        tvBody = view.findViewById(R.id.tvBody);
        String title = getArguments().getString("title", "Title not found");
        String body = getArguments().getString("body", "Body not found");
        //getDialog().setTitle(title);
        tvTitle.setText(title);
        tvBody.setText(body);
    }
     */
}